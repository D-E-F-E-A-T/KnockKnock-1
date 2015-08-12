package com.github.cyanflxy.knockknock.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.cyanflxy.dapenti.htmlparser.JokeBean;
import com.github.cyanflxy.knockknock.R;
import com.github.cyanflxy.knockknock.data.DataSharedPreferences;
import com.github.cyanflxy.knockknock.data.JokeDataBase;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class SplashActivity extends Activity {

    private static final String API_KEY = "mkdFy97aCrSkHX0RtAkxUdIA";

    private static final int SPLASH_TIME = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        LocalHandler handler = new LocalHandler(this);
        handler.setStartTime(System.currentTimeMillis());

        new Thread(new InitRunnable(this, handler)).start();

        PushManager.startWork(this, PushConstants.LOGIN_TYPE_API_KEY, API_KEY);
    }

    private static class LocalHandler extends Handler {

        private Reference<Activity> localActivity;

        private long startTime;

        public LocalHandler(Activity activity) {
            super();

            localActivity = new WeakReference<Activity>(activity);
        }

        public void setStartTime(long start) {
            startTime = start;
        }

        @Override
        public void handleMessage(Message msg) {
            Activity act = localActivity.get();
            if (act == null) {
                return;
            }

            long current = System.currentTimeMillis();
            int remain = (int) (startTime + SPLASH_TIME - current);
            if (remain > 0) {
                sendEmptyMessageDelayed(0, remain);
            } else {
                act.finish();
                act.startActivity(new Intent(act, MainActivity.class));
            }
        }
    }

    private static class InitRunnable implements Runnable {

        private Context context;
        private Handler endHandler;

        public InitRunnable(Context c, Handler endHandler) {
            context = c.getApplicationContext();
            this.endHandler = endHandler;
        }

        @Override
        public void run() {

            String fileName = context.getFilesDir().getParent()
                    + "/databases/" + JokeDataBase.DATABASE_NAME;

            try {
                if (!new File(fileName).exists()) {
                    copyAssetsToDatabase();
                }
            } finally {
                endHandler.sendEmptyMessage(0);
            }

        }

        private void copyAssetsToDatabase() {

            InputStream is = null;
            StringBuilder sb = new StringBuilder();

            try {
                is = context.getAssets().open("jokes.txt");

                byte[] buff = new byte[4096];
                int len = is.read(buff);

                while (len > 0) {
                    sb.append(new String(buff, 0, len));
                    len = is.read(buff);
                }

            } catch (IOException e) {

                e.printStackTrace();
                return;

            } finally {

                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }

            String jokeJson = sb.toString();
            Gson gson = new Gson();
            JokeBean[] jokes = gson.fromJson(jokeJson, JokeBean[].class);

            int maxId = 0;

            JokeDataBase db = JokeDataBase.getInstance();
            for (JokeBean bean : jokes) {
                db.insertJoke(bean);
                if (bean.id > maxId) {
                    maxId = bean.id;
                }
            }

            DataSharedPreferences.setMaxJokeId(maxId);
            DataSharedPreferences.setLastJokePage(182);//代码编写时的页码
            DataSharedPreferences.setLastRefreshTime(System.currentTimeMillis());

        }
    }

}
