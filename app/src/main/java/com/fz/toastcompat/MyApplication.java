package com.fz.toastcompat;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;

public class MyApplication extends Application {
    static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        getTheme().applyStyle(R.style.AppTheme, true);
//        ToastCompat.register(this);
        LeakCanary.install(this);
    }
}
