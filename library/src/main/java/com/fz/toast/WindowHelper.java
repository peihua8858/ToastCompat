package com.fz.toast;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/**
 * Window 辅助类
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/10/17 10:09
 */
class WindowHelper {
    final WindowManager.LayoutParams mParams;
    int mGravity;
    int mX, mY;
    float mHorizontalMargin;
    float mVerticalMargin;
    View mView;
    View mNextView;
    WindowManager mWM;
    String mPackageName;
    Activity mActivity;
    @ToastCompat.Duration
    int mDuration;
    @LayoutRes
    static int mLayoutRes;
    CharSequence mText;
    @StringRes
    int mTextResId;
    boolean canDrawOverlays = false;

    WindowHelper(Context context) {
        // XXX This should be changed to use a Dialog, with a Theme.Toast
        // defined that sets up the layout params appropriately.
        mParams = new WindowManager.LayoutParams();
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        // 8.0以上必须要权限才能设置成全局的悬浮窗，注意需要先申请悬浮窗权限，
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Settings.canDrawOverlays(context)) {
                canDrawOverlays = true;
                mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            //android 7.1不能加，否则报token null is not valid;
            mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }

        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        mParams.windowAnimations = android.R.style.Animation_Toast;
        mParams.setTitle("ToastCompat");
        this.mPackageName = context.getPackageName();
    }


    public void handleShow() {
        if (mView == null && mNextView == null && mActivity == null) {
            handleShowSystemToast();
        } else if (mView != mNextView || mNextView == null) {
            // remove the old view if necessary
            handleHide();
            Context context = canDrawOverlays ? mActivity.getApplicationContext() : mActivity;
            if (canDrawOverlays) {
                mActivity = null;
            }
            if (context == null) {
                if (mNextView == null) {
                    return;
                }
                context = mNextView.getContext();
            }
            if (mNextView == null) {
                mNextView = makeView(context);
            }
            mView = mNextView;
            setViewAttr(mView);
            String packageName = context.getPackageName();
            mWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            // We can resolve the Gravity here by using the Locale for getting
            // the layout direction
            final Configuration config = context.getResources().getConfiguration();
            final int gravity = Gravity.getAbsoluteGravity(mGravity, config.getLayoutDirection());
            mParams.gravity = gravity;
            if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.FILL_HORIZONTAL) {
                mParams.horizontalWeight = 1.0f;
            }
            if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.FILL_VERTICAL) {
                mParams.verticalWeight = 1.0f;
            }
            mParams.x = mX;
            mParams.y = mY;
            mParams.verticalMargin = mVerticalMargin;
            mParams.horizontalMargin = mHorizontalMargin;
            mParams.packageName = packageName;
            if (mView.getParent() != null) {
                mWM.removeView(mView);
            }
            try {
                if (mActivity != null && mActivity.isFinishing()) {
                    mView = null;
                    mNextView = null;
                    mActivity = null;
                    return;
                }
                mWM.addView(mView, mParams);
                trySendAccessibilityEvent(context);
            } catch (Exception e) {
                /* ignore */
                e.printStackTrace();
            }
        }
    }

    void setViewAttr(View view) {
        TextView tvMessage = getMessageView(view);
        Context context = view.getContext();
        tvMessage.setText(getText(context));
    }

    private void trySendAccessibilityEvent(Context context) {
        AccessibilityManager accessibilityManager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (!accessibilityManager.isEnabled()) {
            return;
        }
        // treat toasts as notifications since they are used to
        // announce a transient piece of information to the user
        AccessibilityEvent event = AccessibilityEvent.obtain(
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
        event.setClassName(getClass().getName());
        event.setPackageName(mPackageName);
        mView.dispatchPopulateAccessibilityEvent(event);
        accessibilityManager.sendAccessibilityEvent(event);
    }

    public void handleHide() {
        if (mView != null) {
            // note: checking parent() just to make sure the view has
            // been added...  i have seen cases where we get here when
            // the view isn't yet added, so let's try not to crash.
            if (mView.getParent() != null) {
                mWM.removeViewImmediate(mView);
            }
            mNextView = null;
            mActivity = null;
            mView = null;
        }
    }

    static View makeView(Context context) {
        if (mLayoutRes == 0) {
            mLayoutRes = resolveResourceId(context, R.attr.toast_layout);
        }
        if (mLayoutRes == 0) {
            Resources resources = context.getResources();
            mLayoutRes = resources.getIdentifier("transient_notification", "layout", "android");
        }
        return makeView(context, mLayoutRes);
    }

    static View makeView(Context context, @LayoutRes int layoutId) {
        LayoutInflater inflate = LayoutInflater.from(context);
        View view = inflate.inflate(layoutId, null);
        return view;
    }

    static int resolveResourceId(@NonNull Context context, int attrId) {
        TypedValue outValue = new TypedValue();
        boolean result = context.getTheme().resolveAttribute(attrId, outValue, true);
        if (result) {
            return outValue.resourceId;
        }
        return 0;
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
            final View view = group.getChildAt(i);
            if ((view instanceof TextView)) {
                return (TextView) view;
            } else if (view instanceof ViewGroup) {
                final TextView textView = findTextView((ViewGroup) view);
                if (textView != null) {
                    return textView;
                }
            }
        }
        return null;
    }

    public void handleShowSystemToast() {
        if (!Utils.isNotificationsEnabled(ToastCompat.mHandler.application)) {
            Log.e("ToastCompat", "Notifications not Enabled.");
            return;
        }
        CharSequence text = getText(ToastCompat.mHandler.application);
        Toast toast = Toast.makeText(ToastCompat.mHandler.application, text, mDuration);
        View view = makeView(ToastCompat.mHandler.application);
        TextView textView = getMessageView(view);
        textView.setText(text);
        toast.setView(view);
        if (mGravity != 0) {
            toast.setGravity(mGravity, mX, mY);
        }
        toast.show();
    }

    private CharSequence getText(Context context) {
        if (mText == null && mTextResId != 0) {
            mText = context.getText(mTextResId);
        }
        return mText;
    }
}
