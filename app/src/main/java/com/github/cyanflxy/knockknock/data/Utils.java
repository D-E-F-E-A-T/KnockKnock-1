package com.github.cyanflxy.knockknock.data;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static int getStatusBarHeight() {
        int id = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android");
        return Resources.getSystem().getDimensionPixelSize(id);
    }

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }
}
