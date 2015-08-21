package com.github.cyanflxy.knockknock.share;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.cyanflxy.dapenti.htmlparser.JokeBean;
import com.github.cyanflxy.knockknock.R;
import com.github.cyanflxy.knockknock.data.DataSharedPreferences;
import com.github.cyanflxy.knockknock.data.PictureUtils;
import com.github.cyanflxy.knockknock.statistics.StatUtils;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.utils.Utility;

/**
 * 这是个透明界面，为避免给造成蒙板，需要在任何可能的地方添加finish
 */
public class WeiboAuthActivity extends Activity implements IWeiboHandler.Response {

    public static final String ARG_JOKE = "joke";
    public static final String ARG_START_BY_MYSELF = "start_by_myself";

    public static final String WEIBO_APP_KEY = "1946535056";
    // 应用授权回调页
    public static final String WEIBO_REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    // 应用申请的高级权限
    public static final String WEIBO_SCOPE = "email,"
            + "direct_messages_read,"
            + "direct_messages_write,"
            + "friendships_groups_read,"
            + "friendships_groups_write,"
            + "statuses_to_me_read,"
            + "follow_app_official_microblog,"
            + "invitation_write";

    private SsoHandler mSsoHandler;
    private IWeiboShareAPI weiboShareAPI;
    private JokeBean jokeBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        boolean startByMyself = getIntent().getBooleanExtra(ARG_START_BY_MYSELF, false);
        if (!startByMyself) {
            finish();
            return;
        }

        jokeBean = (JokeBean) getIntent().getSerializableExtra(ARG_JOKE);

        Oauth2AccessToken accessToken = DataSharedPreferences.readAccessToken();
        if (accessToken.isSessionValid()) {
            share();
        } else {
            AuthInfo mAuthInfo = new AuthInfo(this, WEIBO_APP_KEY, WEIBO_REDIRECT_URL, WEIBO_SCOPE);
            mSsoHandler = new SsoHandler(this, mAuthInfo);
            mSsoHandler.authorize(new AuthListener());
        }

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        weiboShareAPI.handleWeiboResponse(intent, this); //当前应用唤起微博分享后，返回当前应用
        finish();
    }

    @Override
    public void onResponse(BaseResponse baseResp) {//接收微客户端博请求的数据。
        switch (baseResp.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                ShareUtil.shareSuccess();
                statistic();
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                ShareUtil.shareFail();
                break;
        }
        finish();
    }

    private void share() {
        weiboShareAPI = WeiboShareSDK.createWeiboAPI(this, WEIBO_APP_KEY);
        weiboShareAPI.registerApp();

        WeiboMultiMessage weiboMessage = getShareMessage();

        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        AuthInfo authInfo = new AuthInfo(this, WEIBO_APP_KEY, WEIBO_REDIRECT_URL, WEIBO_SCOPE);
        Oauth2AccessToken accessToken = DataSharedPreferences.readAccessToken();
        String token = "";
        if (accessToken != null) {
            token = accessToken.getToken();
        }
        weiboShareAPI.sendRequest(this, request, authInfo, token, new WeiboAuthListener() {

            @Override
            public void onWeiboException(WeiboException arg0) {
                ShareUtil.shareFail();
                finish();
            }

            @Override
            public void onComplete(Bundle bundle) {
                Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
                DataSharedPreferences.writeAccessToken(newToken);
                ShareUtil.shareSuccess();
                finish();
                statistic();
            }

            @Override
            public void onCancel() {
                finish();
            }
        });
    }

    private WeiboMultiMessage getShareMessage() {

        if (jokeBean != null) {
            return getJokeMessage();
        } else {
            return getAppMessage();
        }
    }

    private WeiboMultiMessage getJokeMessage() {
        TextObject textObject = new TextObject();
        textObject.text = "【" + jokeBean.title + "】\n"
                + jokeBean.content + "\n"
                + getString(R.string.share_from)
                + getString(R.string.app_name);

        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();//初始化微博的分享消息
        weiboMessage.textObject = textObject;

        return weiboMessage;
    }

    private WeiboMultiMessage getAppMessage() {
        TextObject textObject = new TextObject();
        textObject.text = getString(R.string.share_app_title);

        String qr = PictureUtils.getQRPicture();
        ImageObject imageObject = null;
        if (!TextUtils.isEmpty(qr)) {
            imageObject = new ImageObject();
            imageObject.imagePath = qr;
        }

        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = getString(R.string.app_name);
        mediaObject.description = getString(R.string.app_introduce);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        mediaObject.setThumbImage(bitmap);
        mediaObject.actionUrl = ShareUtil.APK_PAGE;
        mediaObject.defaultText = mediaObject.title;

        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();//初始化微博的分享消息
        weiboMessage.textObject = textObject;
        weiboMessage.imageObject = imageObject;
        weiboMessage.mediaObject = mediaObject;

        return weiboMessage;
    }

    private void statistic() {
        if (jokeBean == null) {
            StatUtils.onShareEvent(StatUtils.EVENT_SHARE_APP, StatUtils.SHARE_WEIBO);
        } else {
            StatUtils.onShareEvent(StatUtils.EVENT_SHARE_JOKE, StatUtils.SHARE_WEIBO);
        }
    }

    class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);

            if (accessToken.isSessionValid()) {
                DataSharedPreferences.writeAccessToken(accessToken);
                share();
            } else {
                Toast.makeText(WeiboAuthActivity.this, R.string.auth_fail, Toast.LENGTH_SHORT).show();
            }
            finish();
        }

        @Override
        public void onCancel() {
            finish();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(WeiboAuthActivity.this, R.string.auth_fail, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

}
