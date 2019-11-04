package com.fz.toast;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 吐司消息队列
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/10/17 11:04
 */
class ToastQueueHandler extends Handler {
    static final int WHAT_ENQUEUE_TOAST = 0x01;
    static final int WHAT_CANCEL_TOAST = 0x02;
    static final int WHAT_NEXT_TOAST = 0x03;

    static final long SHORT_DURATION_TIMEOUT = 2000;
    static final long LONG_DURATION_TIMEOUT = 3500;

    public ToastQueueHandler() {
        super(Looper.getMainLooper());
    }

    /**
     * 维护toast的队列
     */
    private Queue<ToastCompat> mQueue = new LinkedList<>();
    ToastCompat mToast;
    private boolean isShow;
    Activity mActivity;
    ToastLifecycle toastLifecycle;
    Context application;

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case WHAT_ENQUEUE_TOAST:
                addToast(((ToastCompat) msg.obj));
                if (!isShow) {
                    isShow = true;
                    sendEmptyMessage(WHAT_NEXT_TOAST);
                }
                break;
            case WHAT_CANCEL_TOAST:
                mQueue.remove(msg.obj);
                if (mToast == msg.obj) {
                    removeMessages(WHAT_NEXT_TOAST);
                    sendEmptyMessage(WHAT_NEXT_TOAST);
                }
                break;
            case WHAT_NEXT_TOAST:
                if (mToast != null) {
                    mToast.mHelper.handleHide();
                }
                mToast = mQueue.poll();
                if (mToast != null) {
                    mToast.mHelper.mActivity = mActivity;
                    mToast.mHelper.handleShow();
                    sendEmptyMessageDelayed(WHAT_NEXT_TOAST, mToast.mHelper.mDuration == ToastCompat.LENGTH_LONG ? LONG_DURATION_TIMEOUT : SHORT_DURATION_TIMEOUT);
                }
                if (mQueue.isEmpty()) {
                    isShow = false;
                }
                break;
            default:
                break;
        }
    }

    public void addToast(ToastCompat msg) {
        // 添加一个元素并返回true，如果队列已满，则返回false
        if (!mQueue.offer(msg)) {
            // 移除队列头部元素并添加一个新的元素
            mQueue.poll();
            mQueue.offer(msg);
        }
        if (!isShow) {
            isShow = true;
            sendEmptyMessage(WHAT_NEXT_TOAST);
        }
    }

    public void cancel(ToastCompat toast) {
        Message.obtain(this, WHAT_CANCEL_TOAST, toast).sendToTarget();
    }

    void register(Context context) {
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
        application = context.getApplicationContext();
        if (toastLifecycle == null) {
            // 跟随 Activity 的生命周期
            toastLifecycle = ToastLifecycle.register(this, context);
        }
    }

    void onPause(Activity activity) {
        if (activity == mActivity) {
            mActivity = null;
        }
        if (mToast != null && mToast.mHelper.mActivity == activity) {
            mToast.cancel();
            mToast = null;
        }
        Iterator<ToastCompat> iterator = mQueue.iterator();
        while (iterator.hasNext()) {
            ToastCompat toastCompat = iterator.next();
            if (toastCompat.mHelper.mActivity == activity) {
                toastCompat.cancel();
                mQueue.remove(toastCompat);
            }
        }
    }

    void onAttach(Activity activity) {
        mActivity = activity;
    }
}
