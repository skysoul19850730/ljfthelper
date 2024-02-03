package tasks.huodong

import App
import data.HeroBean
import data.Recognize
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import log
import model.CarDoing
import tasks.HeroDoing
import java.awt.event.KeyEvent

class EasyHeroDoing : HeroDoing(0), App.KeyListener {


    override fun initHeroes() {
        heros = arrayListOf<HeroBean>().apply {
            add(HeroBean("wugui"))
            add(HeroBean("bingnv", weightModel = 0))
            add(HeroBean("dapao", isGongCheng = true))
            add(HeroBean("jiaonv"))
            add(HeroBean("saman2"))
            add(HeroBean("nvwang"))


            add(HeroBean("guangqiu", needCar = false))
            add(HeroBean("niutou"))

            add(HeroBean("shengqi"))
            add(HeroBean("xiaochou"))

        }
        mOffset = -40
    }


    override suspend fun afterHeroClick(heroBean: HeroBean) {
        super.afterHeroClick(heroBean)
    }

    override suspend fun dealHero(heros: List<HeroBean?>): Int {


        var result: Int = -1
        this.heros.take(8).forEach {
            var index = heros.indexOf(it)
            if (index > -1) {
                if (it.weightModel == 1) {
                    return index
                } else if (it.weightModel == 0) {
                    if (!it.isInCar()) {
                        return index
                    }
                    if (result < 0) {
                        result = index
                    }
                } else if (it.weightModel == 2) {
                    if (!it.isFull()) {
                        return index
                    }
                }
            }


        }

        if(result == -1){

        }

        return result
    }

    override fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean? {
        var lastIndex =-1
        var lastHero :HeroBean?=null
        var ttIndex = this.heros.indexOf(heroBean)
        carDoing.carps.forEachIndexed { index, carPosition ->
            if(carPosition.hasHero()){
                var hero = carPosition.mHeroBean!!
                var aaindex = this.heros.indexOf(hero)
                if(aaindex>lastIndex && aaindex>ttIndex){
                    lastIndex = aaindex
                    lastHero = hero
                }
            }
        }
        return lastHero
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    suspend fun doOnKeyDown(code: Int): Boolean {
        var handle = true

        return handle
    }

    override suspend fun onKeyDown(code: Int): Boolean {
        return doOnKeyDown(code)
    }

}