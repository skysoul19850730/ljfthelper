package tasks

import androidx.compose.runtime.mutableStateListOf
import data.Config
import data.Config.delayLong
import data.Config.delayNor
import data.Config.zhandou_hero1CheckRect
import data.Config.zhandou_hero2CheckRect
import data.Config.zhandou_hero3CheckRect
import data.HeroBean
import data.MRect
import getImage
import kotlinx.coroutines.*
import log
import logOnly
import utils.ImgUtil
import utils.MRobot
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.coroutines.resume

object Hero : Guanka.ChangeListener {

    private val caijiPath = Config.caiji_main_path + "\\hero"

    private val caijiList = arrayListOf<BufferedImage>()

    fun init() {
        caijiList.clear()
    }
    var caijianIng = false
    var file1:File?=null
    var file2:File?=null
    var file3:File?=null
    fun aotuCaiji(){
        GlobalScope.launch {
            caijianIng = false
            delay(delayNor)
            Config.zhandou_shuaxinPoint.click()
            file1 = File(caijiPath,"${System.currentTimeMillis()}f1")
            file2 = File(caijiPath,"${System.currentTimeMillis()}f2")
            file3 = File(caijiPath,"${System.currentTimeMillis()}f3")
            file1?.mkdirs()
            file2?.mkdirs()
            file3?.mkdirs()

            caijianIng = true
            while (caijianIng){
                save()
            }
        }
    }

    fun save() {
        var img = getImage(Config.zhandou_hero1CheckRect)
        if (isImgExist(img)) {
            logOnly("已有该hero position: 1")
        } else {
            caijiList.add(img)
            ImageIO.write(img, "png", File(file1, "${System.currentTimeMillis()}_1.png"))
            log(img)
        }
        img = getImage(Config.zhandou_hero2CheckRect)
        if (isImgExist(img)) {
            logOnly("已有该hero position: 2")
        } else {
            caijiList.add(img)
            ImageIO.write(img, "png", File(file2, "${System.currentTimeMillis()}_2.png"))
            log(img)
        }

        img = getImage(Config.zhandou_hero3CheckRect)
        if (isImgExist(img)) {
            logOnly("已有该hero position: 3")
        } else {
            caijiList.add(img)
            ImageIO.write(img, "png", File(file3, "${System.currentTimeMillis()}_3.png"))
            log(img)
        }
    }
    class FileParse{
        var file:File?=null
        var imgs= mutableStateListOf<File>()
    }
    var mHeroParse = mutableStateListOf<FileParse>()
    fun jiexiHeros(){
        var folders = File(caijiPath).listFiles().filter {
            it.isDirectory
        }
        var result = arrayListOf<FileParse>()

        folders.forEach {
            var imgs = it.listFiles()
            if(imgs.isEmpty()){
                it.delete()
            }else {
                val bean = FileParse()
                bean.file = it
                bean.imgs.addAll(imgs)
                result.add(bean)
            }
        }

        mHeroParse.clear()
        mHeroParse.addAll(result)
    }

    private fun isImgExist(img: BufferedImage): Boolean {
        caijiList.forEach {
            if (ImgUtil.isImageSim(img, it)) {
                return true
            }
        }
        return false
    }


    override fun onGuanChange(guan: Int) {

    }

}