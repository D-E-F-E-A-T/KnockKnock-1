package com.github.cyanflxy.knockknock.share;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.github.cyanflxy.knockknock.R;

public class ShareDialog extends Dialog implements View.OnClickListener {

    public ShareDialog(Context context) {
        super(context, R.style.common_dialog_style);
        setCancelable(false);
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
                break;
            case R.id.share_weixin_circle:
                break;
            case R.id.share_weixin_friend:
                break;
            case R.id.share_qq:
                break;
            case R.id.share_qzone:
                break;
            case R.id.cancel:
                dismiss();
                break;
        }
    }
}
