package com.github.cyanflxy.knockknock.statistics;

import com.github.cyanflxy.knockknock.AppApplication;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import java.util.HashMap;
import java.util.Map;

public class StatUtils {
    public static final String UMENG_APP_KEY = "55d85439e0f55a3753004da8";
    public static final String UMENG_CHANNEL_ID = "Dev";

    public static void init() {
        AnalyticsConfig.setAppkey(StatUtils.UMENG_APP_KEY);
        AnalyticsConfig.setChannel(StatUtils.UMENG_CHANNEL_ID);

        UmengUpdateAgent.setAppkey(StatUtils.UMENG_APP_KEY);
        UmengUpdateAgent.setChannel(StatUtils.UMENG_CHANNEL_ID);
        UmengUpdateAgent.setUpdateCheckConfig(false);
    }

    public static void onEvent(String event) {
        MobclickAgent.onEventValue(AppApplication.baseContext, event, null, 1);
    }

    public static void onShareEvent(String event, String category) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(SHARE_ARG_CATEGORY, category);
        MobclickAgent.onEventValue(AppApplication.baseContext, event, map, 1);
    }

    public static void onRecentJoke(int id) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(RECENT_JOKE_ID, String.valueOf(id));
        MobclickAgent.onEventValue(AppApplication.baseContext, EVENT_RECENT_JOKE, map, 1);
    }

    public static final String EVENT_LAUNCH = "launch";
    public static final String EVENT_NOTIFICATION_LAUNCH = "notification_launch";
    public static final String EVENT_DELETE_JOKE = "delete_joke";
    public static final String EVENT_NOTIFICATION_JOKE = "notification_joke";

    public static final String EVENT_SHARE_APP = "share_app";
    public static final String EVENT_SHARE_JOKE = "share_joke";

    public static final String SHARE_ARG_CATEGORY = "share_category";

    public static final String SHARE_WEIXIN_FRIEND = "share_weixin_friend";
    public static final String SHARE_WEIXIN_CIRCLE = "share_weixin_circle";
    public static final String SHARE_WEIBO = "share_weibo";
    public static final String SHARE_QQ = "share_qq";
    public static final String SHARE_QZONE = "share_qzone";

    public static final String EVENT_UPDATE_JOKE = "update_joke";
    public static final String EVENT_RECENT_JOKE = "recent_joke";
    public static final String RECENT_JOKE_ID = "recent_joke_ID";
}
