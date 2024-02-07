package utils

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.POINT
import com.sun.jna.platform.win32.WinUser
import log
import java.lang.Exception

object Window {


    fun getCursorPos(): POINT? {
        val pos = POINT()
        var p = User321.instance.GetCursorPos(pos)
        if(p){
            return pos
        }
        return null
//        return pos.firstOrNull()
    }

    fun getWindowAtCursor(ip: Long): WinDef.HWND? {
        return  User32.INSTANCE.GetForegroundWindow()
//        return User32.INSTANCE.WindowFromPoint(ip)
//        return null
    }

    fun getWindowText(win: WinDef.HWND): String {
        var chars = CharArray(100)
        User32.INSTANCE.GetWindowText(win, chars, 100)
        return String(chars)
    }

    fun getWindowRect(win: WinDef.HWND): WinDef.RECT {
        val rect = WinDef.RECT()
        User32.INSTANCE.GetWindowRect(win, rect)
        return rect
    }


    fun findWindowWithName(name: String): WinDef.HWND? {
        val wp = User32.INSTANCE.GetDesktopWindow()
        var tfWp: WinDef.HWND? = null
        User32.INSTANCE.EnumChildWindows(wp, object : WinUser.WNDENUMPROC {
            override fun callback(p0: WinDef.HWND, p1: Pointer?): Boolean {
                val length = User32.INSTANCE.GetWindowTextLength(p0)
//                if (length == name.length) {
                    var text = CharArray(length+10)
                    val max = length + 11
                    log("检测到 length $length")
                    User32.INSTANCE.GetWindowText(p0, text, max)
                    var text2 = String(text)
                    log("检测到 $text2")
                    if (text2.contains(name)) {
                        tfWp = p0
                        return false
                    }
//                }
                return true
            }

        }, Pointer.createConstant(0))
        return tfWp
    }

}