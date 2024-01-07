package tasks

import MainData
import data.*
import data.Config.delayLong
import data.Config.delayNor
import getImage
import kotlinx.coroutines.*
import log
import logOnly
import utils.LogUtil
import utils.MRobot
import kotlin.coroutines.resume

abstract class HeroDoing(var chePosition: Int = -1) : IDoing {

    lateinit var heros: ArrayList<HeroBean>
    lateinit var carDoing: CarDoing

    var running = false
    private var mainJob: Job? = null


    abstract fun initHeroes()

    //决定用哪个，-1都不用(如何车上6个满了，现在要替换英雄，需要实现者去下卡，这个方法只会告诉外面点击哪个上车）
    abstract suspend fun dealHero(heros: List<HeroBean?>): Int

    //每格子是否更换场上英雄，返回null代表不更换，等着开格子,返回bean 下bean再上bean
    abstract fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean?

    //点了英雄后，比如光 魔等，需要延迟
    open suspend fun afterHeroClick(heroBean: HeroBean) {

    }

    override fun init() {
        initHeroes()
        carDoing = CarDoing(chePosition).apply {
            initPositions()
            attchToMain()
        }
    }

    fun heroCountInCar() = heros.filter { it.isInCar() && !it.isGongCheng }.size


    open fun onStart() {

    }

    override fun start() {
        if (running) return
        running = true
        onStart()
        mainJob = GlobalScope.launch {
            var hero = shuaka(false)
            while (running) {
                shangka(hero)
                hero = shuaka()
            }
        }
    }


    open fun onStop() {
        MainData.heros.value = arrayListOf()
        LogUtil.saveAndClear()
    }

    override fun stop() {
        log("herodoing stop")
        running = false
        mainJob?.cancel("tuichu")
        onStop()
    }

    open suspend fun shuaka(needShuaxin: Boolean = true): List<HeroBean?> {
        var hs: List<HeroBean?>? = null
        while (hs == null && running) {
            logOnly("未识别到英雄")
//            if(Recognize.saleRect.isFit()) {//识别不到 识别看看是不是英雄弹窗挡住了//如果上卡和下卡一起操作，这里会导致下卡失败
//                CarDoing.cardClosePoint.click()
//            }
            if (needShuaxin) {
                while(!Config.rect4ShuakaColor.hasWhiteColor()){//有白色（钱够）再点击刷新
                    delay(50)
                }
                MRobot.singleClick(Config.zhandou_shuaxinPoint)
            }
            delay(100)
            hs = getPreHeros(if (needShuaxin) delayLong else 10000)
        }
        log("识别到英雄 ${hs?.getOrNull(0)?.heroName}  ${hs?.getOrNull(1)?.heroName}  ${hs?.getOrNull(2)?.heroName}")
        if(Config.debug) {
            log(
                getImage(
                    MRect.create4P(
                        Config.zhandou_hero1CheckRect.left,
                        Config.zhandou_hero1CheckRect.top,
                        Config.zhandou_hero3CheckRect.right,
                        Config.zhandou_hero1CheckRect.bottom
                    )
                )
            )
        }
        return hs!!
    }

    private suspend fun shangka(hs: List<HeroBean?>) {
        val heroChoose = dealHero(hs)
        logOnly("上卡的index 是 ${heroChoose}")
        if (heroChoose > -1) {
            doUpHero(hs.get(heroChoose)!!, heroChoose)
        }
    }

    open suspend fun doUpHero(heroBean: HeroBean, position: Int, hasKuoJianClicked: Boolean = false) {
        var rect = when (position) {
            0 -> Config.zhandou_hero1CheckRect
            1 -> Config.zhandou_hero2CheckRect
            else -> Config.zhandou_hero3CheckRect
        }
//        MRobot.singleClick(rect.clickPoint)//点击卡片 //这里点击卡片可能刚好点中end得确定按钮，导致检测不到结束
        MRobot.singleClick(MPoint(rect.clickPoint.x, rect.clickPoint.y + 25))
        delay(Config.delayNor)
        //如果正在下卡，弹窗会挡住，识别不到英雄，就等于认为已经上卡了
        while(carDoing.downing){
            delay(50)
        }
        var hs = doGetPreHeros()

        if (hs == null) {//上车了(点击后再检验，目标区域不含英雄了）
            carDoing.addHero(heroBean)
            log("英雄上阵:${heroBean.heroName} 位置:${heroBean.position} 等级:${heroBean.currentLevel}")
            afterHeroClick(heroBean)
        } else {//上不去，没格子了(如何是换卡，在这之前已经下了卡了，下了卡就能上去，所以这里只会因为没有格子而上不去，所以点扩建再尝试上卡
            logOnly("英雄未上阵")
            if(Config.rect4KuojianColor.hasWhiteColor()){
                logOnly("尝试点击一次扩建")
                MRobot.singleClick(Config.zhandou_kuojianPoint)//点扩建
                delay(Config.delayNor)
                doUpHero(heroBean, position, true)
            }else{
                var changeOne = changeHeroWhenNoSpace(heroBean)
                if (changeOne != null) {
                    logOnly("没钱扩建，替换英雄")
                    carDoing.downHero(changeOne)
                    delay(delayNor)
                    doUpHero(heroBean, position)
                } else {
                    logOnly("再次尝试点击扩建")
                    while(!Config.rect4KuojianColor.hasWhiteColor()){
                        delay(50)
                    }
                    MRobot.singleClick(Config.zhandou_kuojianPoint)//点扩建
                    delay(Config.delayNor)
                    doUpHero(heroBean, position, true)
                }
            }
//            if (!hasKuoJianClicked) {//有钱扩建就先不替换
//                logOnly("尝试点击一次扩建")
//                MRobot.singleClick(Config.zhandou_kuojianPoint)//点扩建
//                delay(Config.delayNor)
//                doUpHero(heroBean, position, true)
//            } else {
//                var changeOne = changeHeroWhenNoSpace(heroBean)
//                if (changeOne != null) {
//                    logOnly("没钱扩建，试试要不要替换英雄")
//                    carDoing.downHero(changeOne)
//                    delay(delayNor)
//                    doUpHero(heroBean, position)
//                } else {
//                    logOnly("再次尝试点击扩建")
//                    MRobot.singleClick(Config.zhandou_kuojianPoint)//点扩建
//                    delay(Config.delayNor)
//                    doUpHero(heroBean, position, true)
//                }
//            }
        }

    }

    private suspend fun doGetPreHeros() = suspendCancellableCoroutine<List<HeroBean?>?> {
        GlobalScope.launch {
            val hero1 =
                async { getHeroAtRect(Config.zhandou_hero1CheckRect) }

            val hero2 =
                async { getHeroAtRect(Config.zhandou_hero2CheckRect) }
            val hero3 =
                async { getHeroAtRect(Config.zhandou_hero3CheckRect) }

            val h1 = hero1.await()
            val h2 = hero2.await()
            val h3 = hero3.await()
            if (h1 == null && h2 == null && h3 == null) {
                it.resume(null)
            } else {
                it.resume(arrayListOf(h1, h2, h3))
            }
        }
    }

    open suspend fun getPreHeros(timeout: Long = 2300) = suspendCancellableCoroutine<List<HeroBean?>?> {
        val startTime = System.currentTimeMillis()
        GlobalScope.launch {
            var h1: HeroBean? = null
            var h2: HeroBean? = null
            var h3: HeroBean? = null

            try {
                withTimeout(timeout) {
                    while (h1 == null || h2 == null || h3 == null) {
                        if (!running) {
                            break
                        }
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
//                        var i=0;
//                        if(h1!=null)i++
//                        if(h2!=null)i++
//                        if(h3!=null)i++
//                        if(i>=2)break
                        if (h1 == null || h2 == null || h3 == null) {//省去最后的100ms
                            delay(100)
                        }
                    }
                    logOnly("getPreHeros cost time:${System.currentTimeMillis() - startTime}")
                    it.resume(arrayListOf(h1, h2, h3))
                }
            } catch (e: Exception) {
                if (h1 == null && h2 == null && h3 == null) {
                    it.resume(null)
                } else {
                    it.resume(arrayListOf(h1, h2, h3))
                }
            }
        }
    }

    suspend fun getHeroAtRect(rect: MRect) = suspendCancellableCoroutine<HeroBean?> {
        val img = getImage(rect)
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
        }else logOnly("getHeroAtRect null")
        it.resume(hero)

    }

    suspend fun defaultDealHero(heros: List<HeroBean?>, heroSorted:List<HeroBean>):Int{
        heroSorted.forEach {
            var index = heros.indexOf(it)
            if(index>-1){
                return index
            }
        }
        return -1
    }
}