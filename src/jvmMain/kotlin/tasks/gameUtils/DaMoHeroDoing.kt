package tasks.gameUtils

import data.Config
import data.HeroBean
import kotlinx.coroutines.*
import tasks.HeroDoing
import kotlin.coroutines.resume
import log
import utils.MRobot

//type 0:魔球 1木球
class DaMoHeroDoing(val type: Int) : HeroDoing() {
    val moqiu = when (type) {
        1 -> HeroBean("muqiu", 50, needCar = false)
        else -> HeroBean("moqiu", 50, needCar = false)
    }

    override fun initHeroes() {
        heros = arrayListOf()
        heros.add(
            moqiu
        )
    }

    override suspend fun doUpHero(heroBean: HeroBean, position: Int, hasKuoJianClicked: Boolean) {
        var rect = when (position) {
            0 -> Config.zhandou_hero1CheckRect
            1 -> Config.zhandou_hero2CheckRect
            else -> Config.zhandou_hero3CheckRect
        }
        MRobot.singleClick(rect.clickPoint)//点击卡片
    }

    override suspend fun shuaka(needShuaxin: Boolean): List<HeroBean?> {
        var hs: List<HeroBean?>? = getPreHeros(if (needShuaxin) Config.delayLong else Long.MAX_VALUE)
        while (hs == null && running) {
            log("未识别到英雄")
            MRobot.singleClick(Config.zhandou_shuaxinPoint)
            delay(Config.delayNor)
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
                withTimeout(500) {
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
                        if (h1 != moqiu && h2 != moqiu && h3 != moqiu) {//省去最后的100ms
                            delay(100)
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

    override suspend fun dealHero(heros: List<HeroBean?>): Int {
        return heros.indexOf(moqiu)
    }

    override fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean? {
        return null
    }
}