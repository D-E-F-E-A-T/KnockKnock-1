package com.github.cyanflxy.knockknock.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import static com.github.cyanflxy.knockknock.AppApplication.baseContext;

public class ActiveTimer {

    public static final String ACTION = "com.github.cyanflxy.knockknock.ActiveNotification";
    public static final long START_TIME = 3 * 24 * 60 * 60 * 1000;
    public static final long PERIOD = 24 * 60 * 60 * 1000;

    public static final String KEY_NOTIFICATION_STATE = "notification_state";
    public static final int NOTIFICATION_FLAG_START = 1;
    public static final int NOTIFICATION_FLAG_CANCEL = 0;

    public static void startTimer() {
        cancelTimer();

        Intent intent = new Intent(ACTION);
        intent.putExtra(KEY_NOTIFICATION_STATE, NOTIFICATION_FLAG_START);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                baseContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long startTime = System.currentTimeMillis() + START_TIME;

        // 进行闹铃注册
        AlarmManager manager = (AlarmManager) baseContext.getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, startTime, PERIOD, pendingIntent);

    }

    public static void cancelTimer() {
        Intent intent = new Intent(ACTION);
        intent.putExtra(KEY_NOTIFICATION_STATE, NOTIFICATION_FLAG_CANCEL);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                baseContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // 取消闹铃
        AlarmManager manager = (AlarmManager) baseContext.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }

}
