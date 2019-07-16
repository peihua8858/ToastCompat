package com.fz.toast;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationManagerCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * toast兼容处理
 * 解决因关闭通知导致系统toast不显示的问题
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/2/22 09:39
 */
public final class ToastCompat {
    static final long SHORT_DURATION_TIMEOUT = 2000;
    static final long LONG_DURATION_TIMEOUT = 3500;
    /**
     * Show the view or text notification for a short period of time.  This time
     * could be user-definable.  This is the default.
     *
     * @see {@link Toast#setDuration}
     */
    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;

    /**
     * Show the view or text notification for a long period of time.  This time
     * could be user-definable.
     *
     * @see {@link Toast#setDuration}
     */
    public static final int LENGTH_LONG = Toast.LENGTH_LONG;

    @IntDef({
            LENGTH_SHORT,
            LENGTH_LONG
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }

    private ToastCompat() {
        throw new AssertionError("Can not initialize.");
    }

    /**
     * 记录是否已经调用过
     */
    private static Activity mActivity;
    static ActivityLifecycleManager lifecycleManager;
    static Context mContext;
    private static Handler mHandler = new Handler();
    static ToastQueueHandler mToastHelper;
    static int mGravity;
    static int mYOffset, mXOffset = 0;
    static int mToastLayout;

    public static void initialize(Context context) {
        if (mContext == null) {
            initializeAttr(context);
            mContext = context.getApplicationContext();
            lifecycleManager = new ActivityLifecycleManager(mContext);
            lifecycleManager.registerCallbacks(new ActivityLifecycleManager.Callbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle bundle) {
                    mActivity = activity;
                }

                @Override
                public void onActivityStarted(Activity activity) {
                    mActivity = activity;
                }

                @Override
                public void onActivityResumed(Activity activity) {
                    mActivity = activity;
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    if (mActivity == activity) {
                        mActivity = null;
                    }
                }
            });
        }
    }

    private static void initializeAttr(Context context) {
        Resources resources = context.getResources();
        if (mYOffset == 0) {
            int yOffsetId = resolveResourceId(context, R.attr.toastDefaultYOffset);
            if (yOffsetId == 0) {
                yOffsetId = R.dimen.toast_default_y_offset;
            }
            mYOffset = resources.getDimensionPixelSize(yOffsetId);
        }
        if (mToastLayout == 0) {
            mToastLayout = resolveResourceId(context, R.attr.ToastLayout);
        }
        if (mGravity == 0) {
            int gravityId = resolveResourceId(context, R.attr.toastDefaultGravity);
            if (gravityId != 0) {
                mGravity = resources.getInteger(gravityId);
            }
            mGravity = mGravity == 0 ? Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL : mGravity;
        }
    }

    static int resolveResourceId(@NonNull Context context, int attrId) {
        if (context instanceof Activity) {
            TypedValue outValue = new TypedValue();
            boolean result = context.getTheme().resolveAttribute(attrId, outValue, true);
            if (result) {
                return outValue.resourceId;
            }
        }
        return 0;
    }

    static Activity getActivity() {
        return mActivity;
    }

    static class ActivityLifecycleManager {
        private final Application application;
        private ActivityLifecycleManager.ActivityLifecycleCallbacksWrapper callbacksWrapper;

        public ActivityLifecycleManager(Context context) {
            this.application = (Application) context.getApplicationContext();
            this.callbacksWrapper = new ActivityLifecycleManager.ActivityLifecycleCallbacksWrapper(this.application);
        }

        public boolean registerCallbacks(ActivityLifecycleManager.Callbacks callbacks) {
            return this.callbacksWrapper != null && this.callbacksWrapper.registerLifecycleCallbacks(callbacks);
        }

        public void resetCallbacks() {
            if (this.callbacksWrapper != null) {
                this.callbacksWrapper.clearCallbacks();
            }
        }

        private static class ActivityLifecycleCallbacksWrapper {
            private final Set<Application.ActivityLifecycleCallbacks> registeredCallbacks = new HashSet();
            private final Application application;

            ActivityLifecycleCallbacksWrapper(Application application) {
                this.application = application;
            }

            private void clearCallbacks() {
                Iterator var1 = this.registeredCallbacks.iterator();

                while (var1.hasNext()) {
                    Application.ActivityLifecycleCallbacks callback = (Application.ActivityLifecycleCallbacks) var1.next();
                    this.application.unregisterActivityLifecycleCallbacks(callback);
                }

            }

            private boolean registerLifecycleCallbacks(final ActivityLifecycleManager.Callbacks callbacks) {
                if (this.application != null) {
                    this.application.registerActivityLifecycleCallbacks(callbacks);
                    this.registeredCallbacks.add(callbacks);
                    return true;
                } else {
                    return false;
                }
            }
        }

        /**
         * activity生命周期回调
         *
         * @author dingpeihua
         * @version 1.0
         * @date 2018/9/6 22:11
         */
        public abstract static class Callbacks implements Application.ActivityLifecycleCallbacks {


            /**
             * activity 创建
             *
             * @param activity
             * @param bundle
             * @author dingpeihua
             * @date 2018/9/6 22:11
             * @version 1.0
             * @see {@link Application.ActivityLifecycleCallbacks#onActivityCreated(Activity, Bundle)}
             */
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
            }

            /**
             * @param activity
             * @author dingpeihua
             * @date 2018/9/6 22:13
             * @version 1.0
             * @see {@link Application.ActivityLifecycleCallbacks#onActivityStarted(Activity)}
             */
            @Override
            public void onActivityStarted(Activity activity) {
            }

            /**
             * @param activity
             * @author dingpeihua
             * @date 2018/9/6 22:13
             * @version 1.0
             * @see {@link Application.ActivityLifecycleCallbacks#onActivityResumed(Activity)}
             */
            @Override
            public void onActivityResumed(Activity activity) {
            }

            /**
             * @param activity
             * @author dingpeihua
             * @date 2018/9/6 22:13
             * @version 1.0
             * @see {@link Application.ActivityLifecycleCallbacks#onActivityPaused(Activity)}
             */
            @Override
            public void onActivityPaused(Activity activity) {
            }

            /**
             * @param activity
             * @author dingpeihua
             * @date 2018/9/6 22:13
             * @version 1.0
             * @see {@link Application.ActivityLifecycleCallbacks#onActivityStopped(Activity)}
             */
            @Override
            public void onActivityStopped(Activity activity) {
            }

            /**
             * @param activity
             * @author dingpeihua
             * @date 2018/9/6 22:13
             * @version 1.0
             * @see {@link Application.ActivityLifecycleCallbacks#onActivitySaveInstanceState(Activity, Bundle)}
             */
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            }

            /**
             * @param activity
             * @author dingpeihua
             * @date 2018/9/6 22:13
             * @version 1.0
             * @see {@link Application.ActivityLifecycleCallbacks#onActivityDestroyed(Activity)}
             */
            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        }
    }


    static boolean checkThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    /**
     * 是否打开通知
     *
     * @param context
     * @author dingpeihua
     * @date 2019/2/22 09:53
     * @version 1.0
     */
    static boolean isNotificationEnabled(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
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
    public static Toast makeText(Context context, CharSequence text, @Duration int duration) {
        return createToast(context, text, duration);
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
    public static Toast makeText(Context context, @StringRes int resId, @Duration int duration)
            throws Resources.NotFoundException {
        return makeText(context, context.getResources().getText(resId), duration);
    }


    /**
     * 短时间自定义显示布局Toast提示,Toast显示在默认的位置
     *
     * @param context  当前上下文
     * @param layoutId 自定义显示布局文件ID
     * @param message  显示字符串文本
     */
    public static Toast makeText(Context context, @LayoutRes int layoutId, @NonNull String message) {
        return makeText(context, LayoutInflater.from(context).inflate(layoutId, null), message);
    }

    public static Toast makeText(Context context, @LayoutRes int layoutId, @StringRes int resId, @Duration int duration) {
        return makeText(context, makeView(context, layoutId), context.getString(resId), duration);
    }

    /**
     * 短时间自定义显示布局Toast提示,Toast显示在默认的位置
     *
     * @param context 当前上下文
     * @param view    自定义view
     * @param message 显示字符串文本
     * @author dingpeihua
     * @date 2019/2/22 10:04
     * @version 1.0
     */
    public static Toast makeText(Context context, View view, @NonNull String message) {
        return makeText(context, view, message, LENGTH_SHORT);
    }

    /**
     * 短时间自定义显示布局Toast提示,Toast显示在默认的位置
     *
     * @param context 当前上下文
     * @param view    自定义view
     * @param message 显示字符串文本
     * @author dingpeihua
     * @date 2019/2/22 10:04
     * @version 1.0
     */
    public static Toast makeText(Context context, View view, @NonNull String message, @Duration int duration) {
        Toast toast = createToast(context, message, duration);
        toast.setView(view);
        toast.setText(message);
        toast.setDuration(duration);
        return toast;
    }

    /**
     * 短时间自定义显示布局Toast提示,Toast显示在默认的位置
     *
     * @param context 当前上下文
     * @param view    自定义view
     * @param message 显示字符串文本
     * @author dingpeihua
     * @date 2019/2/22 10:04
     * @version 1.0
     */
    public static Toast makeText(Context context, View view, @NonNull String message,
                                 @Duration int duration, int gravity, int xOffset, int yOffset) {
        Toast toast = createToast(context, message, duration);
        toast.setView(view);
        toast.setText(message);
        toast.setDuration(duration);
        toast.setGravity(gravity, xOffset, yOffset);
        return toast;
    }

    static Toast createToast(Context context, View view, CharSequence text, @Duration int duration) {
        if (context == null) {
            throw new NullPointerException("Context can not null.");
        }
        if (mContext == null) {
            initialize(context);
        }
        initializeAttr(context);
        if (view == null) {
            view = makeView(context);
        }
        return SupportToast.makeText(context, view, text, duration);
    }

    static Toast createToast(Context context, CharSequence text, @Duration int duration) {
        return createToast(context, null, text, duration);
    }

    static View makeView(Context context) {
        int toastLayout = ToastCompat.mToastLayout;
        if (toastLayout == 0) {
            Resources resources = context.getResources();
            toastLayout = resources.getIdentifier("transient_notification", "layout", "android");
        }
        return makeView(context, toastLayout);
    }

    static View makeDefaultView() {
        return makeView(checkContext());
    }

    static View makeView(Context context, @LayoutRes int layoutId) {
        LayoutInflater inflate = LayoutInflater.from(context);
        View view = inflate.inflate(layoutId, null);
        return view;
    }

    static long checkDuration(int duration) {
        return duration == LENGTH_SHORT ? SHORT_DURATION_TIMEOUT : LONG_DURATION_TIMEOUT;
    }

    /**
     * 短时间Toast提示
     *
     * @param layoutId
     * @param resId    资源ID，在res/string.xml中配置的字符ID
     * @param gravity
     * @param xOffset
     * @param yOffset
     */
    public static void show(@LayoutRes int layoutId, @StringRes int resId, @Duration int duration,
                            int gravity, int xOffset, int yOffset) {
        createToast(ToastMsg.crateBuilder()
                .message(mContext.getString(resId))
                .view(LayoutInflater.from(mContext).inflate(layoutId, null))
                .gravity(gravity)
                .duration(duration)
                .xOffset(xOffset)
                .yOffset(yOffset)
                .build());
    }

    /**
     * 短时间Toast提示
     *
     * @param layoutId
     * @param resId    资源ID，在res/string.xml中配置的字符ID
     * @param gravity
     * @param xOffset
     * @param yOffset
     */
    public static void show(@LayoutRes int layoutId, @StringRes int resId, int gravity, int xOffset, int yOffset) {
        createToast(ToastMsg.crateBuilder()
                .message(mContext.getString(resId))
                .view(LayoutInflater.from(mContext).inflate(layoutId, null))
                .gravity(gravity)
                .xOffset(xOffset)
                .yOffset(yOffset)
                .build());
    }

    /**
     * 短时间Toast提示
     *
     * @param layoutId
     * @param message  提示文本
     * @param gravity
     * @param xOffset
     * @param yOffset
     */
    public static void show(@LayoutRes int layoutId, String message, int gravity, int xOffset, int yOffset) {
        createToast(ToastMsg.crateBuilder()
                .message(message)
                .view(LayoutInflater.from(mContext).inflate(layoutId, null))
                .gravity(gravity)
                .xOffset(xOffset)
                .yOffset(yOffset)
                .build());
    }

    public static void showToast(String message, int gravity) {
        createToast(ToastMsg.crateBuilder()
                .message(message)
                .gravity(gravity)
                .build());
    }

    public static void show(String message, int gravity, int xOffset, int yOffset) {
        createToast(ToastMsg.crateBuilder()
                .message(message)
                .gravity(gravity)
                .xOffset(xOffset)
                .yOffset(yOffset)
                .build());
    }

    public static void show(View view, String message, int gravity, int xOffset, int yOffset) {
        createToast(ToastMsg.crateBuilder()
                .message(message)
                .view(view)
                .gravity(gravity)
                .xOffset(xOffset)
                .yOffset(yOffset)
                .build());
    }

    public static void show(@LayoutRes int layoutId, String message, int gravity, int xOffset) {
        createToast(ToastMsg.crateBuilder()
                .message(message)
                .view(LayoutInflater.from(mContext).inflate(layoutId, null))
                .gravity(gravity)
                .xOffset(xOffset)
                .build());
    }

    public static void show(View view, String message, int gravity, int xOffset) {
        createToast(ToastMsg.crateBuilder()
                .message(message)
                .view(view)
                .gravity(gravity)
                .xOffset(xOffset)
                .build());
    }

    public static void show(@LayoutRes int layoutId, String message, int gravity) {
        createToast(ToastMsg.crateBuilder()
                .message(message)
                .view(LayoutInflater.from(mContext).inflate(layoutId, null))
                .gravity(gravity)
                .build());
    }

    public static void show(@LayoutRes int layoutId, String message) {
        createToast(ToastMsg.crateBuilder()
                .message(message)
                .view(LayoutInflater.from(mContext).inflate(layoutId, null))
                .build());
    }

    public static void show(View view, String message, int gravity) {
        createToast(ToastMsg.crateBuilder()
                .message(message)
                .view(view)
                .gravity(gravity)
                .build());
    }

    public static void show(View view, String message) {
        createToast(ToastMsg.crateBuilder()
                .message(message)
                .view(view)
                .build());
    }

    /**
     * 短时间Toast提示
     *
     * @param message 要提示的信息
     */
    public static void show(@NonNull String message) {
        show(message, Toast.LENGTH_SHORT);
    }

    /**
     * 显示Toast提示
     *
     * @param message  提示文本
     * @param duration 停留时间
     */
    public static void show(String message, @Duration int duration) {
        createToast(ToastMsg.crateBuilder()
                .message(message)
                .duration(duration)
                .build());
    }

    /**
     * 短时间Toast提示
     *
     * @param resId 资源ID，在res/string.xml中配置的字符ID
     */
    public static void show(@StringRes int resId) {
        show(resId, LENGTH_SHORT);
    }

    /**
     * 自定义Toast提示停留时间
     *
     * @param resId    要提示的信息，字符串资源ID
     * @param duration 停留时间毫秒数，以毫秒为单位
     */
    public static void show(@StringRes int resId, @Duration int duration) {
        createToast(ToastMsg.crateBuilder()
                .message(mContext.getString(resId))
                .duration(duration)
                .build());
    }

    /**
     * 自定义Toast提示停留时间
     *
     * @param resId    要提示的信息，字符串资源ID
     * @param duration 停留时间毫秒数，以毫秒为单位
     */
    public static void showToast(@LayoutRes int layoutId, int resId, @Duration int duration) {
        createToast(ToastMsg.crateBuilder()
                .view(LayoutInflater.from(mContext).inflate(layoutId, null))
                .message(mContext.getString(resId))
                .duration(duration)
                .build());
    }

    /**
     * 自定义Toast提示停留时间
     *
     * @param layoutId 停留时间毫秒数，以毫秒为单位
     * @param resId    要提示的信息，字符串资源ID
     */
    public static void showToast(@LayoutRes int layoutId, @StringRes int resId) {
        createToast(ToastMsg.crateBuilder()
                .view(LayoutInflater.from(mContext).inflate(layoutId, null))
                .message(mContext.getString(resId))
                .build());
    }

    static void createToast(ToastMsg msg) {
        if (checkThread()) {
            if (null == mToastHelper) {
                Toast toast = createToast(checkContext(), msg.message, msg.duration);
                mToastHelper = new ToastQueueHandler(toast);
            }
            mToastHelper.add(msg);
            mToastHelper.show();
        } else {
            mHandler.post(() -> createToast(msg));
        }
    }

    static Context checkContext() {
        Activity activity = getActivity();
        if (activity != null) {
            return activity;
        }
        return mContext;
    }
}
