package com.github.cyanflxy.knockknock.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.cyanflxy.knockknock.AppApplication;

public class DataSharedPreferences {

    private static final String DATA_PREF_NAME = "data";
    private static SharedPreferences dataSp = AppApplication.baseContext.
            getSharedPreferences(DATA_PREF_NAME, Context.MODE_PRIVATE);

    private static final String KEY_MAX_JOKE_ID = "max_joke_id";

    public static void setMaxJokeId(int id) {
        SharedPreferences.Editor editor = dataSp.edit();
        editor.putInt(KEY_MAX_JOKE_ID, id);
        editor.apply();
    }

    public static int getMaxJokeId() {
        return dataSp.getInt(KEY_MAX_JOKE_ID, 0);
    }

    private static final String KEY_LAST_JOKE_PAGE = "last_joke_page";

    public static void setLastJokePage(int id) {
        SharedPreferences.Editor editor = dataSp.edit();
        editor.putInt(KEY_LAST_JOKE_PAGE, id);
        editor.apply();
    }

    public static int getLastJokePage() {
        return dataSp.getInt(KEY_LAST_JOKE_PAGE, 1);
    }

    private static final String KEY_LAST_REFRESH_TIME = "last_refresh_time";

    public static void setLastRefreshTime(long timestamp) {
        SharedPreferences.Editor editor = dataSp.edit();
        editor.putLong(KEY_LAST_REFRESH_TIME, timestamp);
        editor.apply();
    }

    public static String getLastRefreshTime() {
        long time = dataSp.getLong(KEY_LAST_REFRESH_TIME, 0);
        return Utils.formatTime(time);
    }
}
