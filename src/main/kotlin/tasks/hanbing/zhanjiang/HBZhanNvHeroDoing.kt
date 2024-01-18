package tasks.hanbing.zhanjiang

import data.Config
import data.HeroBean
import data.MPoint
import data.MRect
import getImage
import kotlinx.coroutines.*
import log
import logOnly
import model.CarDoing
import tasks.HeroDoing
import tasks.Zhuangbei
import tasks.hanbing.BaseHBHeroDoing
import utils.ImgUtil.forEach4Result
import java.awt.event.KeyEvent.*

class HBZhanNvHeroDoing : BaseHBHeroDoing() {//默认赋值0，左边，借用左边第一个position得点击，去识别车位置后再更改
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
         * 71 强袭
         */
        g71,

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
         * 下女王 沙皇。 一直刷木
         */
        g139,

        /**
         * 5秒后上女王沙皇 一直木
         */
        g139_2,

        /**
         * 上1星小野（无敌后，快捷键下，再上，刷小野，因为目前攻速不够)
         */
        g139_3,

        /**
         * emmm,正常讲 不需要刷卡了，停木就完了，g140直接返回over 为true，现在139都打不到，先简单点
         * 这时 副卡遇见红球满女王，遇见蓝球下女王并升到3星就可以了
         */
        g140,

        /**
         * 跳小野，等快捷键,但减伤如果够就不用跳了。。其他人这里都是牛换海妖的，这个boss没增伤
         */
        g149,
    }

    var guanka = Guan.g1

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

    override fun doOnGuanChanged(guan: Int) {
        if (guan == 140) {
            guanka = Guan.g140
            waiting = false
            return
        }

        if (guan == 139 && guanka != Guan.g139) {
            guanka = Guan.g139
            waiting = false
            GlobalScope.launch {
                delay(5500)
                guanka = Guan.g139_2
            }

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

        if (guan > 99 && guanka == Guan.g71) {
            longwangObserver = false
            guanka = Guan.g101
            waiting = false
            return
        }
        if (guan in 98..99) {
            startLongWangOberserver()
            return
        }

        if (guan > 70 && guanka == Guan.g41) {
            guanka = Guan.g71
            waiting = false
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

    override suspend fun onLongwangPoint(point: MPoint, downed: (Boolean) -> Unit) {
        when(point){
            Config.hbFSCloud ->{
                carDoing.downHero(shahuang)
                downed.invoke(true)
            }
            Config.hbMSCloud ->{
                carDoing.downHero(xiaoye)
                carDoing.downHero(saman)
                carDoing.downHero(jiaonv)
                downed.invoke(true)
            }
            else ->{
                downed.invoke(false)
            }
        }
    }

    fun isGkOver(g: Guan): Boolean {

        if (g == Guan.g139_3) return false
        if (g == Guan.g140) return xiaoye.isInCar() && Zhuangbei.isYandou()

        var heroOk = zhanjiang.isFull() && jiaonv.isFull() && xiaoye.isFull()
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

            Guan.g71 -> {
                Zhuangbei.isQiangxi() && nvwang.isFull() && shahuang.isFull() && saman.isFull()
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

    override fun initHeroes() {
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

    suspend fun doOnKeyDown(code: Int): Boolean {

//        if (code == VK_NUMPAD1 || code == VK_1) {//按1下小野，并刷一个小野上车放技能
//            carDoing.downHero(xiaoye)
//            guanka = Guan.g139_3
//            waiting = false
//        }
//        if (code == VK_NUMPAD2 || code == VK_2) {//按1下小野，并刷一个小野上车放技能
//            carDoing.downHero(xiaoye)
//            guanka = Guan.g149
//            waiting = false
//        }


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

    override suspend fun onGuangqiuPost() {
        if (guanka == Guan.g108) {//乱补星时不检测光球，必中，小翼减一个，只要用光，要么是自己没降星，要么就满上了
            delay(500)
        } else
            super.onGuangqiuPost()
    }

    override suspend fun doAfterHeroBeforeWaiting(heroBean: HeroBean) {
        if (!waiting && isGkOver(guanka)) {
            waiting = true
        }
    }

    /**
     * 110检查一遍星级，以补满卡
     */
    var recheckStarFor110 = false

    override suspend fun dealHero(heros: List<HeroBean?>): Int {

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

                var fullList = arrayListOf(jiaonv, saman, xiaoye)
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
            var fullList = arrayListOf(nvwang, jiaonv, saman, xiaoye, zhanjiang)//防止没满
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
            var fullList = arrayListOf(shahuang, nvwang, jiaonv, saman, xiaoye, zhanjiang)//防止没满
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
        } else if (guanka == Guan.g71) {

            var fullList = arrayListOf(shahuang, nvwang, jiaonv, saman, xiaoye, zhanjiang)//防止没满
            var index = defaultDealHero(
                heros,
                fullList
            )
            if (index > -1) {
                return index
            }
            index = heros.indexOf(huanqiu)
            if (index > -1 && !Zhuangbei.isQiangxi() && Zhuangbei.hasZhuangbei()) {//小翼 烟斗
                return index
            }
            index = heros.indexOf(guangqiu)
            if (index > -1) {
                return index
            }

        } else if (guanka == Guan.g101) {//101 yandou
            var fullList = arrayListOf(nvwang, shahuang, saman, zhanjiang, xiaoye, jiaonv)
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
            var fullList = arrayListOf(nvwang, shahuang, saman, zhanjiang, xiaoye, jiaonv, muqiu, guangqiu)
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

            var fullList = arrayListOf(zhanjiang, shahuang, jiaonv, xiaoye)
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
            var fullList = arrayListOf(zhanjiang, shahuang, jiaonv, xiaoye)
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
        } else if (guanka == Guan.g139_2) {
//            var index = heros.indexOf(shahuang)
//            if (index > -1) {
//                return index
//            }
//            index = heros.indexOf(nvwang)
//            if (index > -1 && !nvwang.isInCar()) {//换一星女王，副卡满（副卡18）
//                return index
//            }
            return heros.indexOf(muqiu)
        } else if (guanka == Guan.g139_3 || guanka == Guan.g149) {
            //跳小野 刷木（下小野操作，由快捷键触发）//距离上次上小野超过了1秒，就继续上小野，否则刷木,但现在主卡女王萨满不满，可能跳不好。。。。
            var index = heros.indexOf(xiaoye)
            var now = System.currentTimeMillis()
            if(index>-1){
                if(now-lastXiaoye>1000){
                    lastXiaoye = now
                    return index
                }else{
                    delay(1000-(now-lastXiaoye))
                    lastXiaoye = now
                    return index
                }
            }
//            if (now - lastXiaoye > 1000 && index > -1) {
//                if (xiaoye.isInCar()) {
//                    carDoing.downHero(xiaoye)
//                }
//                lastXiaoye = now
//                return index
//            }
            return heros.indexOf(muqiu)
        } else if (guanka == Guan.g140) {
            var index = heros.indexOf(xiaoye)
            if (index > -1 && !xiaoye.isInCar()) {
                return index
            }
            index = heros.indexOf(huanqiu)
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
        if(super.onKeyDown(code)){
            return true
        }
        return doOnKeyDown(code)
    }
}