package com.fz.toast;

import android.content.Context;

import androidx.core.app.NotificationManagerCompat;

final class Utils {
    public static boolean isNotificationsEnabled(Context context) {
        return context != null && NotificationManagerCompat.from(context).areNotificationsEnabled();
    }
}
