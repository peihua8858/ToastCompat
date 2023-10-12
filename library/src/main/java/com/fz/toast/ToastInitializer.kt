package com.fz.toast

import android.content.Context
import androidx.startup.Initializer

/**
 * 工具初始化器
 * @author dingpeihua
 * @date 2023/10/12 15:22
 * @version 1.0
 */
class ToastInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        ToastCompat.initialize(context)
    }

    override fun dependencies() = emptyList<Class<Initializer<*>>>()
}
