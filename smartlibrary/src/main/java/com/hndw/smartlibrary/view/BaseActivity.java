package com.hndw.smartlibrary.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/*
import com.hndw.smartlibrary.until.MyContextWrapper;
import com.hndw.smartlibrary.until.PreferenceTool;
import com.hndw.smartlibrary.until.SystemBarTintUtil;
*/

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author ljh
 */
public abstract class BaseActivity<T extends Activity> extends Activity implements Handler.Callback {
    private List<T> trackList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackList.add((T) this);
       // SystemBarTintUtil tintUtil = new SystemBarTintUtil(this);
       // tintUtil.setStatusBarTintEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
       // Context context = checkLocal(newBase);
        //super.attachBaseContext(context);
    }

/*    private Context checkLocal(Context newBase) {
        String language = PreferenceTool.getInstance().getDefaultLanguage();
        Context context = newBase;
        if (language != null && !"".equals(language)) {
            if (language.toLowerCase().equals("zh")) {
                Locale newLocale = Locale.CHINA;
                context = MyContextWrapper.wrap(newBase, newLocale);
            }
        }
        return context;
    }*/

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean handleMessage(Message msg) {

        return false;
    }
}
