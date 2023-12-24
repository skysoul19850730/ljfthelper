package tasks.normal

import data.Config
import data.HeroBean
import data.MRect
import getImage
import kotlinx.coroutines.*
import logOnly
import tasks.Hero
import kotlin.coroutines.resume

class TaskManager {


    var allTask: ArrayList<NormalTask> = arrayListOf()

    var runningTask: ArrayList<NormalTask> = arrayListOf()

    var curHeroList: List<HeroBean?>? = null

    var running = false

    lateinit var heros: List<HeroBean>

    fun init() {
        //初始化heros，解析tasks等

        GlobalScope.launch {

            curHeroList = getPreHeros(10000)

        }

    }

    fun start() {
        if (running) return
        running = true
        GlobalScope.launch {
            curHeroList = getPreHeros(10000)

            while(allTask.isNotEmpty()){



            }
        }
    }

    suspend fun getCurHeroList4Choose(): ArrayList<HeroBean>? {

        return null
    }


    private suspend fun getPreHeros(timeout: Long = 2300) = suspendCancellableCoroutine<List<HeroBean?>?> {
        val startTime = System.currentTimeMillis()
        GlobalScope.launch {
            var h1: HeroBean? = null
            var h2: HeroBean? = null
            var h3: HeroBean? = null

            try {
                withTimeout(timeout) {
                    while (h1 == null || h2 == null || h3 == null) {
                        if (!running) {
                            break
                        }
                        val hero1 = if (h1 == null) {
                            async { getHeroAtRect(Config.zhandou_hero1CheckRect) }
                        } else null
                        val hero2 = if (h2 == null) {
                            async { getHeroAtRect(Config.zhandou_hero2CheckRect) }
                        } else null
                        val hero3 = if (h3 == null) {
                            async { getHeroAtRect(Config.zhandou_hero3CheckRect) }
                        } else null

                        if (h1 == null) {
                            h1 = hero1?.await()
                        }
                        if (h2 == null) {
                            h2 = hero2?.await()
                        }
                        if (h3 == null) {
                            h3 = hero3?.await()
                        }
                        if (h1 == null || h2 == null || h3 == null) {//省去最后的100ms
                            delay(100)
                        }
                    }
                    logOnly("getPreHeros cost time:${System.currentTimeMillis() - startTime}")
                    it.resume(arrayListOf(h1, h2, h3))
                }
            } catch (e: Exception) {
                if (h1 == null && h2 == null && h3 == null) {
                    it.resume(null)
                } else {
                    it.resume(arrayListOf(h1, h2, h3))
                }
            }
        }
    }

    private suspend fun getHeroAtRect(rect: MRect) = suspendCancellableCoroutine<HeroBean?> {
        val img = getImage(rect)
        val hero = heros.firstOrNull {
//            ImgUtil.isImageSim(img, it.img)
//            ImgUtil.isHeroSim(img,it.img)
            it.fitImage(img)
        }
        it.resume(hero)
//        log(img)
        logOnly("getHeroAtRect ${hero?.heroName ?: "无结果"}")
    }

}