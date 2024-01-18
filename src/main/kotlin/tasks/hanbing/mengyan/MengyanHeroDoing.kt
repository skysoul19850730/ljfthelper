package tasks.hanbing.mengyan

import data.Config.delayNor
import data.HeroBean
import data.Recognize
import getImage
import kotlinx.coroutines.*
import log
import model.CarDoing
import tasks.HeroDoing
import tasks.Zhuangbei
import tasks.guankatask.GuankaTask
import java.awt.event.KeyEvent

class MengyanHeroDoing : HeroDoing(0, FLAG_GUANKA or FLAG_KEYEVENT),
    App.KeyListener {//默认赋值0，左边，借用左边第一个position得点击，去识别车位置后再更改


    /**
     * 1  初始
     * 2  70 强袭
     * 3  90 傀换小野
     * 4  100 烟斗 kuiqian换绿工
     * 5  110 强袭 (海妖圣骑互换）
     *
     * 9 小翼 无限补卡
     */
    var guanka = 1

    val mengyan = HeroBean("mengyan")
    val gugu = HeroBean("gugu")
    val kui = HeroBean("kui")
    val kuiqian = HeroBean("kuiqian")
    val lvgong = HeroBean("lvgong")
    val haiyao = HeroBean("haiyao")
    val xiaoye = HeroBean("xiaoye")
    val sishen = HeroBean("sishen")
    val huanqiu = HeroBean("huanqiu", needCar = false)
    val shengqi = HeroBean("shengqi")

    var hasWuDi = true

    override fun initHeroes() {
        heros = arrayListOf()
        heros.add(mengyan)
        heros.add(gugu)
        heros.add(kui)
        heros.add(kuiqian)
        heros.add(lvgong)
        heros.add(haiyao)
        heros.add(xiaoye)
        heros.add(sishen)
        heros.add(huanqiu)
        heros.add(shengqi)

    }

    override fun onGuanChange(guan: Int) {
        super.onGuanChange(guan)
        if (guan > 129 && guanka != 7) {
            chuanZhangObeserver = false
            guanka = 7
            waiting = false
            return
        }

        if (guan > 126 && guanka < 7) {
            startChuanZhangOberserver()
            return
        }
        if (guan > 110 && (guanka == 4 || guanka == 9)) {
            guanka = 5
            waiting = false
            return
        }

        if (guan in 108..109 && guanka != 9) {
            guanka = 9
            waiting = false
            return
        }

        if (guan > 100 && guanka == 3 && isGk3Over()) {
            guanka = 4
            waiting = false
            return
        }


        if (guan > 90 && guanka == 2 && isGk2Over()) {
            guanka = 3
            waiting = false
            return
        }

        if (guan > 70 && guanka == 1 && isGk1Over()) {
            guanka = 2
            waiting = false
            return
        }
    }

    override suspend fun onKeyDown(code: Int): Boolean {
        return doOnKeyDown(code)
    }

    suspend fun doOnKeyDown(code: Int): Boolean {
        var curGuan = guankaTask?.currentGuanIndex ?: 0
        if (code == KeyEvent.VK_NUMPAD0) {
            hasWuDi = false
        } else if (code == KeyEvent.VK_NUMPAD9) {//小翼出现时按9无限补卡，管卡到110后（小翼死掉）会自动guanka = 5（guanka监听 就处理了，不用再处理)
            //这里试试不用点了，直接等108，109关自动触发有没有问题吧
            guanka = 9
            waiting = false
        } else if (code == KeyEvent.VK_NUMPAD6) {//下海妖上圣骑
            waiting = false
        } else if (code == KeyEvent.VK_NUMPAD2) {
            carDoing.downPosition(0)
            //如果是船长，下卡后，立即触发上卡.(这里打过小翼后，就直接按女王，下女王再上女王 不满，让副卡满女王
            if (curGuan > 120) {
                guanka = 5
                waiting = false
            }
        } else if (code == KeyEvent.VK_NUMPAD1) {
            carDoing.downPosition(1)
            if (curGuan > 120) {
                guanka = 5
                waiting = false
            }
        } else if (code == KeyEvent.VK_NUMPAD5) {
            carDoing.downPosition(2)
            if (curGuan > 120) {
                guanka = 5
                waiting = false
            }
        } else if (code == KeyEvent.VK_NUMPAD4) {
            carDoing.downPosition(3)
            if (curGuan > 120) {
                guanka = 5
                waiting = false
            }
        } else if (code == KeyEvent.VK_NUMPAD8) {
            carDoing.downPosition(4)
            if (curGuan > 120) {
                guanka = 5
                waiting = false
            }
        } else if (code == KeyEvent.VK_NUMPAD7) {
            carDoing.downPosition(5)
            if (curGuan > 120) {
                guanka = 5
                waiting = false
            }
        } else if (code == KeyEvent.VK_NUMPAD3) {
//            if (guankaTask.currentGuanIndex > 136) {
//                guanka = 8//幻龙心
//                waiting = false
//            }
            if (time2 == 0L) {
                time2 = System.currentTimeMillis()
            } else if (time3 == 0L) {
                time3 = System.currentTimeMillis()
            }
            waiting = !waiting
        } else {
            return false
        }

        return true
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
                if (index != null || index2 != null) {
                    //如果本车识别到  并且  另一个车没识别到（以本车为主）或者另一个车识别到了，但结果小于本车，才认为是点的本车
                    if (index != null && (index2 == null || index.second > index2.second)) {
                        var hero = carDoing.carps.get(index.first).mHeroBean
                        log("检测到被标记  位置：$index  英雄：${hero?.heroName}")
//                        if (hasWuDi && hero == mengyan) {//点梦魇，有无敌，不下
//                            hasWuDi = false
//                        } else {
                        if (hero != null) {
                            carDoing.downHero(hero)
                            guanka = 5
                            waiting = false
                        }
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
    }

    override fun onStop() {
        super.onStop()
        chuanZhangObeserver = false
        log("time1 :$time1  time2:$time2  time3:$time3  下卡到停止刷新:${(time2 - time1) / 1000f},停止刷新到 开启刷新：${(time3 - time2) / 1000f}")
    }

    var shengqiUped = false
    private suspend fun checkStars() {
        carDoing.checkStars()
        needCheckStar = false
    }

    override suspend fun doAfterHeroBeforeWaiting(heroBean: HeroBean) {
        if (heroBean == shengqi) {
            delay(300)
            carDoing.downHero(shengqi)
            shengqiUped = true
        }


        if (!waiting && isGuanKaOver() && (guanka != 5 || shengqiUped)) {//等5时上过圣骑了,就要验证是否满了
            //这里是为了：guanka1-4点完英雄判断是否guanka结束，结束了就卡住，不刷新卡
            //5的时候。第一阶段这里不会卡，因为guanka=5 并且shengqi没有上过，整体是false不进这里，waiting是false，这里不会卡
            //刷卡上卡的流程，会判断guanka5over了，会在刷卡那里卡住（为了卡一张圣骑卡牌，解爱神诅咒用）。等快捷建6按下，会下海妖上圣骑，此时shengqiUped为true
            //但此时因为下了海妖guanka5over为false，所以会继续上管卡5的卡牌直到海妖满，会再次在这里卡住。
            waiting = true
        }
    }

    override suspend fun afterHeroClick(heroBean: HeroBean) {

        super.afterHeroClick(heroBean)
    }


    fun isGuanKaOver(): Boolean {
        return when (guanka) {
            1 -> isGk1Over()
            2 -> isGk2Over()
            3 -> isGk3Over()
            4 -> isGk4Over()
            5 -> isGk5Over()
            7 -> isGk7Over()
            10 -> isGk8Over()
            else -> false
        }
    }

    fun isGk1Over(): Boolean {
        return mengyan.isFull() && gugu.isFull() && kui.isFull() && kuiqian.isFull() && haiyao.isFull() && sishen.isFull() && Zhuangbei.isLongxin()
    }

    fun isGk2Over(): Boolean {
        return Zhuangbei.isQiangxi()
    }

    fun isGk3Over(): Boolean {
        return xiaoye.isFull()
    }

    fun isGk4Over(): Boolean {
        return Zhuangbei.isYandou() && lvgong.isFull()
    }

    fun isGk5Over(): Boolean {
        return mengyan.isFull() && gugu.isFull() && lvgong.isFull() && sishen.isFull() && haiyao.isInCar() && xiaoye.isFull() && Zhuangbei.isQiangxi()
    }

    fun isGk7Over(): Boolean {//7比5 就差海妖满 装备烟斗
        return mengyan.isFull() && gugu.isFull() && lvgong.isFull() && sishen.isFull() && haiyao.isFull() && xiaoye.isFull() && Zhuangbei.isYandou()
    }

    fun isGk8Over(): Boolean {//沙皇变大时幻龙心（但基本还是打不过应该，先写上吧）
        return mengyan.isFull() && gugu.isFull() && lvgong.isFull() && sishen.isFull() && haiyao.isFull() && xiaoye.isFull() && Zhuangbei.isLongxin()
    }

    override suspend fun dealHero(heros: List<HeroBean?>): Int {
        if (guanka == 1) {//第一阶段

            var fullList = arrayListOf(mengyan, kuiqian, gugu, sishen, kui, haiyao)
            var index = defaultDealHero(
                heros,
                fullList
            )
            if (index > -1) return index
            index = heros.indexOf(huanqiu)
            if (index > -1 && !Zhuangbei.isLongxin() && Zhuangbei.hasZhuangbei()) {//第一阶段用 龙心
                return index
            }

            return -1

        } else if (guanka == 2) {//279 孤星和289上狂将
            var index = heros.indexOf(huanqiu)
            if (index > -1 && !Zhuangbei.isQiangxi() && Zhuangbei.hasZhuangbei()) {//第一阶段用 龙心
                return index
            }
        } else if (guanka == 3) {
            if (kui.isInCar()) {
                carDoing.downHero(kui)
            }
            var index = heros.indexOf(xiaoye)
            return index

        } else if (guanka == 4) {
            if (kuiqian.isInCar()) {
                carDoing.downHero(kuiqian)
            }
            var index = heros.indexOf(lvgong)
            if (index > -1) {
                return index
            }
            index = heros.indexOf(huanqiu)
            if (index > -1 && !Zhuangbei.isYandou() && Zhuangbei.hasZhuangbei()) {//第一阶段用 龙心
                return index
            }
        } else if (guanka == 5) {//下2傀，上绿工 爱神 用强袭
            if (!shengqiUped && isGuanKaOver()) {

                var shenqi = heros.indexOf(shengqi)
                if (shenqi > -1) {
                    waiting = true
                    while (waiting) {//这里是等点快捷键上圣骑
                        delay(10)
                    }
                    carDoing.downHero(haiyao)
                    return shenqi
                }
                return -1
            }

            //如下，先上个海妖 凑5弓，然后优先幻球幻到强袭再继续补海妖（本身补不补都行）
            var index = heros.indexOf(haiyao)
            if (index > -1 && !haiyao.isInCar()) return index

            index = heros.indexOf(huanqiu)
            if (index > -1 && !Zhuangbei.isQiangxi()) {//强袭
                return index
            }

            var fullList = arrayListOf(mengyan, gugu, xiaoye, lvgong, sishen)
            index = defaultDealHero(
                heros,
                fullList
            )
            if (index > -1) return index
        } else if (guanka == 9) {
            var fullList = arrayListOf(mengyan, haiyao, gugu, xiaoye, lvgong, sishen)
            var index = defaultDealHero(
                heros,
                fullList
            )
            if (index > -1) return index
        } else if (guanka == 7) {
            var fullList = arrayListOf(mengyan, haiyao, gugu, xiaoye, lvgong, sishen)
            var index = defaultDealHero(
                heros,
                fullList
            )
            if (index > -1) return index


            index = heros.indexOf(huanqiu)
            if (index > -1 && !Zhuangbei.isYandou()) {//强袭
                return index
            }
        } else if (guanka == 8) {
            var index = heros.indexOf(huanqiu)
            if (index > -1 && !Zhuangbei.isLongxin()) {//强袭
                return index
            }
        }
        return -1
    }

    override fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean? {
        return null
    }


}