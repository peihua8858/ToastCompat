package com.fz.toast;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

/**
 * Toast 生命周期
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/10/17 9:57
 */
final class ToastLifecycle implements Application.ActivityLifecycleCallbacks {
    private final ToastQueueHandler mHandler;

    private ToastLifecycle(ToastQueueHandler handler) {
        mHandler = handler;
    }

    static ToastLifecycle register(ToastQueueHandler handler, Context context) {
        ToastLifecycle toastLifecycle = new ToastLifecycle(handler);
        Application application = (Application) context.getApplicationContext();
        application.registerActivityLifecycleCallbacks(toastLifecycle);
        return toastLifecycle;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        mHandler.onAttach(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mHandler.onAttach(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        mHandler.onAttach(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // 一定要在 onPaused 方法中销毁掉，如果在 onDestroyed 方法中还是会导致内存泄露
        mHandler.onPause(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}
