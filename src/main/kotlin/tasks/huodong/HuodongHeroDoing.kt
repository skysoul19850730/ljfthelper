package tasks.huodong

import App
import com.sun.jna.platform.win32.WinUser
import data.Config
import data.HeroBean
import data.Recognize
import kotlinx.coroutines.*
import tasks.HeroDoing
import kotlin.coroutines.resume
import log
import tasks.CarDoing
import tasks.gameUtils.GameUtil
import tasks.hezuo.zhannvsha.ZhanNvHeroDoing
import utils.MRobot
import java.awt.event.KeyEvent

class HuodongHeroDoing : HeroDoing(0), App.KeyListener {

    var guanka = 0
    var waiting = false

    lateinit var muqiu: HeroBean
    lateinit var hunqiu: HeroBean

    init {
        hunqiu = HeroBean("hunqiu", 70, needCar = false)
        muqiu = HeroBean("muqiu", 60, needCar = false)
        App.keyListeners.add(this)
    }


    override fun initHeroes() {
        heros = arrayListOf<HeroBean>().apply {
            add(HeroBean("houyi"))
            add(HeroBean("hugong"))

            add(HeroBean("houzi"))
            add(HeroBean("xiaoye"))
            add(HeroBean("gugu"))
            add(HeroBean("dianfa"))
            add(HeroBean("dapao", needCar = false, isGongCheng = true))
            add(HeroBean("guangqiu", needCar = false))
            add(muqiu)
            add(hunqiu)
        }
    }

    var needCheckStar = false
    private suspend fun checkStars() {
        carDoing.checkStars()
        needCheckStar = false
    }

    var mChePos = -1
    var kuojianguo = false

    override suspend fun afterHeroClick(heroBean: HeroBean) {
        if (mChePos == -1 && heroBean.needCar) {//未识别车时,并且 这个hero是上阵得英雄
            log("开始检测车")
            carDoing.carps.get(0).click()
            delay(1000)
            if (Recognize.saleRect.isFit()) {//是自己，啥也不用干，开始初始化得位置就是对得
                mChePos = 0//
            } else {
                //我在右边
                mChePos = 1
                chePosition = 1
                carDoing.chePosition = 1
                carDoing.initPositions()
            }
            log("识别车位结果：$mChePos")
            CarDoing.cardClosePoint.click()
        }

        if (heroCountInCar() > 1) {
            kuojianguo = true
        }

        if (needCheckStar && heroBean.needCar && kuojianguo) {//等再次上英雄时 再查
            checkStars()
        }

        if (heroBean.heroName == "guangqiu") {

            var checked = carDoing.checkStarsWithoutCard()
            if (!checked) {//1.5秒没有check到的话，再使用弹窗识别
                if (kuojianguo) {//扩建过开启检查，否则车位不准，先不检查,等上英雄时再检查
//                    delay(1500)
                    checkStars()
                } else {
                    needCheckStar = true
                }
            }
        }

        if (!waiting && isGuanKaOver()) {
            waiting = true
        }




        while (waiting) {
            delay(100)
        }
    }

    fun isGuanKaOver(): Boolean {
        return when (guanka) {
            0 -> {
                heros.filter {
                    it.needCar || it.isGongCheng
                }.all {
                    it.isFull()
                }
            }

            else -> false
        }
    }


    override suspend fun dealHero(heros: List<HeroBean?>): Int {
        while (waiting) {
            delay(100)
        }
        if(guanka==0){
            return defaultDealHero(heros,this.heros)
        }else if(guanka==1){
            //第二阶段不用光球了，来了就补，不来就补血
            return defaultDealHero(heros,this.heros.filter {
                it.heroName != "guangqiu"
            })
        }
        return -1
    }

    override fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean? {
        return null
    }

    override fun onStart() {
        super.onStart()
        GlobalScope.launch {
            delay(56*1000)
            guanka = 1
            waiting = false
        }
    }
    override fun onStop() {
        super.onStop()
    }

    suspend fun doOnKeyDown(code: Int): Boolean {
        var handle = true
        if (code == KeyEvent.VK_NUMPAD1) {
            guanka = 1
            waiting = false
        } else {
            handle = false
        }

        if (handle) {
            log("code $code is Down")
        }

        return handle
    }

    override suspend fun onKeyDown(code: Int): Boolean {
        return doOnKeyDown(code)
    }

}