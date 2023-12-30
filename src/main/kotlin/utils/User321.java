package utils;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.win32.W32APIOptions;

public interface User321 extends User32 {
//    @Structure.FieldOrder({"x","y"})
//    public class Point extends Structure {
//        public Long x;
//        public Long y;
//        public Point(){
//        }
//
//        public Point(Long x, Long y) {
//            this.x = x;
//            this.y = y;
//        }
//    }
    public User321 instance = Native.loadLibrary("user32", User321.class, W32APIOptions.DEFAULT_OPTIONS);

//    WinDef.HWND WindowFromPoint(long point);
//    boolean GetCursorPos(long[] IpPoint);

    void keybd_event(byte bVk, byte bScan, int dwFlags, int dwExtraInfo);
}

