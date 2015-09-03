package com.github.cyanflxy.knockknock.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.baidu.appx.BDInterstitialAd;
import com.cyanflxy.dapenti.htmlparser.JokeBean;
import com.github.cyanflxy.knockknock.R;
import com.github.cyanflxy.knockknock.ad.AdConstant;
import com.github.cyanflxy.knockknock.data.DataSharedPreferences;
import com.github.cyanflxy.knockknock.data.Utils;
import com.github.cyanflxy.knockknock.share.ShareUtil;

import static android.view.WindowManager.LayoutParams;

public class SingleJokeActivity extends StatActivity implements View.OnClickListener {

    public static final String ARG_JOKE = "arg_joke";
    private JokeBean jokeBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        jokeBean = (JokeBean) getIntent().getSerializableExtra(ARG_JOKE);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_single_joke);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View view = findViewById(R.id.status_padding);
            view.setMinimumHeight(Utils.getStatusBarHeight());
            view.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.share_btn).setOnClickListener(this);

        TextView title = (TextView) findViewById(R.id.joke_title);
        title.setText(jokeBean.title);

        TextView content = (TextView) findViewById(R.id.joke_content);
        content.setText(jokeBean.content);

        if (DataSharedPreferences.isShowAd()) {
            showAd();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.share_btn:
                ShareUtil.shareJoke(SingleJokeActivity.this, jokeBean);
                break;
        }
    }

    private void showAd() {

        final BDInterstitialAd appxInterstitialAdView = new BDInterstitialAd(this,
                AdConstant.BD_AD_API_KEY, AdConstant.BD_AD_API_Interstitial);

        appxInterstitialAdView.setAdListener(new BDInterstitialAd.InterstitialAdListener() {

            boolean shown = false;

            @Override
            public void onAdvertisementDataDidLoadFailure() {
            }

            @Override
            public void onAdvertisementDataDidLoadSuccess() {
                if (!shown) {
                    appxInterstitialAdView.showAd();
                    shown = true;
                }
            }

            @Override
            public void onAdvertisementViewDidClick() {
            }

            @Override
            public void onAdvertisementViewDidHide() {
            }

            @Override
            public void onAdvertisementViewDidShow() {
            }

            @Override
            public void onAdvertisementViewWillStartNewIntent() {
            }

        });

        appxInterstitialAdView.loadAd();

    }
}
