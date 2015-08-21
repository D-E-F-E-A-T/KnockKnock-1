package com.github.cyanflxy.knockknock.share;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.github.cyanflxy.knockknock.R;

public class ShareDialog extends Dialog implements View.OnClickListener {

    private Activity activity;
    private OnShareListener onShareListener;

    public ShareDialog(Activity activity, OnShareListener l) {
        super(activity, R.style.common_dialog_style);
        this.activity = activity;
        onShareListener = l;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.dialog_share_joke);

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);

        findViewById(R.id.share_weibo).setOnClickListener(this);
        findViewById(R.id.share_weixin_circle).setOnClickListener(this);
        findViewById(R.id.share_weixin_friend).setOnClickListener(this);
        findViewById(R.id.share_qq).setOnClickListener(this);
        findViewById(R.id.share_qzone).setOnClickListener(this);

        findViewById(R.id.cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_weibo:
                onShareListener.onShare(activity, ShareUtil.SHARE_TYPE_WEIBO);
                break;
            case R.id.share_weixin_circle:
                onShareListener.onShare(activity, ShareUtil.SHARE_TYPE_WEIXIN_CIRCLE);
                break;
            case R.id.share_weixin_friend:
                onShareListener.onShare(activity, ShareUtil.SHARE_TYPE_WEIXIN_FRIEND);
                break;
            case R.id.share_qq:
                onShareListener.onShare(activity, ShareUtil.SHARE_TYPE_QQ);
                break;
            case R.id.share_qzone:
                onShareListener.onShare(activity, ShareUtil.SHARE_TYPE_QZONE);
                break;
            default:
                break;
        }
        dismiss();
    }

}
