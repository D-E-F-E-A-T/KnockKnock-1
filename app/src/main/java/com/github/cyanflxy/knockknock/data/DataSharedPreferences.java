package com.github.cyanflxy.knockknock.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.github.cyanflxy.knockknock.AppApplication;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

public class DataSharedPreferences {

    private static final String DATA_PREF_NAME = "data";
    private static SharedPreferences dataSp = AppApplication.baseContext.
            getSharedPreferences(DATA_PREF_NAME, Context.MODE_PRIVATE);

    private static final String KEY_MAX_JOKE_ID = "max_joke_id";

    public static void setMaxJokeId(int id) {
        Editor editor = dataSp.edit();
        editor.putInt(KEY_MAX_JOKE_ID, id);
        editor.apply();
    }

    public static int getMaxJokeId() {
        return dataSp.getInt(KEY_MAX_JOKE_ID, 0);
    }

    private static final String KEY_LAST_JOKE_PAGE = "last_joke_page";

    public static void setLastJokePage(int id) {
        Editor editor = dataSp.edit();
        editor.putInt(KEY_LAST_JOKE_PAGE, id);
        editor.apply();
    }

    public static int getLastJokePage() {
        return dataSp.getInt(KEY_LAST_JOKE_PAGE, 1);
    }

    private static final String KEY_LAST_REFRESH_TIME = "last_refresh_time";

    public static void setLastRefreshTime(long timestamp) {
        Editor editor = dataSp.edit();
        editor.putLong(KEY_LAST_REFRESH_TIME, timestamp);
        editor.apply();
    }

    public static String getLastRefreshTime() {
        long time = dataSp.getLong(KEY_LAST_REFRESH_TIME, 0);
        return Utils.formatTime(time);
    }


    private static final String KEY_START_TIME = "start_time";

    public static void setStartTime() {
        Editor editor = dataSp.edit();
        editor.putLong(KEY_START_TIME, System.currentTimeMillis());
        editor.apply();
    }

    public static boolean isShowAd() {
        long start = dataSp.getLong(KEY_START_TIME, 0);
        if (System.currentTimeMillis() - start > 36 * 60 * 60 * 1000) {// 36小时之后打开广告
            return true;
        } else {
            return false;
        }
    }

    private static final String KEY_UID = "uid";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_EXPIRES_IN = "expires_in";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    /**
     * 保存 Token 对象到 SharedPreferences。
     *
     * @param token Token 对象
     */
    public static void writeAccessToken(Oauth2AccessToken token) {
        Editor editor = dataSp.edit();
        editor.putString(KEY_UID, token.getUid());
        editor.putString(KEY_ACCESS_TOKEN, token.getToken());
        editor.putString(KEY_REFRESH_TOKEN, token.getRefreshToken());
        editor.putLong(KEY_EXPIRES_IN, token.getExpiresTime());
        editor.apply();
    }

    /**
     * 从 SharedPreferences 读取 Token 信息。
     *
     * @return 返回 Token 对象
     */
    public static Oauth2AccessToken readAccessToken() {
        Oauth2AccessToken token = new Oauth2AccessToken();
        token.setUid(dataSp.getString(KEY_UID, ""));
        token.setToken(dataSp.getString(KEY_ACCESS_TOKEN, ""));
        token.setRefreshToken(dataSp.getString(KEY_REFRESH_TOKEN, ""));
        token.setExpiresTime(dataSp.getLong(KEY_EXPIRES_IN, 0));

        return token;
    }

}
