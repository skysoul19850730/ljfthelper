package tesshelper

import data.Config
import net.sourceforge.tess4j.Tesseract
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File

object Tess {

    //    val resPath = "C:/Users/Administrator/IdeaProjects/intellij-sdk-code-samples/untitled1/build/resources/main"
    val resPath
        get() =if(Config.isHome) "${Config.appRootPath}build${File.separator}processedResources${File.separator}jvm${File.separator}main"
            else "${Config.appRootPath}build${File.separator}resources${File.separator}main${File.separator}"
//    else  "C:/Users/Administrator/IdeaProjects/intellij-sdk-code-samples/untitled1/build/resources/main"

    val api = Tesseract().apply {
        setDatapath(resPath)
        setLanguage("chi_sim")
    }

    val toWidth = 220

    fun getText(img: BufferedImage): String {

//        var fImg =if(img.width<toWidth){
//            val toW = toWidth
//            val toH = ((img.height*1f/img.width)*toW).toInt()
//            BufferedImage(toW, toH,BufferedImage.TYPE_BYTE_BINARY).apply {
//                graphics.drawImage(img,0,0,toW,toH,null)
//            }
//        }else{
//            img
//        }
        return api.doOCR(img)
    }
}