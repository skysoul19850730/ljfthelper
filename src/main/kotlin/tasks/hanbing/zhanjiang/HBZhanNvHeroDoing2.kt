package tasks.hanbing.zhanjiang

import data.*
import kotlinx.coroutines.*
import log
import tasks.XueLiang
import tasks.Zhuangbei
import tasks.hanbing.BaseHBHeroDoing
import java.awt.event.KeyEvent.*

class HBZhanNvHeroDoing2 : BaseHBHeroDoing() {//默认赋值0，左边，借用左边第一个position得点击，去识别车位置后再更改
//es8  es4 ye5   6e0 y80  y50 y56 e04

    val zhanjiang = HeroBean("zhanjiang", 100)
    val nvwang = HeroBean("nvwang", 90)
    val saman = HeroBean("saman2", 80)
    val jiaonv = HeroBean("jiaonv", 70)
    val shahuang = HeroBean("shahuang", 60, compareRate = 0.9)
    val sishen = HeroBean("sishen", 50)
    val muqiu = HeroBean("muqiu", 40, needCar = false, compareRate = 0.95)
    val baoku = HeroBean("bawang", 30, needCar = true, isGongCheng = true, compareRate = 0.9)
    val huanqiu = HeroBean("huanqiu", 20, needCar = false, compareRate = 0.95)
    val guangqiu = HeroBean("guangqiu", 0, needCar = false)


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
         * 龙心 船长有上下卡，等131再和副卡切女王萨满
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

        /**
         * 熊猫关，改装备
         */
        g159,

    }

    var guanka = Guan.g1


    var needZhuangbei = Zhuangbei.YANDOU//目前熊猫用，打完雷神时是烟斗，默认值用烟斗

    override fun onLeiShenSixBallOver() {
        super.onLeiShenSixBallOver()
        GlobalScope.launch {
            //因为识别到第6个球就回调了。所以这里再延迟5秒再刷木
            delay(5000)
            guanka = Guan.g139
            waiting = false
        }
    }

    override fun onXiongMaoQiuGot(qiu: String) {
        super.onXiongMaoQiuGot(qiu)
        if (qiu == "fs") {
            needZhuangbei = Zhuangbei.YANDOU
        } else if (qiu == "gj") {
            needZhuangbei = Zhuangbei.QIANGXI
        } else if (qiu == "zs" || qiu == "ss") {//展示术士都用龙心
            needZhuangbei = Zhuangbei.LONGXIN
        }
        waiting = false
    }

    override fun doOnGuanChanged(guan: Int) {

        if (guan == 159) {
            guanka = Guan.g159
            startXiongMaoOberser()
            App.startAutoSave()
            waiting = false
            return
        }

        if (guan == 151) {
//            App.stopAutoSave()
            leishenOberser = false
            guanka = Guan.g159
            needZhuangbei = Zhuangbei.LONGXIN
            waiting = true
            return
        }

        if (guan == 149) {
//            App.startAutoSave()
            startLeishenOberserver()
            GlobalScope.launch {
                delay(60000)
                guanka = Guan.g139
                waiting = false
            }
            return
        }

        if (guan == 141) {
            guanka = Guan.g140
            waiting = false
            return
        }

        if (guan == 139) {
//            App.stopAutoSave()
            guanka = Guan.g139
            waiting = false
            return
        }
//        if (guan == 139) {
//            App.startAutoSave()
//        }

        if (guan == 131 || guan == 130) {
            stopChuanZhangOberserver()
            beimu = false
            guanka = Guan.g131
            waiting = false
            return
        }

        if (guan == 128) {
            startChuanZhangOberserver()
            return
        }
        if (guan == 111) {
            guanka = Guan.g110
            waiting = false
            return
        }

        if (guan == 109) {
            guanka = Guan.g108
            waiting = false
            return
        }

        if (guan == 101) {
            longwangObserver = false
            guanka = Guan.g101
            waiting = false
            return
        }
        if (guan == 98) {
            startLongWangOberserver()
            return
        }

        if (guan == 38) {
            guanka = Guan.g41
            waiting = false
            return
        }

        if (guan == 21) {
            guanka = Guan.g26
            waiting = false
            return
        }
    }

    override fun onHeroPointByChuanzhang(hero: HeroBean): Boolean {
        //船长不会点战将，这里除非是识别错了，如果识别错了，肯定也不下战将（目前只有另一个车中了，主车会偶尔识别到战将，这种就不下战将，其他都先不处理）
        return hero != zhanjiang
    }

    override suspend fun onLongwangPoint(point: MPoint, downed: (Boolean) -> Unit) {
        when (point) {
            Config.hbFSCloud -> {
                carDoing.downHero(shahuang)
                downed.invoke(true)
            }
//            Config.hbMSCloud ->{
//                carDoing.downHero(xiaoye)
//                carDoing.downHero(saman)
//                carDoing.downHero(jiaonv)
//                downed.invoke(true)
//            }
            else -> {
                downed.invoke(false)
            }
        }
    }

    private fun isBaoku(): Boolean {
        return baoku.heroName == "baoku" || baoku.heroName == "shexian"
    }

    fun isGkOver(g: Guan): Boolean {
        if (g == Guan.g159) return Zhuangbei.getZhuangBei() == needZhuangbei
        if (g == Guan.g139) return false
        if (g == Guan.g140) return Zhuangbei.isYandou() && saman.isInCar()
        if (g == Guan.g1) return zhanjiang.isFull() && jiaonv.isFull() && sishen.isFull() && saman.isFull() && Zhuangbei.isLongxin()

        var heroOk = zhanjiang.isFull() && jiaonv.isFull() && sishen.isFull()
        if (isBaoku()) {
            heroOk = heroOk && baoku.isFull()
        }
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
                Zhuangbei.isQiangxi() && nvwang.isFull() && shahuang.isFull() && saman.isFull()
            }

            Guan.g131 -> {
                Zhuangbei.isLongxin() && nvwang.isInCar() && shahuang.isFull() && saman.isFull()
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
        heros.add(sishen)
        heros.add(muqiu)
        heros.add(baoku)
        heros.add(huanqiu)
        heros.add(guangqiu)
    }

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


    override suspend fun doAfterHeroBeforeWaiting(heroBean: HeroBean) {
        if (heroBean.heroName == "muqiu") {
            return
        }
        if (!waiting && isGkOver(guanka)) {
            if (guanka == Guan.g110 && beimu) {//如果beimu时，不waiting，去dealhero里去卡住
                waiting = false
            } else {
                waiting = true
            }
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
//        if(!waiting&&isGkOver(guanka)){
//            waiting = true
//        }


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
            var fullList = arrayListOf(nvwang, jiaonv, saman, sishen, zhanjiang)//防止没满
            if (isBaoku()) {
                fullList.add(baoku)
            }
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
            var fullList = arrayListOf(shahuang, nvwang, jiaonv, saman, sishen, zhanjiang)//防止没满
            if (isBaoku()) {
                fullList.add(baoku)
            }
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
            var fullList = arrayListOf(nvwang, shahuang, saman, zhanjiang, sishen, jiaonv)
            if (isBaoku()) {
                fullList.add(baoku)
            }
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
            var fullList = arrayListOf(nvwang, shahuang, saman, zhanjiang, sishen, jiaonv, muqiu, guangqiu)
            if (isBaoku()) {
                fullList.add(baoku)
            }
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

            if (!recheckStarFor110) {
                carDoing.reCheckStars()
                recheckStarFor110 = true
            }

            var fullList = arrayListOf(zhanjiang, shahuang, jiaonv, sishen, nvwang, saman)
            if (isBaoku()) {
                fullList.add(baoku)
            }
            var index = defaultDealHero(
                heros,
                fullList
            )
            if (index > -1) return index

            if (isGkOver(Guan.g110)) {//船长补满卡后，背木
                if (beimu) {
                    index = heros.indexOf(muqiu)
                    if (index > -1) {
                        log("备好了木，等待使用")
                        while (!XueLiang.isMLess(0.4f) && beimu) {//这里如果不加&& beimu，如果副卡打了木，血量一直不低于40，那么这里就会一直卡着，船长监听那里，也会在适当时机把beimu改成false，这样这里就会扔出去了
                            delay(100)
                        }
                        log("血量低于40%，使用木")
                        beimu = false //变成false，用木后，判断needwaiting就是true了，就不会刷完木又来刷卡
                        return index
                    }
                }
            } else {
                index = heros.indexOf(guangqiu)
                if (index > -1 && carDoing.hasNotFull()) {
                    return index
                }
            }


            index = heros.indexOf(huanqiu)
            if (index > -1 && !Zhuangbei.isQiangxi() && Zhuangbei.hasZhuangbei()) {//小翼 烟斗
                if (guankaTask?.currentGuanIndex != 129) {//129 有次识别错了，然后幻了装备,所以129就不用幻
                    return index
                }
            }


        } else if (guanka == Guan.g131) {
            if (nvwang.isFull()) {
                carDoing.downHero(nvwang)
            }


            //防止船长点完卡没补满
            var fullList = arrayListOf(zhanjiang, shahuang, jiaonv, sishen, saman)
            if (isBaoku()) {
                fullList.add(baoku)
            }
            var index = defaultDealHero(
                heros,
                fullList
            )
            if (index > -1) return index
            index = heros.indexOf(nvwang)
            if (index > -1 && !nvwang.isInCar()) {//换一星女王，副卡满（副卡18）
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

            if (saman.isFull()) {
                carDoing.downHero(saman)//主卡萨满16 ，低星有加成
            }
            var index = heros.indexOf(saman)
            if (index > -1 && !saman.isInCar()) {//换一星女王，副卡满（副卡18）
                return index
            }

            index = heros.indexOf(huanqiu)
            if (index > -1 && !Zhuangbei.isYandou() && Zhuangbei.hasZhuangbei()) {//小翼 烟斗
                return index
            }
        } else if (guanka == Guan.g159) {
            var index = heros.indexOf(huanqiu)
            if (index > -1 && Zhuangbei.getZhuangBei() != needZhuangbei) {//小翼 烟斗
                return index
            }
        }
        return -1
    }


    override fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean? {
        return null
    }

    override suspend fun onKeyDown(code: Int): Boolean {
        if (super.onKeyDown(code)) {
            return true
        }
        return doOnKeyDown(code)
    }
}