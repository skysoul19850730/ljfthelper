package utils

import App
import com.sun.jna.platform.unix.X11.XButtonEvent
import com.sun.jna.platform.win32.BaseTSD
import com.sun.jna.platform.win32.GDI32
import com.sun.jna.platform.win32.GDI32Util
import com.sun.jna.platform.win32.Netapi32Util.User
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.User32Util
import com.sun.jna.platform.win32.Win32Exception
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.WORD
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.platform.win32.WinUser.*
import data.Config
import data.MPoint
import data.MRect
import getImageFromRes
import kotlinx.coroutines.*
import log
import logOnly
import saveTo
import tasks.WxUtil
import java.awt.Color
import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.image.BufferedImage
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import kotlin.math.abs

object MRobot {
    val robot = Robot()

    val houtai = true

//    /**
//     * 仅关闭
//     */
//    fun norClick(point: MPoint) {
//        if (houtai) {
//            GlobalScope.launch {
//                App.tfWindow?.clickPoint(point)
//            }
//            return
//        }
//        robot.mouseMove(point.x, point.y)
//        robot.delay(30)
//        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
//        robot.delay(15)
//        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
//    }

    private suspend fun singleClick() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
        delay(15)
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
    }

    suspend fun singleClickPc(point: MPoint, window: WinDef.HWND? = App.tfWindow) {
        if (houtai && window != null && window != WxUtil.wxWindow) {
            logOnly("click point start ${point.x}  ${point.y}")
            window?.clickPoint(point)
            logOnly("click point end ${point.x}  ${point.y}")
            return
        }
        var x = point.x
        var y = point.y
        if (window == WxUtil.wxWindow) {
            var rect = WinDef.RECT()
            User32.INSTANCE.GetWindowRect(WxUtil.wxWindow, rect)
            x += rect.left
            y += rect.top
        }
        withContext(Dispatchers.Main) {
            logOnly("click point start ${x}  ${y}")
            robot.mouseMove(x, y)
            delay(30)
            singleClick()
            logOnly("click point end ${x}  ${y}")
        }

    }

    suspend fun singleClick(point: MPoint, window: WinDef.HWND? = App.tfWindow) {
//        robot.mouseMove(point.x, point.y)
//        robot.delay(30)
//        singleClick()
//        if(Config.platform.value == Config.platform_moniqi) {
//            ADBUtil.tap(MPoint(point.x, point.y - Config.topbarHeight))
//        }else{
        singleClickPc(point, window)
//        }
    }

//    suspend fun inputOneKey(code:Int){
//        robot.keyPress(code)
//        delay(100)
//        robot.keyRelease(code)
//        delay(100)
//    }
//    suspend fun inputKeys(vararg codes:Int){
//        codes.forEach {
//            robot.keyPress(it)
//        }
//        delay(100)
//        codes.forEach {
//            robot.keyRelease(it)
//        }
//    }

    suspend fun niantie(text: String?, window: WinDef.HWND? = App.tfWindow) {
        log("输入房间号$text")
        if (!text.isNullOrEmpty()) {
            copyText(text)
        }
//        delay(1300)
//        if (houtai) {
//            window?.niantie(text ?: "")
//            return
//        }

        robot.keyPress(KeyEvent.VK_CONTROL)
        robot.keyPress(KeyEvent.VK_V)
        delay(200)
        robot.keyRelease(KeyEvent.VK_CONTROL)
        robot.keyRelease(KeyEvent.VK_V)
    }

    fun niantieImg(img: String) {
        var img = getImageFromRes(img)
        copyImage(img)
        robot.keyPress(KeyEvent.VK_CONTROL)
        robot.keyPress(KeyEvent.VK_V)
        robot.keyRelease(KeyEvent.VK_CONTROL)
        robot.keyRelease(KeyEvent.VK_V)
    }

//    fun adbInput(text: String) {
//        ADBUtil.inputText(text)
//    }

    fun copyText(text: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
    }

//    fun doubleClick() {
//        singleClick()
//        singleClick()
//    }

    fun copyImage(image: BufferedImage) {
        val trans = object : Transferable {
            override fun getTransferDataFlavors(): Array<DataFlavor> {
                return arrayOf(DataFlavor.imageFlavor)
            }

            override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean {
                return DataFlavor.imageFlavor.equals(flavor)
            }

            override fun getTransferData(flavor: DataFlavor?): Any? {
                if (isDataFlavorSupported(flavor)) {
                    return image
                }
                return null
            }

        }

        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(trans, null)
    }


    val input = WinUser.INPUT()

    suspend fun test() {
        input.type = WinDef.DWORD(WinUser.INPUT.INPUT_MOUSE.toLong())
        input.input.setType("mi")
        input.input.mi.dx = WinDef.LONG((65535f / 1920 * 26).toLong())
        input.input.mi.dy = WinDef.LONG((65535f / 1080 * 573).toLong())
//        input.input.mi.dy = WinDef.LONG((65535f/1080 *66).toLong())
        input.input.mi.mouseData = WinDef.DWORD(1)
        input.input.mi.dwExtraInfo = BaseTSD.ULONG_PTR(0)
//        input.input.mi.dwFlags = WinDef.DWORD(0x0002 )
        input.input.mi.dwFlags = WinDef.DWORD(0x0002 or 0x0001 or 0x8000)
        User32.INSTANCE.SendInput(WinDef.DWORD(1.toLong()), input.toArray(1) as Array<out WinUser.INPUT>?, input.size())
        delay(30)
        input.input.mi.mouseData = WinDef.DWORD(1)
        input.input.mi.dwExtraInfo = BaseTSD.ULONG_PTR(0)
        input.input.mi.dwFlags = WinDef.DWORD(0x0004)
        User32.INSTANCE.SendInput(WinDef.DWORD(1.toLong()), input.toArray(1) as Array<out WinUser.INPUT>?, input.size())
        log("test click with input")

    }

    suspend fun clickTest() {
        val tf = Window.findWindowWithName("中國同盟会")
//        User32.INSTANCE.SetForegroundWindow(tf)
//        delay(1000)
//        testSendMsg(tf!!)


//        test()
//        delay(1000)
//        var test = "test"
//        test.forEach {
//            sendChar(it)
//        }
//        delay(100)
//        test()
//        sendChar('c')
        val start = System.currentTimeMillis()
        val bitmap = GDI32Util.getScreenshot(tf)
        val time = System.currentTimeMillis() - start
        logOnly("const $time")
        bitmap.saveTo(File(App.caijiPath, "${System.currentTimeMillis()}.png"))
    }


    suspend fun testSendMsg(tf: WinDef.HWND) {
//        var value: Long = (613 shl 16) or 470
        var value: Long = (13 shr 16) or 440
        User32.INSTANCE.SendMessage(tf, 0x0201, WinDef.WPARAM(0x0001), WinDef.LPARAM(value.toLong()))
        delay(30)
        User32.INSTANCE.SendMessage(tf, 0x0202, WinDef.WPARAM(0x0001), WinDef.LPARAM(value.toLong()))
    }

    fun sendChar(ch: Char) {
        var code = KeyEvent.getExtendedKeyCodeForChar(ch.code).toLong()
        input.type = WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD.toLong())
        input.input.setType("ki")
        input.input.ki.run {
            wScan = WinDef.WORD(0)
            time = WinDef.DWORD(0)
            dwExtraInfo = BaseTSD.ULONG_PTR(0)
            wVk = WinDef.WORD(code.toLong())
            dwFlags = WinDef.DWORD(0)
        }
        User32.INSTANCE.SendInput(WinDef.DWORD(1.toLong()), input.toArray(1) as Array<out WinUser.INPUT>?, input.size())

//        input.input.ki.wVk = WinDef.WORD(code)
//        input.input.ki.dwFlags = WinDef.DWORD(2)
//        User32.INSTANCE.SendInput(WinDef.DWORD(1.toLong()), input.toArray(1) as Array<out WinUser.INPUT>?, input.size())
    }


    suspend fun WinDef.HWND.clickPoint(point: MPoint) {
        withContext(Dispatchers.Main) {
            var value: Long = ((point.y shl 16) or point.x).toLong()
            User32.INSTANCE.SendMessage(this@clickPoint, 0x0201, WinDef.WPARAM(0x0001), WinDef.LPARAM(value.toLong()))
            delay(30)
            User32.INSTANCE.SendMessage(this@clickPoint, 0x0202, WinDef.WPARAM(0x0001), WinDef.LPARAM(value.toLong()))
        }
    }

    suspend fun WinDef.HWND.niantie(text: String) {
        log("WinDef.HWND.niantie ")
        var value: Long = 0.toLong()
//        User32.INSTANCE.SendMessage(this, 0x0100, WinDef.WPARAM(0x11), WinDef.LPARAM(value.toLong()))
//        User32.INSTANCE.SendMessage(this, 0x0100, WinDef.WPARAM(0x56), WinDef.LPARAM(value.toLong()))
//        User32.INSTANCE.SendMessage(this, WM_KEYDOWN, WinDef.WPARAM(KeyEvent.VK_CONTROL.toLong()), WinDef.LPARAM(0x20000001))
        User32.INSTANCE.PostMessage(this, WM_KEYDOWN, WinDef.WPARAM(KeyEvent.VK_CONTROL.toLong()), WinDef.LPARAM(0))
        User32.INSTANCE.PostMessage(this, WM_KEYDOWN, WinDef.WPARAM((VK_V).toLong()), WinDef.LPARAM(value.toLong()))
//        User32.INSTANCE.PostMessage(this, WM_SYSKEYUP, WinDef.WPARAM(KeyEvent.VK_CONTROL.toLong()), WinDef.LPARAM(value.toLong()))
//        User32.INSTANCE.SendMessage(this, WM_KEYUP, WinDef.WPARAM(KeyEvent.VK_V.toLong()), WinDef.LPARAM(value.toLong()))
//        User32.INSTANCE.PostMessage(this, WM_KEYUP, WinDef.WPARAM(VK_SPACE.toLong()), WinDef.LPARAM(value.toLong()))
//        User32.INSTANCE.SendMessage(this, WM_KEYUP, WinDef.WPARAM(VK_SPACE.toLong()), WinDef.LPARAM(value.toLong()))
//        delay(30)
//        User32.INSTANCE.SendMessage(this, 0x0101, WinDef.WPARAM(0x11), WinDef.LPARAM(value.toLong()))
//        User32.INSTANCE.SendMessage(this, 0x0101, WinDef.WPARAM(0x56), WinDef.LPARAM(value.toLong()))

    }

}