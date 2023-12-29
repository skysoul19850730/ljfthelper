package tasks.hezuo.zhannvsha

import kotlinx.coroutines.delay
import tasks.WxUtil

class WXRoomFind:IRoomFind() {

    override suspend fun beforeFind() {
        WxUtil.findWindowAndMove()
//        WxUtil.sendImg("hezuogonglue.jpg")
//        delay(200)
        WxUtil.sendText("发送 a0000(四位房号)a,副卡先上猴子，牛头，工匠，恶匪，小野，死神。 等主卡满级猴子换女王挂机(着急也可以不等)")
    }

    override suspend fun getText(): String {
        return WxUtil.getText()
    }

    override suspend fun onRoomFail(room:String) {
        WxUtil.sendText("房间${room} 进入失败")
    }
    override suspend fun onTextTry(text: String) {
        WxUtil.sendText("正在尝试进入房间$text")
    }
}