package com.github.cyanflxy.knockknock.share;

import android.content.Context;

import com.cyanflxy.dapenti.htmlparser.JokeBean;

public class ShareJoke {

    public static void share(Context c,JokeBean jokeBean){
        new ShareDialog(c).show();
    }
}
