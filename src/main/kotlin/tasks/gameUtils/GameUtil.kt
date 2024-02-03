package tasks.gameUtils

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import log

object GameUtil {
    var ShuaMoValue = mutableStateOf(false)
    var shuamoHeroDoing :DaMoHeroDoing?=null

    fun startShuaMo(type:Int = 0) {
        log("准备刷 ${if(type==0)"魔" else "木"}")
        shuamoHeroDoing?.stop()
        shuamoHeroDoing = DaMoHeroDoing(type)
        shuamoHeroDoing?.init()
        shuamoHeroDoing?.start()
    }
    fun stopShuaMo(){
        shuamoHeroDoing?.stop()
    }

}