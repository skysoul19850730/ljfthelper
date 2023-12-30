package tasks.hanbing

import data.Config
import data.MPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tasks.HeroDoing
import tasks.guankatask.GuankaTask

abstract class BaseHBHeroDoing(chePosition:Int):HeroDoing(chePosition),App.KeyListener {

    var waiting = false

    var guankaTask = GuankaTask().apply {
        changeListener = object : GuankaTask.ChangeListener {
            override fun onGuanChange(guan: Int) {
                doOnGuanChanged(guan)
            }
        }
    }

    abstract fun doOnGuanChanged(guan:Int)

    /**
     * 龙王点名了 与config中的龙王云point对比
     * 看看下什么卡
     * 如何下了卡需要返回true
     */
    abstract fun onLongwangPoint(point:MPoint,downed:(Boolean)->Unit)


    var longwangObserver = false
    private fun startLongWangOberserver() {
        if (longwangObserver) return
        longwangObserver = true
        GlobalScope.launch {
            while (longwangObserver) {
                var needDown = false
                if (Config.hbFSCloud.isFit()) {
                    onLongwangPoint(Config.hbFSCloud){
                        waiting = it
                        needDown = it
                    }
                } else if (Config.hbMSCloud.isFit()) {
                    onLongwangPoint(Config.hbMSCloud){
                        waiting = it
                        needDown = it
                    }
                } else {
                    needDown = false
                }


                if (needDown) {
                    delay(10000)
                    waiting = false
                }
                delay(100)
            }
        }
    }
}