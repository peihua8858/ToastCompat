package com.fz.toast;

import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 兼容吐司,解决7.1 报token null is not valid
 * 及关闭通知无法显示toast的问题
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/10/16 21:14
 */
public class ToastCompat {
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

    final WindowHelper mHelper;
    static final ToastQueueHandler mHandler = new ToastQueueHandler();

    public static void initialize(Context context) {
        mHandler.register(context);
    }

    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link android.app.Application}
     *                or {@link android.app.Activity} object.
     */
    public ToastCompat(Context context) {
        Context applicationContext = context.getApplicationContext();
        mHelper = new WindowHelper(applicationContext);
        int offsetY = applicationContext.getResources().getDimensionPixelSize(Resources.getSystem()
                .getIdentifier("toast_y_offset", "dimen", "android"));
        int gravity = compatGetToastDefaultGravity(applicationContext);
        setGravity(gravity, 0, offsetY);
        mHandler.register(context);
    }

    private int compatGetToastDefaultGravity(Context context) {
        int toastDefaultGravityId = Resources.getSystem().getIdentifier("config_toastDefaultGravity",
                "integer", "android");
        if (toastDefaultGravityId != 0) {
            return context.getResources().getInteger(toastDefaultGravityId);
        } else {
            return Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        }
    }

    /**
     * Make a standard toast to display using the specified looper.
     * If looper is null, Looper.myLooper() is used.
     */
    public static ToastCompat makeText(@NonNull Context context, @LayoutRes int layoutRes, @NonNull CharSequence text, @Duration int duration) {
        ToastCompat result = new ToastCompat(checkedContext(context));
        result.setText(text);
        result.setView(layoutRes);
        result.setDuration(duration);
        return result;
    }

    /**
     * Make a standard toast to display using the specified looper.
     * If looper is null, Looper.myLooper() is used.
     */
    public static ToastCompat makeText(@NonNull Context context, @NonNull CharSequence text, @Duration int duration) {
        ToastCompat result = new ToastCompat(checkedContext(context));
        result.setText(text);
        result.setDuration(duration);
        return result;
    }

    /**
     * Make a standard toast that just contains a text view with the text from a resource.
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     * @param resId    The resource id of the string resource to use.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or
     *                 {@link #LENGTH_LONG}
     * @throws Resources.NotFoundException if the resource can't be found.
     */
    public static ToastCompat makeText(Context context, @StringRes int resId, @Duration int duration) throws Resources.NotFoundException {
        return makeText(checkedContext(context)).setText(resId).setDuration(duration);
    }

    /**
     * Make a standard toast to display using the specified looper.
     * If looper is null, Looper.myLooper() is used.
     */
    public static ToastCompat makeText(@NonNull Context context, @LayoutRes int layoutRes) {
        return new ToastCompat(checkedContext(context)).setView(layoutRes);
    }

    /**
     * Make a standard toast to display using the specified looper.
     * If looper is null, Looper.myLooper() is used.
     */
    public static ToastCompat makeText(Context context) {
        ToastCompat result = new ToastCompat(checkedContext(context));
        return result;
    }

    /**
     * Check current context
     *
     * @param context
     * @author dingpeihua
     * @date 2019/10/25 11:31
     * @version 1.0
     */
    static Context checkedContext(Context context) {
        if (context == null && mHandler.application == null) {
            throw new NullPointerException("Please call the registration method to register first.");
        }
        if (context == null) {
            return mHandler.application;
        }
        return context;
    }

    /**
     * Show the view for the specified duration.
     */
    public void show() {
        mHandler.addToast(this);
    }

    /**
     * Close the view if it's showing, or don't show it if it isn't showing yet.
     * You do not normally have to call this.  Normally view will disappear on its own
     * after the appropriate duration.
     */
    public void cancel() {
        mHelper.handleHide();
    }

    /**
     * Set the view to show.
     */
    public ToastCompat setView(View view) {
        mHelper.mNextView = view;
        return this;
    }

    /**
     * Set the view to show.
     */
    public ToastCompat setView(@LayoutRes int layoutRes) {
        mHelper.mLayoutRes = layoutRes;
        return this;
    }

    /**
     * Return the view.
     *
     * @see #setView
     */
    public View getView() {
        return mHelper.mNextView;
    }

    /**
     * Set how long to show the view for.
     *
     * @see #LENGTH_SHORT
     * @see #LENGTH_LONG
     */
    public ToastCompat setDuration(@Duration int duration) {
        mHelper.mDuration = duration;
        return this;
    }

    /**
     * Return the duration.
     *
     * @see #setDuration
     */
    @Duration
    public int getDuration() {
        return mHelper.mDuration;
    }

    /**
     * Set the margins of the view.
     *
     * @param horizontalMargin The horizontal margin, in percentage of the
     *                         container width, between the container's edges and the
     *                         notification
     * @param verticalMargin   The vertical margin, in percentage of the
     *                         container height, between the container's edges and the
     *                         notification
     */
    public ToastCompat setMargin(float horizontalMargin, float verticalMargin) {
        mHelper.mHorizontalMargin = horizontalMargin;
        mHelper.mVerticalMargin = verticalMargin;
        return this;
    }

    /**
     * Return the horizontal margin.
     */
    public float getHorizontalMargin() {
        return mHelper.mHorizontalMargin;
    }

    /**
     * Return the vertical margin.
     */
    public float getVerticalMargin() {
        return mHelper.mVerticalMargin;
    }

    /**
     * Set the location at which the notification should appear on the screen.
     *
     * @see android.view.Gravity
     */
    public ToastCompat setGravity(int gravity, int xOffset, int yOffset) {
        mHelper.mGravity = gravity;
        mHelper.mX = xOffset;
        mHelper.mY = yOffset;
        return this;
    }

    /**
     * Get the location at which the notification should appear on the screen.
     *
     * @see android.view.Gravity
     * @see #getGravity
     */
    public int getGravity() {
        return mHelper.mGravity;
    }

    /**
     * Return the X offset in pixels to apply to the gravity's location.
     */
    public int getXOffset() {
        return mHelper.mX;
    }

    /**
     * Return the Y offset in pixels to apply to the gravity's location.
     */
    public int getYOffset() {
        return mHelper.mY;
    }

    /**
     * Gets the LayoutParams for the Toast window.
     */
    public WindowManager.LayoutParams getWindowParams() {
        return mHelper.mParams;
    }

    /**
     * Update the text in a Toast that was previously created using one of the makeText() methods.
     *
     * @param resId The new text for the Toast.
     */
    public ToastCompat setText(@StringRes int resId) {
        mHelper.mTextResId = resId;
        return this;
    }

    /**
     * Update the text in a Toast that was previously created using one of the makeText() methods.
     *
     * @param s The new text for the Toast.
     */
    public ToastCompat setText(CharSequence s) {
        mHelper.mText = s;
        return this;
    }

    /**
     * 注册Toast
     *
     * @param context
     * @author dingpeihua
     * @date 2019/10/25 11:14
     * @version 1.0
     */
    public static void register(Context context) {
        ToastLifecycle.register(mHandler, context);
    }

    /**
     * 显示Toast提示
     *
     * @param message  提示文本
     * @param duration 停留时间
     */
    private static void showToast(Context context, String message, int duration) {
        ToastCompat.makeText(context).setText(message).setDuration(duration).show();
    }

    /**
     * 显示Toast提示
     *
     * @param resId 提示文本资源ID
     */
    private static void showToast(Context context, int resId) {
        makeText(context).setText(resId).show();
    }

    /**
     * 短时间Toast提示
     *
     * @param message 要提示的信息
     */
    public static void showShortMessage(Context context, @NonNull String message) {
        makeText(context).setText(message).show();
    }

    /**
     * 短时间Toast提示
     *
     * @param resId 资源ID，在res/string.xml中配置的字符ID
     */
    public static void showShortMessage(Context context, @StringRes int resId) {
        makeText(context).setText(resId).show();
    }

    /**
     * 短时间自定义显示布局Toast提示,Toast显示在默认的位置
     *
     * @param layoutId 自定义显示布局文件ID
     * @param message  显示字符串文本
     */
    public static void showShortMessage(Context context, @LayoutRes int layoutId, @NonNull String message) {
        makeText(context).setView(layoutId).setText(message).show();
    }


    /**
     * 长时间Toast提示
     *
     * @param message 要提示的信息
     */
    public static void showLongMessage(Context context, @NonNull String message) {
        makeText(context).setText(message).setDuration(ToastCompat.LENGTH_LONG).show();
    }

    /**
     * 长时间Toast提示
     *
     * @param resId 资源ID
     */
    public static void showLongMessage(Context context, @StringRes int resId) {
        makeText(context).setText(resId).setDuration(ToastCompat.LENGTH_LONG).show();
    }

    /**
     * 自定义Toast提示停留时间
     *
     * @param message  要提示的信息
     * @param duration 停留时间毫秒数，以毫秒为单位
     */
    public static void showMessage(Context context, @NonNull String message, int duration) {
        makeText(context).setText(message).setDuration(duration).show();
    }

    /**
     * 长时间Toast提示
     *
     * @param resId 资源ID
     */
    public static void showMessage(Context context, @StringRes int resId) {
        makeText(context).setText(resId).show();
    }

    /**
     * 自定义Toast提示停留时间
     *
     * @param message 要提示的信息
     */
    public static void showMessage(Context context, @NonNull String message) {
        makeText(context).setText(message).setDuration(ToastCompat.LENGTH_SHORT).show();
    }

    /**
     * 自定义Toast提示停留时间
     *
     * @param resId    要提示的信息，字符串资源ID
     * @param duration 停留时间毫秒数，以毫秒为单位
     */
    public static void showToast(Context context, @StringRes int resId, int duration) {
        makeText(context).setText(resId).setDuration(duration).show();
    }


    /**
     * 显示Toast提示
     *
     * @param message  提示文本
     * @param duration 停留时间
     */
    private static void showToast(String message, int duration) {
        ToastCompat.makeText(mHandler.application).setText(message).setDuration(duration).show();
    }

    /**
     * 显示Toast提示
     *
     * @param resId 提示文本资源ID
     */
    private static void showToast(int resId) {
        makeText(mHandler.application).setText(resId).show();
    }

    /**
     * 短时间Toast提示
     *
     * @param message 要提示的信息
     */
    public static void showShortMessage(@NonNull String message) {
        makeText(mHandler.application).setText(message).show();
    }

    /**
     * 短时间Toast提示
     *
     * @param resId 资源ID，在res/string.xml中配置的字符ID
     */
    public static void showShortMessage(@StringRes int resId) {
        makeText(mHandler.application).setText(resId).show();
    }

    /**
     * 短时间自定义显示布局Toast提示,Toast显示在默认的位置
     *
     * @param layoutId 自定义显示布局文件ID
     * @param message  显示字符串文本
     */
    public static void showShortMessage(@LayoutRes int layoutId, @NonNull String message) {
        makeText(mHandler.application).setView(layoutId).setText(message).show();
    }


    /**
     * 长时间Toast提示
     *
     * @param message 要提示的信息
     */
    public static void showLongMessage(@NonNull String message) {
        makeText(mHandler.application).setText(message).setDuration(ToastCompat.LENGTH_LONG).show();
    }

    /**
     * 长时间Toast提示
     *
     * @param resId 资源ID
     */
    public static void showLongMessage(@StringRes int resId) {
        makeText(mHandler.application).setText(resId).setDuration(ToastCompat.LENGTH_LONG).show();
    }

    /**
     * 自定义Toast提示停留时间
     *
     * @param message  要提示的信息
     * @param duration 停留时间毫秒数，以毫秒为单位
     */
    public static void showMessage(@NonNull String message, int duration) {
        makeText(mHandler.application).setText(message).setDuration(duration).show();
    }

    /**
     * 长时间Toast提示
     *
     * @param resId 资源ID
     */
    public static void showMessage(@StringRes int resId) {
        makeText(mHandler.application).setText(resId).show();
    }

    /**
     * 自定义Toast提示停留时间
     *
     * @param message 要提示的信息
     */
    public static void showMessage(@NonNull String message) {
        makeText(mHandler.application).setText(message).setDuration(ToastCompat.LENGTH_SHORT).show();
    }

    /**
     * 自定义Toast提示停留时间
     *
     * @param resId    要提示的信息，字符串资源ID
     * @param duration 停留时间毫秒数，以毫秒为单位
     */
    public static void showToast(@StringRes int resId, int duration) {
        makeText(mHandler.application).setText(resId).setDuration(duration).show();
    }
}
