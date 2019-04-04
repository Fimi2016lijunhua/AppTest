package com.hndw.smartlibrary;

import android.app.Application;

//import com.hndw.smartlibrary.until.PreferenceTool;
import com.squareup.leakcanary.LeakCanary;

public class BaseApplication extends Application {
   // private PreferenceTool tool;
    @Override
    public void onCreate() {
        super.onCreate();
       // tool =  PreferenceTool.getInstance();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
