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

class EasyHeroDoing : HeroDoing(0, FLAG_KEYEVENT), App.KeyListener {


    override fun initHeroes() {
        heros = arrayListOf<HeroBean>().apply {
            add(HeroBean("nvwang"))
            add(HeroBean("bingnv", weightModel = 0))
            add(HeroBean("wugui"))
            add(HeroBean("dapao", isGongCheng = true))
            add(HeroBean("shengqi", weightModel = 0))
            add(HeroBean("jiaonv"))
            add(HeroBean("saman2"))
            add(HeroBean("guangqiu", needCar = false))

            add(HeroBean("xiaochou"))
            add(HeroBean("niutou"))


        }
        carPosOffset = -50
    }


    override suspend fun afterHeroClick(heroBean: HeroBean) {
        super.afterHeroClick(heroBean)
    }

    override suspend fun dealHero(heros: List<HeroBean?>): Int {

        var nvwang = this.heros.find { it.heroName == "nvwang" }!!
        if (!nvwang.isFull()) {
            //女王没满，最多就上冰女和萨满，其他都不上，女王满了再上其他
            var index = heros.indexOf(nvwang)
            if (index > -1) {
                return index
            }
            index = heros.indexOfFirst { it?.heroName == "guangqiu" }
            if (index > -1 && nvwang.isInCar()) {
                carDoing.carps.forEach {
                    if (it.mHeroBean != null && it.mHeroBean?.heroName != "nvwang" && it.mHeroBean?.isFull() != true) {
                        carDoing.downPosition(it.mPos)
                    }
                }
                return index
            }
            index = heros.indexOfFirst { it?.heroName == "bingnv" }
            if (index > -1) {
                return index
            }
            index = heros.indexOfFirst { it?.heroName == "saman2" }
            if (index > -1) {
                return index
            }
        } else {
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
                        if (!it.isInCar()) {
                            return index
                        }
                        if (result < 0 && it.currentLevel < 3) {
                            result = index
                        }
                    }
                }


            }
            return result
        }

        return -1
    }

    override fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean? {
        var lastIndex = -1
        var lastHero: HeroBean? = null
        var ttIndex = this.heros.indexOf(heroBean)
        carDoing.carps.forEachIndexed { index, carPosition ->
            if (carPosition.hasHero()) {
                var hero = carPosition.mHeroBean!!
                var aaindex = this.heros.indexOf(hero)
                if (aaindex > lastIndex && aaindex > ttIndex) {
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
        if (code == KeyEvent.VK_NUMPAD1) {
            //副卡满了女王按1下这边女王
            var hero = heros.find { it.heroName == "nvwang" }!!
            if (hero.isFull()) {
                hero.weightModel == 2
                carDoing.downHero(hero)
            }
        }
        var handle = true

        return handle
    }

    override suspend fun onKeyDown(code: Int): Boolean {
        return doOnKeyDown(code)
    }

}