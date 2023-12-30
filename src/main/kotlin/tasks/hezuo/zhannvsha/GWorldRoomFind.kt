package tasks.hezuo.zhannvsha

import data.Config
import data.Config.delayLong
import data.Config.delayNor
import data.MPoint
import data.Recognize
import getImage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import log
import tasks.HomePage
import tasks.WxUtil
import tesshelper.Tess
import utils.MRobot

class GWorldRoomFind : IRoomFind() {

    override suspend fun findAndJoinRoom() {
        log("findAndJoinRoom")
        findGameHezuo()
    }

    suspend fun findGameHezuo() {
        delay(3000)
        var finding = true
        HomePage.openWorldMsg()
        var clicking = true
        GlobalScope.launch {
            while (finding){
                log("clcik")
                if(clicking) {
                    MPoint(765, 480).clickPc()
                }else{
                    delay(200)
                }
            }
        }
        GlobalScope.launch {
            while (finding) {
                if (Recognize.NoAdvOk.isFit()) {
                    log("try in")
                    clicking = false
                    Recognize.NoAdvOk.click()
                    clicking = true
                }
            }
        }
        var checkCount = 0
        while (finding){

            if(!Recognize.NoAdvOk.isFit()&&!HomePage.pointMsgClose.isFit()){
                checkCount++
                if(checkCount>3) {
                    log("进了")
                    finding = false
                }
                delay(1000)
            }
        }
//        while (finding) {
//            HomePage.openWorldMsg()
//            delay(delayNor)
//            MRobot.robot.mouseMove(750,480)
//            var color = MRobot.robot.getPixelColor(750, 480)
//            if (color.red == 255 && color.green == 195 && color.blue == 49) {
//                println("找到了房间")
//                MPoint(750, 480).clickPc()
////                MRobot.quickClick()
//                delay(10)
//                Config.hezuo_0tip_ok.clickPc()
//                delay(delayNor)
//                HomePage.pointMsgClose.clickPc()
//                delay(2000)
//                if(!HomePage.isAtHomePage()) {
//                    finding = false
//                }
//            }
//        }
    }
}