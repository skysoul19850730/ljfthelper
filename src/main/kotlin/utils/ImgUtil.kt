package utils

import data.Config
import data.MRect
import getImage
import getImageFromRes
import log
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.abs

object ImgUtil {

    val sim = 20

    //        val simRate = 0.95 //太小了，比较管卡 都特么一样，操
    val simRate
        get() = if (App.caijing.value) 0.98 else if (Config.platform.value == Config.platform_moniqi) 0.85 else 0.75

    fun isImageSim(img1: BufferedImage, img2: BufferedImage?, sim: Double = simRate): Boolean {
        img2 ?: return false
        if (img1.width != img2.width || img1.height != img2.height) return false

        var rect = MRect.createWH(0, 0, img1.width, img1.height)

        return quickCompare(rect, sim) { x, y ->
            img1.getRGB(x, y) to img2.getRGB(x, y)
        }
    }

    fun isHeroSim(img1: BufferedImage, img2: BufferedImage?, sim: Double = simRate): Boolean {
        img2 ?: return false
        if (img1.width != img2.width || img1.height != img2.height) return false

        var rect = MRect.createWH(0, 0, img1.width, img1.height)

        return quickCompare(rect, sim) { x, y ->
            val c1 = img1.getRGB(x, y)
            val c2 = img2.getRGB(x, y)
            if (c1 == Color.WHITE.rgb || c2 == Color.WHITE.rgb) {//白色就认为相同（刷新时的白色闪光
//                log("has shanguang baise ")
                Color.WHITE.rgb to Color.WHITE.rgb
            } else {
                c1 to c2
            }
        }
    }

    private fun quickCompare(rect: MRect, sim: Double = simRate, c1c2: (Int, Int) -> Pair<Int, Int>): Boolean {
        var yes = 0
        var no = 0
        val all = rect.width * rect.height
        val yesall = all * sim
        val noall = all * (1 - sim)
        //计算不合格已经超标就不用再继续循环后面的了

        rect.forEach { x, y ->
            var result = c1c2.invoke(x, y)
            if (colorCompare(result.first, result.second)) {
                yes++
                if (yes > yesall) {
                    var rate = (yes * 1f / all)
//                    println("pre over suc rate is $rate")
                    return true
                }
            } else {
                no++
                if (no > noall) {
                    var rate = (yes * 1f / all)
//                    println("pre over fail rate is $rate")
                    return false
                }
            }
        }
        var rate = (yes * 1f / (rect.width * rect.height))
//        println("rate is $rate")
        return rate > sim
    }

    fun isImageInRect(imgName: String, rect: MRect, sim: Double = simRate): Boolean {
        val img = getImageFromRes(imgName)

        return isImageInRect(img, rect, sim)
    }

    fun isImageInRect(img: BufferedImage, rect: MRect, sim: Double = simRate): Boolean {
        val img2 = getImage(rect)
//        log(img)
//        log((img2))
        return isImageSim(img, img2, sim)
    }


    inline fun MRect.forEach(callback: (Int, Int) -> Unit) {
        for (x in left..right) {
            for (y in top..bottom) {
                callback.invoke(x, y)
            }
        }
    }
    inline fun MRect.forEach4Result(callback: (Int, Int) -> Boolean) {
        for (x in left..right) {
            for (y in top..bottom) {
                if(callback.invoke(x, y)){
                    return
                }
            }
        }
    }

    private fun colorCompare(color1: Int, color2: Int): Boolean {
        if (sim == 0) {
            return color1 == color2
        }
        return colorCompare(Color(color1), Color(color2))
    }


    private fun colorCompare(c1: Color, c2: Color): Boolean {
        return (abs(c1.red - c2.red) <= sim
                && abs(c1.green - c2.green) <= sim
                && abs(c1.blue - c2.blue) <= sim)
    }
}