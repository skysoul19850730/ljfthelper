package tasks.hanbing

import data.Config
import data.MPoint
import data.MRect
import getImage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import log
import logOnly
import model.CarDoing
import saveTo
import tasks.HeroDoing
import tasks.guankatask.GuankaTask
import utils.ImgUtil.forEach4Result
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.io.File

abstract class BaseHBHeroDoing() : HeroDoing(0, FLAG_GUANKA or FLAG_KEYEVENT) {

    var beimu = false

    override fun onGuanChange(guan: Int) {
        doOnGuanChanged(guan)
    }

    abstract fun doOnGuanChanged(guan: Int)

    /**
     * 龙王点名了 与config中的龙王云point对比
     * 看看下什么卡
     * 如果下了卡需要返回true
     */
    abstract suspend fun onLongwangPoint(point: MPoint, downed: (Boolean) -> Unit)


    var longwangObserver = false
    protected fun startLongWangOberserver() {
        if (longwangObserver) return
        longwangObserver = true
        GlobalScope.launch {
            while (longwangObserver) {
                var needDown = false
                if (Config.hbFSCloud.isFit()) {
                    onLongwangPoint(Config.hbFSCloud) {
                        waiting = it
                        needDown = it
                    }
                } else if (Config.hbMSCloud.isFit()) {
                    onLongwangPoint(Config.hbMSCloud) {
                        waiting = it
                        needDown = it
                    }
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

    var chuanzhangDownLoadPositionFromKey = -1

    override suspend fun onKeyDown(code: Int): Boolean {
        if(guankaTask?.currentGuanIndex == 129){//船长
            chuanzhangDownLoadPositionFromKey = -1
            chuanzhangDownLoadPositionFromKey = when(code){
                KeyEvent.VK_NUMPAD2->0
                KeyEvent.VK_NUMPAD1->1
                KeyEvent.VK_NUMPAD5->2
                KeyEvent.VK_NUMPAD4->3
                KeyEvent.VK_NUMPAD8->4
                KeyEvent.VK_NUMPAD7->5
                KeyEvent.VK_NUMPAD0->6
                KeyEvent.VK_NUMPAD3->100
                else -> {
                    return false
                }
            }
            return true
        }
        return false
    }

    var chuanZhangObeserver = false
    var chuanzhangDownCount = 0

    var time1 = 0L
    var time2 = 0L
    var time3 = 0L
    fun startChuanZhangOberserver() {
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


                var img = getImage(App.rectWindow,null)

                if(chuanzhangDownLoadPositionFromKey>-1){
                    log("船长 ：接收到快捷键事件")
                    if(chuanzhangDownLoadPositionFromKey<100){//100代表对面车
                        carDoing.downPosition(chuanzhangDownLoadPositionFromKey)
                        waiting = false
                    }
                    onChuanZhangPoint(img)
                    chuanzhangDownLoadPositionFromKey = -1
                }else {

                    var index = carDoing.getChuanZhangMax(img)
//                    var index2 = otherCarDoing.getChuanZhangMax(img)
                    var index2:Pair<Int, Float>? = null
                    if (index != null || index2 != null) {
                        if (index != null && (index2 == null || index.second > index2.second)) {
                            var hero = carDoing.carps.get(index.first).mHeroBean
                            log("检测到被标记  位置：$index  英雄：${hero?.heroName}")
                            if (hero != null) {
                                carDoing.downHero(hero)
                                waiting = false
                            }
                        }
                        onChuanZhangPoint(img)
                    } else {
                        var fitCount = 0
                        MRect.createWH(4, 100, 300, 370).forEach4Result { x, y ->
                            if (img.getRGB(x, y) == Config.Color_ChuangZhang.rgb) {
                                fitCount++
                            }
                            fitCount > 1000
                        }
                        if (fitCount > 1000) {
                            onChuanZhangPoint(img)
                        }
                    }
                }
            }
        }
    }

    private suspend fun onChuanZhangPoint(img2: BufferedImage? = null) {
        var img = img2 ?: getImage(App.rectWindow)
        logOnly("船长点名啦")
        img.saveTo(File(App.caijiPath, "${System.currentTimeMillis()}.png"))
        chuanzhangDownCount++
        var isSencodDianming = chuanzhangDownCount % 2 == 0
        if (!isSencodDianming) {//第一次点卡后等3秒再开始识别
            log("第一次点名,3秒后再开始监听")
            delay(3000)
        } else {//第二次点卡后 刷6秒补卡然后停止（这个时间慢慢校验)要撞船了
            if (chuanZhangObeserver) {
                time1 = System.currentTimeMillis()
//                            delay(10000)
                log("第二次点名,刷卡7.5秒后，然后暂停刷卡")
                beimu = true
                delay(7500)
                waiting = true
                log("第二次点名,已经刷卡7.5秒，暂停刷卡，10.5秒开启监听")
                GlobalScope.launch {
                    delay(13500)
                    beimu = false //waiting前，把beimu改为false
                    waiting = false
                    log("第二次点名,暂停刷卡13.5秒后恢复刷卡")
                }
                delay(10500)//5秒后 效果消失，继续补卡，并监听点名
                log("第二次点名,延迟10.5秒,恢复监听")
//                waiting = false
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }
    override fun onStop() {
        super.onStop()
        chuanZhangObeserver = false
    }
}