package com.fz.toast

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle

/**
 * Toast 生命周期
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/10/17 9:57
 */
internal class ToastLifecycle private constructor(private val mHandler: ToastQueueHandler) :
        ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        mHandler.onAttach(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        mHandler.onAttach(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        mHandler.onAttach(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        // 一定要在 onPaused 方法中销毁掉，如果在 onDestroyed 方法中还是会导致内存泄露
        mHandler.onPause(activity)
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}
    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        fun register(handler: ToastQueueHandler, context: Context): ToastLifecycle {
            val toastLifecycle = ToastLifecycle(handler)
            val application = context.applicationContext as Application
            application.registerActivityLifecycleCallbacks(toastLifecycle)
            return toastLifecycle
        }
    }

}