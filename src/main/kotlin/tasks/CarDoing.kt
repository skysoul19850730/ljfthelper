package tasks

import MainData
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import data.*
import data.Config.debug
import data.Config.delayNor
import getImage
import getImageFromFile
import getImageFromRes
import getSubImage
import kotlinx.coroutines.*
import log
import logOnly
import saveTo
import utils.ImgUtil.forEach
import utils.MRobot
import java.awt.Color
import java.awt.Image.SCALE_SMOOTH
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max

class CarDoing(var chePosition: Int = -1, var cheType: Int = CheType_YangChe) {


    var salePoint = MPoint().apply {
        x = 310
        y = 440
    }

    var carps = arrayListOf<CarPosition>()

    companion object {
        val CheType_YangChe = 0//羊车和寒冰车星星位置差不多，马车差两三个像素
        val CheType_MaChe = 1

        val starCheckRect = MRect.create4P(310, 340, 344, 368)
        val saleCheckRect = MRect.create4P(260, 430, 300, 460)
        val cardClosePoint = MPoint(760, 126)

        var showTouziRect = mutableStateOf(false)
        var touziLeft = mutableStateOf(0)
        val touziRect = MRect.createWH(301, 330, 20, 50)

        fun moveLeft() {
            showTouziRect.value = true
            touziRect.apply {
                left -= 3
                right -= 3
            }
            touziLeft.value = touziRect.left
            log("touzileft ${touziRect.left}")
        }

        fun moveRight() {
            showTouziRect.value = true

            touziRect.apply {
                left += 3
                right += 3
            }
            log("touzileft ${touziRect.left}")
            touziLeft.value = touziRect.left

        }
    }


//    lateinit var img:BufferedImage//test用

    inner class CarPosition(
        val mPos: Int,
        var mRect: MRect,
        var starPoint: MPoint
    ) {

        var level4 = Color(255, 193, 10)
        var level3 = Color(173, 142, 255)
        var level2 = Color(102, 192, 255)
        var level1 = Color(102, 248, 32)

        suspend fun click() {
            MRobot.singleClick(mRect.clickPoint)
        }

        fun rateSelectByChuanZhang(testImg: BufferedImage): Float {
            var mHeight = mRect.bottom - mRect.top + 1
            var simCount = 0
//            var resultImg = BufferedImage(testImg.width, testImg.height, TYPE_INT_RGB)
//            resultImg.graphics.drawImage(testImg, 0, 0, testImg.width, testImg.height, null)
            for (y in mRect.top..mRect.bottom) {
                if (colorCompare(Color(testImg.getRGB(mRect.clickPoint.x, y)), Config.Color_ChuangZhang, 10)
                ) {
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
            logOnly("rectSize:${mRect.width}X${mRect.height} ${mRect.width * mRect.height} totalCount $mHeight       okCount:$simCount   rate:$rate")

            return rate
        }

        fun rateSelectByChuanZhang2(testImg: BufferedImage): Float {
//            var img = getImageFromRes("chuangzhangaim.bmp")
            //101 138 226  260
            var img = getImageFromRes("chuangzhangaim2.png")
            var newImg = BufferedImage(mRect.width, mRect.height, TYPE_INT_RGB)
            newImg.graphics.drawImage(img, 0, 0, mRect.width, mRect.height, null)

            var resultImg = BufferedImage(testImg.width, testImg.height, TYPE_INT_RGB)
            resultImg.graphics.drawImage(testImg, 0, 0, testImg.width, testImg.height, null)
            var totalCount = 0
            var okCount = 0

            var fx = if (chePosition == 0) {
                if (mPos % 2 == 0) {
                    138
                } else 101
            } else {
                if (mPos % 2 == 0) {
                    260
                } else 226
            }

            mRect.forEach { x, y ->
                if (x >= 0 && y >= 0) {
                    if (newImg.getRGB(x - mRect.left, y - mRect.top) != Color.WHITE.rgb) {
                        totalCount++
                        if (colorCompare(Color(testImg.getRGB(x, y)), Config.Color_ChuangZhang, 10)) {
                            okCount++
                            resultImg.setRGB(x, y, Color.RED.rgb)
                        }
                    }
                }
            }
            log("position:")
            log(resultImg)

            var rate = (okCount * 1f) / totalCount

            log("rectSize:${mRect.width}X${mRect.height} ${mRect.width * mRect.height} totalCount $totalCount       okCount:$okCount   rate:$rate")

            return rate
        }

        fun getStarLevel(): Int {
//            var starColor = Color(img.getRGB(starPoint.x, starPoint.y))
            var starColor = MRobot.robot.getPixelColor(starPoint.x, starPoint.y)

            var img = getImage(MRect.create4P(starPoint.x - 5, starPoint.y - 5, starPoint.x + 5, starPoint.y + 5))
            if (Config.debug) {
                log(img)
                log("star color is red:${starColor.red} green:${starColor.green} blue:${starColor.blue}")
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

        fun getTestStarLevel(imgTest: BufferedImage): Int {
            var starColor = Color(imgTest.getRGB(starPoint.x, starPoint.y))
//            var starColor = MRobot.robot.getPixelColor(starPoint.x, starPoint.y)

            var img =
                imgTest.getSubImage(MRect.create4P(starPoint.x - 5, starPoint.y - 5, starPoint.x + 5, starPoint.y + 5))
            if (debug) {
                log(img)
                log("star color is red:${starColor.red} green:${starColor.green} blue:${starColor.blue}")
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


        private fun colorCompare(c1: java.awt.Color, c2: java.awt.Color, sim: Int = 10): Boolean {
            return (abs(c1.red - c2.red) <= sim
                    && abs(c1.green - c2.green) <= sim
                    && abs(c1.blue - c2.blue) <= sim)
        }
    }

    suspend fun checkStarMuluti() {
        heroList.filter { it != null }.forEach {
            val hero = it!!
            var hl = carps.get(hero.position).getStarLevel()
            if (hl <= 0) {
                hero.checkStarLevelUseCard(this)
            } else {
                hero.currentLevel = hl
            }
        }
    }

    suspend fun checkStarsWithoutCard(): Boolean {

        var checkHasChanged = false
        try {
            withTimeout(1000) {
                while (!checkHasChanged) {
                    var changeCount = 0
                    heroList.filter { it != null }.forEach {
                        val hero = it!!
                        var hl = carps.get(hero.position).getStarLevel()
                        if (hl > hero.currentLevel) {
                            log("check star without card hero:${hero.heroName} changed,cur level is ${hl}")
                            changeCount++
                            hero.currentLevel = hl
                        }
                    }
                    //如果处理实时降星（比如大空翼）就不要按光球来考虑了
                    if (changeCount == 1) {//一个光球只有一个会变，太多的变了，可能是有些英雄的弹道颜色和星星一直识别错误了，这种就导致最终识别变的>1，就再使用弹窗检测
                        checkHasChanged = true
                    }
                    delay(100)//withTimeOut中如果没有挂起方法，就不会触发timeout。。
                }
            }
        } catch (e: Exception) {
            println("check timeout")
            return false
        }
        sysDataToMain()
        return checkHasChanged
    }

    /**
     * 此方法是全部检测一遍星级，用于打败降星的boss后，检测一遍，不同于@see checkStars 是用完光之后check，遇到一个星级改变就结束了
     */
    suspend fun reCheckStars() {
        var s = ""
        heroList.forEachIndexed { index, heroBean ->
            if (heroBean != null) {
                s += "${heroBean.heroName}:位置${index}  "
            }
        }
        log(s)
        heroList.filter { it != null }.forEach {
            val hero = it!!
            hero.checkStarLevelUseCard(this)
            log("${hero.heroName} level is ${hero.currentLevel}")
        }
        sysDataToMain()
    }

    suspend fun checkStars() {
        var checkHasChanged = false

        var s = ""
        heroList.forEachIndexed { index, heroBean ->
            if (heroBean != null) {
                s += "${heroBean.heroName}:位置${index}  "
            }
        }
        log(s)

        heroList.filter { it != null }.forEach {
            val hero = it!!
            if (!hero.isFull() && !checkHasChanged) {//如果记录过满星了，就不用识别，因为合作没有降星
//                var hl = carps.get(hero.position).getStarLevel()
//                while (hl <=0) {//上阵得一定可以识别到，如果识别不到，要么算法有问题，要么暂时被挡住了，那么就等到能识别为止
//                    hl = carps.get(hero.position).getStarLevel()
//                    delay(20)
//                }
//                hero.currentLevel = hl
                checkHasChanged = hero.checkStarLevelUseCard(this)
            }
            log("${hero.heroName} level is ${hero.currentLevel}")

        }
        sysDataToMain()
    }

    /**
     * 暂时不考虑工程位的中间态（车开格子不同，高度不同。工程位置是变化的，如果想处理，就要看不同层数的时候，工程位都记录下来，然后工程位单独管理，不放在这个list中）
     */
    fun initPositions() {
        var r = 123//影响船长识别，不要乱调，目前为止123合适
        if (chePosition == -1) {//单车模式
            carps.apply {
                clear()
                //单车没采集过星星位置，目前逻辑用不到，随便写上先
                add(CarPosition(carps.size, MRect.createWH(70, 388, 37, 70), MPoint(88, 448)))
                add(CarPosition(carps.size, MRect.createWH(28, 388, 37, 70), MPoint(46, 448)))
                add(CarPosition(carps.size, MRect.createWH(70, 318, 37, 70), MPoint(46, 377)))
                add(CarPosition(carps.size, MRect.createWH(28, 318, 37, 70), MPoint(88, 378)))
                add(CarPosition(carps.size, MRect.createWH(70, 248, 37, 70), MPoint(46, 312)))
                add(CarPosition(carps.size, MRect.createWH(28, 248, 37, 70), MPoint(88, 313)))

                //工程位
                add(CarPosition(carps.size, MRect.createWH(28, 248, 37, 70), MPoint(88, 313)))
            }
        } else {
            //101 138 226  260
            if (chePosition == 0) {
                carps.apply {
                    clear()
                    if (cheType == CheType_YangChe) {
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(139, 430), r), MPoint(138, 450)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(101, 430), r), MPoint(100, 450)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(139, 360), r), MPoint(138, 380)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(101, 360), r), MPoint(100, 380)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(139, 286), r), MPoint(138, 312)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(101, 286), r), MPoint(100, 311)))
                    } else if (cheType == CheType_MaChe) {
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(138, 425), r), MPoint(138, 448)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(101, 425), r), MPoint(100, 449)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(138, 355), r), MPoint(138, 377)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(101, 355), r), MPoint(100, 378)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(138, 286), r), MPoint(138, 312)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(101, 286), r), MPoint(100, 313)))
                    }

                    add(CarPosition(carps.size, MRect.createPointR(Config.point7p_houche, r), MPoint(118, 236)))
                }
            } else {
                carps.apply {
                    clear()
                    if (cheType == CheType_YangChe) {
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(260, 430), r), MPoint(260, 450)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(226, 430), r), MPoint(226, 450)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(260, 360), r), MPoint(260, 380)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(226, 360), r), MPoint(226, 380)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(260, 286), r), MPoint(260, 312)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(226, 286), r), MPoint(226, 311)))
                    } else if (cheType == CheType_MaChe) {
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(260, 425), r), MPoint(260, 448)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(226, 425), r), MPoint(226, 449)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(260, 355), r), MPoint(260, 377)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(226, 355), r), MPoint(226, 378)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(260, 286), r), MPoint(260, 312)))
                        add(CarPosition(carps.size, MRect.createPointR(MPoint(226, 286), r), MPoint(226, 313)))
                    }

                    add(CarPosition(carps.size, MRect.createPointR(Config.point7p_qianche, r), MPoint(216, 236)))
                }
            }
        }

    }

    /**
     * 第7个是工程位
     */
    var heroList = arrayListOf<HeroBean?>(null, null, null, null, null, null, null)

    fun addHero(heroBean: HeroBean) {
        if (heroBean.needCar) {
            if (heroBean.position >= 0) {//已在车上
                heroBean.currentLevel++
                if (debug) {
                    log("${heroBean.heroName} is in car allReady")
                }
            } else {
                if (heroBean.isGongCheng) {


                } else {
                    var position = heroList.indexOfFirst {
                        it == null
                    }
                    if (position == -1 || position == 6) {//没位置了,不会出现，脚本正常，流程正确一定有位置

                    } else {
                        heroBean.position = position
                        heroBean.currentLevel++
                        heroList.set(position, heroBean)
                    }
                }
            }

        } else if (heroBean.isGongCheng) {
            heroBean.position = 6
            heroBean.currentLevel++
            heroList.set(6, heroBean)
        }

//        log("hero ${heroBean.heroName} add ,position is ${heroBean.position} level is ${heroBean.currentLevel}")

        sysDataToMain()
    }

    suspend fun downPosition(position: Int) {
        var hero = heroList.get(position)
        if (hero != null) {
            downHero(hero)
        }
    }

    var downing = false
    suspend fun downHero(heroBean: HeroBean) {
        if (heroBean.isInCar()) {
//            delay(50)
            withContext(Dispatchers.Main) {
                logOnly("下卡开始 ${heroBean.heroName}")
                downing = true
                carps.get(heroBean.position).click()
                withTimeoutOrNull(400) {//下卡时 被结束的弹窗挡住，这里一直不fit（挡住了就检测不到出售按钮）。然后也不会执行 结束的按钮点击。所以这里加个超时
                    while (!Recognize.saleRect.isFit()) {
                        delay(10)//妈的，这里不加delay就检测不会timeout，fuck
                    }
                }

                MRobot.singleClick(salePoint)
                heroList.set(heroBean.position, null)
                log("下卡完成：${heroBean.heroName} position:${heroBean.position}")
                heroBean.reset()
                downing = false
            }
        }
        sysDataToMain()
    }


    fun touziCheck(): Boolean {
        var img = getImage(touziRect)

        for (x in 0 until img.width) {
            for (y in 0 until img.height) {
                val color = Color(img.getRGB(x, y))
                if (isColorWhite(color)) {
                    if (x - 2 >= 0 && x + 7 < img.width && y + 9 < img.height) {
                        var rect = MRect.createWH(x - 2, y, 10, 10)
                        var white = 0
                        rect.forEach { x1, y1 ->
                            val color1 = Color(img.getRGB(x1, y1))
                            if (isColorWhite(color1)) {
                                white++
                            }
                        }
                        if (white > 60) {
                            log("识别到骰子")
                            return true
                        }
                    }
                }
            }
        }


        return false
    }

    private fun colorCompare(c1: java.awt.Color, c2: java.awt.Color, sim: Int = 10): Boolean {
        return (abs(c1.red - c2.red) <= sim
                && abs(c1.green - c2.green) <= sim
                && abs(c1.blue - c2.blue) <= sim)
    }

    fun getChuanZhangMax(imgTest: BufferedImage? = null): Pair<Int,Float>? {
        var startTime = System.currentTimeMillis()
        val img = imgTest ?: getImage(App.rectWindow)
        var maxIndex = -1
        var maxRate = 0f
        var allNull = heroList.all {
            it==null
        }
        carps.forEachIndexed { index, carPosition ->
            if(heroList.get(index)!=null || allNull) {//allNull是判断副车，不涉及下卡，所以每个位置都检测。如果是主车，null的位置就不检测，比如没有工程位，或者下掉的位置没补上
                logOnly("车$chePosition 车位$index ")
                var rate = carPosition.rateSelectByChuanZhang(img)
                logOnly("车$chePosition 车位$index 识别率：$rate")
                if (rate > maxRate) {
                    maxRate = rate
                    maxIndex = index
                }
            }
        }
        var coast = System.currentTimeMillis() - startTime
        if (maxRate > 0.1) {
            log("最符合的车位是$maxIndex 其比例为:$maxRate  coast:$coast")
//            log(img)
        }
        if (maxRate > 0.2) {
//            img.saveTo(File(App.caijiPath, "${System.currentTimeMillis()}.png"))
            return Pair(maxIndex,maxRate)
        }
//        log(img)
        logOnly("未识别到合适的车位  coast:$coast")
        return null
    }

//    fun testStarAndChuanzhang(img: BufferedImage) {
//
////        var img =
////            getImageFromFile(File("C:\\Users\\Administrator\\IdeaProjects\\intellij-sdk-code-samples\\untitled1\\tfres\\window\\test1111.png"))
////        for (x in 0 until img.width) {
////            for (y in 0 until img
////                .height) {
////                if (!colorCompare(Config.Color_ChuangZhang, Color(img.getRGB(x, y)))) {
////                    img.setRGB(x, y, Color.WHITE.rgb)
////                }
////            }
////        }
////
////
////        ImageIO.write(img, "png", File(App.caijiPath, "${System.currentTimeMillis()}.png"))
//        var maxIndex = -1
//        var maxRate = 0f
//        carps.forEachIndexed { index, carPosition ->
//
////            var level = carPosition.getTestStarLevel(img)
////            log("position $index star is $level")
////            var isSelect = carPosition.isSelectByChuanzhang(img)
////            log("position $index isSelectByChuanzhang $isSelect")
//            var rate = carPosition.rateSelectByChuanZhang(img)
//            if (rate > maxRate) {
//                maxRate = rate
//                maxIndex = index
//            }
//        }
//        log("最符合的车位是$maxIndex 其比例为:$maxRate")
//    }

    private fun isColorWhite(c1: Color, c2: Color = Color.white): Boolean {
        return (abs(c1.red - c2.red) <= 20
                && abs(c1.green - c2.green) <= 20
                && abs(c1.blue - c2.blue) <= 20)
    }

    var mainData:MutableState<ArrayList<HeroBean?>>?=null
    fun attchToMain(){
        mainData = MainData.heros
    }
    private fun sysDataToMain(){
        mainData?.value = arrayListOf<HeroBean?>().apply {
            addAll(heroList.map {
                it?.copy()?.apply {
                    currentLevel = it.currentLevel
                }
            })
        }
//        log("sysDataToMain")
    }
}