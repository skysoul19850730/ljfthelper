package test

import com.sun.jna.platform.WindowUtils
import logOnly
import utils.MRobot
import java.awt.Shape
import java.awt.Window
import java.awt.event.InputEvent

object WindowTest {
    fun test(){
        WindowUtils.getAllWindows(true).forEach {
            logOnly("${it.title}")
            if(it.title.contains("微信")){
                var x = it.locAndSize.x
                var y = it.locAndSize.y
                MRobot.robot.mouseMove(x+15,y+15)
                MRobot.robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                MRobot.robot.delay(3000)
                MRobot.robot.mouseMove(15,15)
                MRobot.robot.delay(3000)
                MRobot.robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            }
        }
    }
}