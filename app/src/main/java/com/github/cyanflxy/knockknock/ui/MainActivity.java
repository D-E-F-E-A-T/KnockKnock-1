package com.github.cyanflxy.knockknock.ui;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.appx.BDBannerAd;
import com.cyanflxy.dapenti.htmlparser.JokeBean;
import com.cyanflxy.dapenti.htmlparser.JokeDownloader;
import com.cyanflxy.dapenti.htmlparser.JokeDownloader.OnJokeDownloadListener;
import com.cyanflxy.dapenti.htmlparser.JokeHrefRequest;
import com.github.cyanflxy.knockknock.BuildConfig;
import com.github.cyanflxy.knockknock.R;
import com.github.cyanflxy.knockknock.ad.AdConstant;
import com.github.cyanflxy.knockknock.data.DataSharedPreferences;
import com.github.cyanflxy.knockknock.data.JokeDataBase;
import com.github.cyanflxy.knockknock.data.Utils;
import com.github.cyanflxy.knockknock.notification.ActiveTimer;
import com.github.cyanflxy.knockknock.share.ShareUtil;
import com.github.cyanflxy.knockknock.statistics.StatUtils;
import com.markmao.pulltorefresh.widget.XListView;
import com.umeng.update.UmengUpdateAgent;

import java.util.List;

public class MainActivity extends StatActivity implements OnItemClickListener, OnClickListener {

    public static final String ARG_FROM_TIMER_NOTIFICATION = "start_from_timer_notification";

    private XListView listView;
    private CursorAdapter listAdapter;
    private JokeDownloader jokeDownloader;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View view = findViewById(R.id.status_padding);
            view.setMinimumHeight(Utils.getStatusBarHeight());
            view.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.share_app).setOnClickListener(this);

        JokeDataBase dataBase = JokeDataBase.getInstance();

        listView = (XListView) findViewById(R.id.list_view);
        listAdapter = new LocalAdapter(this, dataBase.query(), false);
        listView.setAdapter(listAdapter);

        listView.setPullRefreshEnable(true);
        listView.setPullLoadEnable(false);

        listView.setRefreshTime(DataSharedPreferences.getLastRefreshTime());

        listView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                listView.stopRefresh();
                downloadJoke();
                StatUtils.onEvent(StatUtils.EVENT_UPDATE_JOKE);
            }

            @Override
            public void onLoadMore() {
                // unused
            }
        });

        listView.setOnItemClickListener(this);
        jokeDownloader = new JokeDownloader(this, listener);

        showBannerAd();

        // 友盟更新
        UmengUpdateAgent.update(this);
        ActiveTimer.startTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().getBooleanExtra(ARG_FROM_TIMER_NOTIFICATION, false)) {
            listView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    downloadJoke();
                }
            }, 1500);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ViewHolder holder = (ViewHolder) view.getTag();

        if (holder != null) {
            Intent intent = new Intent(this, SingleJokeActivity.class);
            intent.putExtra(SingleJokeActivity.ARG_JOKE, holder.jokeBean);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_app:
                shareApp();
                break;
            case R.id.joke_id:
                copyJokeUrl((JokeBean) v.getTag());
                break;
            case R.id.delete:
                StatUtils.onEvent(StatUtils.EVENT_DELETE_JOKE);
                delete((JokeBean) v.getTag());
                break;
            case R.id.share:
                sharedJoke((JokeBean) v.getTag());
                break;
        }
    }

    private void copyJokeUrl(JokeBean bean) {
        if (bean == null) {
            return;
        }
        String url = JokeHrefRequest.JOKE_URL + bean.id;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            //noinspection deprecation
            android.text.ClipboardManager cmb = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setText(url);
        } else {
            android.content.ClipboardManager cmb = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setPrimaryClip(ClipData.newPlainText(null, url));
        }

        Toast.makeText(this, R.string.copy_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    private void downloadJoke() {
        if (progressDialog == null) {
            progressDialog = initProgressDialog();
        }

        progressDialog.setProgress(0);
        progressDialog.show();

        int lastPage = DataSharedPreferences.getLastJokePage();
        int lastJokeId = DataSharedPreferences.getMaxJokeId();
        jokeDownloader.download(lastPage, lastJokeId);
    }

    private ProgressDialog initProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(R.string.downloading);

        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (jokeDownloader != null) {
                    jokeDownloader.cancel();
                }
            }
        });
        return progressDialog;
    }

    private void shareApp() {
        ShareUtil.shareApp(this);
    }

    private void delete(JokeBean bean) {
        if (bean != null) {
            JokeDataBase.getInstance().delete(bean.id);
            refreshAdapter();
        }
    }

    private void refreshAdapter() {
        Cursor cursor = JokeDataBase.getInstance().query();
        listAdapter.changeCursor(cursor);
    }

    private void sharedJoke(JokeBean bean) {
        if (bean != null) {
            ShareUtil.shareJoke(this, bean);
        }
    }

    private void showBannerAd() {
        BDBannerAd bannerAdView = new BDBannerAd(this, AdConstant.BD_AD_API_KEY, AdConstant.BD_AD_ID_BANNER);
        ViewGroup parent = (ViewGroup) findViewById(R.id.ad);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        parent.addView(bannerAdView,params);
    }

    private OnJokeDownloadListener listener = new OnJokeDownloadListener() {
        @Override
        public void onDownloadError(int lastPage, int maxJokeId) {
            onEnd();

            DataSharedPreferences.setLastJokePage(lastPage);
            DataSharedPreferences.setMaxJokeId(maxJokeId);

            Toast.makeText(MainActivity.this, R.string.pull_data_error, Toast.LENGTH_SHORT).show();

            StatUtils.onRecentJoke(maxJokeId);
        }

        @Override
        public void onNoMoreContent() {
            onEnd();

            Toast.makeText(MainActivity.this, R.string.pull_data_no_more_joke, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDownload(List<JokeBean> list, int lastPage, int maxJokeId) {
            onEnd();

            int count = list.size();

            Toast.makeText(MainActivity.this, getString(R.string.pull_data_success, count), Toast.LENGTH_SHORT).show();
            DataSharedPreferences.setLastJokePage(lastPage);
            DataSharedPreferences.setMaxJokeId(maxJokeId);

            JokeDataBase dataBase = JokeDataBase.getInstance();
            for (JokeBean bean : list) {
                dataBase.insertJoke(bean);
            }

            refreshAdapter();

            listView.setSelection(count);
            StatUtils.onRecentJoke(maxJokeId);
        }

        @Override
        public void onProgress(int progress) {
            progressDialog.setProgress(progress);
        }

        private void onEnd() {
            progressDialog.dismiss();

            long timestamp = System.currentTimeMillis();
            DataSharedPreferences.setLastRefreshTime(timestamp);
            listView.setRefreshTime(Utils.formatTime(timestamp));

        }

    };

    private class LocalAdapter extends CursorAdapter {

        public LocalAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.joke_item, null);

            ViewHolder holder = new ViewHolder();
            holder.idView = (TextView) view.findViewById(R.id.joke_id);
            holder.titleView = (TextView) view.findViewById(R.id.title);
            holder.contentView = (TextView) view.findViewById(R.id.content);
            holder.deleteView = view.findViewById(R.id.delete);
            holder.shareView = view.findViewById(R.id.share);

            holder.idView.setOnClickListener(MainActivity.this);
            holder.deleteView.setOnClickListener(MainActivity.this);
            holder.shareView.setOnClickListener(MainActivity.this);

            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();

            int id = cursor.getInt(JokeDataBase.COLUMN_ID_INDEX);
            String title = cursor.getString(JokeDataBase.COLUMN_TITLE_INDEX);
            String content = cursor.getString(JokeDataBase.COLUMN_CONTENT_INDEX);
            JokeBean jokeBean = new JokeBean(id, title, content);

            holder.idView.setText(String.valueOf(id));
            holder.titleView.setText(title);
            holder.contentView.setText(content);

            holder.idView.setTag(jokeBean);
            holder.deleteView.setTag(jokeBean);
            holder.shareView.setTag(jokeBean);
            holder.jokeBean = jokeBean;

            if (!BuildConfig.DEBUG) {
                holder.idView.setVisibility(View.GONE);
            }
        }
    }

    private class ViewHolder {
        public TextView idView;
        public TextView titleView;
        public TextView contentView;
        public View deleteView;
        public View shareView;
        public JokeBean jokeBean;
    }

}
