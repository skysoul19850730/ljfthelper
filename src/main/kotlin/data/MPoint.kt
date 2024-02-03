@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package data

import com.sun.jna.platform.win32.GDI32Util
import com.sun.jna.platform.win32.User32Util
import com.sun.jna.platform.win32.WinDef
import sun.awt.Win32GraphicsDevice
import utils.MRobot
import utils.MRobot.houtai
import java.awt.Color
import java.awt.GraphicsEnvironment

class MPoint {
    var x: Int = 0
    var y: Int = 0

    var mColorCaiji: Color? = null

    override fun toString(): String {
        return "point:${super.toString()}  x is $x ,y is $y"
    }

    constructor(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    constructor(x: Int, y: Int, color: Color) {
        this.x = x
        this.y = y
        this.mColorCaiji = color
    }

    constructor()

    suspend fun click(window: WinDef.HWND? = App.tfWindow) {
        MRobot.singleClick(this, window)
    }

    suspend fun clickPc(window: WinDef.HWND? = App.tfWindow) {
        MRobot.singleClickPc(this, window)
    }

    fun isFit(window: WinDef.HWND? = App.tfWindow): Boolean {
//        if(houtai){
//            return GDI32Util.getScreenshot(window).getRGB(x,y) == mColorCaiji?.rgb
//        }
        return MRobot.robot.getPixelColor(x, y) == mColorCaiji
    }
}