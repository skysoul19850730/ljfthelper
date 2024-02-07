package model

import colorCompare
import data.*
import getImage
import getSubImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import log
import logOnly
import loges
import model.CarDoing.Companion.salePoint
import utils.MRobot
import java.awt.Color
import java.awt.image.BufferedImage

/**
 * 一个车有7个position
 */
data class CarPosition(
    val mPos: Int = -1,
    var mRect: MRect,
    var starPoint: MPoint,
    val carDoing: CarDoing
) {

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    fun reInitRectAndPoint(
        mRect: MRect,
        starPoint: MPoint
    ) {
        this.mRect = mRect
        this.starPoint = starPoint
    }

    companion object {
        val level4 = Color(255, 193, 10)
        val level3 = Color(173, 142, 255)
        val level2 = Color(102, 192, 255)
        val level1 = Color(102, 248, 32)
    }

    /**
     * 车位是否已打开，0，6默认就是开的。暂时不用这个属性，但如果比如实时计算 工程位的位置，那么就得知道开了多少格子（开了格子不一定上英雄，所以不能按英雄null来判断)
     */
    var mIsOpen = false
        get() {
            return if (mPos == 0) true//0号位和6号位，上来就是开启状态
            else field
        }

    /**
     * 本车位寄住英雄
     */
    var mHeroBean: HeroBean? = null


    fun hasHero() = mHeroBean != null

    fun addHero(heroBean: HeroBean? = null) {
        if (heroBean == null) {
            if (mHeroBean == null) return
            else {
                mHeroBean!!.currentLevel++
            }
            return
        }
        mIsOpen = true
        mHeroBean = heroBean
        heroBean.currentLevel++
        heroBean.position = mPos
    }

    suspend fun downHero() {
        if (mHeroBean != null) {
            withContext(Dispatchers.Main) {
                logOnly("车位:$mPos 下卡开始 ${mHeroBean?.heroName}")
                var start = System.currentTimeMillis()
                var cardShow = false
                withTimeoutOrNull(2000) {
                    while (!cardShow) {
                        click()
                        cardShow = withTimeoutOrNull(500) {
                            while (!Recognize.saleRect.isFit()) {//下卡时 被结束的弹窗挡住，这里一直不fit（挡住了就检测不到出售按钮）。然后也不会执行 结束的按钮点击。所以这里加个超时
                                delay(10)//妈的，这里不加delay就检测不会timeout，fuck
                            }
                            true
                        } ?: false
                    }
                }

                var cardMiss = false
                while (!cardMiss) {
                    MRobot.singleClick(salePoint)
                    cardMiss = withTimeoutOrNull(500){
                        while (Recognize.saleRect.isFit()) {//下卡时 被结束的弹窗挡住，这里一直不fit（挡住了就检测不到出售按钮）。然后也不会执行 结束的按钮点击。所以这里加个超时
                            delay(10)//妈的，这里不加delay就检测不会timeout，fuck
                        }
                        true
                    } ?: false
                }
                log("下卡完成：${mHeroBean?.heroName} position:${mHeroBean?.position} coast:${System.currentTimeMillis() - start}")
                mHeroBean?.reset()
                mHeroBean = null
            }
        }
    }

    suspend fun click() {
        MRobot.singleClick(mRect.clickPoint)
    }

    fun rateSelectByChuanZhang(testImg: BufferedImage): Float {
        var mHeight = mRect.bottom - mRect.top + 1
        var simCount = 0
//            var resultImg = BufferedImage(testImg.width, testImg.height, TYPE_INT_RGB)
//            resultImg.graphics.drawImage(testImg, 0, 0, testImg.width, testImg.height, null)
        for (y in mRect.top..mRect.bottom) {
            var fit = try {
                colorCompare(Color(testImg.getRGB(mRect.clickPoint.x, y)), Config.Color_ChuangZhang, 10)
            } catch (e: Exception) {
                loges(e.toString())
                loges("mRect : ${mRect.toString()}, y is $y")
                false
            }
            if (fit) {
                simCount++
//                    resultImg.setRGB(mRect.clickPoint.x,y,Color.RED.rgb)
//                    resultImg.setRGB(mRect.clickPoint.x-1,y,Color.RED.rgb)
//                    resultImg.setRGB(mRect.clickPoint.x-2,y,Color.RED.rgb)
//                    resultImg.setRGB(mRect.clickPoint.x+1,y,Color.RED.rgb)
//                    resultImg.setRGB(mRect.clickPoint.x+2,y,Color.RED.rgb)
            }
//                if (colorCompare(Color(testImg.getRGB(mRect.clickPoint.x-1, y)), Config.Color_ChuangZhang, 10)) {
//                    simCount++
//                }
//                if (colorCompare(Color(testImg.getRGB(mRect.clickPoint.x+1, y)), Config.Color_ChuangZhang, 10)) {
//                    simCount++
//                }
        }
        var rate = (simCount * 1f) / (mHeight)
//            log(resultImg)
        logOnly("compareHeight:${mRect.width}X${mRect.height} ${mRect.width * mRect.height} totalCount $mHeight       okCount:$simCount   rate:$rate")

        return rate
    }

    suspend fun checkStarMuluty() {
        var hl = getStarLevelDirect()
        if (hl <= 0) {
            mHeroBean?.checkStarLevelUseCard(carDoing)
        } else {
            mHeroBean?.currentLevel = hl
        }
    }

    suspend fun checkStarLevelUseCard() {
        mHeroBean?.run {
            checkStarLevelUseCard(carDoing)
            log("${heroName} level is ${currentLevel}")
        }
    }

    fun getStarLevelDirect(imgTest: BufferedImage? = null): Int {
//            var starColor = Color(img.getRGB(starPoint.x, starPoint.y))
        var testColor = imgTest?.getRGB(starPoint.x, starPoint.y)?.run {
            Color(this)
        }
        var starColor = testColor ?: MRobot.robot.getPixelColor(starPoint.x, starPoint.y)

        if (Config.debug) {
            var starRect = MRect.create4P(starPoint.x - 5, starPoint.y - 5, starPoint.x + 5, starPoint.y + 5)
            var img = imgTest?.getSubImage(starRect) ?: getImage(starRect)
            log(img)
            logOnly("star color is red:${starColor.red} green:${starColor.green} blue:${starColor.blue}")
        }
        if (colorCompare(starColor, level4)) {
            return 4
        }
        if (colorCompare(starColor, level3)) {
            return 3
        }
        if (colorCompare(starColor, level2)) {
            return 2
        }
        if (colorCompare(starColor, level1)) {
            return 1
        }
        return 0
    }

}