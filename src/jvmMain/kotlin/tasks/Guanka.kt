package tasks

import data.Config
import getImage
import getImageFromRes
import kotlinx.coroutines.*
import log
import utils.ImgUtil
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object Guanka {

    interface ChangeListener {
        fun onGuanChange(guan: Int)
    }

    private var running = false
    private var shibiPinlv = 3000L
    var caijiing = false
    private val caijiPinlv = 1000L
    val caijiPath = Config.caiji_main_path + "\\guanka"
    val maxGk = 309
    val listeners = arrayListOf<ChangeListener>()

    var currectCaijiSubPath = ""

    fun init() {

    }

    fun addListener(listener: ChangeListener) {
        listeners.add(listener)
    }

    fun startCaiji() {
        if (caijiing) return
        caijiing = true
        currectCaijiSubPath = "${System.currentTimeMillis()}"
        val file = File(caijiPath, currectCaijiSubPath)
        file.mkdirs()
        var lastImg: BufferedImage? = null
        GlobalScope.launch {
            while (caijiing) {
                var img = getImage(Config.zhandou_hezuo_guankaRect)
                if (!ImgUtil.isImageSim(img, lastImg, 0.99)) {
                    log("获取了一张图片:新图，已存")
                    lastImg = img
                    ImageIO.write(img, "png", File(file, "${System.currentTimeMillis()}.png"))
                    log(img)

                } else {
                    log(("获取了一张图片:与上一张相同，废弃"))
                }
                delay(caijiPinlv)
            }
        }
    }


    fun stopCaiji() {
        caijiing = false
    }

    fun save() {
        val img = getImage(Config.zhandou_hezuo_guankaRect)
        ImageIO.write(img, "png", File(caijiPath, "${System.currentTimeMillis()}.png"))
        log(img)
    }


    var currectGuan = 0
//    fun start() {
//        if (running) return
//        running = true
//        val file = File(caijiPath, "caiji")
//        val files = file.listFiles()
//        currectGuan = 0
//        var img = ImageIO.read(files[currectGuan])
//        var cannot = 0
//        GlobalScope.launch {
//            while (running) {
//                if (img != null) {
//                    if (ImgUtil.isImageInRect(img, Config.zhandou_hezuo_guankaRect)) {
//                        addGuan()
//                        log("识别到了关卡：$currectGuan")
//                        img = ImageIO.read(files[currectGuan])
//                    } else {
//                        cannot++//识别不到就++
//                        if (cannot > 4) {
//                            var zhuigan = true
//                            while(zhuigan){
//                                addGuan()
//                                val tmpImg = ImageIO.read(files[currectGuan])
//                                if(ImgUtil.isImageInRect(tmpImg, Config.zhandou_hezuo_guankaRect)){
//                                    addGuan()
//                                    addGuan()
//                                    img = ImageIO.read(files[currectGuan])
//                                    zhuigan=false
//                                    cannot = 0
//                                }
//                            }
//                        }
//                    }
//                }
//                if(currectGuan == maxGk){
//                    stop()
//                }
//                delay(shibiPinlv)
//            }
//        }
//    }

//    private fun addGuan(){
//        currectGuan++
//        listeners.forEach {
//            it.onGuanChange(currectGuan)
//        }
//    }

    fun stop() {
        running = false
    }


}