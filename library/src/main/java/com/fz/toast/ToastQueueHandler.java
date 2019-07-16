package com.fz.toast;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Toast 队列辅助类
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/5/15 19:45
 */
final class ToastQueueHandler extends Handler {
    final static int WHAT_SHOW = 22;
    final static int WHAT_HIDE = 23;
    final static int WHAT_ACTIVE = 24;
    Toast mToast;
    View mView;
    /**
     * 当前是否正在执行显示操作
     */
    private volatile boolean isShow;

    public ToastQueueHandler(Toast mToast) {
        super(Looper.getMainLooper());
        this.mToast = mToast;
        mView = ToastCompat.makeDefaultView();
    }

    /**
     * 维护toast的队列
     */
    private BlockingQueue<ToastMsg> mQueue = new LinkedBlockingQueue<>();

    public void add(ToastMsg msg) {
        if (mQueue.isEmpty() || !mQueue.contains(msg)) {
            // 添加一个元素并返回true，如果队列已满，则返回false
            if (!mQueue.offer(msg)) {
                // 移除队列头部元素并添加一个新的元素
                mQueue.poll();
                mQueue.offer(msg);
            }
        }
    }

    void show() {
        if (!isShow) {
            isShow = true;
            sendEmptyMessageDelayed(WHAT_SHOW, DELAY_TIMEOUT);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case WHAT_SHOW:
                ToastMsg toastMsg = mQueue.peek();
                if (toastMsg != null) {
                    handleShow(toastMsg);
                    sendEmptyMessageDelayed(WHAT_ACTIVE, ToastCompat.checkDuration(toastMsg.duration));
                } else {
                    isShow = false;
                }
                break;
            case WHAT_HIDE:
                isShow = false;
                mQueue.clear();
                mToast.cancel();
                break;
            case WHAT_ACTIVE:
                mQueue.poll();
                if (!mQueue.isEmpty()) {
                    sendEmptyMessage(WHAT_SHOW);
                } else {
                    isShow = false;
                }
                break;
            default:
                break;
        }
    }

    void handleShow(ToastMsg msg) {
        mToast.setView(msg.view == null ? mView : msg.view);
        if (msg.duration != null) {
            mToast.setDuration(msg.duration);
        }
        if (msg.duration != null) {
            mToast.setDuration(msg.duration);
        }
        mToast.setText(msg.message);
        mToast.setGravity(msg.gravity, msg.xOffset, msg.yOffset);
        mToast.show();
    }

    /**
     * 延迟时间
     */
    private static final int DELAY_TIMEOUT = 300;

}
