package tasks.huodong

import androidx.compose.runtime.mutableStateOf
import tasks.HeroDoing

object HuodongUtil {
    var state = mutableStateOf(false)
    var shuamoHeroDoing :HeroDoing?=null

    fun start(model:Int) {
        state.value = true
        shuamoHeroDoing?.stop()
        shuamoHeroDoing =if(model==1) HuodongHeroDoing() else EasyHeroDoing()
        shuamoHeroDoing?.init()
        shuamoHeroDoing?.start()
    }
    fun stop(){
        state.value = false
        shuamoHeroDoing?.stop()
    }

}