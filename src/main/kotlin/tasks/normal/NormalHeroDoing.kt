package tasks.normal

import data.HeroBean
import tasks.HeroDoing

class NormalHeroDoing : HeroDoing(-1) {

    init {
    }

    override fun initHeroes() {
        heros = arrayListOf()
    }

    override suspend fun afterHeroClick(heroBean: HeroBean) {
    }

    override fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean? {
        return null
    }

    override suspend fun dealHero(heros: List<HeroBean?>): Int {

        var list = arrayListOf<HeroBean?>()
        list.addAll(heros.filter { it != null })
        if (list.isEmpty()) return -1

        var hero: HeroBean? = null

        while (list.size > 0) {

            var h = list.removeAt(0)

        }

        hero ?: return -1

        var result: HeroBean = hero!!

        return heros.indexOf(result)
    }

}