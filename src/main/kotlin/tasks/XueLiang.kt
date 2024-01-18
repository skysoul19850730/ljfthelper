package tasks

import colorCompare
import data.MPoint
import data.MRect
import utils.MRobot
import java.awt.Color

object XueLiang {

    val y = 100

    val mRect = MRect.createWH(100,y,308,1)
    val otherRect = MRect.createWH(593,y,308,1)

    var blackColor = Color(46,39,41)


    fun isMLess(rate:Float):Boolean{

        var checkPoint = MPoint((mRect.left+ mRect.width*rate).toInt(),y)
        return colorCompare(MRobot.robot.getPixelColor(checkPoint.x,checkPoint.y) ,blackColor)

    }

}