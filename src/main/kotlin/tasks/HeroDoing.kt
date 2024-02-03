package tasks

import data.*
import data.Config.delayLong
import data.Config.delayNor
import doDebug
import getImage
import kotlinx.coroutines.*
import log
import logOnly
import model.CarDoing
import tasks.guankatask.GuankaTask
import utils.LogUtil
import utils.MRobot
import kotlin.coroutines.resume

abstract class HeroDoing(var chePosition: Int = -1, val flags: Int = 0) : IDoing, GuankaTask.ChangeListener,
    App.KeyListener {

    companion object {
        val FLAG_GUANKA = 0x00000001
        val FLAG_KEYEVENT = 0x00000010
    }

    //在InitHero里更改吧。这个就是天空的时候发现车有偏移，与合作和寒冰的车的坐标有出入，但大小没变，所以暂时只改偏移就行
    var carPosOffset = 0

    lateinit var heros: ArrayList<HeroBean>
    lateinit var carDoing: CarDoing
    lateinit var otherCarDoing: CarDoing
    private var carChecked = false

    var running = false
    private var mainJob: Job? = null
    var waiting = false

    var guankaTask: GuankaTask? = null


    abstract fun initHeroes()

    //决定用哪个，-1都不用(如何车上6个满了，现在要替换英雄，需要实现者去下卡，这个方法只会告诉外面点击哪个上车）
    abstract suspend fun dealHero(heros: List<HeroBean?>): Int

    //每格子是否更换场上英雄，返回null代表不更换，等着开格子,返回bean 下bean再上bean
    abstract fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean?

    //点了英雄后，比如光 魔等，需要延迟
    open suspend fun afterHeroClick(heroBean: HeroBean) {
        //-1 为单车，不用check
        if (chePosition != -1 && !carChecked && heroBean.position == 0) {
            checkCar()
//            if(carDoing.chePosition==1){//变换了
//                heroBean.reset()
//                carDoing.addHero(heroBean)
//            }
        }

        if (needCheckStar && carDoing.openCount() > 1) {
            carDoing.checkStars()
            needCheckStar = false
        }

        if (heroBean.heroName == "guangqiu") {
            onGuangqiuPost()
        } else if (heroBean.heroName == "huanqiu") {
            onHuanQiuPost()
        }

        doAfterHeroBeforeWaiting(heroBean)

        if (App.reCheckStar) {
            carDoing.reCheckStars()
            App.reCheckStar = false
        }

        while (waiting) {//卡住 不再刷卡，幻的原因是，之前先预选了卡，比如第一个是木球，但过程中使用幻或者其他操作已经改变了预选卡的组成，比如第一个变成了幻。导致小翼无限刷卡时第一个判断上木，结果就上成了幻！！！
            delay(100)
        }
    }

    open suspend fun doAfterHeroBeforeWaiting(heroBean: HeroBean) {

    }

    var curZhuangBei: Int = 0
    open suspend fun onHuanQiuPost() {
        curZhuangBei = Zhuangbei.getZhuangBei()
        delay(Config.delayNor)
        try {
            withTimeout(1500) {//加个超时保险一些，防止死循环
                while (Zhuangbei.getZhuangBei() == curZhuangBei && Zhuangbei.getZhuangBei() != 0) {
                    delay(Config.delayNor)
                }
            }
        } catch (e: Exception) {

        }
    }

    var needCheckStar = false
    open suspend fun onGuangqiuPost() {
        if(App.mLaunchModel and App.model_duizhan !=0){
            carDoing.checkStars()
            return
        }

        var noFullCount = carDoing.carps.count {
            !(it.mHeroBean?.isFull() ?: true)
        }
        if (noFullCount == 1) {//只有一个英雄就直接长星，15光了
            carDoing.carps.find {
                !(it.mHeroBean?.isFull() ?: true)
            }?.addHero()
        } else if (noFullCount < 1) {//都满着就不用验了
            return
        } else {
            var checked = carDoing.checkStarsWithoutCard()
            if (!checked) {//1.5秒没有check到的话，再使用弹窗识别
                if (carDoing.openCount() > 1 || chePosition == 0) {//前车或开格子多余1个
                    carDoing.checkStars()
                } else {
                    needCheckStar = true
                }
            }
        }


    }

    override fun init() {
        initHeroes()
        carDoing = CarDoing(chePosition).apply {
            initPositions(carPosOffset)
            attchToMain()
        }
        if (flags and FLAG_GUANKA != 0) {
            guankaTask = GuankaTask().apply {
                changeListener = this@HeroDoing
            }
        }
        if (flags and FLAG_KEYEVENT != 0) {
            App.keyListeners.add(this)
        }
    }

    fun heroCountInCar() = heros.filter { it.isInCar() && !it.isGongCheng }.size


    open fun onStart() {
        guankaTask?.start()
    }

    private suspend fun checkCar() {
        log("开始检测车")
        carDoing.carps.get(0).click()
        delay(1000)
        if (Recognize.saleRect.isFit()) {//是自己，啥也不用干，开始初始化得位置就是对得
            chePosition = 0//
        } else {
            //我在右边
            chePosition = 1
            carDoing.chePosition = 1
            carDoing.reInitPositions(carPosOffset)
        }
        log("识别车位结果：$chePosition")
        CarDoing.cardClosePoint.click()
        otherCarDoing = CarDoing((chePosition + 1) % 2).apply {
            initPositions()
        }
        carChecked = true
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
        guankaTask?.stop()
        App.keyListeners.remove(this)
        MainData.carPositions.clear()
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
//                withTimeoutOrNull(2000) {//超时就点一下，这里没有问题
                    while (!Config.rect4ShuakaColor.hasWhiteColor()) {//有白色（钱够）再点击刷新
                        delay(50)
                    }
//                }
                MRobot.singleClick(Config.zhandou_shuaxinPoint)
            }
            delay(200)
            hs = getPreHeros(if (needShuaxin) 600 else 10000)//点完刷新如果1秒中识别不出来应该就是识别不出来了，这里不需要2秒
        }
        log("识别到英雄 ${hs?.getOrNull(0)?.heroName}  ${hs?.getOrNull(1)?.heroName}  ${hs?.getOrNull(2)?.heroName}")
        doDebug {
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
        while (waiting) {
            delay(100)
        }
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
        if (heroBean.isInCar() || !heroBean.needCar || heroBean.isGongCheng || carDoing.hasOpenSpace()) {
            //英雄在车上，或者此卡不需要车位,或者该卡是工程卡（暂时一套阵容不会有带两个工程），或者车上有空位
            MRobot.singleClick(MPoint(rect.clickPoint.x, rect.clickPoint.y + 25))
        } else {
            //需要开格子
            if (Config.rect4KuojianColor.hasWhiteColor()) {//可以开格子
                log("点击扩建")
                MRobot.singleClick(Config.zhandou_kuojianPoint)//点扩建
                delay(50)
                MRobot.singleClick(MPoint(rect.clickPoint.x, rect.clickPoint.y + 25))
            } else {
                var changeOne = changeHeroWhenNoSpace(heroBean)
                if (changeOne != null) {//钱不够扩建时，是否需要替换已在车上的卡
                    log("没钱扩建，替换英雄")
                    carDoing.downHero(changeOne)
                    delay(50)
                    MRobot.singleClick(MPoint(rect.clickPoint.x, rect.clickPoint.y + 25))
                } else {//不替换就等钱够
                    while (!Config.rect4KuojianColor.hasWhiteColor()) {
                        delay(50)
                    }
                    log("点击扩建")
                    MRobot.singleClick(Config.zhandou_kuojianPoint)//点扩建
                    delay(50)
                    MRobot.singleClick(MPoint(rect.clickPoint.x, rect.clickPoint.y + 25))
                }
            }
        }
        carDoing.addHero(heroBean)
        log("英雄上阵:${heroBean.heroName} 位置:${heroBean.position} 等级:${heroBean.currentLevel}")
//        delay(100)//这里略微等待一下，否则点完上卡，立马点刷新可能还会识别到
        delay(100)//这里略微等待一下，否则点完上卡，立马点刷新可能还会识别到,而且这里如果不延迟，可能点刷新太快了，几乎是点了上卡马上点刷新，可能没点上刷新（因为日志体现了好几组点完刷新后1秒超时无法识别的，大概率就是 点刷新没有效果）
        //这里还有一组日志是 上了猫咪点刷新然后识别到了猫咪，其他两个都是null。推测：点了猫咪 去点刷新（没点出效果）所以没有立即触发刷新卡牌，点击猫咪 猫咪自己有个动画，其他两个就消失了，所以这个时候会再次识别到猫咪，导致310猫咪不满
        //如果这里还有问题，就延迟加大一点，但50后点刷新不出问题应该就不会有问题了，因为以前刷不出需要的卡，然后去点刷新，也没有被残影破坏过识别，点了刷新还会延迟100毫秒才开始识别呢
        afterHeroClick(heroBean)


////        MRobot.singleClick(rect.clickPoint)//点击卡片 //这里点击卡片可能刚好点中end得确定按钮，导致检测不到结束
//        MRobot.singleClick(MPoint(rect.clickPoint.x, rect.clickPoint.y + 25))
//        delay(Config.delayNor)
//        //如果正在下卡，弹窗会挡住，识别不到英雄，就等于认为已经上卡了
//        while (carDoing.downing) {
//            delay(50)
//        }
//        var hs = doGetPreHeros()
//
//        if (hs == null) {//上车了(点击后再检验，目标区域不含英雄了）
//            carDoing.addHero(heroBean)
//            log("英雄上阵:${heroBean.heroName} 位置:${heroBean.position} 等级:${heroBean.currentLevel}")
//            afterHeroClick(heroBean)
//        } else {//上不去，没格子了(如何是换卡，在这之前已经下了卡了，下了卡就能上去，所以这里只会因为没有格子而上不去，所以点扩建再尝试上卡
//            logOnly("英雄未上阵")
//            if (Config.rect4KuojianColor.hasWhiteColor()) {
//                logOnly("尝试点击一次扩建")
//                MRobot.singleClick(Config.zhandou_kuojianPoint)//点扩建
//                delay(Config.delayNor)
//                doUpHero(heroBean, position, true)
//            } else {
//                var changeOne = changeHeroWhenNoSpace(heroBean)
//                if (changeOne != null) {
//                    logOnly("没钱扩建，替换英雄")
//                    carDoing.downHero(changeOne)
//                    delay(delayNor)
//                    doUpHero(heroBean, position)
//                } else {
//                    logOnly("再次尝试点击扩建")
//                    while (!Config.rect4KuojianColor.hasWhiteColor()) {
//                        delay(50)
//                    }
//                    MRobot.singleClick(Config.zhandou_kuojianPoint)//点扩建
//                    delay(Config.delayNor)
//                    doUpHero(heroBean, position, true)
//                }
//            }
////            if (!hasKuoJianClicked) {//有钱扩建就先不替换
////                logOnly("尝试点击一次扩建")
////                MRobot.singleClick(Config.zhandou_kuojianPoint)//点扩建
////                delay(Config.delayNor)
////                doUpHero(heroBean, position, true)
////            } else {
////                var changeOne = changeHeroWhenNoSpace(heroBean)
////                if (changeOne != null) {
////                    logOnly("没钱扩建，试试要不要替换英雄")
////                    carDoing.downHero(changeOne)
////                    delay(delayNor)
////                    doUpHero(heroBean, position)
////                } else {
////                    logOnly("再次尝试点击扩建")
////                    MRobot.singleClick(Config.zhandou_kuojianPoint)//点扩建
////                    delay(Config.delayNor)
////                    doUpHero(heroBean, position, true)
////                }
////            }
//        }

    }

//    private suspend fun doGetPreHeros() = suspendCancellableCoroutine<List<HeroBean?>?> {
//        GlobalScope.launch {
//            val hero1 =
//                async { getHeroAtRect(Config.zhandou_hero1CheckRect) }
//
//            val hero2 =
//                async { getHeroAtRect(Config.zhandou_hero2CheckRect) }
//            val hero3 =
//                async { getHeroAtRect(Config.zhandou_hero3CheckRect) }
//
//            val h1 = hero1.await()
//            val h2 = hero2.await()
//            val h3 = hero3.await()
//            if (h1 == null && h2 == null && h3 == null) {
//                it.resume(null)
//            } else {
//                it.resume(arrayListOf(h1, h2, h3))
//            }
//        }
//    }

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
//                        if (h1 == null || h2 == null || h3 == null) {//省去最后的100ms
//                            delay(50)
//                        }
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

    open suspend fun getHeroAtRect(rect: MRect) = suspendCancellableCoroutine<HeroBean?> {
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
        } else logOnly("getHeroAtRect null")
        it.resume(hero)

    }

    suspend fun defaultDealHero(heros: List<HeroBean?>, heroSorted: List<HeroBean>): Int {
        heroSorted.forEach {
            var index = heros.indexOf(it)
            if (index > -1) {
                return index
            }
        }
        return -1
    }

    open override fun onGuanChange(guan: Int) {
    }

    override suspend fun onKeyDown(code: Int): Boolean {
        return false
    }
}