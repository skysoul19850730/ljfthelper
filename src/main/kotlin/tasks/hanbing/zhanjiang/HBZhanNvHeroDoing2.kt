package tasks.hanbing.zhanjiang

import data.Config
import data.HeroBean
import data.MRect
import data.Recognize
import getImage
import kotlinx.coroutines.*
import log
import logOnly
import tasks.CarDoing
import tasks.HeroDoing
import tasks.Zhuangbei
import tasks.guankatask.GuankaTask
import utils.ImgUtil.forEach4Result
import java.awt.event.KeyEvent.*

class HBZhanNvHeroDoing2 : HeroDoing(0), App.KeyListener {//默认赋值0，左边，借用左边第一个position得点击，去识别车位置后再更改
//es8  es4 ye5   6e0 y80  y50 y56 e04

    enum class Guan {
        /**
         * 1-25g zhanjiang jiaonv xiaoye saman 装备龙心
         */
        g1,

        /**
         * 26-40 nvwang (shexian,现在没射线)
         */
        g26,

        /**
         * 41 shahuang
         */
        g41,


        /**
         * 烟斗
         */
        g101,

        /**
         * 无脑补卡
         */
        g108,

        /**
         * checkStars 补满 强袭
         */
        g110,

        /**
         * 龙心
         */
        g131,

        /**
         *  一直刷木
         */
        g139,

        /**
         * emmm,正常讲 不需要刷卡了，停木就完了，g140直接返回over 为true，现在139都打不到，先简单点
         * 这时 副卡遇见红球满女王，遇见蓝球下女王并升到3星就可以了
         */
        g140,

    }

    var guanka = Guan.g1
    var waiting = false

    val zhanjiang = HeroBean("zhanjiang", 100)
    val nvwang = HeroBean("nvwang", 90)
    val saman = HeroBean("saman2", 80)
    val jiaonv = HeroBean("jiaonv", 70)
    val shahuang = HeroBean("shahuang", 60, compareRate = 0.9)
    val sishen = HeroBean("sishen", 50)
    val muqiu = HeroBean("muqiu", 40, needCar = false, compareRate = 0.95)
    val shexian = HeroBean("shexian", 30, needCar = false, isGongCheng = true, compareRate = 0.9)
    val huanqiu = HeroBean("huanqiu", 20, needCar = false, compareRate = 0.95)
    val guangqiu = HeroBean("guangqiu", 0, needCar = false)


    var guankaTask = GuankaTask().apply {
        changeListener = object : GuankaTask.ChangeListener {
            override fun onGuanChange(guan: Int) {


                if (guan == 140) {
                    guanka = Guan.g140
                    waiting = false
                    return
                }

                if (guan == 139 && guanka != Guan.g139) {
                    guanka = Guan.g139
                    waiting = false
                    return
                }

                if (guan > 129 && guanka != Guan.g131) {
                    chuanZhangObeserver = false
                    guanka = Guan.g131
                    waiting = false
                    return
                }

                if (guan in 128..129 && guanka == Guan.g110) {
                    startChuanZhangOberserver()
                    return
                }
                if (guan > 109 && guanka == Guan.g108) {
                    guanka = Guan.g110
                    waiting = false
                    return
                }

                if (guan in 108..109 && guanka == Guan.g101) {
                    guanka = Guan.g108
                    waiting = false
                    return
                }

                if (guan > 99 && guanka == Guan.g41) {
                    longwangObserver = false
                    guanka = Guan.g101
                    waiting = false
                    return
                }
                if (guan in 98..99) {
                    startLongWangOberserver()
                    return
                }

                if (guan >= 38 && guanka == Guan.g26) {
                    guanka = Guan.g41
                    waiting = false
                    return
                }

                if (guan > 25 && guanka == Guan.g1) {
                    guanka = Guan.g26
                    waiting = false
                    return
                }
            }
        }
    }

    fun isGkOver(g: Guan): Boolean {

        if (g == Guan.g139) return false
        if (g == Guan.g140) return Zhuangbei.isYandou()
        if( g == Guan.g1) return zhanjiang.isFull() && jiaonv.isFull() && sishen.isFull()&&saman.isFull()&&Zhuangbei.isLongxin()

        var heroOk = zhanjiang.isFull() && jiaonv.isFull() && sishen.isFull() && shexian.isFull()
        if (!heroOk) return false
        return when (g) {
            Guan.g1 -> {
                Zhuangbei.isLongxin() && saman.isFull()
            }

            Guan.g26 -> {
                Zhuangbei.isLongxin() && nvwang.isFull() && saman.isFull()
            }

            Guan.g41 -> {
                Zhuangbei.isLongxin() && nvwang.isFull() && shahuang.isFull() && saman.isFull()
            }


            Guan.g101 -> {
                Zhuangbei.isYandou() && nvwang.isFull() && shahuang.isFull() && saman.isFull()
            }

            Guan.g108 -> {//无脑补卡，这时降星 是不知道星级的
                false
            }

            Guan.g110 -> {
                Zhuangbei.isQiangxi() && nvwang.isInCar() && shahuang.isFull() && saman.isInCar()
            }

            Guan.g131 -> {
                Zhuangbei.isLongxin() && nvwang.isInCar() && shahuang.isFull() && saman.isInCar()
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
                } else if(Config.hbMSCloud.isFit()){
                    carDoing.downHero(saman)
                    carDoing.downHero(sishen)
                    carDoing.downHero(jiaonv)
                    waiting = true
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

//        GlobalScope.launch {
//            var shibiedao = false
//            var firstttt = 0L
//            while (chuanZhangObeserver){
//                var img = getImage(MRect.createWH(4,100,300,370))
//                var count =0
//                img.foreach { i, i2 ->
//                    if(img.getRGB(i,i2) == Config.Color_ChuangZhang.rgb){
//                        count++
//                    }
//                    false
//                }
//                if(count>0) {
//                    if(!shibiedao) {//首次识别到
//                        firstttt = System.currentTimeMillis()
//                        shibiedao = true
//                        img.log("数量：$count   xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx船长识别到关键颜色 ")
//                    }
//                }else{
//                    if(shibiedao) {//首次识别到   到  识别不到.可以代表本次的持续时间,排查有没有特殊的和船长颜色一样的颜色被错误识别成，以便后续逻辑优化
//                        shibiedao = false
//                        img.logOnly("本次未识别到船长颜色 持续时间 ${System.currentTimeMillis() - firstttt}")
//                        firstttt = 0L
//                    }
//                }
//            }
//        }

        GlobalScope.launch {
            while (chuanZhangObeserver) {
                //4 100 300 370
                var img = getImage(App.rectWindow)
                var fitCount = 0
                MRect.createWH(4, 100, 300, 370).forEach4Result { x, y ->
                    if (img.getRGB(x, y) == Config.Color_ChuangZhang.rgb) {
                        fitCount++
                    }
                    fitCount > 1000
                }


                var index = carDoing.getChuanZhangMax(img)
                var index2 = CarDoing((carDoing.chePosition + 1) % 2, CarDoing.CheType_YangChe).run {
                    initPositions()
                    getChuanZhangMax(img)
                }
                var shibiedao = false
                if (index != null || index2 != null) {
                    shibiedao = true
                    if (index != null && (index2 == null || index.second > index2.second)) {
                        var hero = carDoing.heroList.get(index.first)
                        log("检测到被标记 本车：  位置：$index  英雄：${hero?.heroName}")

//                        if (hasWuDi && hero == mengyan) {//点梦魇，有无敌，不下
//                            hasWuDi = false
//                        } else {
                        if (hero != null) {
                            carDoing.downHero(hero)
                            waiting = false
                        }
//                        }
                    }else{
                        log("检测到被标记 副车：  位置：$index2 ")

                    }


                }
                if(fitCount>100){
                    log("fitcount is $fitCount")
                }
                if (fitCount > 1000 || shibiedao) {
                    log("船长点名啦")
                    log(img)
                    chuanzhangDownCount++

                    var isSencodDianming = chuanzhangDownCount % 2 == 0
                    if (!isSencodDianming) {//第一次点卡后等3秒再开始识别
                        log("第1次识别到船长，休整3秒后再开始识别：")
                        delay(3000)
                    } else {//第二次点卡后 刷6秒补卡然后停止（这个时间慢慢校验)要撞船了
                        if (chuanZhangObeserver) {
                            time1 = System.currentTimeMillis()
//                            delay(10000)
                            log("第2次识别到船长，6.5秒后暂停刷卡：")

                            delay(6500)
                            waiting = true
                            log("第2次识别到船长，暂停刷卡,14秒后开始刷卡,暂停识别，10秒后开始识别")

                            GlobalScope.launch {
                                delay(14000)
                                log("第2次识别到船长，恢复刷卡")
                                waiting = false
                            }
                            delay(10000)//5秒后 效果消失，继续补卡，并监听点名,10秒后开始监听，但刷卡由上面代码14秒后执行，再调整

//                            waiting = false
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
        heros.add(sishen)
        heros.add(muqiu)
        heros.add(shexian)
        heros.add(huanqiu)
        heros.add(guangqiu)

        App.keyListeners.add(this)
    }

    var longyunStart = 0L
    suspend fun doOnKeyDown(code: Int): Boolean {

        if (code == VK_NUMPAD1 || code == VK_1) {//按1刷木
            guanka = Guan.g139
            waiting = false
        }


//        if (code == VK_1 || code == VK_NUMPAD1) {
//            App.save()
//            carDoing.downHero(shahuang)
//            longyunStart = System.currentTimeMillis()
//        } else if (code == VK_2) {
//            App.save()
//            carDoing.downHero(xiaoye)
//            carDoing.downHero(saman)
//            carDoing.downHero(jiaonv)
//            longyunStart = System.currentTimeMillis()
//        } else if (code == VK_3) {
//            waiting = false
//            var endTime = System.currentTimeMillis()
//            log("识别到云到云炸，大约：${(endTime - longyunStart) / 1000}")
//        }

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

            if (guanka == Guan.g108) {//乱补星时不检测光球，必中，小翼减一个，只要用光，要么是自己没降星，要么就满上了
                delay(500)
            } else {
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

    /**
     * 110检查一遍星级，以补满卡
     */
    var recheckStarFor110 = false

    override suspend fun dealHero(heros: List<HeroBean?>): Int {

        while (waiting) {
            delay(100)
        }

        if (guanka == Guan.g1) {//第一阶段
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

                var fullList = arrayListOf(jiaonv, saman, sishen)
                var index = defaultDealHero(
                    heros,
                    fullList
                )
                if (index > -1) return index

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
        } else if (guanka == Guan.g26) {
            var fullList = arrayListOf(nvwang, jiaonv, saman, sishen, zhanjiang, shexian)//防止没满
            var index = defaultDealHero(
                heros,
                fullList
            )
            if (index > -1) {
                return index
            }
            index = heros.indexOf(huanqiu)
            if (index > -1 && !Zhuangbei.isLongxin() && Zhuangbei.hasZhuangbei()) {//小翼 烟斗
                return index
            }
            index = heros.indexOf(guangqiu)
            if (index > -1) {
                return index
            }

        } else if (guanka == Guan.g41) {
            var fullList = arrayListOf(shahuang, nvwang, jiaonv, saman, sishen, zhanjiang, shexian)//防止没满
            var index = defaultDealHero(
                heros,
                fullList
            )
            if (index > -1) {
                return index
            }
            index = heros.indexOf(huanqiu)
            if (index > -1 && !Zhuangbei.isLongxin() && Zhuangbei.hasZhuangbei()) {//小翼 烟斗
                return index
            }
            index = heros.indexOf(guangqiu)
            if (index > -1) {
                return index
            }
        } else if (guanka == Guan.g101) {//101 yandou
            var fullList = arrayListOf(nvwang, shahuang, saman, zhanjiang, sishen, jiaonv, shexian)
            var index = defaultDealHero(
                heros,
                fullList
            )
            if (index > -1) {
                return index
            }
            //在之前基础上 先刷龙心
            index = heros.indexOf(huanqiu)
            if (index > -1 && !Zhuangbei.isYandou() && Zhuangbei.hasZhuangbei()) {//小翼 烟斗
                return index
            }
            index = heros.indexOf(guangqiu)
            if (index > -1) {
                return index
            }

        } else if (guanka == Guan.g108) {//乱补
            var fullList = arrayListOf(nvwang, shahuang, saman, zhanjiang, sishen, jiaonv, muqiu, guangqiu, shexian)
            var index = defaultDealHero(
                heros,
                fullList
            )
            if (index > -1) return index


            index = heros.indexOf(huanqiu)
            if (index > -1 && !Zhuangbei.isYandou() && Zhuangbei.hasZhuangbei()) {//小翼 烟斗
                return index
            }

        } else if (guanka == Guan.g110) {//111
            if (nvwang.isFull()) {
                carDoing.downHero(nvwang)
            }
            if (saman.isFull()) {
                carDoing.downHero(saman)//主卡萨满16 ，低星有加成
            }
            if (!recheckStarFor110) {
                carDoing.reCheckStars()
                recheckStarFor110 = true
            }

            var fullList = arrayListOf(zhanjiang, shahuang, jiaonv, sishen, shexian)
            var index = defaultDealHero(
                heros,
                fullList
            )
            if (index > -1) return index
            index = heros.indexOf(nvwang)
            if (index > -1 && !nvwang.isInCar()) {//换一星女王，副卡满（副卡18）
                return index
            }
            index = heros.indexOf(saman)
            if (index > -1 && !saman.isInCar()) {//换一星女王，副卡满（副卡18）
                return index
            }


            index = heros.indexOf(huanqiu)
            if (index > -1 && !Zhuangbei.isQiangxi() && Zhuangbei.hasZhuangbei()) {//小翼 烟斗
                return index
            }


        } else if (guanka == Guan.g131) {

            //防止船长点完卡没补满
            var fullList = arrayListOf(zhanjiang, shahuang, jiaonv, sishen, shexian)
            var index = defaultDealHero(
                heros,
                fullList
            )
            if (index > -1) return index
            index = heros.indexOf(nvwang)
            if (index > -1 && !nvwang.isInCar()) {//换一星女王，副卡满（副卡18）
                return index
            }
            index = heros.indexOf(saman)
            if (index > -1 && !saman.isInCar()) {//换一星女王，副卡满（副卡18）
                return index
            }


            index = heros.indexOf(huanqiu)
            if (index > -1 && !Zhuangbei.isLongxin() && Zhuangbei.hasZhuangbei()) {//小翼 烟斗
                return index
            }
        } else if (guanka == Guan.g139) {//刷木
//            if (shahuang.isInCar()) {
//                carDoing.downHero(shahuang)
//            }
//            if (nvwang.isInCar()) {
//                carDoing.downHero(nvwang)
//            }
            return heros.indexOf(muqiu)
        } else if (guanka == Guan.g140) {
            var index = heros.indexOf(huanqiu)
            if (index > -1 && !Zhuangbei.isYandou() && Zhuangbei.hasZhuangbei()) {//小翼 烟斗
                return index
            }
        }
        return -1
    }

    var lastXiaoye = 0L

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