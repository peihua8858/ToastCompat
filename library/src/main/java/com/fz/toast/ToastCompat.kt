package com.fz.toast

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment

@JvmField
internal val mHandler = ToastQueueHandler()
internal lateinit var mContext: Context
/**
 * Return the horizontal margin.
 */
fun Application.initialize() {
    mContext = this
    mHandler.register(this)
}

fun hasContext(context: Context?): Boolean {
    if (context == null) {
        return false
    }
    if (context is Activity) {
        return !context.isFinishing && !context.isDestroyed
    }
    return true
}

fun checkContext(any: Any?): Context? {
    return when {
        any is Activity -> {
            any
        }
        any is View -> {
            any.context
        }
        any is Fragment -> {
            any.context
        }
        any is Dialog -> {
            any.context
        }
        mHandler.mActivity != null -> {
            mHandler.mActivity
        }
        else -> {
            mContext
        }
    }
}

fun Any?.showToast(message: CharSequence) {
    val cxt: Context? = checkContext(this)
    if (hasContext(cxt)) {
        ToastCompat.makeText(cxt).setText(message).show()
    }
}

fun Any?.showLongToast(message: CharSequence) {
    val cxt: Context? = checkContext(this)
    if (hasContext(cxt)) {
        ToastCompat.makeText(cxt).setText(message).setDuration(ToastCompat.LENGTH_LONG).show()
    }
}

fun Any?.showToast(message: CharSequence, duration: Int) {
    val cxt: Context? = checkContext(this)
    if (hasContext(cxt)) {
        ToastCompat.makeText(cxt).setText(message).setDuration(duration).show()
    }
}

fun Any?.showToast(@StringRes resId: Int) {
    val cxt: Context? = checkContext(this)
    if (hasContext(cxt)) {
        ToastCompat.makeText(cxt).setText(resId).show()
    }
}

fun Any?.showLongToast(@StringRes resId: Int) {
    val cxt: Context? = checkContext(this)
    if (hasContext(cxt)) {
        ToastCompat.makeText(cxt).setText(resId).setDuration(ToastCompat.LENGTH_LONG).show()
    }
}

fun Any?.showToast(@StringRes resId: Int, duration: Int) {
    val cxt: Context? = checkContext(this)
    if (hasContext(cxt)) {
        ToastCompat.makeText(cxt).setText(resId).setDuration(duration).show()
    }
}

fun Any?.isNotificationsEnabled(context: Context?): Boolean {
    return context != null && NotificationManagerCompat.from(context).areNotificationsEnabled()
}

/**
 * ????????????,??????7.1 ???token null is not valid
 * ???????????????????????????toast?????????
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/10/16 21:14
 */
class ToastCompat(context: Context) {
    @IntDef(LENGTH_SHORT, LENGTH_LONG)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Duration

    @JvmField
    internal val mHelper: WindowHelper
    private fun compatGetToastDefaultGravity(context: Context): Int {
        val toastDefaultGravityId = Resources.getSystem().getIdentifier("config_toastDefaultGravity",
                "integer", "android")
        return if (toastDefaultGravityId != 0) {
            context.resources.getInteger(toastDefaultGravityId)
        } else {
            Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        }
    }

    /**
     * Show the view for the specified duration.
     */
    fun show() {
        mHandler.addToast(this)
    }

    /**
     * Close the view if it's showing, or don't show it if it isn't showing yet.
     * You do not normally have to call this.  Normally view will disappear on its own
     * after the appropriate duration.
     */
    fun cancel() {
        mHelper.handleHide()
    }

    /**
     * Set the view to show.
     */
    fun setView(view: View?): ToastCompat {
        mHelper.mNextView = view
        return this
    }

    /**
     * Set the view to show.
     */
    fun setView(@LayoutRes layoutRes: Int): ToastCompat {
        WindowHelper.mLayoutRes = layoutRes
        return this
    }

    /**
     * Return the view.
     *
     * @see .setView
     */
    val view: View?
        get() = mHelper.mNextView

    /**
     * Set how long to show the view for.
     *
     * @see .LENGTH_SHORT
     *
     * @see .LENGTH_LONG
     */
    fun setDuration(@Duration duration: Int): ToastCompat {
        mHelper.mDuration = duration
        return this
    }

    /**
     * Return the duration.
     *
     * @see .setDuration
     */
    @get:Duration
    val duration: Int
        get() = mHelper.mDuration

    /**
     * Set the margins of the view.
     *
     * @param horizontalMargin The horizontal margin, in percentage of the
     * container width, between the container's edges and the
     * notification
     * @param verticalMargin   The vertical margin, in percentage of the
     * container height, between the container's edges and the
     * notification
     */
    fun setMargin(horizontalMargin: Float, verticalMargin: Float): ToastCompat {
        mHelper.mHorizontalMargin = horizontalMargin
        mHelper.mVerticalMargin = verticalMargin
        return this
    }

    /**
     * Return the horizontal margin.
     */
    val horizontalMargin: Float
        get() = mHelper.mHorizontalMargin

    /**
     * Return the vertical margin.
     */
    val verticalMargin: Float
        get() = mHelper.mVerticalMargin

    /**
     * Set the location at which the notification should appear on the screen.
     *
     * @see android.view.Gravity
     */
    fun setGravity(gravity: Int, xOffset: Int, yOffset: Int): ToastCompat {
        mHelper.mGravity = gravity
        mHelper.mX = xOffset
        mHelper.mY = yOffset
        return this
    }

    /**
     * Get the location at which the notification should appear on the screen.
     *
     * @see android.view.Gravity
     *
     * @see .getGravity
     */
    val gravity: Int
        get() = mHelper.mGravity

    /**
     * Return the X offset in pixels to apply to the gravity's location.
     */
    val xOffset: Int
        get() = mHelper.mX

    /**
     * Return the Y offset in pixels to apply to the gravity's location.
     */
    val yOffset: Int
        get() = mHelper.mY

    /**
     * Gets the LayoutParams for the Toast window.
     */
    val windowParams: WindowManager.LayoutParams
        get() = mHelper.mParams

    /**
     * Update the text in a Toast that was previously created using one of the makeText() methods.
     *
     * @param resId The new text for the Toast.
     */
    fun setText(@StringRes resId: Int): ToastCompat {
        mHelper.mTextResId = resId
        return this
    }

    /**
     * Update the text in a Toast that was previously created using one of the makeText() methods.
     *
     * @param s The new text for the Toast.
     */
    fun setText(s: CharSequence?): ToastCompat {
        mHelper.mText = s
        return this
    }

    /**
     * Return the horizontal margin.
     */
    companion object {

        /**
         * Show the view or text notification for a short period of time.  This time
         * could be user-definable.  This is the default.
         *
         * @see {@link Toast.setDuration}
         */
        const val LENGTH_SHORT = Toast.LENGTH_SHORT

        /**
         * Show the view or text notification for a long period of time.  This time
         * could be user-definable.
         *
         * @see {@link Toast.setDuration}
         */
        const val LENGTH_LONG = Toast.LENGTH_LONG

        @JvmStatic
        fun initialize(context: Context) {
            mContext = context
            mHandler.register(context)
        }

        /**
         * ??????Toast
         *
         * @param context
         * @author dingpeihua
         * @date 2019/10/25 11:14
         * @version 1.0
         */
        @JvmStatic
        fun register(context: Context) {
            mContext = context
            ToastLifecycle.register(mHandler, context)
        }

        /**
         * Make a standard toast to display using the specified looper.
         * If looper is null, Looper.myLooper() is used.
         */
        @JvmStatic
        fun makeText(context: Context, @LayoutRes layoutRes: Int, text: CharSequence, @Duration duration: Int): ToastCompat {
            val result = ToastCompat(checkedCxt(checkContext(context)))
            result.setText(text)
            result.setView(layoutRes)
            result.setDuration(duration)
            return result
        }

        @JvmStatic
        fun application(): Context? {
            if (!::mContext.isInitialized) {
                return null
            }
            return mContext
        }

        @JvmStatic
        fun checkContext(): Context? {
            return checkContext(application())
        }

        /**
         * Make a standard toast to display using the specified looper.
         * If looper is null, Looper.myLooper() is used.
         */
        @JvmStatic
        fun makeText(context: Context, text: CharSequence, @Duration duration: Int): ToastCompat {
            val result = ToastCompat(checkedCxt(checkContext(context)))
            result.setText(text)
            result.setDuration(duration)
            return result
        }

        /**
         * Make a standard toast that just contains a text view with the text from a resource.
         *
         * @param context  The context to use.  Usually your [android.app.Application]
         * or [android.app.Activity] object.
         * @param resId    The resource id of the string resource to use.  Can be formatted text.
         * @param duration How long to display the message.  Either [.LENGTH_SHORT] or
         * [.LENGTH_LONG]
         * @throws Resources.NotFoundException if the resource can't be found.
         */
        @JvmStatic
        @Throws(NotFoundException::class)
        fun makeText(context: Context?, @StringRes resId: Int, @Duration duration: Int): ToastCompat {
            return makeText(checkContext(checkContext(context))).setText(resId).setDuration(duration)
        }

        /**
         * Make a standard toast to display using the specified looper.
         * If looper is null, Looper.myLooper() is used.
         */
        @JvmStatic
        fun makeText(context: Context, @LayoutRes layoutRes: Int): ToastCompat {
            return ToastCompat(checkedCxt(checkContext(context))).setView(layoutRes)
        }

        /**
         * Make a standard toast to display using the specified looper.
         * If looper is null, Looper.myLooper() is used.
         */
        @JvmStatic
        fun makeText(context: Context?): ToastCompat {
            return ToastCompat(checkedCxt(checkContext(context)))
        }

        /**
         * Check current context
         *
         * @param context
         * @author dingpeihua
         * @date 2019/10/25 11:31
         * @version 1.0
         */
        @JvmStatic
        fun checkedCxt(context: Context?): Context {
            if (context == null) {
                throw NullPointerException("Please call the registration method to register first.")
            }
            return context
        }

        /**
         * ??????Toast??????
         *
         * @param message  ????????????
         * @param duration ????????????
         */
        @JvmStatic
        private fun showToast(context: Context, message: CharSequence, duration: Int) {
            makeText(context).setText(message).setDuration(duration).show()
        }

        /**
         * ??????Toast??????
         *
         * @param resId ??????????????????ID
         */
        @JvmStatic
        private fun showToast(context: Context, resId: Int) {
            makeText(context).setText(resId).show()
        }

        /**
         * ?????????Toast??????
         *
         * @param message ??????????????????
         */
        @JvmStatic
        fun showShortMessage(context: Context?, message: CharSequence) {
            makeText(context).setText(message).show()
        }

        /**
         * ?????????Toast??????
         *
         * @param resId ??????ID??????res/string.xml??????????????????ID
         */
        @JvmStatic
        fun showShortMessage(context: Context?, @StringRes resId: Int) {
            makeText(context).setText(resId).show()
        }

        /**
         * ??????????????????????????????Toast??????,Toast????????????????????????
         *
         * @param layoutId ???????????????????????????ID
         * @param message  ?????????????????????
         */
        @JvmStatic
        fun showShortMessage(context: Context?, @LayoutRes layoutId: Int, message: CharSequence) {
            makeText(context).setView(layoutId).setText(message).show()
        }

        /**
         * ?????????Toast??????
         *
         * @param message ??????????????????
         */
        @JvmStatic
        fun showLongMessage(context: Context?, message: CharSequence) {
            makeText(context).setText(message).setDuration(LENGTH_LONG).show()
        }

        /**
         * ?????????Toast??????
         *
         * @param resId ??????ID
         */
        @JvmStatic
        fun showLongMessage(context: Context?, @StringRes resId: Int) {
            makeText(context).setText(resId).setDuration(LENGTH_LONG).show()
        }

        /**
         * ?????????Toast??????????????????
         *
         * @param message  ??????????????????
         * @param duration ??????????????????????????????????????????
         */
        @JvmStatic
        fun showMessage(context: Context?, message: CharSequence, duration: Int) {
            makeText(context).setText(message).setDuration(duration).show()
        }

        /**
         * ?????????Toast??????
         *
         * @param resId ??????ID
         */
        @JvmStatic
        fun showMessage(context: Context?, @StringRes resId: Int) {
            makeText(context).setText(resId).show()
        }

        /**
         * ?????????Toast??????????????????
         *
         * @param message ??????????????????
         */
        @JvmStatic
        fun showMessage(context: Context?, message: CharSequence) {
            makeText(context).setText(message).setDuration(LENGTH_SHORT).show()
        }

        /**
         * ?????????Toast??????????????????
         *
         * @param resId    ????????????????????????????????????ID
         * @param duration ??????????????????????????????????????????
         */
        @JvmStatic
        fun showToast(context: Context?, @StringRes resId: Int, duration: Int) {
            makeText(context).setText(resId).setDuration(duration).show()
        }

        /**
         * ??????Toast??????
         *
         * @param message  ????????????
         * @param duration ????????????
         */
        @JvmStatic
        private fun showToast(message: CharSequence, duration: Int) {
            makeText(mHandler.application).setText(message).setDuration(duration).show()
        }

        /**
         * ??????Toast??????
         *
         * @param resId ??????????????????ID
         */
        @JvmStatic
        private fun showToast(resId: Int) {
            makeText(mHandler.application).setText(resId).show()
        }

        /**
         * ?????????Toast??????
         *
         * @param message ??????????????????
         */
        @JvmStatic
        fun showShortMessage(message: CharSequence) {
            makeText(mHandler.application).setText(message).show()
        }

        /**
         * ?????????Toast??????
         *
         * @param resId ??????ID??????res/string.xml??????????????????ID
         */
        @JvmStatic
        fun showShortMessage(@StringRes resId: Int) {
            makeText(mHandler.application).setText(resId).show()
        }

        /**
         * ??????????????????????????????Toast??????,Toast????????????????????????
         *
         * @param layoutId ???????????????????????????ID
         * @param message  ?????????????????????
         */
        @JvmStatic
        fun showShortMessage(@LayoutRes layoutId: Int, message: CharSequence) {
            makeText(mHandler.application).setView(layoutId).setText(message).show()
        }

        /**
         * ?????????Toast??????
         *
         * @param message ??????????????????
         */
        @JvmStatic
        fun showLongMessage(message: CharSequence) {
            makeText(mHandler.application).setText(message).setDuration(LENGTH_LONG).show()
        }

        /**
         * ?????????Toast??????
         *
         * @param resId ??????ID
         */
        @JvmStatic
        fun showLongMessage(@StringRes resId: Int) {
            makeText(mHandler.application).setText(resId).setDuration(LENGTH_LONG).show()
        }

        /**
         * ?????????Toast??????????????????
         *
         * @param message  ??????????????????
         * @param duration ??????????????????????????????????????????
         */
        @JvmStatic
        fun showMessage(message: CharSequence, duration: Int) {
            makeText(mHandler.application).setText(message).setDuration(duration).show()
        }

        /**
         * ?????????Toast??????
         *
         * @param resId ??????ID
         */
        @JvmStatic
        fun showMessage(@StringRes resId: Int) {
            makeText(mHandler.application).setText(resId).show()
        }

        /**
         * ?????????Toast??????????????????
         *
         * @param message ??????????????????
         */
        @JvmStatic
        fun showMessage(message: CharSequence) {
            makeText(mHandler.application).setText(message).setDuration(LENGTH_SHORT).show()
        }

        /**
         * ?????????Toast??????????????????
         *
         * @param resId    ????????????????????????????????????ID
         * @param duration ??????????????????????????????????????????
         */
        @JvmStatic
        fun showToast(@StringRes resId: Int, duration: Int) {
            makeText(mHandler.application).setText(resId).setDuration(duration).show()
        }
    }

    /**
     * Construct an empty Toast object.  You must call [.setView] before you
     * can call [.show].
     *
     * @param context The context to use.  Usually your [android.app.Application]
     * or [android.app.Activity] object.
     */
    init {
        val applicationContext = context.applicationContext
        mHelper = WindowHelper(applicationContext)
        val offsetY = applicationContext.resources.getDimensionPixelSize(Resources.getSystem()
                .getIdentifier("toast_y_offset", "dimen", "android"))
        val gravity = compatGetToastDefaultGravity(applicationContext)
        setGravity(gravity, 0, offsetY)
        mHandler.register(context)
    }
}
