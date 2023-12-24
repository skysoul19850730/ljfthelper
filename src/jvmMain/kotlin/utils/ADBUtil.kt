package utils

//import com.android.ddmlib.AndroidDebugBridge
//import com.android.ddmlib.IDevice
//import com.android.ddmlib.RawImage
import data.MPoint
import kotlinx.coroutines.delay
import java.awt.image.BufferedImage

object ADBUtil {

//    val runtime = Runtime.getRuntime()

//    fun jietu() {
//        try {
//
//            Runtime.getRuntime().exec("cmd /c adb shell input keyevent 3")
//        } catch (e: Exception) {
//            println("e ${e.message}")
//        }
//    }

    fun tap(point: MPoint) {
        try {
            Runtime.getRuntime().exec("cmd /c adb shell input tap ${point.x} ${point.y}").waitFor()
        } catch (e: Exception) {
            println("e ${e.message}")
        }
    }

    fun inputText(text: String) {
        try {
            Runtime.getRuntime().exec("cmd /c adb shell input text ${text}").waitFor()
        } catch (e: Exception) {
            println("e ${e.message}")
        }
    }

//var mdevice:IDevice?=null
//    private fun getDevice(): IDevice?{
//        AndroidDebugBridge.init(false)
//        val bridge = AndroidDebugBridge.createBridge("C:\\Users\\Administrator\\AppData\\Local\\Android\\Sdk\\platform-tools\\adb",false)
//        while(!bridge.hasInitialDeviceList()){
//            Thread.sleep(100)
//        }
//        return bridge.devices.getOrNull(0)
//    }
//
//    fun time() = System.currentTimeMillis()
//
//    fun getScreenShot():BufferedImage?{
//        println("start ${time()}")
//        var rawImg : RawImage? = try {
//            mdevice = mdevice?: getDevice()
//            println("getdevice ${time()}")
//            mdevice?.screenshot
//        }catch (e:java.lang.Exception){
//            println(" getScreenShot error :${e.message}")
//            null
//        }
//        rawImg?:return null
//        println("getscreen ${time()}")
//
//        var result = BufferedImage(rawImg.width,rawImg.height,BufferedImage.TYPE_INT_RGB)
//
//        var index = 0
//        val inc: Int = rawImg.bpp shr 3
//        for (y in 0 until rawImg.height) {
//            var x = 0
//            while (x < rawImg.width) {
//                val value: Int = rawImg.getARGB(index)
//                result.setRGB(x,y,value)
//                x++
//                index += inc
//
//            }
//        }
//        println("change to bufimg ${time()}")
//        return result
//    }

}