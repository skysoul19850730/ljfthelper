package tasks.hezuo.zhannvsha

import kotlinx.coroutines.delay
import tasks.WxUtil

class WXRoomFind:IRoomFind() {

    override suspend fun beforeFind() {
        WxUtil.findWindowAndMove()
//        WxUtil.sendImg("hezuogonglue.jpg")
//        delay(100)
        WxUtil.sendText("发送 a0000(四位房号)a,满级后猴子换女王挂机，哭脸退出（实验）")
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