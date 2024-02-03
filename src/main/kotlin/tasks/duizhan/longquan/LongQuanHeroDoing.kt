package tasks.duizhan.longquan

import data.Config
import data.HeroBean
import data.MPoint
import kotlinx.coroutines.delay
import log
import tasks.HeroDoing
import tasks.XueLiang
import utils.MRobot
import kotlin.math.abs
import kotlin.math.max

class LongQuanHeroDoing(val renji: Boolean = false) : HeroDoing(-1) {

    lateinit var longquan: HeroBean
    lateinit var bawang: HeroBean
    lateinit var nvwang: HeroBean
    lateinit var kuanglong: HeroBean
    lateinit var guangqiu: HeroBean
    lateinit var moqiu: HeroBean
    lateinit var bingnv: HeroBean
    lateinit var dijing: HeroBean
    lateinit var xiaochou: HeroBean
    lateinit var xiaolu: HeroBean

    var gaojimengyanJudgePoing = MPoint(74, 488)

    fun isGaojiMengyan(): Boolean {
        var color = MRobot.robot.getPixelColor(74, 488)
        var red = color.red
        var blue = color.blue
        if (abs(red - 225) < 30 && abs(blue - 255) < 20) {
            log("高级梦魇啊")
            return true
        }
        return false
    }

    init {
        longquan = HeroBean("longquan", 100)
        bawang = HeroBean("bawang", 90)
        nvwang = HeroBean("nvwang", 80)
        kuanglong = HeroBean("kuanglong", 70)
        guangqiu = HeroBean("guangqiu", 60, needCar = false, compareRate = 0.99)
        moqiu = HeroBean("moqiu", 50, needCar = false, compareRate = 0.95)
        bingnv = HeroBean("bingnv", 40)
        dijing = HeroBean("longwang", 30, compareRate = 0.95)
        xiaochou = HeroBean("xiaochou", 0)
        xiaolu = HeroBean("xiaolu", 0)
    }

    override fun initHeroes() {
        heros = arrayListOf()
        heros.add(longquan)
        heros.add(bawang)
        heros.add(nvwang)
        heros.add(kuanglong)
        heros.add(xiaolu)
        heros.add(guangqiu)
        heros.add(moqiu)
        heros.add(bingnv)
        heros.add(dijing)
        heros.add(xiaochou)
    }

    var mChePositionCount = 0

    override suspend fun doAfterHeroBeforeWaiting(heroBean: HeroBean) {
        super.doAfterHeroBeforeWaiting(heroBean)
        if (heroBean.needCar) {
            mChePositionCount = max(mChePositionCount, heroCountInCar())
        }
        if (heroBean == moqiu) {
//            delay(1000)
        } else if (heroBean == xiaolu) {
            carDoing.downHero(heroBean)
        } else if (heroBean == bingnv) {
            if (isGaojiMengyan()) {
                carDoing.downHero(bingnv)
            }
        }
        if (renji) {
            if (!bawang.isFull()) {
//                bawang.checkStarLevelUseCard(carDoing)
                bawang.checkStarMix(carDoing)
            }
        } else {
            if (!longquan.isFull()) {
                if (heroBean == longquan && longquan.currentLevel == 1) { //刚上的龙拳就不检测了，总莫名卡住card
                } else {
//                    longquan.checkStarLevelUseCard(carDoing)
                    longquan.checkStarMix(carDoing)
                }
            }
            if (!nvwang.isFull() && nvwang.currentLevel > 1) {
//                nvwang.checkStarLevelUseCard(carDoing)
                nvwang.checkStarMix(carDoing)
            }
        }
    }


    fun changeHeroWhenNoSpaceRenji(heroBean: HeroBean): HeroBean? {
        if (mChePositionCount < 3) {
            return null
        }
        if (heroBean == xiaolu) {
            if (bingnv.isInCar()) {
                return bingnv
            }
        }

        return heros.firstOrNull {
            it.isInCar() && it != bawang && it != nvwang && it != bingnv
        }

    }

    override fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean? {
//        var hs = arrayListOf<HeroBean>().apply { addAll(heros.filter { it.isInCar() }) }
//        hs.sortBy { it.weight }
//        var h = hs.firstOrNull()
//        var w = h?.weight ?: 10000
//        if (w < heroBean.weight) {
//            if (h == longquan || h == nvwang || h == bawang) {//这些都不用换，属于前三得英雄
//                return null
//            }
//            return h!!
//        }
        if (renji) {
            return changeHeroWhenNoSpaceRenji(heroBean)
        }

        if (mChePositionCount < 4) {//不管，开过5格开会替换

        } else {

            if (bingnv.isInCar()) {
                return bingnv
            }
            if (xiaochou.isInCar()) {
                return xiaochou
            }

//            if(bawang.isFull()){
//                if(dijing.isInCar()){//霸王满的时候，可以用下地精（这里应该只会上小鹿来替换地精了)
//                    return dijing
//                }
//            }

        }

        return null
    }

    suspend fun dealHeroRenji(heros: List<HeroBean?>): Int {
        var list = arrayListOf<HeroBean?>()
        list.addAll(heros)
        if (list.isEmpty()) return -1

        var indexlongquan = list.indexOf(longquan)
        var indexbawang = list.indexOf(bawang)
        var indexnvwang = list.indexOf(nvwang)
        var indexkuanglong = list.indexOf(kuanglong)
        var indexguangqiu = list.indexOf(guangqiu)
        var indexmoqiu = list.indexOf(moqiu)
        var indexbingnv = list.indexOf(bingnv)
        var indexdijing = list.indexOf(dijing)
        var indexxiaochou = list.indexOf(xiaochou)
        var indexxiaolu = list.indexOf(xiaolu)

        if (indexbawang > -1) {
            return indexbawang
        }
        if (indexguangqiu > -1 && bawang.isInCar() && !bawang.isFull()) {//打人机优先霸王
            this.heros.forEach {
                if (it != bawang && it.isInCar() && !it.isFull() && !(it == nvwang && it.currentLevel == 3)) {//如果女王3级了就不下女王了，拼拼运气
                    carDoing.downHero(it)
                }
            }
            return indexguangqiu
        }

        if (indexguangqiu > -1 && nvwang.isInCar() && !nvwang.isFull()) {//打人机优先霸王
            this.heros.forEach {
                if (it != nvwang && it.isInCar() && !it.isFull()) {
                    carDoing.downHero(it)
                }
            }
            return indexguangqiu
        }

        if (moqiu.heroName=="moqiu" && indexmoqiu > -1 && heroCountInCar() > 0 && xuliangOk()) {
            return indexmoqiu
        }

        if (indexnvwang > -1) {
            return indexnvwang
        }
        if (indexxiaolu > -1) {
            return indexxiaolu
        }
        if (indexbingnv > -1) {
            if (bingnv.isInCar()) {
                carDoing.downHero(bingnv)
            }
            return indexbingnv
        }

        if (!bawang.isInCar()) {//霸王在车时就不上其他，这里是防止一直是这些英雄，刷完都没上一个，被卡死了
            if (indexlongquan > -1) {
                return indexlongquan
            }
            if (indexkuanglong > -1) {
                return indexkuanglong
            }
            if (indexxiaochou > -1) {
                return indexxiaochou
            }
        }

        if (moqiu.heroName=="moqiu" && indexmoqiu > -1) {
            return indexmoqiu
        }

        return -1
    }



    override suspend fun dealHero(heros: List<HeroBean?>): Int {


        if (renji) {
            return dealHeroRenji(heros)
        }



        var list = arrayListOf<HeroBean?>()
        list.addAll(heros)
        if (list.isEmpty()) return -1

        var indexlongquan = list.indexOf(longquan)
        var indexbawang = list.indexOf(bawang)
        var indexnvwang = list.indexOf(nvwang)
        var indexkuanglong = list.indexOf(kuanglong)
        var indexguangqiu = list.indexOf(guangqiu)
        var indexmoqiu = list.indexOf(moqiu)
        var indexbingnv = list.indexOf(bingnv)
        var indexdijing = list.indexOf(dijing)
        var indexxiaochou = list.indexOf(xiaochou)
        var indexxiaolu = list.indexOf(xiaolu)


        if (indexlongquan > -1) {//有龙上龙
            return indexlongquan
        }

        if (!longquan.isFull()) {
            if (indexguangqiu > -1 && longquan.isInCar() ) {//优先满龙拳
                this.heros.forEach {
                    if (it != null && it != longquan && it.isInCar() && !it.isFull()) {
                        if(it == nvwang && nvwang.currentLevel==3){
                         //女王如果3星了就不下了
                        }else {
                            carDoing.downHero(it)
                        }
                    }
                }
                return indexguangqiu
            }

            if (indexxiaolu > -1) {//
                return indexxiaolu
            }
            //经济上冰女和地精 有就行
            if (indexbingnv > -1 && !bingnv.isInCar()) {
                return indexbingnv
            }
//            if (indexdijing > -1 && !dijing.isInCar()) {
//                return indexdijing
//            }

            if (indexxiaochou > -1 && !xiaochou.isInCar()) {
                return indexxiaochou
            }

            if (indexbingnv > -1) {//如果上面因为有冰女没上，而且上面的优先逻辑hero也没有，那么就卖冰女上冰女
                carDoing.downHero(bingnv)
                return indexbingnv
            }

            if (indexguangqiu > -1) {//光球
                return indexguangqiu
            }
            if(indexnvwang>-1){
                return indexnvwang
            }

//            if (indexdijing > -1) {
//                return indexdijing
//            }

            if (indexxiaochou > -1) {
                return indexxiaochou
            }

            if (moqiu.heroName=="moqiu" && indexmoqiu > -1 && xuliangOk()) {
                return indexmoqiu
            }

        } else {

            //优先羁绊
            if (indexkuanglong > -1 && !kuanglong.isInCar()) {
                return indexkuanglong
            }
            if (indexbawang > -1 && !bawang.isInCar()) {
                return indexbawang
            }

            //先凑满龙拳组合 就无脑女王了
            if (indexnvwang > -1) {
                return indexnvwang
            }

            if (indexguangqiu > -1 && !nvwang.isFull() && nvwang.isInCar()) {//女王没满就碰运气光球
                if(nvwang.currentLevel==3){
                    if(bawang.isInCar() && kuanglong.isInCar()){
                        //如果羁绊已形成，就不强满女王了
                    }else{
                        this.heros.forEach {//不是女王 龙拳的都下掉，强满女王
                            if(!it.isFull() && it!=nvwang && it!=longquan){
                                carDoing.downHero(it)
                            }
                        }
                    }
                }
                return indexguangqiu
            }

//            if (indexdijing > -1 && !dijing.isInCar()) {//补个地精防止熊猫
//                return indexdijing
//            }
            if (indexbawang > -1 && nvwang.isInCar()) {//女王已经在车了，就随缘升霸王，比小鹿等级高，毕竟小鹿可能卡住升格子
                return indexbawang
            }

            if (indexxiaolu > -1) {//
//                if(bingnv.isInCar() || xiaochou.isInCar() || bawang.isFull()) {//如果这俩都不在，就只剩4主力+地精了，因为熊猫可能存在，所以不下地精,但如果霸王满了，可以下地精（霸王攻击比龙拳高
                if (bingnv.isInCar() || xiaochou.isInCar() || heroCountInCar() < 5) {//算了，地精就一直在吧,<5是因为可能上次小鹿时小丑在，把小丑卖了，小鹿也卖了，这是有空位，但遇到小鹿它不上，所以小于5时也可以上小鹿
                    if(nvwang.isFull()&& kuanglong.isInCar() && bawang.isInCar()&&carDoing.openCount()<5){
                     //如果这时只有4个开的格子，并且女王龙拳都已经满了，就不刷小鹿了，就随缘刷刷霸王，这时上小鹿还要等开格子没意义了，女王如果没满就等小鹿，毕竟需要第5个格子来上下小鹿冰女来刷女王
                    }else {
                        return indexxiaolu
                    }
                }
            }

            if (moqiu.heroName=="moqiu" && indexmoqiu > -1 && xuliangOk()) {
                return indexmoqiu
            }

            if (indexbawang > -1) {
                return indexbawang
            }

            if (indexguangqiu > -1 && !nvwang.isFull()) {//光球垫底,女王满了就不用了，防止狂龙满（龙拳阵容用的人太多，自己狂龙满了等于帮助对面的狂龙了)
                return indexguangqiu
            }
            if (indexbingnv > -1) {
                if (bingnv.isInCar()) {
                    carDoing.downHero(bingnv)
                    return indexbingnv
                }
                if (carDoing.hasOpenSpace()) {//下了小鹿有位置时，如果没刷到其他卡，刷到冰女可以上冰女
                    return indexbingnv
                }
            }

            if (indexkuanglong > -1 && kuanglong.currentLevel>1) {
                //其他卡都验过了，轮到有狂龙了，那么如果狂龙等级太高就下掉重新上,比如当前卡片组是 狂龙 任务卡 光球。那就下狂龙再上
                carDoing.downHero(kuanglong)
                return indexkuanglong
            }
//            if (indexdijing > -1) {
//                return indexdijing
//            }
        }
        return -1
    }


    fun xuliangOk(): Boolean {
        var xlOk = !XueLiang.isMLess(0.6f)
        var msg = if(xlOk) "血量健康，释放魔球" else "血量不健康，本次不释放魔球"
        log(msg)
        return xlOk
    }
}