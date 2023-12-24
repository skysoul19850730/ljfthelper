package tasks.duizhan.faqiang

import data.Config
import data.Config.delayLong
import data.Config.delayNor
import data.MPoint
import data.Recognize.*
import kotlinx.coroutines.*
import log
import logOnly
import tasks.Hero
import tasks.IGameLaunch

class FaQiangGameLaunch : IGameLaunch {

    var isRunning = false
    var kaida = false

    var heroDoing: FaQiangHeroDoing? = null

    var mJob: Job? = null

    override fun init() {
    }


    override fun start() {

        if (isRunning) return
        isRunning = true
        mJob = GlobalScope.launch {
            while (isRunning) {

                if (!kaida) {
//                    kaida = true
//                    startOneGame()
                    checkDuizhan()
                }
                if (kaida) {
                    checkEnd()
                }
                delay(delayLong)
            }
        }
    }

    override fun stop() {
        isRunning = false
        mJob?.cancel("tuichu")
        heroDoing?.stop()
        kaida = false
    }

    private suspend fun checkDuizhan() {
        log("checkDuizhan")
        if (Duizhan.isFit()) {
            log("checkDuizhan ok")
//            while (Duizhan.isFit()) {
            delay(delayNor)
            Duizhan.click()
            delay(delayNor)
//            }

            Pipei.click()
            kaida = true
//            withContext(Dispatchers.Main){
            startOneGame()
            delay(delayLong)//等loading
//            }
        }
    }

    private suspend fun checkEnd() {
        logOnly("checkIfEnd")
        if (BtnOk.isFit()) {
            log("checkIfEnd ok")
            stopOneGame()
            delay(100)
            while (BtnOk.isFit()) {
                BtnOk.click()
                delay(1000)
            }
            delay(2000)//点完确定多留些时间检测广告弹窗（只检测一次，有就按，没有就算（可能把广告看完了，也可能系统没给广告
            checkAdv()
        } else if (CanreJujue.isFit()) {
            log("checkIfEnd CanreJujue ok")
            stopOneGame()
            delay(100)
            checkAdv()
        }
    }

    private suspend fun checkAdv() {
        log("checkAdv")
        if (CanreJujue.isFit()) {
//            CanreJujue.click()
            Config.adv_point.click()
            delay(17000)
            Config.adv_close.click()
            delay(2000)
            MPoint(100,130).click()
        }
        delay(delayNor)
        kaida=false
//        if (CanreJujue.isFit()) {
//        CanreJujue.click()
//        delay(delayNor)
//        kaida = false
//        }
    }

    private suspend fun startOneGame() {
        heroDoing = FaQiangHeroDoing()
        heroDoing!!.init()
        heroDoing!!.start()
    }

    private fun stopOneGame() {
        heroDoing?.stop()
    }
}