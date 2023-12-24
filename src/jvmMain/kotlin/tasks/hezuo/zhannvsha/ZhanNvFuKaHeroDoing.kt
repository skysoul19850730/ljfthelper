package tasks.hezuo.zhannvsha

import App
import data.Config
import data.Config.delayLong
import data.Config.delayNor
import data.HeroBean
import data.Recognize
import kotlinx.coroutines.*
import log
import logOnly
import tasks.Boss
import tasks.CarDoing
import tasks.HeroDoing
import tasks.Zhuangbei
import utils.MRobot

class ZhanNvFuKaHeroDoing : HeroDoing(0) {//默认赋值0，左边，借用左边第一个position得点击，去识别车位置后再更改

    var guanka = 0

    val zhanjiang = HeroBean("zhanjiang", 100)
    val nvwang = HeroBean("nvwang", 90)
    val saman = HeroBean("saman", 80)
    val jiaonv = HeroBean("jiaonv", 70)
    val shahuang = HeroBean("shahuang", 60)
    val houzi = HeroBean("houzi", 50)
    val maomi = HeroBean("maomi", 40)
    val kuangjiang = HeroBean("kuangjiang", 30)
    val huanqiu = HeroBean("huanqiu", 20, needCar = false)
    val guangqiu = HeroBean("guangqiu", 0, needCar = false)


    override fun initHeroes() {
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


    private fun isGuanKa0Over(): Boolean {
        return (zhanjiang.isFull()
                && houzi.isInCar()
                && shahuang.isFull()
                && saman.isFull()
                && jiaonv.isFull()
                && nvwang.isInCar())
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
            if (kuojianguo) {//扩建过开启检查，否则车位不准，先不检查,等上英雄时再检查
                delay(1500)
                checkStars()
            } else {
                needCheckStar = true
            }
        } else if (heroBean == huanqiu) {
            //魔球只有310扔，扔完等3秒（等筛子过去)
            delay(1000)
        }

    }

    override suspend fun dealHero(heros: List<HeroBean?>): Int {
        if (guanka == 0) {
            if (isGuanKa0Over()) {
                guanka = 1
            }
        }

        if (guanka == 0) {//第一阶段

            if (zhanjiang.currentLevel != 4) {//战将没满,不会开第四个格子
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
                        if (houzi.isInCar() || jiaonv.isInCar()) {
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
                if (huanqiuP > -1 && Zhuangbei.hasZhuangbei() && !Zhuangbei.isLongxin()) {
                    return huanqiuP//有装备时，不是龙心 用幻
                }

                return -1 //战将没满不会上其他英雄
            } else {
                //战将满了 优先冰女
                if (!houzi.isInCar()) {//没猴子 优先猴子
                    var index = heros.indexOf(houzi)
                    if (index > -1) return index
                }

                var index = heros.indexOf(shahuang)
                if (index > -1) return index
                index = heros.indexOf(saman)
                if (index > -1) return index
                index = heros.indexOf(jiaonv)
                if (index > -1) return index
                index = heros.indexOf(guangqiu)

                if (!shahuang.isFull() || !saman.isFull() || !jiaonv.isFull()) {
                    //如果必要英雄没满,下女王，用光.如果都满了就不用光了
                    if (nvwang.isInCar()) {
                        carDoing.downHero(nvwang)
                    }
                    if(houzi.isInCar() && !houzi.isFull()){
                        carDoing.downHero(houzi)
                    }
                    return index
                }
//                index = heros.indexOf(moqiu)
//                if (index > -1 && useMoqiu()) {
//                    return index
//                }

                if(!nvwang.isInCar()) {
                    index = heros.indexOf(nvwang)
                    if (index > -1) return index
                }

                val huanqiuP = heros.indexOf(huanqiu)
                if (huanqiuP > -1 && Zhuangbei.hasZhuangbei() && !Zhuangbei.isLongxin()) {
                    return huanqiuP//有装备时，不是龙心 用幻
                }

            }
        } else if (guanka == 1) {//279 孤星和289上狂将
            //在之前基础上 先刷龙心
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
                    if (Boss.isGuxing()) {
                        log("识别到孤星")
                        delay(15 * 1000)//等20秒再下冰女
                        carDoing.downHero(houzi)
                    }
                    if (Boss.isWuGui() || Boss.isShenLong()) {
                        log("识别到乌龟和神龙")
                        if (houzi.isInCar()) {
                            delay(15 * 1000)//等20秒再下冰女
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
            var needZhuangbei: String? = null
            while (needZhuangbei == null) {
                if (Boss.isNvwangChe()) {
                    needZhuangbei = "yandou"
                } else if (Boss.isLongwangChe()) {
                    needZhuangbei = "longxin"
                }
                delay(delayLong)
            }

            if (needZhuangbei == "longxin") {
                guanka = 2
                return dealHero(heros)
            } else if (needZhuangbei == "yandou") {
                if (Zhuangbei.isYandou()) {
                    guanka = 2
                    return dealHero(heros)
                } else {
                    return heros.indexOf(huanqiu)
                }
            }


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
            if (maomi.isFull() && nvwang.isInCar()) {
                guanka = 4
                return dealHero(heros)
            }
            var index = heros.indexOf(maomi)
            if (index > -1) {
                return index
            }
            index = heros.indexOf(guangqiu)
            if (index > -1) {
                if (!maomi.isFull()) {//猫咪没满 下女王用光
                    if (nvwang.isInCar()) {
                        carDoing.downHero(nvwang)
                    }
                    return index
                }
            }
            index = heros.indexOf(nvwang)
            if (index > -1) {
                if (!nvwang.isInCar()) {
                    return index
                }
            }
        } else {
            if (!Zhuangbei.isShengjian()) {//310有杂钱就幻圣剑玩吧，无所谓了
                return heros.indexOf(huanqiu)
            }
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