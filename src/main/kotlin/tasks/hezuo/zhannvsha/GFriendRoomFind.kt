package tasks.hezuo.zhannvsha

import data.Config.delayNor
import getImage
import kotlinx.coroutines.delay
import tasks.HomePage
import tasks.WxUtil
import tesshelper.Tess

class GFriendRoomFind:IRoomFind() {

    override suspend fun beforeFind() {
        HomePage.openFriendMsg()
        delay(delayNor)
    }
    override suspend fun getText(): String {


       var text = Tess.getText(getImage(HomePage.friendTextRect))
        HomePage.pointCloseFriend.click()
        delay(delayNor)

        return text
    }

    override suspend fun onRoomFail(room:String) {
    }
    override suspend fun onTextTry(text: String) {
        HomePage.pointMsgClose.click()
        delay(delayNor)
    }
}