package tasks

import com.sun.jna.platform.win32.GDI32Util
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import data.Config
import data.MPoint
import data.MRect
import getImage
import kotlinx.coroutines.delay
import tesshelper.Tess
import utils.MRobot
import utils.Window
import log
import tasks.hezuo.zhannvsha.ZhanNvGameLaunch

object WxUtil {

    var wxWindowRect = MRect.createWH(0, 620, 700, 400)
    var wxWindow: WinDef.HWND? = null
//    val wxWindowRect = MRect.createWH(200,120,600,300)

    var inputPoint = MPoint(35, 924)
    var sendPoint = MPoint(610, 990)


    val openSend = true

    fun findWindowAndMove() {
        var name = if (ZhanNvGameLaunch.parterner == 0) "中國同盟会" else "承澄"
        wxWindow = Window.findWindowWithName(name)
        if (wxWindow != null) {
            var rect = WinDef.RECT()
            User32.INSTANCE.GetWindowRect(wxWindow, rect)
            wxWindowRect = MRect.createWH(0, 0, rect.right - rect.left + 1, rect.bottom - rect.top + 1)
            inputPoint = MPoint(40, wxWindowRect.height-100)
            sendPoint = MPoint(wxWindowRect.width-80,wxWindowRect.height - 25)
//            User32.INSTANCE.MoveWindow(wxWindow, 0, 620, 700, 400, true)
        }
//       var bitmap = GDI32Util.getScreenshot(wxWindow)
//        log(bitmap)
    }

    suspend fun sendText(text: String) {
        if (openSend) {
            inputPoint.clickPc(wxWindow)
            delay(300)
            MRobot.niantie(text, wxWindow)
            delay(300)
            sendPoint.clickPc(wxWindow)
        }
    }

    suspend fun sendImg(img: String) {
        if (openSend) {
            inputPoint.clickPc(wxWindow)
            User32.INSTANCE.BringWindowToTop(wxWindow)
            MRobot.niantieImg(img)
            sendPoint.clickPc(wxWindow)
        }
    }

    fun getText(): String {
        val img = getImage(wxWindowRect,wxWindow)
        if(Config.debug) {
            log(img)
        }
        var text = Tess.getText(img)
        return text
    }

}