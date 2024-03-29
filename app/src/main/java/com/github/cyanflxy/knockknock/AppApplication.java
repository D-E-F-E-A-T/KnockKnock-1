package com.github.cyanflxy.knockknock;

import android.app.Application;
import android.content.Context;

import com.github.cyanflxy.knockknock.data.Utils;
import com.github.cyanflxy.knockknock.statistics.StatUtils;

public class AppApplication extends Application {


    public static Context baseContext;

    @Override
    public void onCreate() {
        super.onCreate();

        if (!getPackageName().equals(Utils.getCurProcessName(this))) {
            return;
        }

        baseContext = this.getApplicationContext();
        StatUtils.init();
    }

}
