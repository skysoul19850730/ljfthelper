package tasks.huodong

import data.HeroBean
import kotlinx.coroutines.*
import log
import tasks.HeroDoing
import tasks.Zhuangbei
import tasks.hanbing.zhanjiang.HBZhanNvHeroDoing2
import java.awt.event.KeyEvent

class XiePaoHeroDoing : HeroDoing(0, FLAG_GUANKA) {

    val gugong = HeroBean("gugong")//
    val xie = HeroBean("xie")
    val bingnv = HeroBean("bingnv")
    val nvyao = HeroBean("nvyao")
    val saman2 = HeroBean("saman2")
    val jiaonv = HeroBean("jiaonv")//
    val lvgong = HeroBean("lvgong")//
    val haiyao = HeroBean("haiyao")//
    val moqiu = HeroBean("moqiu", needCar = false)
    val dapao = HeroBean("dapao", isGongCheng = true)

    override fun initHeroes() {
        heros = arrayListOf<HeroBean>().apply {
            add(gugong)
            add(xie)
            add(bingnv)
            add(nvyao)
            add(saman2)
            add(jiaonv)
            add(lvgong)
            add(haiyao)
            add(moqiu)
            add(dapao)
        }
    }

    override suspend fun afterHeroClick(heroBean: HeroBean) {
        super.afterHeroClick(heroBean)
    }

    override suspend fun doAfterHeroBeforeWaiting(heroBean: HeroBean) {
        if (!isFull) {
            var list = arrayListOf<HeroBean>().apply {
                add(dapao)
                add(bingnv)
                add(xie)
                add(gugong)
                add(haiyao)
                add(lvgong)
                add(saman2)
            }
            if (list.all {
                    it.isFull()
                }) {
                isFull = true
            }
        }
        waiting = false
    }

    var isFull = false
    var lastMo = System.currentTimeMillis() + 30 * 1000
    override suspend fun dealHero(heros: List<HeroBean?>): Int {
        var index = heros.indexOf(moqiu)
        var time = System.currentTimeMillis()
        var df = time - lastMo
        var curGuan = guankaTask?.currentGuanIndex ?: 0
        if (curGuan != 99) {
            if (df > 5500) {//超过5秒打魔
                lastMo = System.currentTimeMillis()
                return index
            } else if (df > 4000 || isFull) {//如果没满，且还有时间，那么就继续上卡。
                delay(5500 - df)
                lastMo = System.currentTimeMillis()
                return index
            }
        }


        var list = arrayListOf<HeroBean>().apply {
            add(dapao)
            add(bingnv)
            add(xie)
            add(gugong)
            add(haiyao)
            add(lvgong)
            add(saman2)
        }
        index = defaultDealHero(heros, list)
        if (index > -1) {
            return index
        }

        return -1
    }

    override fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean? {
        return null
    }

}