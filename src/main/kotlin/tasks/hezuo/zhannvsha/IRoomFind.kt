package tasks.hezuo.zhannvsha

import data.Config
import data.Recognize
import kotlinx.coroutines.delay
import tasks.WxUtil
import utils.MRobot
import log
import logOnly

abstract class IRoomFind {
    var roomUsed = arrayListOf<String>()//用过的房间号
    var roomToUser = arrayListOf<String>()
    open suspend fun findAndJoinRoom() {
        var room = findRoom()
        inputRoom(room)
        delay(3000)
        if (Config.hezuo_Join_Sure.isFit()) {
            log("加入失败")
            onRoomFail(room)
            Config.hezuo_room_join_close.click()
            delay(500)
            findAndJoinRoom()
        }
    }

    open suspend fun beforeFind() {

    }

    open suspend fun onRoomFail(room: String) {

    }

    open suspend fun getText(): String {
        return ""
    }

    open suspend fun onTextTry(text: String) {

    }

    suspend fun findRoom(): String {
        if (roomToUser.isNotEmpty()) {
            val result = roomToUser.removeAt(0)
            roomUsed.add(result)
            log("正在尝试加入:$result")
            onTextTry(result)
            return result
        }
        while (roomToUser.isEmpty()) {
            var text = getText()
            logOnly("截图text:$text")
            Regex("(?<=a).*?(?=a)").findAll(text, 0).forEach {
                log("find text :" + it.value, true)
                var text = it.value.toString().replace("l", "1")
                if (!roomUsed.contains(text) && !roomToUser.contains(it.value) && it.value.length == 4

                ) {

                    if (text.all {
                            it.isDigit()
                        }) {
                        roomToUser.add(text)
                    }
                }
            }
            delay(Config.delayLong)
        }
        return findRoom()
    }

    private suspend fun inputRoom(text: String) {
        while (!Recognize.Duizhan.isFit()) {//等回到“对战” 首页
            delay(Config.delayNor)
        }
        Config.hezuo_startPoint.click()
        delay(Config.delayLong)
        //如果有广告
        if(Recognize.IcAdv4Hezuo.isFit()){
            Recognize.IcAdv4Hezuo.click()
            delay(34*1000)
            Config.adv_close.click()
            delay(2000)

            while (!Recognize.Duizhan.isFit()) {//等回到“对战” 首页
                Config.adv_close.click()
                delay(Config.delayNor)
            }
            Config.hezuo_startPoint.click()
            delay(Config.delayNor)
        }


        //有次数   的时候，步骤是点和好友一起，加入房间，然后点击输入框。但没次数的时候这样点击也不影响
        Config.hezuo_friend.click()
        delay(Config.delayNor)
        Config.hezuo_Join.click()
        delay(Config.delayNor)
        //有次数 end
        //无次数时 点击了开始合作（合作助战），就直接出现输入框
        Config.hezuo_room_input_game.click()
        delay(Config.delayNor)

        Config.hezuo_room_input_wx.click(null)
        delay(Config.delayNor)

        MRobot.niantie(text)
//        MRobot.adbInput(text)
        delay(Config.delayNor)
        Config.hezuo_room_input_wx_over.click()
        delay(Config.delayNor)
        Config.hezuo_Join_Sure.click()
    }

}