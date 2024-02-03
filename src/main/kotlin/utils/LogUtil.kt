package utils

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import data.Config
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import saveTo
import java.awt.image.BufferedImage
import java.io.File
import java.text.SimpleDateFormat

object LogUtil {
    val logDirPath = Config.caiji_main_path + File.separator + "logs"
    val messages = mutableStateListOf<Any>()


    fun saveAndClear() {
        var logDir = File(logDirPath, logChildDirWithModel())
        if(!logDir.exists()){
            logDir.mkdirs()
        }
        var data = SimpleDateFormat("yy_MM_dd").format(System.currentTimeMillis())
        logDir = File(logDir.path,data)
        if(!logDir.exists()){
            logDir.mkdirs()
        }
        logDir = File(logDir.path,SimpleDateFormat("hh_mm_ss").format(System.currentTimeMillis()))
        if(!logDir.exists()){
            logDir.mkdirs()
        }

        var logFile = File(logDir.path,"日志.txt")
        if(!logFile.exists()){
            logFile.createNewFile()
        }

        arrayListOf<Any>().also {
            it.addAll(messages)
        }.forEach {
            if(it is LogData){
                if(it.data is String){
                    logFile.appendText("${it.time} ${it.data}\n")
                }else if(it.data is BufferedImage){
                    logFile.appendText("${it.time} 图片：${it.data?.hashCode()}\n")
                    (it.data as BufferedImage).saveTo(File(logDir.path,"${it.data?.hashCode()}.png"))
                }
            }
        }
        MainScope().launch {
            messages.clear()
        }

    }

    private fun logChildDirWithModel(): String {
        if (App.mLaunchModel and App.model_duizhan != 0) {
            return "duizhan"
        }
        if (App.mLaunchModel and App.model_hezuo != 0) {
            return "hezuo"
        }
        if (App.mLaunchModel and App.model_hanbing != 0) {
            return "hanbing"
        }
        return "others"
    }

    class LogData {
        var time: String = ""
        var data: Any? = null
        var color:Color?=null
    }
}