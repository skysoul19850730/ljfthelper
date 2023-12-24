package tasks.guankatask

import data.Config
import getImage
import getImageFromFile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import log
import resFile
import tasks.Guanka
import utils.ImgUtil
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

class GuankaTask {

    var currentGuanIndex = 0

    var changeListener: ChangeListener? = null

    var running = false

    var imgList = arrayListOf<BufferedImage>().apply {
        var suFolder =resFile("${Config.platName}/guanka/gf1")
        suFolder.listFiles().sortedBy {
            it.name.substring(1,it.name.length-4).toInt()
        }.forEach {
            add(getImageFromFile(it))
        }
    }
    interface ChangeListener {
        fun onGuanChange(guan: Int)
    }



    fun start() {
        if (running) return
        running = true
        GlobalScope.launch {
            while (running) {

                var curImg = getImage(Config.zhandou_hezuo_guankaRect)
                var has = false
                for(i in currentGuanIndex..min(currentGuanIndex+10,imgList.size-1)){
                    if(ImgUtil.isImageSim(curImg,imgList.get(i),0.98)){
                        if(currentGuanIndex != i){
                            currentGuanIndex = i
                            log("当前关卡：${currentGuanIndex+1}")
                            has = true
                            changeListener?.onGuanChange(currentGuanIndex+1)
                            break
                        }
                    }
                }
                if(!has){
//                    log("未识别到关卡")
//                    log(curImg)
                }

                delay(1000)
            }
        }
    }

    fun stop() {
        running = false
    }
}