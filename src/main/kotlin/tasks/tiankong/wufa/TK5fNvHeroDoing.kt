package tasks.tiankong.wufa

import data.HeroBean
import data.Recognize
import kotlinx.coroutines.*
import log
import tasks.CarDoing
import tasks.HeroDoing
import tasks.XueLiang
import tasks.guankatask.GuankaTask
import java.awt.event.KeyEvent
class TK5fNvHeroDoing : HeroDoing(0) {//默认赋值0，左边，借用左边第一个position得点击，去识别车位置后再更改

    var guanka = 0

    val leishen = HeroBean("leishen", 100)
    val nvwang = HeroBean("nvwang", 90)
    val saman = HeroBean("saman", 80)
    val xiaochou = HeroBean("xiaochou", 70)
    val bingnv = HeroBean("bingnv", 60)
    val dijing = HeroBean("dijing", 50)
    val niutou = HeroBean("niutou", 40, needCar = false, compareRate = 0.95)
    val shengqi = HeroBean("shengqi", 30, needCar = false)
    val moqiu = HeroBean("moqiu", 20, needCar = false, compareRate = 0.95)
    val guangqiu = HeroBean("guangqiu", 0, needCar = false)


    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    companion object {
        var che300Name: String? = null
    }

    override fun initHeroes() {
        che300Name = null
        heros = arrayListOf()
        heros.add(leishen)
        heros.add(nvwang)
        heros.add(saman)
        heros.add(xiaochou)
        heros.add(bingnv)
        heros.add(dijing)
        heros.add(niutou)
        heros.add(shengqi)
        heros.add(moqiu)
        heros.add(guangqiu)

    }

    var needCheckStar = false
    private suspend fun checkStars() {
        carDoing.checkStars()
        needCheckStar = false
    }

    var mChePos = -1
    var kuojianguo = false
    override suspend fun afterHeroClick(heroBean: HeroBean) {

        if (mChePos == -1 && heroBean.needCar) {//未识别车时,并且 这个hero是上阵得英雄
            log("开始检测车")
            carDoing.carps.get(0).click()
            delay(1000)
            if (Recognize.saleRect.isFit()) {//是自己，啥也不用干，开始初始化得位置就是对得
                mChePos = 0//
            } else {
                //我在右边
                mChePos = 1
                chePosition = 1
                carDoing.chePosition = 1
                carDoing.initPositions()
            }
            log("识别车位结果：$mChePos")
            CarDoing.cardClosePoint.click()
        }
        if (heroCountInCar() > 1) {
            kuojianguo = true
        }

        if (needCheckStar && heroBean.needCar && kuojianguo) {//等再次上英雄时 再查
            checkStars()
        }

        if (heroBean == guangqiu) {

            var checked = carDoing.checkStarsWithoutCard()
            if (!checked) {//1.5秒没有check到的话，再使用弹窗识别
                if (kuojianguo) {//扩建过开启检查，否则车位不准，先不检查,等上英雄时再检查
                    delay(1500)
                    checkStars()
                } else {
                    needCheckStar = true
                }
            }
        }

        if(heroBean == moqiu){
            lastMoqiuTime = System.currentTimeMillis()
        }
    }
    var lastMoqiuTime = 0L

    var curZhuangBei: Int = 0
    var needZhuangbei300: String? = null
    override suspend fun dealHero(heros: List<HeroBean?>): Int {

        var index = heros.indexOf(leishen)
        if(index>-1){
            return index
        }
        index = heros.indexOf(bingnv)
        if(index>-1 && !bingnv.isInCar()){
            return index
        }
        index = heros.indexOf(shengqi)
        if(index>-1 && !shengqi.isInCar()){
            return index
        }
        index = heros.indexOf(xiaochou)
        if(index>-1 && !xiaochou.isInCar()){
            return index
        }
        index = heros.indexOf(niutou)
        if(index>-1 && !niutou.isInCar()){
            return index
        }
        index = heros.indexOf(saman)
        if(index>-1){
            return index
        }
        index = heros.indexOf(xiaochou)
        if(index>-1){
            return index
        }
        index = heros.indexOf(niutou)
        if(index>-1){
            return index
        }
        index = heros.indexOf(shengqi)
        if(index>-1){
            return index
        }
        index = heros.indexOf(guangqiu)
        if(index>-1){
            return index
        }
        index = heros.indexOf(bingnv)
        if(index>-1){
            return index
        }
        index = heros.indexOf(moqiu)
        if(index>-1){
            if(useMoqiu()) {
                while (System.currentTimeMillis() - lastMoqiuTime < 5 * 1000) {
                    delay(500)
                }
                return index
            }
        }

        return -1
    }

    private suspend fun useMoqiu(): Boolean {
        return !XueLiang.isMLess(0.5f)
    }

    override fun changeHeroWhenNoSpace(heroBean: HeroBean): HeroBean? {
        return null
    }
}