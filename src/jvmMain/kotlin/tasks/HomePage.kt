package tasks

import data.Config
import data.Config.delayNor
import data.MPoint
import data.MRect
import data.Recognize
import kotlinx.coroutines.delay
import java.awt.Color

object HomePage {

    val pointMsg = MPoint(940, 460)//点击msg
    val pointFriend = MPoint(762, 66)//切到好友
    val friendTextRect = MRect.create4P(579, 196, 977, 528)//截取文字信息
    val pointCloseFriend = MPoint(642, 107)//关闭这个好友（这样下次打开就是另一个好友了)//尝试关闭好友后继续识别，不用退出重新走流程，遇到合适的text就直接进入了，退出应该就回首页了
    val pointMsgClose = MPoint(593, 565, Color(255,255,255))

    fun isAtHomePage(): Boolean {
        return Recognize.Duizhan.isFit()
    }

    suspend fun openFriendMsg() {
        if (isAtHomePage()) {
            pointMsg.click()
            delay(delayNor)
            pointFriend.click()
            delay(delayNor)
        }
    }

    suspend fun openWorldMsg() {
        if (isAtHomePage()) {
            pointMsg.click()
            delay(delayNor)
        }


    }
}