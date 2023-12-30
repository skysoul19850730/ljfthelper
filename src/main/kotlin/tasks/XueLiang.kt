package tasks

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import data.MPoint
import data.MRect
import utils.MRobot

object XueLiang {

    val y = 100

    val mRect = MRect.createWH(100,y,308,1)
    val otherRect = MRect.createWH(593,y,308,1)

    var blackColor = Color(46,39,41)


    fun isMLess(rate:Float):Boolean{

        var checkPoint = MPoint((mRect.left+ mRect.width*rate).toInt(),y)
        return MRobot.robot.getPixelColor(checkPoint.x,checkPoint.y).rgb == blackColor.toArgb()

    }

}