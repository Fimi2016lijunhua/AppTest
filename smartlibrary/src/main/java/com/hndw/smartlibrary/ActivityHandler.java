package com.hndw.smartlibrary;


import android.os.Handler;
import android.os.Looper;

public class ActivityHandler {
    public static Handler getMainThreadHandler(Handler.Callback callback) {
        Looper looper = Looper.getMainLooper();
        return new Handler(looper, callback);
    }

    public static Handler getOtherThreadHandler(Looper looper, Handler.Callback callback) {
        return new Handler(looper, callback);
    }
}
