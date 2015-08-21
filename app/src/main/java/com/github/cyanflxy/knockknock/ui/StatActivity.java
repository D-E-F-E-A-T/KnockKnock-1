package com.github.cyanflxy.knockknock.ui;

import android.app.Activity;

import com.umeng.analytics.MobclickAgent;

public class StatActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
