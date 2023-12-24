package tasks.duizhan.faqiang

import data.HeroBean
import kotlinx.coroutines.delay
import tasks.HeroDoing

class FaQiangHeroDoing : HeroDoing(-1) {

    lateinit var leishen: HeroBean
    lateinit var yanmo: HeroBean
    lateinit var dianfa: HeroBean
    lateinit var huoqiang: HeroBean
    lateinit var leiqiu: HeroBean
    lateinit var moqiu: HeroBean
    lateinit var houyi: HeroBean
    lateinit var gugong: HeroBean
    lateinit var tuqiu: HeroBean
    lateinit var xiaolu: HeroBean

    init {
//        if(Config.platform.value == Config.platform_moniqi){
        leishen = HeroBean("leishen", 100)
        yanmo = HeroBean("yanmo", 90)
        dianfa = HeroBean("dianfa", 80)
        huoqiang = HeroBean("huoqiang", 70)
        leiqiu = HeroBean("leiqiu", 60, needCar = false)
        moqiu = HeroBean("moqiu", 50, needCar = false)
        houyi = HeroBean("houyi", 40)
        gugong = HeroBean("gugong", 30)
        tuqiu = HeroBean("tuqiu", 0, needCar = false)
        xiaolu = HeroBean("xiaolu", 0)
//        }else{
//            leishen = HeroBean("leishenxcx", 100)
//            yanmo = HeroBean("yanmoxcx", 90)
//            dianfa = HeroBean("dianfaxcx", 80)
//            huoqiang = HeroBean("huoqiangxcx", 70)
//            leiqiu = HeroBean("leiqiuxcx", 60, needCar = false)
//            moqiu = HeroBean("moqiuxcx", 50, needCar = false)
//            houyi = HeroBean("houyixcx", 40)
//            gugong = HeroBean("gugongxcx", 30)
//            tuqiu = HeroBean("tuqiuxcx", 20, needCar = false)
//            xiaolu = HeroBean("xiaoluxcx", 0)
//        }
    }

    override fun initHeroes() {
        heros = arrayListOf()
        heros.add(leishen)
        heros.add(yanmo)
        heros.add(dianfa)
        heros.add(huoqiang)
        heros.add(xiaolu)
        heros.add(leiqiu)
        heros.add(moqiu)
        heros.add(houyi)
        heros.add(gugong)
        heros.add(tuqiu)
    }

    override suspend fun afterHeroClick(heroBean: HeroBean) {
        if (heroBean == moqiu) {
            delay(3000)
        } else if (heroBean == xiaolu) {
            carDoing.downHero(heroBean)
        }
    }

    override fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean? {
        var hs = arrayListOf<HeroBean>().apply { addAll(heros.filter { it.isInCar() }) }
        hs.sortBy { it.weight }
        var h = hs.firstOrNull()
        var w = h?.weight ?: 10000
        if (w < heroBean.weight) {
            if (h == leishen || h == dianfa || h == yanmo) {//这些都不用换，属于前三得英雄
                return null
            }
            return h!!
        }
        return null
    }

    override suspend fun dealHero(heros: List<HeroBean?>): Int {

        var list = arrayListOf<HeroBean?>()
        list.addAll(heros.filter { it != null })
        if (list.isEmpty()) return -1

        var hero: HeroBean? = null

        while (list.size > 0) {

            var h = list.removeAt(0)

            if (h == tuqiu) {
//                val carHerosCount = this.heros.filter { it.isInCar() }.size
//                if (carHerosCount >= 6) {//开满了
                    continue
//                }
            }

            if (h == xiaolu) {//小鹿前期需求大，超过4格后 作用变小
                if (heroCountInCar() >= 6) {
                    continue
                }
                if (heroCountInCar() < 3) {//小于3时，比火枪weight高
                    h.weight = 75
                } else if (heroCountInCar() < 4) {
                    h.weight = 65
                } else {
                    h.weight = 25
                }
            }

            if (dianfa.isInCar() && yanmo.isInCar() && leishen.isInCar()) {//三法都上了之后，重要度为 雷神 火枪 炎魔
                huoqiang.weight = 95
            }

            if (h == gugong && heroCountInCar() < 5) {//骨弓最后上
                continue
            }
            if (h == houyi && heroCountInCar() < 4) {//骨弓最后上
                continue
            }


            if (hero == null || (!h!!.isInCar() && h.weight > hero.weight)) {
                hero = h
            } else {
                if ((h.isInCar() && hero.isInCar()) || (!h.isInCar() && !hero.isInCar())) {
                    //都在车或都不在车上时，比 weight
                    if (h.weight > hero.weight) {
                        hero = h
                    }
                } else if (!h.isInCar()) {
                    hero = h
                }
            }
        }

        hero ?: return -1

        var result: HeroBean = hero!!

        return heros.indexOf(result)
    }

}