package tasks.hanbing.mengyan

import androidx.compose.material.Card
import androidx.compose.runtime.mutableStateOf
import data.HeroBean
import getImageFromFile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tasks.CarDoing
import tasks.CarDoing.Companion.CheType_MaChe
import tasks.CarDoing.Companion.CheType_YangChe
import java.io.File

object ChuanZhangTest {


    fun startChuanZhangOberserver() {
        GlobalScope.launch {

//            var car0 = CarDoing(0,CheType_MaChe).apply {
//                initPositions()
//            }
//            var car1 = CarDoing(1,CheType_YangChe).apply {
//                initPositions()
//            }
//            var img = getImageFromFile(File(App.caijiPath, "1699936572466.png"))
//            car0.getChuanZhangMax(img)
//            car1.getChuanZhangMax(img)

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
}