import com.sun.jna.platform.win32.GDI32Util
import com.sun.jna.platform.win32.WinDef
import data.Config
import data.MRect
import tasks.WxUtil
import utils.MRobot
import utils.MRobot.houtai
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs

fun colorCompare(c1: java.awt.Color, c2: java.awt.Color, sim: Int = 10): Boolean {
    return (abs(c1.red - c2.red) <= sim
            && abs(c1.green - c2.green) <= sim
            && abs(c1.blue - c2.blue) <= sim)
}

fun getImage(rect: MRect,window: WinDef.HWND? = App.tfWindow): BufferedImage {
    var img2 =
        if (houtai && window!=null && window == WxUtil.wxWindow) {
            GDI32Util.getScreenshot(window).getSubImage(rect)
        } else
            MRobot.robot.createScreenCapture(Rectangle().apply {
                x = rect.left
                y = rect.top
                width = rect.right - rect.left + 1
                height = rect.bottom - rect.top + 1
            })
    return img2
}

fun BufferedImage.foreach(back:(Int,Int)->Boolean){
    for(x in 0 until width){
        for(y in 0 until height){
            if(back.invoke(x,y)){
                return
            }
        }
    }
}

fun BufferedImage.getSubImage(rect: MRect): BufferedImage {
    if(rect.width >= width || rect.height>=height){
        return this
    }
    return getSubimage(rect.left, rect.top, rect.width, rect.height)
}

fun BufferedImage.saveTo(file: File) {
    if(!file.parentFile.exists()){
        file.parentFile.mkdirs()
    }
    ImageIO.write(this, "png", file)
//    log(this)
}
fun doDebug(call:()->Unit){
    if(Config.debug){
        call.invoke()
    }
}

fun BufferedImage.saveSubTo(subRect: MRect, file: File) {
    getSubImage(subRect).saveTo(file)
}

fun MRect.saveImgTo(file: File) {
    getImage(this).saveTo(file)
}

fun getImageFromRes(name: String): BufferedImage {
    var loader = Thread.currentThread().contextClassLoader!!
    return ImageIO.read(loader.getResourceAsStream(name))
}

fun resFile(fileName: String): File {
    var loader = Thread.currentThread().contextClassLoader!!
    return File(loader.getResource(fileName).file)
}

fun getImageFromFile(file: File): BufferedImage {
    return ImageIO.read(file)
}

fun File.rename(text: String): File {
    var f = File(parent, text)
    renameTo(f)
    return f
}



