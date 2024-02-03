package tasks.hanbing.mengyan

import getImage
import getImageFromFile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import log
import loges
import model.CarDoing
import model.CarDoing.Companion.CheType_MaChe
import model.CarDoing.Companion.CheType_YangChe
import java.io.File
import kotlin.math.abs

object ChuanZhangTest {


    fun startChuanZhangOberserver() {
        GlobalScope.launch {

            var car0 = CarDoing(0, CheType_YangChe).apply {
                initPositions()
            }
            var car1 = CarDoing(0, CheType_YangChe).apply {
                initPositions()
                chePosition=1
                reInitPositions()
            }
//            File(App.caijiPath, "chuanzhang").listFiles().forEach {
//                if(it.name.startsWith("1699936572466")) {
//                    var img = getImageFromFile(it)
                   var img = getImage(App.rectWindow, null)
                    log(img)
//                var count1 = 0
//                var count2 = 0
//                    MRect.createWH(0,0,176,img.height).forEach{x,y->
//                        if(colorCompare(Color(img.getRGB(x,y)),Config.Color_ChuangZhang,10)){
//                            count1++
//                        }
//                    }
//                    MRect.createWH(177,0,176,img.height).forEach{x,y->
//                        if(colorCompare(Color(img.getRGB(x,y)),Config.Color_ChuangZhang,10)){
//                            count2++
//                        }
//                    }
//                log("count1 $count1   count2 $count2")

                    var index = car0.getChuanZhangMax(img)
                    var index2 = car1.getChuanZhangMax(img)
                    if (index != null && (index2 == null || index.second > index2.second)) {
                        loges("检测结果 车位 0  位置 ${index.first} rate  ${index.second}")
                    } else if (index2 != null && (index == null || index2.second > index.second)) {
                        loges("检测结果 车位 1 位置 ${index2.first} rate ${index2.second}")
                    }
//                }
//            }


//            var p = 0
//            var carDoing = CarDoing(p, CheType_MaChe)
//            carDoing.initPositions()
//            var img = getImageFromFile(File(App.caijiPath, "1699951855366.png"))
//            carDoing.getChuanZhangMax(img)
//            img = getImageFromFile(File(App.caijiPath, "1699935515650.png"))
//            carDoing.getChuanZhangMax(img)
//            img = getImageFromFile(File(App.caijiPath, "test2.png"))
//            carDoing.getChuanZhangMax(img)
//            delay(50)
//
//
//            p = 1
//            carDoing = CarDoing(p, CheType_MaChe)
//            carDoing.initPositions()
//            img = getImageFromFile(File(App.caijiPath, "1699936572466.png"))
//            carDoing.getChuanZhangMax(img)
//            img = getImageFromFile(File(App.caijiPath, "1699936545563.png"))
//            carDoing.getChuanZhangMax(img)
//            delay(50)
        }
    }

    private fun colorCompare(c1: java.awt.Color, c2: java.awt.Color, sim: Int = 10): Boolean {
        return (abs(c1.red - c2.red) <= sim
                && abs(c1.green - c2.green) <= sim
                && abs(c1.blue - c2.blue) <= sim)
    }
}