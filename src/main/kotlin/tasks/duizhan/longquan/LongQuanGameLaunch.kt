package tasks.duizhan.longquan

import data.Config
import data.Config.delayLong
import data.Config.delayNor
import data.MPoint
import data.Recognize.*
import kotlinx.coroutines.*
import log
import logOnly
import tasks.IGameLaunch

class LongQuanGameLaunch : IGameLaunch {

    var isRunning = false
    var kaida = false

    var heroDoing: LongQuanHeroDoing? = null

    var mJob: Job? = null

    var allSucCount = 0
    var allFailCount = 0

    var failCount = 0

    var lastSuc = false

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
        stopOneGame()
        kaida = false
    }

    //0打  1认输阵容
    var mCurrentZhen = -1
    private suspend fun changeToFailZhen() {
        if (mCurrentZhen == 1) return
        Config.pointHeroChoose.click()
        delay(500)
        Config.pointHeroDuiZhanFail.click()
        delay(delayNor)
        Config.pointHeroChooseBack.click()
        delay(delayNor)
        mCurrentZhen = 1

    }

    private suspend fun changeToSucZhen() {
        if (mCurrentZhen == 0) return
        Config.pointHeroChoose.click()
        delay(500)
        Config.pointHeroDuiZhan.click()
        delay(delayNor)
        Config.pointHeroChooseBack.click()
        delay(delayNor)
        mCurrentZhen = 0
    }

    private suspend fun changeZhenrong() {
        if (Config.touxiangAuto.value) {
            if (Config.touxiangAll.value) {
                changeToFailZhen()
            } else {
                if (failCount < 2) {
                    //切认输
                    changeToFailZhen()
                } else {
                    changeToSucZhen()
                }
            }
        } else {
            changeToSucZhen()
        }
    }

    private suspend fun checkDuizhan() {
        log("checkDuizhan")
        if (Duizhan.isFit()) {
            log("checkDuizhan ok")
//            while (Duizhan.isFit()) {
            changeZhenrong()
            delay(delayNor)
            Duizhan.click()
            delay(1000)
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

            if (DuiZhanResultSuc.isFit()) {
                log("战斗胜利")
                allSucCount++
                MainData.sucCount.value++
                failCount = 0
            } else {
                log("战斗失败")
                allFailCount++
                MainData.failCount.value++
                failCount++
            }
            log("战斗胜利： $allSucCount 失败：$allFailCount 胜率：${(allSucCount * 100f) / (allSucCount + allFailCount)}%")

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
        } else {
            if (Config.touxiangAuto.value && Config.touxiangAll.value) {
                touxiang()
            } else if (Config.touxiangAuto.value && failCount<2) {
                touxiang()
            }
        }
    }
    private suspend fun touxiang(){
        delay(500)
        Config.pointDuiZhanRenshu.click()
        delay(500)
        Config.pointDuiZhanRenshuOk.click()
    }

    private suspend fun checkAdv() {
        log("checkAdv")
        //failCount = 0 可以认为是 胜利。
        if ((CanreJujue.isFit() || CanreJujueFail.isFit()) && (failCount == 0 || Config.viewFailAdv.value)) {
//            CanreJujue.click()
            Config.adv_point.click()
            delay(33000)
            Config.adv_close.click()
            delay(2000)
            MPoint(100, 130).click()
        } else {
            delay(delayNor)
            CanreJujue.click()
        }
        delay(delayNor)
        kaida = false
//        if (CanreJujue.isFit()) {
//        CanreJujue.click()
//        delay(delayNor)
//        kaida = false
//        }
    }

    private suspend fun startOneGame() {
        val renji = failCount >= 2
        heroDoing = LongQuanHeroDoing(renji)
        heroDoing!!.init()
        heroDoing!!.start()
    }

    private fun stopOneGame() {
        heroDoing?.stop()
        heroDoing = null
    }
}