package tasks.hezuo.zhannvsha

import App
import data.Config
import data.Config.delayLong
import data.Config.delayNor
import data.HeroBean
import data.Recognize
import getImage
import kotlinx.coroutines.*
import log
import logOnly
import tasks.*
import tasks.guankatask.GuankaTask

class ZhanNvHeroDoing : HeroDoing(0) {//默认赋值0，左边，借用左边第一个position得点击，去识别车位置后再更改

    var guanka = 0

    val zhanjiang = HeroBean("zhanjiang", 100)
    val nvwang = HeroBean("nvwang", 90)
    val saman = HeroBean("saman2", 80)
    val jiaonv = HeroBean("jiaonv", 70)
    val shahuang = HeroBean("shahuang", 60)
    val houzi = HeroBean("houzi", 50)
    val maomi = HeroBean("maomi", 40)
    val kuangjiang = HeroBean("kuangjiang", 30)
    val huanqiu = HeroBean("huanqiu", 20, needCar = false)
    val guangqiu = HeroBean("guangqiu", 0, needCar = false)

    var guankaTask = GuankaTask().apply {
        changeListener = object : GuankaTask.ChangeListener {
            override fun onGuanChange(guan: Int) {

            }
        }
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
        heros.add(houzi)
        heros.add(maomi)
        heros.add(kuangjiang)
        heros.add(huanqiu)
        heros.add(guangqiu)
    }

    var startTime = 0L
    override fun onStart() {
        super.onStart()
        guankaTask.start()
        startTime = System.currentTimeMillis()
    }

    override fun onStop() {
        super.onStop()
        var gk = guankaTask.currentGuanIndex
        guankaTask.stop()
        val endTime = System.currentTimeMillis()
        val cost = (endTime - startTime)/1000
        var time ="合作完毕：$gk 关，用时： ${cost/60}分${cost%60}秒"
        log(time)
    }

    private fun isGuanKa0Over(): Boolean {
        return (zhanjiang.isFull()
                && houzi.isInCar()
                && shahuang.isFull()
                && saman.isFull()
                && jiaonv.isFull()
                && nvwang.isFull())
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
        } else if (heroBean == huanqiu) {
            //扔幻时 记录当前  发生改变后就可以结束（因为主卡幻一定成功）否则这里逻辑就不可以了
            curZhuangBei = Zhuangbei.getZhuangBei()
            delay(delayNor)
            try {
                withTimeout(1500) {//加个超时保险一些，防止死循环
                    while (Zhuangbei.getZhuangBei() == curZhuangBei && Zhuangbei.getZhuangBei() != 0) {
                        delay(delayNor)
                    }
                }
            } catch (e: Exception) {

            }

            if (guanka == 21 && Zhuangbei.isYandou()) {
                guanka = 2
            }
        }

        if(App.reCheckStar){
            carDoing.reCheckStars()
            App.reCheckStar = false
        }

    }

    var curZhuangBei: Int = 0
    var needZhuangbei300: String? = null
    override suspend fun dealHero(heros: List<HeroBean?>): Int {
        if (guanka == 0) {
            if (isGuanKa0Over()) {
                guanka = 1
            }
        }

        if (guanka == 0) {//第一阶段

            if (zhanjiang.currentLevel != 4) {//战将没满,不会开第四个格子
//                log("战将没满")
                var index = heros.indexOf(zhanjiang)//有战将用战将
                if (index > -1) return index
                index = heros.indexOf(guangqiu)
                if (index > -1) {//有光球
                    if (zhanjiang.isInCar()) {//车上有战将（未满）
                        this.heros.filter { //下掉不满星的，用光球
                            it.isInCar() && it.currentLevel != 4 && it != zhanjiang
                        }.forEach {
                            carDoing.downHero(it)
                        }
                        return index
                    } else {//车上没战将
                        //候选里有冰女用冰女，有娇女用娇女，没有就用光
                        if (houzi.isInCar()) {//有猴子了，优先娇女
                            val jiaonvP = heros.indexOf(jiaonv)
                            if (jiaonvP > -1) return jiaonvP
                        }
                        val bingnvP = heros.indexOf(houzi)
                        if (bingnvP > -1) return bingnvP
                        val jiaonvP = heros.indexOf(jiaonv)
                        if (jiaonvP > -1) return jiaonvP
                        if (houzi.isInCar() || jiaonv.isInCar()) {//车上有人时用光球，否则就没必要用了
                            return index
                        }
                    }
                }
                //战将没满，候选里有冰女用冰女，有娇女用娇女
                if (houzi.isInCar()) {//有猴子了，优先娇女
                    val jiaonvP = heros.indexOf(jiaonv)
                    if (jiaonvP > -1) return jiaonvP
                }
                val bingnvP = heros.indexOf(houzi)
                if (bingnvP > -1) return bingnvP
                val jiaonvP = heros.indexOf(jiaonv)
                if (jiaonvP > -1) return jiaonvP

//                val moqiuP = heros.indexOf(moqiu)
//                if (moqiuP > -1 && useMoqiu()) {
//                    return moqiuP
//                }

                val huanqiuP = heros.indexOf(huanqiu)
                if (huanqiuP > -1 && Zhuangbei.hasZhuangbei() && !Zhuangbei.isLongxin() && !Zhuangbei.isQiangxi()) {
                    return huanqiuP//有装备时，不是龙心或强袭 用幻（前面遇到强袭或龙心都可以）
                }

                return -1 //战将没满不会上其他英雄
            } else {
//                log("战将满了")
                //战将满了 优先冰女
                if (!houzi.isInCar()) {//没猴子 优先猴子
                    var index = heros.indexOf(houzi)
                    if (index > -1) return index
                }

                var index = defaultDealHero(heros, arrayListOf(jiaonv,saman,shahuang,nvwang))

                if (index > -1) return index

                index = heros.indexOf(guangqiu)
                if (index > -1 && (!shahuang.isFull() || !saman.isFull() || !jiaonv.isFull() || !nvwang.isFull())) {
                    return index
                }
//                index = heros.indexOf(moqiu)
//                if (index > -1 && useMoqiu()) {
//                    return index
//                }


                val huanqiuP = heros.indexOf(huanqiu)
                if (huanqiuP > -1 && Zhuangbei.hasZhuangbei() && !Zhuangbei.isLongxin() && !Zhuangbei.isQiangxi()) {
                    return huanqiuP//有装备时，不是龙心或强袭 用幻
                }
                index = heros.indexOf(houzi)
                if (index > -1) return index

            }
        } else if (guanka == 1) {//279 孤星和289上狂将
            //在之前基础上 先刷龙心

            while (guankaTask.currentGuanIndex < 250) {
                delay(delayLong)
            }
            //250后幻龙心
            if (!Zhuangbei.isLongxin()) {
                val huanqiuP = heros.indexOf(huanqiu)
                if (huanqiuP > -1 && Zhuangbei.hasZhuangbei()) {
                    return huanqiuP//有装备时，不是龙心 用幻
                }
                return -1
            }

            var index = heros.indexOf(kuangjiang)
            if (index > -1) {
                //如果有狂将  卡住 等
                while (guanka == 1) {
                    if (guankaTask.currentGuanIndex > 275 && Boss.isGuxing()) {
                        log("孤星下猴子")
                        carDoing.downHero(houzi)
                    }
                    var guanUp = if (houzi.isInCar()) 286 else 281
                    if (guankaTask.currentGuanIndex > guanUp) {
//                        log("识别到乌龟和神龙")
//                        if(!houzi.isInCar()){//如果猴子275已下,这里就直接上了
//                            guanka = 20
//                            return index
//                        }
                        if (houzi.isInCar()) {
                            carDoing.downHero(houzi)
                        }
                        guanka = 20
                        return index
                    }
                    logOnly("未识别到boss")
                    delay(delayLong)
                }
            }
        } else if (guanka == 20) {
            while (guankaTask.currentGuanIndex < 291) {
                delay(delayNor)
            }
            //打印一下当前的boss，检测是不是有伤害数字飘上来的情况
//            log(getImage(Config.zhandou_hezuo_bossRect))

//            delay(3000)
            try {
                withTimeout(10000) {
                    while (needZhuangbei300 == null) {

                        if (Boss.isNvwangChe()) {
                            log("女王车")
                            if(ZhanNvGameLaunch.model == 3){//如果是世界的。女王车还是要幻烟斗
                                needZhuangbei300 = "yandou"
                            }else {//自己人打的阵容，直接龙心就过了，因为不再卡钱了，大家都直接挂机到最后了
                                needZhuangbei300 = "longxin"
                            }
                        } else if (Boss.isLongwangChe()) {
                            log("龙王车")
                            needZhuangbei300 = "longxin"
                        } else {
                            log("没识别到300 boss，请按快捷键 1 女王车 2 龙王车")
                            needZhuangbei300 = che300Name
                        }
                        delay(delayNor)
                    }
                }
                //截止时女王车的字样比较清晰，所以先判断的女王车，如果识别时不是女王车，大概率就是龙王车了。
                //如果在屏幕前就 按键了，如果没在屏幕前，这里不处理就卡住了，即使过了300也不能上猫咪之类的。所以这里使用大概率的龙王车来兼容一下
            } catch (e: Exception) {
                log("未识别到300车，且10秒内没有按键监听，默认按龙王车处理")
                needZhuangbei300 = "longxin"
            }

            if (needZhuangbei300 == "longxin") {
//                if(Zhuangbei.isLongxin()) {//防止到300时依然没刷到龙心//算了，如果没刷到龙心，是不会来到这里的，会在guanka=1的地方卡住
                guanka = 2
                return dealHero(heros)
//                }else{
//                    return heros.indexOf(huanqiu)
//                }
            } else if (needZhuangbei300 == "yandou") {
                if (Zhuangbei.isYandou()) {
                    guanka = 2
                    return dealHero(heros)
                } else {
                    guanka = 21
                    return heros.indexOf(huanqiu)
                }
            }


        } else if (guanka == 21) {
            return heros.indexOf(huanqiu)
        } else if (guanka == 2) {//这里是310了，副卡带幻，所以300不用管
            log("找猫咪")
            var index = heros.indexOf(maomi)
            if (index > -1) {//先刷出猫咪等着
                log("找到猫咪")
                while (!Boss.is310()) {
                    delay(delayNor)
                }
                guanka = 3
                if (kuangjiang.isInCar()) {
                    carDoing.downHero(kuangjiang)
                }
                return index
            }
        } else if (guanka == 3) {
            if (maomi.isFull() && nvwang.isFull()) {
                guanka = 4
                return dealHero(heros)
            }
            var index = heros.indexOf(maomi)
            if (index > -1) {
                return index
            }
            index = heros.indexOf(nvwang)
            if (index > -1) {
                return index
            }
            index = heros.indexOf(guangqiu)
            if (index > -1) {
                if (!maomi.isFull()||!nvwang.isFull()) {//猫咪没满 下女王用光
                    return index
                }
            }
        } else if (guanka == 4) {
            if (!Zhuangbei.isShengjian()) {//310有杂钱就幻圣剑玩吧，无所谓了
                return heros.indexOf(huanqiu)
            } else {
                log("完毕，等结束吧")
                while (running) {
                    delay(delayLong)
                }
            }
        } else {
            log("this is a case not handle ,guanka is $guanka")
        }
//        else {
//            var index = heros.indexOf(moqiu)
//            if (index > -1) {//刷到魔球卡住,等骰子
//                log("开始识别骰子")
//                CarDoing.showTouziRect.value = true
//                CarDoing.touziLeft.value = CarDoing.touziRect.left
//                while (!carDoing.touziCheck() && running) {
//                    delay(5)
//                }
////            return heros.indexOf(guangqiu)
////                withContext(Dispatchers.Main) {
////                GlobalScope.launch {
////                    App.save()//保存下按魔时的骰子位置，放异步存
////                }
////            }
//                CarDoing.showTouziRect.value = false
//                return index
//            }
//        }
        return -1
    }

    private suspend fun useMoqiu(): Boolean {
        return false
    }

    override fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean? {
        if (heroBean == zhanjiang) {
            if (jiaonv.isInCar() && !jiaonv.isFull()) {
                return jiaonv
            }
        }
        return null
    }
}