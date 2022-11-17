package com.fz.toast

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.fz.toast.ToastCompat.Companion.application
import com.fz.toast.ToastCompat.Companion.checkContext

/**
 * Window 辅助类
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/10/17 10:09
 */
class WindowHelper internal constructor(context: Context) {
    val mParams: WindowManager.LayoutParams = WindowManager.LayoutParams()
    var mGravity = 0
    var mX = 0
    var mY = 0
    var mHorizontalMargin = 0f
    var mVerticalMargin = 0f
    var mView: View? = null
    var mNextView: View? = null
    var mWM: WindowManager? = null
    var mPackageName: String
    var mActivity: Activity? = null

    @ToastCompat.Duration
    var mDuration = 0
    var mText: CharSequence? = null

    @StringRes
    var mTextResId = 0

    var canDrawOverlays = false
    fun handleShow() {
        if (mView == null && mNextView == null && mActivity == null) {
            handleShowSystemToast(application())
        } else if (mView !== mNextView || mNextView == null) {
            // remove the old view if necessary
            handleHide()
            var context = if (canDrawOverlays) mActivity!!.applicationContext else mActivity
            if (canDrawOverlays) {
                mActivity = null
            }
            if (context == null) {
                if (mNextView == null) {
                    return
                }
                context = mNextView!!.context
            }
            if (handleShowSystemToast(context)) {
                return
            }
            if (mNextView == null) {
                mNextView = makeView(context)
            }
            mView = mNextView
            setViewAttr(mView)
            val packageName = context!!.packageName
            mWM = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            // We can resolve the Gravity here by using the Locale for getting
            // the layout direction
            val config = context.resources.configuration
            val gravity = if (ToastCompat.isChangeOffset) {
                Gravity.getAbsoluteGravity(ToastCompat.mGravity, config.layoutDirection)
            } else {
                Gravity.getAbsoluteGravity(mGravity, config.layoutDirection)
            }
            mParams.gravity = gravity
            if (gravity and Gravity.HORIZONTAL_GRAVITY_MASK == Gravity.FILL_HORIZONTAL) {
                mParams.horizontalWeight = 1.0f
            }
            if (gravity and Gravity.VERTICAL_GRAVITY_MASK == Gravity.FILL_VERTICAL) {
                mParams.verticalWeight = 1.0f
            }
            mParams.x = if (ToastCompat.isChangeOffset) ToastCompat.mXOffset else mX
            mParams.y = if (ToastCompat.isChangeOffset) ToastCompat.mYOffset else mY
            mParams.verticalMargin = mVerticalMargin
            mParams.horizontalMargin = mHorizontalMargin
            mParams.packageName = packageName
            if (mView!!.parent != null) {
                mWM!!.removeView(mView)
            }
            try {
                if (mActivity != null && mActivity!!.isFinishing) {
                    mView = null
                    mNextView = null
                    mActivity = null
                    return
                }
                mWM!!.addView(mView, mParams)
                trySendAccessibilityEvent(context)
            } catch (e: Exception) {
                /* ignore */
                e.printStackTrace()
            }
        }
    }

    fun setViewAttr(view: View?) {
        val tvMessage = getMessageView(view)
        val context = view!!.context
        tvMessage.text = getText(context)
    }

    private fun trySendAccessibilityEvent(context: Context?) {
        val accessibilityManager = context!!.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (!accessibilityManager.isEnabled) {
            return
        }
        // treat toasts as notifications since they are used to
        // announce a transient piece of information to the user
        val event = AccessibilityEvent.obtain(
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
        )
        event.className = javaClass.name
        event.packageName = mPackageName
        mView!!.dispatchPopulateAccessibilityEvent(event)
        accessibilityManager.sendAccessibilityEvent(event)
    }

    fun handleHide() {
        if (mView != null) {
            // note: checking parent() just to make sure the view has
            // been added...  i have seen cases where we get here when
            // the view isn't yet added, so let's try not to crash.
            if (mView!!.parent != null) {
                mWM!!.removeViewImmediate(mView)
            }
            mNextView = null
            mActivity = null
            mView = null
        }
    }

    private fun handleShowSystemToast(cxt: Context?): Boolean {
        var context: Context? = cxt?.applicationContext
        if (context == null) {
            context = checkContext()
        }
        if (context == null) {
            return false
        }
        if (!this.isNotificationsEnabled(context)) {
            Log.e("ToastCompat", "Notifications not Enabled.")
            return false
        }
        val text = getText(context)
        val toast = Toast.makeText(context, text, mDuration)
        val view = makeView(context)
        val textView = getMessageView(view)
        textView.text = text
        toast.view = view
        if (ToastCompat.isChangeOffset) {
            toast.setGravity(ToastCompat.mGravity, ToastCompat.mXOffset, ToastCompat.mYOffset)
        } else if (ToastCompat.mGravity != 0) {
            toast.setGravity(mGravity, mX, mY)
        }
        toast.show()
        return true
    }

    private fun getText(context: Context): CharSequence? {
        if (mText == null && mTextResId != 0) {
            mText = context.getText(mTextResId)
        }
        return mText
    }

    companion object {
        @LayoutRes
        var mLayoutRes = 0
        fun makeView(context: Context?): View {
            if (mLayoutRes == 0) {
                mLayoutRes = resolveResourceId(context!!, R.attr.toast_layout)
            }
            if (mLayoutRes == 0) {
                val resources = context!!.resources
                mLayoutRes = resources.getIdentifier("transient_notification", "layout", "android")
            }
            return makeView(context, mLayoutRes)
        }

        fun makeView(context: Context?, @LayoutRes layoutId: Int): View {
            val inflate = LayoutInflater.from(context)
            return inflate.inflate(layoutId, null)
        }

        fun resolveResourceId(context: Context, attrId: Int): Int {
            val outValue = TypedValue()
            val result = context.theme.resolveAttribute(attrId, outValue, true)
            return if (result) {
                outValue.resourceId
            } else 0
        }

        /**
         * 智能获取用于显示消息的 TextView
         */
        fun getMessageView(view: View?): TextView {
            if (view is TextView) {
                return view
            } else {
                val message = view!!.findViewById<View>(android.R.id.message)
                if (message is TextView) {
                    return message
                } else if (view is ViewGroup) {
                    val textView = findTextView(view)
                    if (textView != null) {
                        return textView
                    }
                }
            }
            throw IllegalArgumentException("The layout must contain a TextView")
        }

        /**
         * 递归获取 ViewGroup 中的 TextView 对象
         */
        private fun findTextView(group: ViewGroup): TextView? {
            for (i in 0 until group.childCount) {
                val view = group.getChildAt(i)
                if (view is TextView) {
                    return view
                } else if (view is ViewGroup) {
                    val textView = findTextView(view)
                    if (textView != null) {
                        return textView
                    }
                }
            }
            return null
        }
    }

    init {
        // XXX This should be changed to use a Dialog, with a Theme.Toast
        // defined that sets up the layout params appropriately.
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        // 8.0以上必须要权限才能设置成全局的悬浮窗，注意需要先申请悬浮窗权限，
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Settings.canDrawOverlays(context)) {
                canDrawOverlays = true
                mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            //android 7.1不能加，否则报token null is not valid;
            mParams.type = WindowManager.LayoutParams.TYPE_TOAST
        }
        mParams.format = PixelFormat.TRANSLUCENT
        mParams.flags = (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mParams.windowAnimations = android.R.style.Animation_Toast
        mParams.title = "ToastCompat"
        mPackageName = context.packageName
    }
}