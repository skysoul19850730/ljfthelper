package tasks.huodong

import data.HeroBean
import kotlinx.coroutines.*
import log
import tasks.HeroDoing
import tasks.Zhuangbei
import tasks.hanbing.zhanjiang.HBZhanNvHeroDoing2
import java.awt.event.KeyEvent

class HuodongHeroDoing2 : HeroDoing(0, FLAG_GUANKA or FLAG_KEYEVENT) {

    enum class Guan {
        g0,
        g1,
        g79,
        g91,
        g101,
        g111,
        g131,
        g141,
        g151,
        g161,
        g171,
    }

    var guanka = Guan.g0

    val shan = HeroBean("shan")//
    val mengyan = HeroBean("mengyan")
    val kuiqian = HeroBean("kuiqian")
    val kui = HeroBean("kui")
    val daoke = HeroBean("daoke")
    val efei = HeroBean("efei")//
    val gongjiang = HeroBean("gongjiang")//
    val ganglie = HeroBean("ganglie")//
    val huanqiu = HeroBean("huanqiu", needCar = false)
    val maomi = HeroBean("maomi")

    var needZhuangBei = Zhuangbei.YANDOU

    override fun initHeroes() {
        heros = arrayListOf<HeroBean>().apply {
            add(shan)
            add(mengyan)
            add(kuiqian)
            add(kui)
            add(efei)
            add(ganglie)
            add(daoke)
            add(gongjiang)
            add(huanqiu)
            add(maomi)
        }
    }

    override suspend fun afterHeroClick(heroBean: HeroBean) {
        super.afterHeroClick(heroBean)
    }

    override suspend fun doAfterHeroBeforeWaiting(heroBean: HeroBean) {
        if (!waiting && isGkOver(guanka)) {
            waiting = true
        }
    }

    fun isGkOver(g: Guan): Boolean {
        var result = when (g) {
            Guan.g0 -> true
            Guan.g1 -> shan.isFull() && mengyan.isFull() && kui.isFull() && kuiqian.isFull() && efei.isFull() && ganglie.isFull()
            Guan.g79->daoke.isFull()
            Guan.g91 -> maomi.isFull()
            Guan.g101 -> ganglie.isFull()
            Guan.g111 -> maomi.isFull()
            Guan.g131 -> ganglie.isFull()
            Guan.g141 -> maomi.isFull() && gongjiang.isFull()
            Guan.g151 -> ganglie.isFull()&&kui.isFull()
            Guan.g161 -> maomi.isFull()
            Guan.g171 -> ganglie.isFull()
        }

        return result && Zhuangbei.hasZhuangbei() && Zhuangbei.getZhuangBei() == needZhuangBei
    }

    override suspend fun dealHero(heros: List<HeroBean?>): Int {
        while (waiting || guanka == Guan.g0) {
            delay(100)
        }
        if (guanka == Guan.g1) {
            var list = arrayListOf(shan, mengyan, kuiqian, kui, ganglie, efei)
            var index = defaultDealHero(heros, list)
            if (index > -1) {
                return index
            }
        }else if (guanka == Guan.g79) {
            carDoing.downHero(kuiqian)
            var list = arrayListOf(daoke)
            var index = defaultDealHero(heros, list)
            if (index > -1) {
                return index
            }
        } else if (guanka == Guan.g91) {
            carDoing.downHero(ganglie)
            var list = arrayListOf( maomi)
            var index = defaultDealHero(heros, list)
            if (index > -1) {
                return index
            }
        } else if (guanka == Guan.g101) {
            carDoing.downHero(maomi)
            var list = arrayListOf(ganglie)
            var index = defaultDealHero(heros, list)
            if (index > -1) {
                return index
            }
        } else if (guanka == Guan.g111) {
            carDoing.downHero(ganglie)
            var list = arrayListOf( maomi)
            var index = defaultDealHero(heros, list)
            if (index > -1) {
                return index
            }
        } else if (guanka == Guan.g141) {
            carDoing.downHero(kui)
            carDoing.downHero(ganglie)
            var list = arrayListOf(gongjiang, maomi)
            var index = defaultDealHero(heros, list)
            if (index > -1) {
                return index
            }
        } else if (guanka == Guan.g151) {
            carDoing.downHero(gongjiang)
            carDoing.downHero(maomi)
            var list = arrayListOf(ganglie,kui)
            var index = defaultDealHero(heros, list)
            if (index > -1) {
                return index
            }
        } else if (guanka == Guan.g161) {
            carDoing.downHero(ganglie)
            var list = arrayListOf(maomi)
            var index = defaultDealHero(heros, list)
            if (index > -1) {
                return index
            }
        } else if (guanka == Guan.g171) {
            carDoing.downHero(maomi)
            var list = arrayListOf(ganglie)
            var index = defaultDealHero(heros, list)
            if (index > -1) {
                return index
            }
        }
        var index = heros.indexOf(huanqiu)
        if (index > -1 && Zhuangbei.getZhuangBei() != needZhuangBei) {
            return index
        }
        return -1
    }

    override fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean? {
        return null
    }

    suspend fun doOnKeyDown(code: Int): Boolean {
        var handle = true
        if (code == KeyEvent.VK_NUMPAD1) {
            needZhuangBei = Zhuangbei.QIANGXI
            waiting = false
        } else if (code == KeyEvent.VK_NUMPAD2) {
            needZhuangBei = Zhuangbei.YANDOU
            waiting = false
        } else if (code == KeyEvent.VK_NUMPAD3) {
            needZhuangBei = Zhuangbei.LONGXIN
            waiting = false
        } else {
            handle = false
        }

        if (handle) {
            log("code $code is Down")
        }

        return handle
    }

    override fun onGuanChange(guan: Int) {
        super.onGuanChange(guan)
        if (guan == 181) {
            needZhuangBei = Zhuangbei.QIANGXI
            waiting = false
        } else if (guan == 171) {
            if (guanka != Guan.g171) {
                guanka = Guan.g171
                waiting = false
            }
        } else if (guan == 161) {
            needZhuangBei = Zhuangbei.YANDOU
            if (guanka != Guan.g161) {
                guanka = Guan.g161
                waiting = false
            }
        } else if (guan == 151) {
            needZhuangBei = Zhuangbei.QIANGXI
            if (guanka != Guan.g151) {
                guanka = Guan.g151
                waiting = false
            }
        } else if (guan == 141) {
            needZhuangBei = Zhuangbei.LONGXIN
            if (guanka != Guan.g141) {
                guanka = Guan.g141
                waiting = false
            }
        } else if (guan == 131) {
            needZhuangBei = Zhuangbei.QIANGXI
            if (guanka != Guan.g131) {
                guanka = Guan.g131
                waiting = false
            }
        } else if (guan == 111) {
            needZhuangBei = Zhuangbei.YANDOU
            if (guanka != Guan.g111) {
                guanka = Guan.g111
                waiting = false
            }

        } else if (guan == 101) {
            needZhuangBei = Zhuangbei.QIANGXI
            if (guanka != Guan.g101) {
                guanka = Guan.g101
                waiting = false
            }

        } else if (guan == 99) {
            if (guanka != Guan.g91) {
                guanka = Guan.g91
                waiting = false
            }
        }else if (guan == 91) {
            needZhuangBei = Zhuangbei.LONGXIN
            waiting = false
        } else if (guan == 81) {
            needZhuangBei = Zhuangbei.QIANGXI
            waiting = false
        }else if(guan==79){
            if (guanka != Guan.g79) {
                guanka = Guan.g79
                waiting = false
            }
        }
        else if (guan == 61) {
            needZhuangBei = Zhuangbei.YANDOU
            waiting = false
        } else if (guan == 51) {
            needZhuangBei = Zhuangbei.QIANGXI
            waiting = false
        }else if(guan>9&&guanka!=Guan.g1){
            guanka = Guan.g1
            waiting = false
        }
    }

    override suspend fun onKeyDown(code: Int): Boolean {
        return doOnKeyDown(code)
    }

}