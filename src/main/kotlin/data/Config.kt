package data

import androidx.compose.runtime.mutableStateOf
import java.awt.Color
import java.awt.Point

object Config {

    val debug = false

    var viewFailAdv = mutableStateOf(true)
    var touxiangAuto = mutableStateOf(false)
    var touxiangAll = mutableStateOf(false)

    var appRootPath = ""

    val platform_moniqi = 0
    val platform_wx = 1
    val platform_qq = 2


    var isHome4Setting = mutableStateOf(true)
    var platform = mutableStateOf(platform_wx)
    val platName: String
        get() = if (isHome) "moniqi" else "xiaochengxu"
    val dtTopMargin: Int
        get() {
            return when (platform.value) {
                platform_moniqi -> 15
                platform_qq -> 0
                else -> if (!isHome) 0 else -3
            }
        }

    val dtHeight: Int
        get() {
            return when (platform.value) {
                platform_moniqi -> 592
                platform_qq -> 605
                else -> if (!isHome) 607 else 609
            }
        }

    val dtLeftMargin: Int
        get() {
            return when (platform.value) {
                platform_moniqi -> 0
                platform_qq -> if (!isHome) 0 else -3
                else -> if (!isHome) 0 else -1
            }
        }
    val isHome
        get() = isHome4Setting.value


    val pointClose = MPoint(974, 23)
    val windowWidth = 1000
    val caiji_main_path
        get() = "${appRootPath}tfres"

    //      get() =  if (isHome) "C:\\Users\\85963\\asd\\untitled1\\tfres" else "C:\\Users\\Administrator\\IdeaProjects\\intellij-sdk-code-samples\\untitled1\\tfres"
    val topbarHeight = 44


    val delayLong = 2000L

    val delayNor = 300L

    val delShort = 20L


    val heroW = 50
    val heroH = 70
    val heroT = 460 + topbarHeight

    var zhandou_zhuangbeiCheckRect: MRect = MRect.createWH(30, 500 + topbarHeight, 40, 40)

    var zhandou_hero1CheckRect: MRect = MRect.createWH(393, heroT, heroW, heroH)
    var zhandou_hero2CheckRect: MRect = MRect.createWH(475, heroT, heroW, heroH)
    var zhandou_hero3CheckRect: MRect = MRect.createWH(557, heroT, heroW, heroH)

    val zhandou_hezuo_guankaRect = MRect.createWH(488, 81, 26, 18)

    //    val guankaRect2 = data.MRect.createWH(472,30,55,25)
    val zhandou_hezuo_bossRect = data.MRect.createWH(910, 26 + topbarHeight, 50, 50)
    val zhandou_hezuo_bossNameRect = data.MRect.createWH(825, 52, 73, 21)

    //    val shaiziPoint = Point(285,300+ topbarHeight)
    var zhandou_shuaxinPoint = MPoint(700, 500 + topbarHeight)
    var zhandou_kuojianPoint = MPoint(300, 500 + topbarHeight)

    val hezuo_startPoint = MPoint(610, 550)

    //和好友一起
    val hezuo_friend = MPoint(330, 490)

    //加入房间
    val hezuo_Join = MPoint(600, 420)
    val hezuo_Join_Sure = MPoint(460, 420, Color(35, 147, 255))
    val hezuo_room_input_game = MPoint(400, 350)

    val hezuo_room_input_wx = MPoint(380, 300)
    val hezuo_room_input_wx_over = MPoint(440, 360)

    val hezuo_room_join_close = MPoint(679, 215)

    val hezuo_0tip_ok = MPoint(620, 450)


    val adv_close = MPoint(956, 73)
    val adv_point = MPoint(500, 510)

    val Color_ChuangZhang = Color(0, 255, 213)


    val point7p_houche = MPoint(118, 220)
    val point7p_qianche = MPoint(216, 220)

    val rectKuojian = MRect.createWH(292, 570, 57, 21)
    val rectShuaxin = MRect.createWH(690, 570, 40, 22)
//es8  es4 ye5   6e0 y80  y50 y56 e04

    val hbMSCloud = MPoint(610, 180, Color(238, 234, 125))
    val hbFSCloud = MPoint(610, 180, Color(150, 156, 204))
    val hbSSCloud = MPoint(610, 180, Color(81, 81, 81))


    val rect4ShuakaColor = MRect.createWH(693, 574, 20, 15)
    val rect4KuojianColor = MRect.createWH(288, 574, 25, 15)

    val pointHeroChoose = MPoint(940, 320)
    val pointHeroChooseBack = MPoint(42, 83)
    val pointHeroDuiZhan = MPoint(700,85)
    val pointHeroDuiZhanFail = MPoint(780,85)
    val pointDuiZhanRenshu = MPoint(43,161)
    val pointDuiZhanRenshuOk = MPoint(620, 450)

    //600 270 200 130
    val colorLeishenLanqiu = Color(255,255,255)
    val colorLeishenHongqiu = Color(255,45,85)
//    val rectCheckOfLeishen = MRect.createWH(700,270,50,25)
    val rectCheckOfLeishen = MRect.createWH(666,273,120,120)
    val count4SureLeishen = 30

    val leishenqiuXueTiao = Color(211,71,71)
    val leishenqiuXueTiao2 = Color(225,150,130)
    val leishenqiuXueTiaoRect = MRect.createWH(730,244,70,10)


    val xiongmaoFS = Color(186,71,231)
    val xiongmaoGJ = Color(10,180,100)
    val xiongmaoZS = Color(225,110,48)
    val xiongmaoQiuRect = MRect.createWH(700,300,50,50)
}