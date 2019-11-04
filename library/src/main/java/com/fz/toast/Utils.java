package com.fz.toast;

import android.content.Context;

import androidx.core.app.NotificationManagerCompat;

final class Utils {
    public static boolean isNotificationsEnabled(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }
}
