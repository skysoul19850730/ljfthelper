package tasks.hanbing.zhanjiang

import data.Config
import data.HeroBean
import data.Recognize
import getImage
import kotlinx.coroutines.*
import log
import tasks.CarDoing
import tasks.HeroDoing
import tasks.Zhuangbei
import tasks.guankatask.GuankaTask
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*

class HBZhanNvHeroDoing : HeroDoing(0), App.KeyListener {//默认赋值0，左边，借用左边第一个position得点击，去识别车位置后再更改
//es8  es4 ye5   6e0 y80  y50 y56 e04
    /**1 全满 龙心
     *2 101 烟斗
     *3 111 强袭
     *4 131 龙心
     *9 小易无限补卡
     **/
    var guanka = 1
    var waiting = false

    val zhanjiang = HeroBean("zhanjiang", 100)
    val nvwang = HeroBean("nvwang", 90)
    val saman = HeroBean("saman2", 80)
    val jiaonv = HeroBean("jiaonv", 70)
    val shahuang = HeroBean("shahuang", 60, compareRate = 0.9)
    val xiaoye = HeroBean("xiaoye", 50)
    val muqiu = HeroBean("muqiu", 40, needCar = false, compareRate = 0.95)
    val moqiu = HeroBean("daoke", 30, needCar = false, compareRate = 0.9)
    val huanqiu = HeroBean("huanqiu", 20, needCar = false, compareRate = 0.95)
    val guangqiu = HeroBean("guangqiu", 0, needCar = false)


    var guankaTask = GuankaTask().apply {
        changeListener = object : GuankaTask.ChangeListener {
            override fun onGuanChange(guan: Int) {
                if (guan > 129 && guanka != 4) {
                    chuanZhangObeserver = false
                    guanka = 4
                    waiting = false
                    return
                }

                if (guan > 126 && guanka < 7) {
                    startChuanZhangOberserver()
                    return
                }
                if (guan > 110 && guanka == 9) {
                    guanka = 3
                    waiting = false
                    return
                }

                if (guan in 108..109 && guanka != 9) {
                    guanka = 9
                    waiting = false
                    return
                }

                if (guan > 100 && guanka == 8 && isGkOver(guanka)) {
                    longwangObserver = false
                    guanka = 2
                    waiting = false
                    return
                }
                if (guan in 98..99 && guanka != 9) {
                    startLongWangOberserver()
                    return
                }

                if (guan > 70 && guanka == 202 && isGkOver(guanka)) {
                    guanka = 8
                    waiting = false
                    return
                }
                if(guan>40 && guanka==1 && isGkOver(guanka)){
                    guanka = 202
                    waiting = false
                    return
                }
            }
        }
    }

    fun isGkOver(g: Int): Boolean {

        if(g==1){
            var heroOk = zhanjiang.isFull() && jiaonv.isFull() && saman.isFull() && xiaoye.isFull() && nvwang.isInCar() && Zhuangbei.isLongxin()
            return heroOk
        }

        var heroOk = zhanjiang.isFull() && jiaonv.isFull() && saman.isFull() && shahuang.isFull() && xiaoye.isFull()
        if (!heroOk) return false
        return when (g) {
            202 -> {
                Zhuangbei.isLongxin() && nvwang.isFull()
            }

            2 -> {
                Zhuangbei.isYandou() && nvwang.isFull()
            }

            3 -> {
                Zhuangbei.isQiangxi() && nvwang.isInCar()
            }

            4 -> {
                Zhuangbei.isLongxin() && nvwang.isInCar()
            }

            8 -> {
                Zhuangbei.isQiangxi() && nvwang.isFull()
            }

            else -> false
        }
    }

    var longwangObserver = false
    private fun startLongWangOberserver() {
        if (longwangObserver) return
        longwangObserver = true
        GlobalScope.launch {
            while (longwangObserver) {
                var needDown = true
                if (Config.hbFSCloud.isFit()) {
                    waiting = true
                    carDoing.downHero(shahuang)
                } else if (Config.hbMSCloud.isFit()) {
                    waiting = true
                    carDoing.downHero(xiaoye)
                    carDoing.downHero(saman)
                    carDoing.downHero(jiaonv)
                } else {
                    needDown = false
                }
                if (needDown) {
                    delay(10000)
                    waiting = false
                }
                delay(100)
            }
        }
    }

    var chuanZhangObeserver = false
    var chuanzhangDownCount = 0
    var time1 = 0L
    var time2 = 0L
    var time3 = 0L
    private fun startChuanZhangOberserver() {
        if (chuanZhangObeserver) return
        chuanZhangObeserver = true
        GlobalScope.launch {
            while (chuanZhangObeserver) {
                var img = getImage(App.rectWindow)
                var index = carDoing.getChuanZhangMax(img)
                var index2 = CarDoing((carDoing.chePosition + 1) % 2, CarDoing.CheType_YangChe).run {
                    initPositions()
                    getChuanZhangMax(img)
                }
                if (index >= 0 || index2 > 0) {
                    if (index >= 0) {
                        var hero = carDoing.heroList.get(index)!!
                        log("检测到被标记  位置：$index  英雄：${hero.heroName}")

//                        if (hasWuDi && hero == mengyan) {//点梦魇，有无敌，不下
//                            hasWuDi = false
//                        } else {
                        carDoing.downHero(hero)
                        guanka = 3
                        waiting = false
//                        }
                    }
                    chuanzhangDownCount++
                    var isSencodDianming = chuanzhangDownCount % 2 == 0
                    if (!isSencodDianming) {//第一次点卡后等3秒再开始识别
                        delay(3000)
                    } else {//第二次点卡后 刷6秒补卡然后停止（这个时间慢慢校验)要撞船了
                        if (chuanZhangObeserver) {
                            time1 = System.currentTimeMillis()
//                            delay(10000)
                            delay(7500)
                            waiting = true
                            delay(12500)//5秒后 效果消失，继续补卡，并监听点名
                            waiting = false
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        guankaTask.start()
    }

    override fun onStop() {
        super.onStop()
        chuanZhangObeserver = false
        guankaTask.stop()
        App.keyListeners.remove(this)
    }

    companion object {
        var che300Name: String? = null
    }

    override fun initHeroes() {
        che300Name = null
        heros = arrayListOf()
        heros.add(zhanjiang)
        heros.add(nvwang)
        heros.add(saman)
        heros.add(jiaonv)
        heros.add(shahuang)
        heros.add(xiaoye)
        heros.add(muqiu)
        heros.add(moqiu)
        heros.add(huanqiu)
        heros.add(guangqiu)

        App.keyListeners.add(this)
    }

    var longyunStart = 0L
    suspend fun doOnKeyDown(code: Int): Boolean {

        if (code == VK_1) {
            App.save()
            carDoing.downHero(shahuang)
            longyunStart = System.currentTimeMillis()
        } else if (code == VK_2) {
            App.save()
            carDoing.downHero(xiaoye)
            carDoing.downHero(saman)
            carDoing.downHero(jiaonv)
            longyunStart = System.currentTimeMillis()
        } else if (code == VK_3) {
            waiting = false
            var endTime = System.currentTimeMillis()
            log("识别到云到云炸，大约：${(endTime - longyunStart) / 1000}")
        }

        return true
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
        if (heroCountInCar() > 1) {
            kuojianguo = true
        }

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

            if (mChePos == 1) {
                Config.zhandou_kuojianPoint.click()//如果在右边就扩建，否则无法识别星级
                delay(500)
            }
            kuojianguo = true
        }


//        if (needCheckStar && heroBean.needCar && kuojianguo) {//等再次上英雄时 再查
        if (needCheckStar && kuojianguo) {//等再次上英雄时 再查
            checkStars()
        }

        if (heroBean == guangqiu) {

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

        if (heroBean == huanqiu) {
            //扔幻时 记录当前  发生改变后就可以结束（因为主卡幻一定成功）否则这里逻辑就不可以了
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

        if (!waiting && isGkOver(guanka)) {
            waiting = true
        }

        while (waiting) {//卡住 不再刷卡，幻的原因是，之前先预选了卡，比如第一个是木球，但过程中使用幻或者其他操作已经改变了预选卡的组成，比如第一个变成了幻。导致小翼无限刷卡时第一个判断上木，结果就上成了幻！！！
            delay(100)
        }
    }

    override suspend fun dealHero(heros: List<HeroBean?>): Int {

        while (waiting) {
            delay(100)
        }
        if (guanka == 202) {
            var index = heros.indexOf(shahuang)
            if (index > -1) {
                return index
            }
            index = heros.indexOf(guangqiu)
            if (index > -1 && !shahuang.isFull() && shahuang.isInCar()) {
                return index
            }
        } else

            if (guanka == 1) {//第一阶段
                if (!zhanjiang.isFull()) {//直上战将
                    log("战将没满")
//                log("战将没满")
                    var index = heros.indexOf(zhanjiang)
                    if (index > -1) {
                        return index
                    }
                    index = heros.indexOf(guangqiu)
                    if (index > -1 && zhanjiang.isInCar()) {
                        return index
                    }
                } else {
                    log("战将满了")

                    var fullList = arrayListOf(jiaonv, saman, xiaoye, nvwang)
                    var index = defaultDealHero(
                        heros,
                        fullList
                    )
                    if (index > -1) return index

                    index = heros.indexOf(nvwang)
                    if (index > -1 && !nvwang.isFull()) {
                        return index
                    }
                    index = heros.indexOf(guangqiu)
                    if (index > -1) {
                        return index
                    }
                }

                var index = heros.indexOf(huanqiu)
                if (index > -1 && !Zhuangbei.isLongxin() && Zhuangbei.hasZhuangbei()) {
                    return index
                }
                return -1
            } else if (guanka == 2) {//101 yandou
                //在之前基础上 先刷龙心
                var index = heros.indexOf(huanqiu)
                if (index > -1 && !Zhuangbei.isYandou() && Zhuangbei.hasZhuangbei()) {//小翼 烟斗
                    return index
                }


            } else if (guanka == 3) {//111
                if (nvwang.isFull()) {
                    carDoing.downHero(nvwang)
                }


                var index = heros.indexOf(huanqiu)
                if (index > -1 && !Zhuangbei.isQiangxi() && Zhuangbei.hasZhuangbei()) {//小翼 烟斗
                    return index
                }
                index = heros.indexOf(nvwang)
                if (index > -1 && !nvwang.isInCar()) {
                    return index
                }
                var fullList = arrayListOf(jiaonv, saman, xiaoye, shahuang, nvwang, zhanjiang)
                index = defaultDealHero(
                    heros,
                    fullList
                )
                if (index > -1) return index

            } else if (guanka == 4) {


                var index = heros.indexOf(huanqiu)
                if (index > -1 && !Zhuangbei.isLongxin() && Zhuangbei.hasZhuangbei()) {//小翼 烟斗
                    return index
                }
            } else if (guanka == 9) {
                var fullList = arrayListOf(nvwang, shahuang, jiaonv, saman, xiaoye, muqiu, zhanjiang)
                var index = defaultDealHero(
                    heros,
                    fullList
                )
                if (index > -1) return index
            } else if (guanka == 8) {//70强袭
                var index = heros.indexOf(huanqiu)
                if (index > -1 && !Zhuangbei.isQiangxi() && Zhuangbei.hasZhuangbei()) {//小翼 烟斗
                    return index
                }

                var fullList = arrayListOf(jiaonv, saman, xiaoye, shahuang, nvwang, guangqiu)
                index = defaultDealHero(
                    heros,
                    fullList
                )
                if (index > -1) return index
            }
        return -1
    }

    private suspend fun useMoqiu(): Boolean {
        return false
    }

    override fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean? {
        return null
    }

    override suspend fun onKeyDown(code: Int): Boolean {
        return doOnKeyDown(code)
    }
}