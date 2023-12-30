package tasks

import data.Config
import getImage
import getImageFromRes
import kotlinx.coroutines.delay
import log
import utils.ImgUtil
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object Zhuangbei {
    val caijiPath = Config.caiji_main_path + "\\zhuangbei"

    var mInit = false

    val LONGXIN = 1
    val YANDOU = 2
    val QIANGXI = 3
    val SHENGJIAN = 4


    var longxins = arrayListOf<BufferedImage>()
    var yandous = arrayListOf<BufferedImage>()
    var qiangxis = arrayListOf<BufferedImage>()
    var shengjians = arrayListOf<BufferedImage>()

    private fun path(name: String): String = "${Config.platName}/zhuangbei/$name"

    init {
        longxins.apply {
            add(getImageFromRes(path("longxin1.png")))
            add(getImageFromRes(path("longxin2.png")))
            add(getImageFromRes(path("longxin3.png")))
            add(getImageFromRes(path("longxin4.png")))
        }
        yandous.apply {
            add(getImageFromRes(path("yandou1.png")))
            add(getImageFromRes(path("yandou2.png")))
            add(getImageFromRes(path("yandou3.png")))
            add(getImageFromRes(path("yandou4.png")))
        }
        qiangxis.apply {
            add(getImageFromRes(path("qiangxi1.png")))
            add(getImageFromRes(path("qiangxi2.png")))
            add(getImageFromRes(path("qiangxi3.png")))
            add(getImageFromRes(path("qiangxi4.png")))
        }
        shengjians.apply {
            add(getImageFromRes(path("shengjian1.png")))
            add(getImageFromRes(path("shengjian2.png")))
            add(getImageFromRes(path("shengjian3.png")))
            add(getImageFromRes(path("shengjian4.png")))
        }
    }

    fun save() {
        val img = getImage(Config.zhandou_zhuangbeiCheckRect)
        ImageIO.write(img, "png", File(caijiPath, "${System.currentTimeMillis()}.png"))
        log(img)
    }

    fun getZhuangBei():Int{
        return if(isLongxin()) LONGXIN
        else if(isYandou()) YANDOU
        else if(isQiangxi()) QIANGXI
        else if(isShengjian()) SHENGJIAN
        else 0
    }

    fun isLongxin(): Boolean {
        longxins.forEach {
            if (ImgUtil.isImageInRect(it, Config.zhandou_zhuangbeiCheckRect)) {
                log("是龙心")
                return true
            }
        }
        log("不是龙心")
        return false
    }

    fun isYandou(): Boolean {
        yandous.forEach {
            if (ImgUtil.isImageInRect(it, Config.zhandou_zhuangbeiCheckRect)) {
                log("是烟斗")

                return true
            }
        }
        log("不是烟斗")
        return false
    }

    fun isQiangxi(): Boolean {
        qiangxis.forEach {
            if (ImgUtil.isImageInRect(it, Config.zhandou_zhuangbeiCheckRect)) {
                log("是强袭")

                return true
            }
        }
        log("不是强袭")
        return false
    }

    fun isShengjian(): Boolean {
        shengjians.forEach {
            if (ImgUtil.isImageInRect(it, Config.zhandou_zhuangbeiCheckRect)) {
                log("是圣剑")
                return true
            }
        }
        log("不是圣剑")
        return false
    }

    fun hasZhuangbei():Boolean{
        var result = isLongxin() || isShengjian() || isYandou() || isQiangxi()
        if(!result){
            log("没装备")
        }
        return result
    }
}