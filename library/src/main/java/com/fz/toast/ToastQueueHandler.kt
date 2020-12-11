package com.fz.toast

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import java.util.*

/**
 * 吐司消息队列
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/10/17 11:04
 */
internal class ToastQueueHandler : Handler(Looper.getMainLooper()) {
    /**
     * 维护toast的队列
     */
    private val mQueue: Queue<ToastCompat> = LinkedList()
    var mToast: ToastCompat? = null
    private var isShow = false
    var mActivity: Activity? = null
    var toastLifecycle: ToastLifecycle? = null
    var application: Context? = null
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            WHAT_ENQUEUE_TOAST -> {
                addToast(msg.obj as ToastCompat)
                if (!isShow) {
                    isShow = true
                    sendEmptyMessage(WHAT_NEXT_TOAST)
                }
            }
            WHAT_CANCEL_TOAST -> {
                mQueue.remove(msg.obj)
                if (mToast == msg.obj) {
                    removeMessages(WHAT_NEXT_TOAST)
                    sendEmptyMessage(WHAT_NEXT_TOAST)
                }
            }
            WHAT_NEXT_TOAST -> {
                if (mToast != null) {
                    mToast!!.mHelper.handleHide()
                }
                mToast = mQueue.poll()
                if (mToast != null) {
                    mToast!!.mHelper.mActivity = mActivity
                    mToast!!.mHelper.handleShow()
                    sendEmptyMessageDelayed(WHAT_NEXT_TOAST, if (mToast!!.mHelper.mDuration == ToastCompat.LENGTH_LONG) LONG_DURATION_TIMEOUT else SHORT_DURATION_TIMEOUT)
                }
                if (mQueue.isEmpty()) {
                    isShow = false
                }
            }
            else -> {
            }
        }
    }

    fun addToast(msg: ToastCompat) {
        // 添加一个元素并返回true，如果队列已满，则返回false
        if (!mQueue.offer(msg)) {
            // 移除队列头部元素并添加一个新的元素
            mQueue.poll()
            mQueue.offer(msg)
        }
        if (!isShow) {
            isShow = true
            sendEmptyMessage(WHAT_NEXT_TOAST)
        }
    }

    fun cancel(toast: ToastCompat?) {
        Message.obtain(this, WHAT_CANCEL_TOAST, toast).sendToTarget()
    }

    fun register(context: Context) {
        if (context is Activity) {
            mActivity = context
        }
        application = context.applicationContext
        if (toastLifecycle == null) {
            // 跟随 Activity 的生命周期
            toastLifecycle = ToastLifecycle.register(this, context)
        }
    }

    fun onPause(activity: Activity?) {
        if (activity === mActivity) {
            mActivity = null
        }
        if (mToast != null && mToast!!.mHelper.mActivity === activity) {
            mToast!!.cancel()
            mToast = null
        }
        val iterator: Iterator<ToastCompat> = mQueue.iterator()
        while (iterator.hasNext()) {
            val toastCompat = iterator.next()
            if (toastCompat.mHelper.mActivity === activity) {
                toastCompat.cancel()
                mQueue.remove(toastCompat)
            }
        }
    }

    fun onAttach(activity: Activity?) {
        mActivity = activity
    }

    companion object {
        const val WHAT_ENQUEUE_TOAST = 0x01
        const val WHAT_CANCEL_TOAST = 0x02
        const val WHAT_NEXT_TOAST = 0x03
        const val SHORT_DURATION_TIMEOUT: Long = 2000
        const val LONG_DURATION_TIMEOUT: Long = 3500
    }
}