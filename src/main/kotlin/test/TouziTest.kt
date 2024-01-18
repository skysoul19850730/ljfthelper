package test

import data.MRect
import getImageFromRes
import getSubImage
import logOnly
import utils.ImgUtil.forEach
import java.awt.image.BufferedImage
import kotlin.math.abs

object TouziTest {


    fun test(){
        var starttime = System.currentTimeMillis()
        val touziRect = MRect.createWH(405, 220, 20, 50)
        var img2 = getImageFromRes("touzitest.jpg")
        var img = img2.getSubImage(touziRect)
        var result = touziCheck(img)
        var end = System.currentTimeMillis()
        logOnly("识别骰子:$result  time :${(end - starttime)}")
    }

    private fun touziCheck(img: BufferedImage): Boolean {

        for (x in 0 until img.width) {
            for (y in 0 until img.height) {
                val color = java.awt.Color(img.getRGB(x, y))
                if (isColorWhite(color)) {
                    if (x - 2 >= 0 && x + 7 < img.width && y + 9 < img.height) {
                        var rect = MRect.createWH(x - 2, y, 10, 10)
                        var white = 0
                        rect.forEach { x1, y1 ->
                            val color1 = java.awt.Color(img.getRGB(x1, y1))
                            if (isColorWhite(color1)) {
                                white++
                            }
                        }
                        if (white > 60) {
                            return true
                        }
                    }
                }
            }
        }


        return false
    }
    private fun isColorWhite(c1: java.awt.Color, c2: java.awt.Color = java.awt.Color.white): Boolean {
        return (abs(c1.red - c2.red) <= 20
                && abs(c1.green - c2.green) <= 20
                && abs(c1.blue - c2.blue) <= 20)
    }
}