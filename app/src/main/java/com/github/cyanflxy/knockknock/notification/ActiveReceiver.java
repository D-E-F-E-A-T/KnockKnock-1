package com.github.cyanflxy.knockknock.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.cyanflxy.knockknock.R;
import com.github.cyanflxy.knockknock.ui.MainActivity;
import com.github.cyanflxy.knockknock.ui.SplashActivity;

public class ActiveReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 100012;

    @Override
    public void onReceive(Context context, Intent intent) {

        int state = intent.getIntExtra(ActiveTimer.KEY_NOTIFICATION_STATE, ActiveTimer.NOTIFICATION_FLAG_CANCEL);
        if (state == ActiveTimer.NOTIFICATION_FLAG_CANCEL) {
            return;
        }

        Intent activityIntent = new Intent(context, SplashActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activityIntent.putExtra(MainActivity.ARG_FROM_TIMER_NOTIFICATION, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String app = context.getString(R.string.app_name);
        String tickerText = context.getString(R.string.active_notification_tip);

        @SuppressWarnings("deprecation")
        Notification notification = new Notification(R.drawable.logo, tickerText, System.currentTimeMillis());
        //noinspection deprecation
        notification.setLatestEventInfo(context, app, tickerText, pendingIntent);
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        notificationManager.notify(NOTIFICATION_ID, notification);

    }
}
