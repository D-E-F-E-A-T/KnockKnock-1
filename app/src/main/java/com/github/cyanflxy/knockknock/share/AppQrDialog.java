package com.github.cyanflxy.knockknock.share;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import com.github.cyanflxy.knockknock.R;

public class AppQrDialog extends Dialog implements View.OnClickListener {

    private Activity activity;
    private OnShareListener onShareListener;

    public AppQrDialog(Activity activity, OnShareListener l) {
        super(activity, R.style.common_dialog_style);
        this.activity = activity;
        onShareListener = l;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_share_app_qr);

        findViewById(R.id.share_app).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_app:
                dismiss();
                shareApp();
                break;
        }
    }

    private void shareApp() {
        ShareDialog dialog = new ShareDialog(activity, onShareListener);
        dialog.show();
    }
}
