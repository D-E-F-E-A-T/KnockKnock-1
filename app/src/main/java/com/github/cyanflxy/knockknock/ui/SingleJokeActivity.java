package com.github.cyanflxy.knockknock.ui;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyanflxy.dapenti.htmlparser.JokeBean;
import com.github.cyanflxy.knockknock.R;
import com.github.cyanflxy.knockknock.data.Utils;
import com.github.cyanflxy.knockknock.share.ShareJoke;

import static android.view.WindowManager.LayoutParams;

public class SingleJokeActivity extends Activity {

    public static final String ARG_JOKE = "arg_joke";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final JokeBean jokeBean = (JokeBean) getIntent().getSerializableExtra(ARG_JOKE);

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

        View back = findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView share = (ImageView) findViewById(R.id.right_btn);
        share.setImageResource(R.drawable.share_btn);
        share.setVisibility(View.VISIBLE);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareJoke.share(SingleJokeActivity.this, jokeBean);
            }
        });


        TextView title = (TextView) findViewById(R.id.joke_title);
        title.setText(jokeBean.title);

        TextView content = (TextView) findViewById(R.id.joke_content);
        content.setText(jokeBean.content);
    }

}
