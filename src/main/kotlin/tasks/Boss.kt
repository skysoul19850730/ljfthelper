package tasks

import data.Config
import getImage
import getImageFromRes
import log
import utils.ImgUtil
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object Boss {
    val caijiPath = Config.caiji_main_path + "\\boss"

    class BossBean(val name: String) {
        fun isFit(): Boolean {
            var fName = "${Config.platName}/boss/$name.png"
//            var rect =if(name=="nvwangche" || name == "longwangche"){
            var rate = ImgUtil.simRate
            var rect = if (name == "longwangche" || name == "nvwangche") {

                rate = if (name == "nvwangche") 0.98 else 0.9
                Config.zhandou_hezuo_bossNameRect
            } else {
                Config.zhandou_hezuo_bossRect
            }
            return ImgUtil.isImageInRect(fName, rect, rate)
        }

        fun testFitImg(img: BufferedImage): Boolean {
            var fName = "${Config.platName}/boss/$name.png"
//            var rect =if(name=="nvwangche" || name == "longwangche"){
            var rate = ImgUtil.simRate
            var rect = if (name == "longwangche" || name == "nvwangche") {
                rate = 0.90
                Config.zhandou_hezuo_bossNameRect
            } else {
                Config.zhandou_hezuo_bossRect
            }
            var result = ImgUtil.isImageSim(getImageFromRes(fName), img, rate)
            log(result.toString())
            return result
        }
    }

    val guxing by lazy { BossBean("guxing") }
    val shenlong by lazy { BossBean("shenlong") }
    val wugui by lazy { BossBean("wugui") }
    val mengyan by lazy { BossBean("mengyan") }
    val nvwangche by lazy { BossBean("nvwangche") }
    val longwangche by lazy { BossBean("longwangche") }


    val xiaoyi by lazy { BossBean("guxing") }
    val hbchuanzhang by lazy { BossBean("hbchuanzhang") }
    val hbying by lazy { BossBean("hbying") }
    fun init() {
    }

    fun save() {
        val img = getImage(Config.zhandou_hezuo_bossRect)
        ImageIO.write(img, "png", File(caijiPath, "${System.currentTimeMillis()}.png"))
        log(img)
        val img2 = getImage(Config.zhandou_hezuo_bossNameRect)
        ImageIO.write(img2, "png", File(caijiPath, "${System.currentTimeMillis()}name.png"))
        log(img2)
    }

    fun isXiaoYi(): Boolean {
        return xiaoyi.isFit()
    }

    fun isChuanZhang(): Boolean {
        return hbchuanzhang.isFit()
    }

    fun isGuxing(): Boolean {
        return guxing.isFit()
    }

    fun isNvwangChe(): Boolean {
        return nvwangche.isFit()
    }

    fun isLongwangChe(): Boolean {
        return longwangche.isFit()
    }

    fun isShenLong(): Boolean {
        return shenlong.isFit()
    }

    fun isWuGui(): Boolean {
        return wugui.isFit()
    }

    fun is310(): Boolean {
        return mengyan.isFit()
    }


}