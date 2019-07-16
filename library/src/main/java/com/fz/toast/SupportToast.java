package com.fz.toast;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/**
 * 兼容toast，当关闭通知后，toast依然能显示
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/5/15 18:57
 */
final class SupportToast extends Toast implements Handler.Callback {
    final static int WHAT_HIDE = 23;
    private WindowManager mWindowManager;
    private Handler mHandler = new Handler(this);
    private Context mContext;
    private TextView mTvMessage;
    private CharSequence mText;

    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     *                or {@link Activity} object.
     */
    public SupportToast(Context context) {
        super(context);
        mContext = context;
    }

    /**
     * Make a standard toast that just contains a text view.
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     * @param text     The text to show.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link Toast#LENGTH_SHORT} or
     *                 {@link Toast#LENGTH_LONG}
     */
    public static Toast makeText(@NonNull Context context, View view, @NonNull CharSequence text, int duration) {
        SupportToast result = new SupportToast(context);
        result.mText = text;
        result.setView(view);
        result.setDuration(duration);
        return result;
    }

    /**
     * Make a standard toast that just contains a text view.
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     * @param text     The text to show.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link Toast#LENGTH_SHORT} or
     *                 {@link Toast#LENGTH_LONG}
     */
    public static Toast makeText(@NonNull Context context, @NonNull CharSequence text, int duration) {
        return makeText(context, null, text, duration);
    }

    /**
     * Make a standard toast that just contains a text view with the text from a resource.
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     * @param resId    The resource id of the string resource to use.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link Toast#LENGTH_SHORT} or
     *                 {@link Toast#LENGTH_LONG}
     * @throws Resources.NotFoundException if the resource can't be found.
     */
    public static Toast makeText(Context context, @StringRes int resId, int duration)
            throws Resources.NotFoundException {
        return makeText(context, context.getResources().getText(resId), duration);
    }

    @Override
    public void setGravity(int gravity, int xOffset, int yOffset) {
        super.setGravity(gravity, xOffset, yOffset);
    }


    @Override
    public void setView(View view) {
        super.setView(view);
        mTvMessage = getMessageView(view);
        setText(mText);
    }

    @Override
    public void setText(CharSequence s) {
        mText = s;
        if (mTvMessage == null) {
            mTvMessage = getMessageView(getView());
        }
        if (mTvMessage != null) {
            mTvMessage.setText(s);
        }
    }

    /**
     * 智能获取用于显示消息的 TextView
     */
    static TextView getMessageView(View view) {
        if (view instanceof TextView) {
            return (TextView) view;
        } else {
            View message = view.findViewById(android.R.id.message);
            if (message instanceof TextView) {
                return (TextView) message;
            } else if (view instanceof ViewGroup) {
                TextView textView = findTextView((ViewGroup) view);
                if (textView != null) {
                    return textView;
                }
            }
        }
        // 如果设置的布局没有包含一个 TextView 则抛出异常，必须要包含一个 TextView 作为 MessageView
        throw new IllegalArgumentException("The layout must contain a TextView");
    }

    /**
     * 递归获取 ViewGroup 中的 TextView 对象
     */
    private static TextView findTextView(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View view = group.getChildAt(i);
            if ((view instanceof TextView)) {
                return (TextView) view;
            } else if (view instanceof ViewGroup) {
                TextView textView = findTextView((ViewGroup) view);
                if (textView != null) {
                    return textView;
                }
            }
        }
        return null;
    }

    @Override
    public void cancel() {
        if (ToastCompat.isNotificationEnabled(mContext)) {
            super.cancel();
        } else {
            handleCancel();
        }
    }

    @Override
    public void show() {
        if (ToastCompat.isNotificationEnabled(mContext)) {
            super.show();
        } else {
            handleShow();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case WHAT_HIDE:
                cancel();
                break;
            default:
                break;
        }
        return false;
    }

    private void handleCancel() {
        // 移除之前移除吐司的任务
        mHandler.removeMessages(WHAT_HIDE);
        try {
            // 如果当前 WindowManager 没有附加这个 View 则会抛出异常
            // java.lang.IllegalArgumentException:
            // View=android.widget.TextView not attached to window manager
            if (mWindowManager != null && getView().getParent() != null) {
                mWindowManager.removeView(getView());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void handleShow() {
        View view = getView();
        if (view != null) {
            Context context = mContext;
            if (!(context instanceof Activity)) {
                context = ToastCompat.checkContext();
            }
            if (context instanceof Activity) {
                if (((Activity) context).isFinishing()) {
                    context = ToastCompat.getActivity();
                }
            }
            if (context == null) {
                return;
            }
            String packageName = context.getPackageName();
            WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
            mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mParams.format = PixelFormat.TRANSLUCENT;
            mParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            mParams.windowAnimations = android.R.style.Animation_Toast;
            mParams.setTitle("SupportToast");
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            // We can resolve the Gravity here by using the Locale for getting
            // the layout direction
            final Configuration config = view.getContext().getResources().getConfiguration();
            final int gravity = Gravity.getAbsoluteGravity(getGravity(), config.getLayoutDirection());
            mParams.gravity = gravity;
            if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.FILL_HORIZONTAL) {
                mParams.horizontalWeight = 1.0f;
            }
            if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.FILL_VERTICAL) {
                mParams.verticalWeight = 1.0f;
            }
            mParams.x = getXOffset();
            mParams.y = getYOffset();
            mParams.verticalMargin = getVerticalMargin();
            mParams.horizontalMargin = getHorizontalMargin();
            // We can resolve the Gravity here by using the Locale for getting
            // the layout direction
            mParams.packageName = packageName;
            if (view.getParent() != null) {
                mWindowManager.removeView(view);
            }
            // Since the notification manager service cancels the token right
            // after it notifies us to cancel the toast there is an inherent
            // race and we may attempt to add a window after the token has been
            // invalidated. Let us hedge against that.
            try {
                mWindowManager.addView(view, mParams);
                mHandler.sendEmptyMessageDelayed(WHAT_HIDE, ToastCompat.checkDuration(getDuration()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
