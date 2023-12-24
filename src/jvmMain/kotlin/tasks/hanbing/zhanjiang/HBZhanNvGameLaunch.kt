package tasks.hanbing.zhanjiang

import data.Config
import data.Config.delayLong
import data.Recognize
import kotlinx.coroutines.*
import log
import logOnly
import tasks.IGameLaunch

class HBZhanNvGameLaunch : IGameLaunch {

    //判断车尾匹配 为后车，车头匹配 为前车。然后根据车位 init points
    //前两个格子只能上战 冰女，遇到光球 战未满（其他不满的都下掉，然后用光），战满，女王下了再用，用完后两秒后检测星级
    //幻给副卡，脚本不用
    //270（管卡不采集检测了，直接检测右上角的boss，每5秒一次即可），看到孤星 下冰女卡住，遇到 神龙和乌龟，上狂将。
    var isRunning = false
    var heroDoing: HBZhanNvHeroDoing? = null
    var mJob: Job? = null

    var isHezuoIng = false

    override fun init() {
    }

    suspend fun checkHezuo(){
        startOneHezuo()
    }

    override fun start() {
        if (isRunning) return
        isRunning = true
        mJob = GlobalScope.launch {
            while (isRunning) {
                if(!isHezuoIng){
                    checkHezuo()
                }
                if(isHezuoIng) {
                    checkEnd()
                }
                delay(Config.delayLong)
            }

        }

    }
    suspend fun startOneHezuo(){
        isHezuoIng = true
//        log("开始识别车辆位置")
//        var chePosition = -1
//        while (chePosition == -1) {
//            if (Recognize.QianChe.isFit()) {
//                chePosition = 1
//            } else if (Recognize.Houche.isFit()) {
//                chePosition = 0
//            }
//            if(!isRunning){
//                return
//            }
//        }
//        log("识别车辆位置 $chePosition")
//        heroDoing = ZhanNvHeroDoing(chePosition)
        heroDoing = HBZhanNvHeroDoing()
        heroDoing?.init()
        heroDoing?.start()

    }

    private suspend fun checkEnd() {
        logOnly("checkIfEnd")
        if (Recognize.BtnOk.isFit()) {
            log("checkIfEnd ok")
            stopOneGame()
            delay(100)
            Recognize.BtnOk.click()
            delay(delayLong)
            delay(delayLong)
            isHezuoIng = false
        }
    }

    private suspend fun stopOneGame(){
        heroDoing?.stop()

    }


    override fun stop() {
        isRunning = false
        isHezuoIng = false
        mJob?.cancel()
        heroDoing?.stop()
    }
}