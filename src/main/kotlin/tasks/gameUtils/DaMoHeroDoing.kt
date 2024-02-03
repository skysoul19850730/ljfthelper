package tasks.gameUtils

import data.Config
import data.HeroBean
import data.MRect
import getImage
import kotlinx.coroutines.*
import tasks.HeroDoing
import kotlin.coroutines.resume
import log
import logOnly
import utils.MRobot

//type 0:魔球 1木球
class DaMoHeroDoing(val type: Int) : HeroDoing() {
    val moqiu = when (type) {
        1 -> HeroBean("muqiu", 50, needCar = false)
        else -> HeroBean("moqiu", 50, needCar = false)
    }

    var count = 0

    //葫芦活动，注释的都在车上，所以刷木时不会出现
    val otherHeros = arrayListOf<HeroBean>().apply {
//        add(HeroBean("zhanjiang"))
//        add(HeroBean("xiongmao"))//
        add(HeroBean("nvyao"))
//        add(HeroBean("ganglie"))
        add(HeroBean("emo"))//
//        add(HeroBean("saman2"))
        add(HeroBean("bingnv"))
        add(HeroBean("guangqiu", needCar = false))
//        add(HeroBean("wangjiang"))//
    }

    override fun initHeroes() {
        heros = arrayListOf()
        heros.add(
            moqiu
        )
        heros.addAll(otherHeros)
    }

    var statrTime = 0L
    override fun onStart() {
        super.onStart()
        statrTime = System.currentTimeMillis()
    }

    override fun onStop() {
        super.onStop()
        log("打球结果：耗时 ${(System.currentTimeMillis() - statrTime) / 1000}  个数:${count}")
    }

    override suspend fun doUpHero(heroBean: HeroBean, position: Int, hasKuoJianClicked: Boolean) {
        var rect = when (position) {
            0 -> Config.zhandou_hero1CheckRect
            1 -> Config.zhandou_hero2CheckRect
            else -> Config.zhandou_hero3CheckRect
        }
        MRobot.singleClick(rect.clickPoint)//点击卡片
        count++
    }

    override suspend fun shuaka(needShuaxin: Boolean): List<HeroBean?> {
        var hs: List<HeroBean?>? = getPreHeros(if (needShuaxin) Config.delayLong else Long.MAX_VALUE)
        while (hs == null && running) {
            log("未识别到英雄")
            MRobot.singleClick(Config.zhandou_shuaxinPoint)
            delay(150)
            hs = getPreHeros(if (needShuaxin) Config.delayLong else Long.MAX_VALUE)
        }
        log("识别到英雄 ${hs?.getOrNull(0)?.heroName}  ${hs?.getOrNull(1)?.heroName}  ${hs?.getOrNull(2)?.heroName}")
        return hs!!
    }

    override suspend fun getPreHeros(timeout: Long): List<HeroBean?>? = suspendCancellableCoroutine<List<HeroBean?>?> {
        val startTime = System.currentTimeMillis()
        GlobalScope.launch {
            var h1: HeroBean? = null
            var h2: HeroBean? = null
            var h3: HeroBean? = null

            try {
                withTimeout(400) {
                    while (h1 != moqiu && h2 != moqiu && h3 != moqiu) {
                        val hero1 = if (h1 == null) {
                            async { getHeroAtRect(Config.zhandou_hero1CheckRect) }
                        } else null
                        val hero2 = if (h2 == null) {
                            async { getHeroAtRect(Config.zhandou_hero2CheckRect) }
                        } else null
                        val hero3 = if (h3 == null) {
                            async { getHeroAtRect(Config.zhandou_hero3CheckRect) }
                        } else null

                        if (h1 == null) {
                            h1 = hero1?.await()
                        }
                        if (h2 == null) {
                            h2 = hero2?.await()
                        }
                        if (h3 == null) {
                            h3 = hero3?.await()
                        }
//                        if (h1 != moqiu && h2 != moqiu && h3 != moqiu) {//省去最后的100ms
//                            delay(30)
//                        }
                        if ((h1 != null && h2 != null && h3 != null) || h1 == moqiu || h2 == moqiu || h3 == moqiu) {
                            break
                        }
                    }
                    log("getPreHeros cost time:${System.currentTimeMillis() - startTime}")
                    it.resume(arrayListOf(h1, h2, h3))
                }
            } catch (e: Exception) {
                it.resume(null)
            }
        }
    }

    override suspend fun getHeroAtRect(rect: MRect) = suspendCancellableCoroutine<HeroBean?> {
        val img = getImage(rect)
        //只需要检测没满的，满的不可能再出现
        val hero = heros.firstOrNull {
//            ImgUtil.isImageSim(img, it.img)
//            ImgUtil.isHeroSim(img,it.img)
            it.fitImage(img)
        }
        if (Config.debug) {
            log(img)
        }
        if (hero != null) {
            logOnly("getHeroAtRect ${hero?.heroName ?: "无结果"}")
        } else logOnly("getHeroAtRect null")
        it.resume(hero)

    }

    override suspend fun dealHero(heros: List<HeroBean?>): Int {
        return heros.indexOf(moqiu)
    }

    override fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean? {
        return null
    }
}