package com.fz.toastcompat;

import android.app.Application;

import com.fz.toast.ToastCompat;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ToastCompat.initialize(this);
    }
}
