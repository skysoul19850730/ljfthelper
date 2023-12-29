package tasks.huodong

import App
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinUser
import data.Config
import data.HeroBean
import data.Recognize
import kotlinx.coroutines.*
import tasks.HeroDoing
import kotlin.coroutines.resume
import log
import tasks.CarDoing
import tasks.Zhuangbei
import tasks.gameUtils.GameUtil
import tasks.hezuo.zhannvsha.ZhanNvHeroDoing
import utils.MRobot
import java.awt.event.KeyEvent

class HuodongHeroDoing2 : HeroDoing(0), App.KeyListener {

    var needChangeZhuangbei = false

    override fun initHeroes() {
        heros = arrayListOf<HeroBean>().apply {
            add(HeroBean("tieqi"))
            add(HeroBean("jianke", weightModel = 0))

            add(HeroBean("bingnv", weightModel = 0))
            add(HeroBean("saman"))
            add(HeroBean("kuangjiang"))
            add(HeroBean("daoke"))

            add(HeroBean("guangqiu", needCar = false))

            add(HeroBean("huanqiu", needCar = false))

            add(HeroBean("sishen"))
            add(HeroBean("xiaochou"))
        }
    }

    var needCheckStar = false
    private suspend fun checkStars() {
        carDoing.checkStars()
        needCheckStar = false
    }

    var mChePos = -1
    var kuojianguo = false

    var curZhuangBei: Int = 0

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

        if (heroBean.heroName == "huanqiu") {
            //扔幻时 记录当前  发生改变后就可以结束（因为主卡幻一定成功）否则这里逻辑就不可以了

            var newZB = withTimeoutOrNull(2000) {
                while (Zhuangbei.getZhuangBei() == curZhuangBei && Zhuangbei.getZhuangBei() != 0) {
                    delay(Config.delayNor)
                }
                Zhuangbei.getZhuangBei()
            } ?: 0
            if (newZB != curZhuangBei) {
                needChangeZhuangbei = false
                curZhuangBei = newZB
            }

        }
    }


    override suspend fun dealHero(heros: List<HeroBean?>): Int {

        var index = defaultDealHero2(heros, this.heros.take(6))
        if (index > -1) {
            return index
        }
        index = heros.indexOf(this.heros.get(6))
        if (index > -1 && !this.heros.take(6).all {//光
                it.isFull()
            }) {
            return index
        }

        index = heros.indexOf(this.heros.get(7))//幻
        if (index > -1 && needChangeZhuangbei) {
            return index
        }

        return -1
    }

    suspend fun defaultDealHero2(heros: List<HeroBean?>, heroSorted: List<HeroBean>): Int {
        var pre = -1
        heroSorted.forEach {
            var index = heros.indexOf(it)
            if (index > -1) {
                if (it.weightModel == 1 || !it.isInCar()) {
                    return index
                } else {
                    if (pre < 0) {//先预备的优先级高，不被后来的覆盖
                        pre = index
                    }
                }
            }
        }
        return pre
    }

    override fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean? {

        var toIndex = this.heros.indexOf(heroBean)

        var downHero = this.heros.filter {
            it.isInCar()
        }.maxByOrNull {
            this.heros.indexOf(it)
        }
        if (downHero != null && toIndex < this.heros.indexOf(downHero)) {//to 更考前
            return downHero
        }

        return null
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    suspend fun doOnKeyDown(code: Int): Boolean {
        var handle = true
        if (code == KeyEvent.VK_NUMPAD1) {
            needChangeZhuangbei = true
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