/* Copyright (c) 2015 Michael Freeman, All Rights Reserved
 *
 * The contents of this file is dual-licensed under 2
 * alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to
 * the project.
 *
 * You may obtain a copy of the LGPL License at:
 *
 * http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */
package com.sun.jna.platform.win32

import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef.*
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO
import com.sun.jna.platform.win32.WinNT.HANDLE
import java.awt.image.*

/**
 * GDI32 utility API.
 *
 * @author mlfreeman[at]gmail.com
 */
object TTTT {
    private val SCREENSHOT_COLOR_MODEL = DirectColorModel(24, 0x00FF0000, 0xFF00, 0xFF)
    private val SCREENSHOT_BAND_MASKS = intArrayOf(
        SCREENSHOT_COLOR_MODEL.redMask,
        SCREENSHOT_COLOR_MODEL.greenMask,
        SCREENSHOT_COLOR_MODEL.blueMask
    )

    /**
     * Takes a screenshot of the given window
     *
     * @param target The window to target
     *
     * @return the window captured as a screenshot, or null if the BufferedImage
     * doesn't construct properly
     *
     * @throws IllegalStateException if the rectangle from GetWindowRect has a
     * width and/or height of 0. <br></br>
     * if the device context acquired from the original HWND doesn't release
     * properly
     */
    fun getScreenshot(target: HWND?): BufferedImage? {
        val rect = RECT()
        if (!User32.INSTANCE.GetWindowRect(target, rect)) {
            throw Win32Exception(Native.getLastError())
        }
        val jRectangle = rect.toRectangle()
        val windowWidth = jRectangle.width
        val windowHeight = jRectangle.height
        check(!(windowWidth == 0 || windowHeight == 0)) { "Window width and/or height were 0 even though GetWindowRect did not appear to fail." }

        val hdcTarget = User32.INSTANCE.GetDC(target) ?: throw Win32Exception(Native.getLastError())
        var we: Win32Exception? = null

        // device context used for drawing
        var hdcTargetMem: HDC? = null

        // handle to the bitmap to be drawn to
        var hBitmap: HBITMAP? = null

        // original display surface associated with the device context
        var hOriginal: HANDLE? = null

        // final java image structure we're returning.
        var image: BufferedImage? = null
        try {
            hdcTargetMem = GDI32.INSTANCE.CreateCompatibleDC(hdcTarget)
            if (hdcTargetMem == null) {
                throw Win32Exception(Native.getLastError())
            }
            hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcTarget, windowWidth, windowHeight)
            if (hBitmap == null) {
                throw Win32Exception(Native.getLastError())
            }
            hOriginal = GDI32.INSTANCE.SelectObject(hdcTargetMem, hBitmap)
            if (hOriginal == null) {
                throw Win32Exception(Native.getLastError())
            }

            // draw to the bitmap
            if (!GDI32.INSTANCE.BitBlt(hdcTargetMem, 0, 0, windowWidth, windowHeight, hdcTarget, 0, 0, GDI32.SRCCOPY or 0x40000000)) {
                throw Win32Exception(Native.getLastError())
            }
            val bmi = BITMAPINFO()
            bmi.bmiHeader.biWidth = windowWidth
            bmi.bmiHeader.biHeight = -windowHeight
            bmi.bmiHeader.biPlanes = 1
            bmi.bmiHeader.biBitCount = 32
            bmi.bmiHeader.biCompression = WinGDI.BI_RGB
            val buffer = Memory((windowWidth * windowHeight * 4).toLong())
            val resultOfDrawing = GDI32.INSTANCE.GetDIBits(
                hdcTarget, hBitmap, 0, windowHeight, buffer, bmi,
                WinGDI.DIB_RGB_COLORS
            )
            if (resultOfDrawing == 0 || resultOfDrawing == WinError.ERROR_INVALID_PARAMETER) {
                throw Win32Exception(Native.getLastError())
            }
            val bufferSize = windowWidth * windowHeight
            val dataBuffer: DataBuffer = DataBufferInt(buffer.getIntArray(0, bufferSize), bufferSize)
            val raster = Raster.createPackedRaster(
                dataBuffer, windowWidth, windowHeight, windowWidth,
                SCREENSHOT_BAND_MASKS, null
            )
            image = BufferedImage(SCREENSHOT_COLOR_MODEL, raster, false, null)
        } catch (e: Win32Exception) {
            we = e
        } finally {
            if (hOriginal != null) {
                // per MSDN, set the display surface back when done drawing
                val result = GDI32.INSTANCE.SelectObject(hdcTargetMem, hOriginal)
                // failure modes are null or equal to HGDI_ERROR
                if (result == null || WinGDI.HGDI_ERROR == result) {
                    val ex = Win32Exception(Native.getLastError())
                    if (we != null) {
                        ex.addSuppressedReflected(we)
                    }
                    we = ex
                }
            }
            if (hBitmap != null) {
                if (!GDI32.INSTANCE.DeleteObject(hBitmap)) {
                    val ex = Win32Exception(Native.getLastError())
                    if (we != null) {
                        ex.addSuppressedReflected(we)
                    }
                    we = ex
                }
            }
            if (hdcTargetMem != null) {
                // get rid of the device context when done
                if (!GDI32.INSTANCE.DeleteDC(hdcTargetMem)) {
                    val ex = Win32Exception(Native.getLastError())
                    if (we != null) {
                        ex.addSuppressedReflected(we)
                    }
                    we = ex
                }
            }
            if (hdcTarget != null) {
                check(0 != User32.INSTANCE.ReleaseDC(target, hdcTarget)) { "Device context did not release properly." }
            }
        }
        if (we != null) {
            throw we
        }
        return image
    }
}
