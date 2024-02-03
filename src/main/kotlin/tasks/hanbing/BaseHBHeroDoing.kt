package tasks.hanbing

import data.Config
import data.HeroBean
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

                if (longWangDownLoadPositionFromKey > -1 && chuanzhangDownLoadPositionFromKey < 100) {
                    //如果龙王识别出错可以按快捷下对应卡牌，但不知道快捷键按下得时间，所以不能延时进行上卡，只能快捷键9来恢复上卡
                    var hero = carDoing.carps[chuanzhangDownLoadPositionFromKey].mHeroBean
                    if (hero != null && onHeroPointByChuanzhang(hero)) {
                        carDoing.downHero(hero)
                        waiting = true
                    }
                    longWangDownLoadPositionFromKey = -1
                    while (waiting){
                        delay(100)
                    }
//                    waiting = false

                } else {
                    var needDown = false

                    if (Config.hbFSCloud.isFit()) {
                        onLongwangPoint(Config.hbFSCloud) {
                            if(it) {//只需要处理true。默认Needdown是false，waiting默认打龙王也是true得（如果之前是false，比如点了次名没上满，又点，那赋值还是false，所以只处理true）。如果返回false，waiting变false没有意义
                                waiting = it
                                needDown = it
                            }
                        }
                    } else if (Config.hbMSCloud.isFit()) {
                        onLongwangPoint(Config.hbMSCloud) {
                            if(it) {
                                waiting = it
                                needDown = it
                            }
                        }
                    } else {
                        needDown = false
                    }
                    if (needDown) {
                        delay(10000)
                        waiting = false
                    }
                }

                delay(100)
            }
        }
    }

    var chuanzhangDownLoadPositionFromKey = -1
    var longWangDownLoadPositionFromKey = -1


    override suspend fun onKeyDown(code: Int): Boolean {
        //如果龙王识别出错可以按快捷下对应卡牌，但不知道快捷键按下得时间，所以不能延时进行上卡，只能快捷键9来恢复上卡

        if (code == KeyEvent.VK_NUMPAD9) {//9强制改变waitting，防止waiting有逻辑错误不上卡
            waiting = !waiting
            return true
        }

        if (guankaTask?.currentGuanIndex == 129 || guankaTask?.currentGuanIndex == 99) {//船长
            chuanzhangDownLoadPositionFromKey = -1
            chuanzhangDownLoadPositionFromKey = when (code) {
                KeyEvent.VK_NUMPAD2 -> 0
                KeyEvent.VK_NUMPAD1 -> 1
                KeyEvent.VK_NUMPAD5 -> 2
                KeyEvent.VK_NUMPAD4 -> 3
                KeyEvent.VK_NUMPAD8 -> 4
                KeyEvent.VK_NUMPAD7 -> 5
                KeyEvent.VK_NUMPAD0 -> 6
                KeyEvent.VK_NUMPAD3 -> 100
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


    open fun onHeroPointByChuanzhang(hero: HeroBean): Boolean {
        return true
    }

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


                var img = getImage(App.rectWindow, null)

                if (chuanzhangDownLoadPositionFromKey > -1) {
                    log("船长 ：接收到快捷键事件")
                    if (chuanzhangDownLoadPositionFromKey < 100) {//100代表对面车
                        var hero = carDoing.carps[chuanzhangDownLoadPositionFromKey].mHeroBean
                        if (hero != null && onHeroPointByChuanzhang(hero)) {
                            carDoing.downHero(hero)
                            waiting = false
                        }
                    }
                    onChuanZhangPoint(img)
                    chuanzhangDownLoadPositionFromKey = -1
                } else {

                    var index = carDoing.getChuanZhangMax(img)
                    var index2 = otherCarDoing.getChuanZhangMax(img)
//                    var index2:Pair<Int, Float>? = null
                    if (index != null || index2 != null) {
                        if (index != null && (index2 == null || index.second > index2.second)) {
                            var hero = carDoing.carps.get(index.first).mHeroBean
                            log("检测到被标记  位置：$index  英雄：${hero?.heroName}")
                            if (hero != null && onHeroPointByChuanzhang(hero)) {
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
                waiting = false
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
    open fun onLeiShenSixBallOver(){

    }
    open fun onLeiShenRedBallShow(){

    }
    open fun onLeiShenBlueBallShow(){

    }
    var leishenOberser = false
    fun startLeishenOberserver() {
        if (leishenOberser) return
        leishenOberser = true
        var leishenStart = System.currentTimeMillis()
        var checkCount = 0
        GlobalScope.launch {
            delay(5000)//5秒左右才出球
            //按截图算，大约10秒一个球（9.5）
            while (leishenOberser) {

                var img = getImage(App.rectWindow)

                if (Config.leishenqiuXueTiaoRect.hasColorCount(
                        Config.leishenqiuXueTiao, testImg = img
                    ) > 50 || Config.leishenqiuXueTiaoRect.hasColorCount(Config.leishenqiuXueTiao2, testImg = img) > 50
                ) {

                    var count = Config.rectCheckOfLeishen.hasColorCount(Config.colorLeishenHongqiu, testImg = img)
                    if (count > 300) {
                        onLeiShenRedBallShow()
                        log("检测到红球")
                        log(img)
                        checkCount++
                        if (checkCount == 6) {
                            onLeiShenSixBallOver()
                            leishenOberser = false
                            var time = System.currentTimeMillis()
                            log("识别完6个球，耗时：${time - leishenStart} ms")
                        }
                        delay(3000)
                    } else {
                        //其实如果外层的血条逻辑足够准的话（因为不知道血条会不会被挡死，但挡死也就进不来了）.这里就不用再判断了，毕竟蓝色判断不准
                        //即：如果有血条那么一定有球，如果不是红球，则就是蓝球。（经过实验，红球的判断相对很准，无非就是调下数值，如果被挡的厉害,反正如果不是红球，待检测区域redcount基本都是0)
                        var count2 = Config.rectCheckOfLeishen.hasColorCount(Config.colorLeishenLanqiu, testImg = img)
                        if (count2 > 2000) {
                            onLeiShenBlueBallShow()
                            log("检测到蓝球")
                            log(img)
                            checkCount++
                            if (checkCount == 6) {
                                onLeiShenSixBallOver()
                                leishenOberser = false
                                var time = System.currentTimeMillis()
                                log("识别完6个球，耗时：${time - leishenStart} ms")
                            }
                            delay(3000)
                        } else {
                            delay(20)
                        }
                    }

                }
            }
        }
    }
    open fun onXiongMaoQiuGot(qiu:String){
        log("熊猫识别到球：$qiu")
        hasQiu = true
    }
    var xiongmaoOberserver = false
    var hasQiu = false
    fun startXiongMaoOberser(){
        if (xiongmaoOberserver) return
        xiongmaoOberserver = true
        var leishenStart = System.currentTimeMillis()
        var checkCount = 0
        GlobalScope.launch {
            //按截图算，大约10秒一个球（9.5）
            while (xiongmaoOberserver) {

                var img = getImage(App.rectWindow)
                hasQiu = false
                if (Config.xiongmaoQiuRect.hasColorCount(
                        Config.xiongmaoFS, testImg = img
                    ) > 50){
                    onXiongMaoQiuGot("fs")
                }else  if (Config.xiongmaoQiuRect.hasColorCount(
                        Config.xiongmaoGJ, testImg = img
                    ) > 50){
                    onXiongMaoQiuGot("gj")
                }

                if(hasQiu){//识别到一个球后，延迟5秒再识别，节省
                    delay(5000)
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
        longwangObserver = false
        leishenOberser = false
        xiongmaoOberserver = false
        App.stopAutoSave()
    }
}