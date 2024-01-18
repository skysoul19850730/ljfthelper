package data

import data.Config.platform
import model.CarDoing
import utils.ImgUtil
import utils.MRobot

enum class Recognize(private val resName: String, private val rect: MRect, private val rectMNQ: MRect? = null) {

    Duizhan(
        "duizhan.png",
        MRect.create4P(782, 511, 947, 573),
        MRect.create4P(762, 515, 931, 572)
    ),
    DuiZhanResultSuc("vectory.png", MRect.createWH(460,150,70,40)),

    Pipei(
        "pipei.png",
        MRect.create4P(623, 480, 725, 521),
        MRect.create4P(619, 477, 727, 524)
    ),

    BtnOk("custom_435_501_565_539.png", MRect.create4P(435, 501, 565, 539)),
    CanreJujue(
        "advjujue.png",
        MRect.create4P(302, 489, 399, 535),
        MRect.create4P(307, 490, 395, 532)
    ),
    CanreJujueFail(
        "advjujue_fail.png",
        MRect.create4P(302, 489, 399, 535),
        MRect.create4P(307, 490, 395, 532)
    ),

    //无广告 确认键
    NoAdvOk("custom_571_436_696_465.png", MRect.create4P(571, 436, 696, 465)),
    heroStar1("startLv1.png", CarDoing.starCheckRect),
    heroStar2("startLv2.png", CarDoing.starCheckRect),
    heroStar3("startLv3.png", CarDoing.starCheckRect),
    heroStar4("startLv4.png", CarDoing.starCheckRect),
    saleRect("salecheck.png", CarDoing.saleCheckRect),
    IcAdv4Hezuo("hezuoadv.png",MRect.createWH(400,400,35,35)),
    ;


    val resNameFinal: String
        get() {
            return "${Config.platName}/recognize/$resName"
        }

    val rectFinal: MRect
        get() {
            return if (platform.value == Config.platform_moniqi) {
                rectMNQ ?: rect
            } else {
                rect
            }
        }

    fun isFit(sim: Double = ImgUtil.simRate): Boolean {
        return ImgUtil.isImageInRect(resNameFinal, rectFinal, sim)
    }

    suspend fun click() {
        MRobot.singleClick(rectFinal.clickPoint)
    }
}