package nouse//import org.bytedeco.leptonica.global.lept
//import org.bytedeco.tesseract.TessBaseAPI
//import org.jetbrains.skiko.toBitmap
//import java.io.File
//import javax.imageio.ImageIO
//
//object JaveCVKt {
//
//
//    fun OCR(lng: String?, imagePath: String?): String? {
//        val dataPath = File("C:\\Users\\Administrator\\Desktop\\tfpic").path
//        val api = TessBaseAPI()
//        if (api.Init(dataPath, lng) != 0) {
//            println("error")
//        }
//        var file = File("C:\\Users\\Administrator\\Desktop\\tfpic\\img111.png")
//        val image = lept.pixRead(file.absolutePath) ?: return ""
//
//        api.SetImage(image)
//        val outText = api.GetUTF8Text()
//        val result = outText.string
//        api.End()
//        outText.deallocate()
//        lept.pixDestroy(image)
//        return result
//    }
//
//    fun saveGUan() {
//        var imgpath: File =
//            File("C:\\Users\\Administrator\\IdeaProjects\\intellij-sdk-code-samples\\untitled1\\build\\resources\\main\\tftest.png")
//
//        var img = data.Config.guankaRect2.run {
//            ImageIO.read(imgpath).getSubimage(left, top, width, height)
//        }
//
//        var file = File("C:\\Users\\Administrator\\Desktop\\tfpic\\img111.png")
//        var result = ImageIO.write(img, "png", file)
//        println("保存图片 $result")
//
//    }
//}