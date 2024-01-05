package data

import data.Config.delayNor
import data.Config.platform
import getImageFromRes
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import log
import resFile
import tasks.CarDoing
import utils.ImgUtil
import java.awt.image.BufferedImage
import java.io.File

//weightModel 0有就行，1 优先满金,2不满星
//heroname取文件夹名字，但文件夹名字就是取得英雄名字
data class HeroBean(
    val heroName: String,
    var weight: Int = 0,
    val weightModel: Int = 1,
    val needCar: Boolean = true,
    var position: Int = -1,
    var compareRate: Double = ImgUtil.simRate,
    var isGongCheng: Boolean = false,
) {
    //-1未上，其他代表车号位置
//    val img: BufferedImage = getImageFromRes(imgPath + ".png")
//    val img: BufferedImage = ImageIO.read(File(imgPath+".png"))

    var currentLevel = 0//0也未上，绿-》金为1234

    val imgList = arrayListOf<BufferedImage>().apply {
        var subFoler = "${Config.platName}/heros/${heroName}"
        var heroFolder = resFile(subFoler)
        heroFolder.listFiles().forEach {
            add(getImageFromRes("$subFoler${File.separator}${it.name}"))
        }


//        add(getImageFromRes(heroName + ".png"))
//        for (i in 1..9) {
//            try {
//
//                add(getImageFromRes(heroName + "$i" + ".png"))
//            } catch (e: Exception) {
//                println(e.message)
//            }
//        }
    }

    suspend fun checkStarMix(carDoing: CarDoing): Boolean {
        if (!checkStarLevelDirect(carDoing) || isFull()) {//如果检测到了，但满星了，就用卡片确认一次
            return checkStarLevelUseCard(carDoing)
        }
        return true
    }

    suspend fun checkStarLevelDirect(carDoing: CarDoing): Boolean {
        var checked = false
        try {
            withTimeout(300) {
                while (!checked) {
                    var level = carDoing.carps.get(position).getStarLevel()
//                    if (level > 0 && level-currentLevel==1) {//检测星,按升星1.其它的检测到就不算了
                    if (level > 0 ) {
                        currentLevel = level
                        checked = true
                    } else {
                        delay(100)
                    }
                }
            }
        } catch (e: Exception) {

        }
        return checked
    }

    /**
     * use click show card way to check star ,because of the star here will not be hide by something else
     * result is tell if has starChanged
     */
    suspend fun checkStarLevelUseCard(carDoing: CarDoing): Boolean {
        if (isInCar()) {
            delay(delayNor)
            log("$heroName is check star")
            carDoing.carps.get(position).click()
            delay(delayNor)
            var level = 0
            var list = arrayListOf<Recognize>(
                Recognize.heroStar1,
                Recognize.heroStar2,
                Recognize.heroStar3,
                Recognize.heroStar4
            )
            try {
                withTimeout(1000) {
                    level@ while (level == 0) {
                        for (i in 0..3) {
                            if (list.get(i).isFit()) {
                                level = i + 1
                                break@level
                            }
                        }
                        delay(100)//为了尽快识别，每100ms检测下，否则要delay 比如300等弹窗出来再识别，万一多余了300，就无法识别了
                    }
                }
            } catch (e: Exception) {
                log("未检测到星级，超时")
            }
            var result = (level != currentLevel)
            if (level > 0) {
                currentLevel = level
            }
            try {
                withTimeout(1000) {
                    while (!Recognize.saleRect.isFit()) {
                        delay(100)
                    }
                }
            } catch (e: Exception) {
            }
            CarDoing.cardClosePoint.click()
            log("$heroName check star is ${level} changed:${result}")
            return result
        }
        return false
    }

    fun fitImage(oImg: BufferedImage): Boolean {
        imgList.forEach {
            if (ImgUtil.isImageSim(it, oImg, compareRate)) {
//                log(it)
                return true
            }
        }
        return false
    }

    fun isInCar() = position > -1

    fun isFull() = currentLevel >= 4

    fun reset() {
        position = -1
        currentLevel = 0
    }
}