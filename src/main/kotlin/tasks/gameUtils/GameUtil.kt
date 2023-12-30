package tasks.gameUtils

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object GameUtil {
    var ShuaMoValue = mutableStateOf(false)
    var shuamoHeroDoing :DaMoHeroDoing?=null

    fun startShuaMo(type:Int = 0) {
        shuamoHeroDoing?.stop()
        shuamoHeroDoing = DaMoHeroDoing(type)
        shuamoHeroDoing?.init()
        shuamoHeroDoing?.start()
    }
    fun stopShuaMo(){
        shuamoHeroDoing?.stop()
    }

}