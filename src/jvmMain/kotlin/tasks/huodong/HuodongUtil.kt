package tasks.huodong

import androidx.compose.runtime.mutableStateOf

object HuodongUtil {
    var state = mutableStateOf(false)
    var shuamoHeroDoing :HuodongHeroDoing?=null

    fun start() {
        state.value = true
        shuamoHeroDoing?.stop()
        shuamoHeroDoing = HuodongHeroDoing()
        shuamoHeroDoing?.init()
        shuamoHeroDoing?.start()
    }
    fun stop(){
        state.value = false
        shuamoHeroDoing?.stop()
    }

}