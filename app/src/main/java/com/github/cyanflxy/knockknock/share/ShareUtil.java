package com.github.cyanflxy.knockknock.share;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.cyanflxy.dapenti.htmlparser.JokeBean;
import com.github.cyanflxy.knockknock.R;
import com.github.cyanflxy.knockknock.data.PictureUtils;
import com.github.cyanflxy.knockknock.statistics.StatUtils;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.io.File;
import java.util.ArrayList;

import static com.github.cyanflxy.knockknock.AppApplication.baseContext;

public class ShareUtil {
    public static final String APK_PAGE = "http://apps.wandoujia.com/apps/com.github.cyanflxy.knockknock";
    public static final String DOWNLOAD_APK_URL = "http://apps.wandoujia.com/apps/com.github.cyanflxy.knockknock/download";

    public static final String WEIXIN_APP_ID = "wx14f539fb2d4550df";
    public static final String QQ_APP_ID = "1104757667";

    private static IWXAPI wxapi;
    private static Tencent tencent;

    public static void register() {
        // 向微信注册
        wxapi = WXAPIFactory.createWXAPI(baseContext, WEIXIN_APP_ID, true);
        wxapi.registerApp(WEIXIN_APP_ID);

        tencent = Tencent.createInstance(QQ_APP_ID, baseContext);
    }

    public static final int SHARE_TYPE_WEIXIN_CIRCLE = 1;
    public static final int SHARE_TYPE_WEIXIN_FRIEND = 2;
    public static final int SHARE_TYPE_WEIBO = 3;
    public static final int SHARE_TYPE_QQ = 4;
    public static final int SHARE_TYPE_QZONE = 5;

    // 分享单个笑话
    public static void shareJoke(Activity activity, JokeBean jokeBean) {
        OnShareListener l = new OnShareJokeListener(jokeBean);
        ShareDialog dialog = new ShareDialog(activity, l);
        dialog.show();
    }

    private static class OnShareJokeListener implements OnShareListener {

        private JokeBean jokeBean;

        public OnShareJokeListener(JokeBean joke) {
            jokeBean = joke;
        }

        @Override
        public void onShare(Activity activity, int shareType) {
            switch (shareType) {
                case SHARE_TYPE_WEIXIN_FRIEND:
                    shareJokeToWeiXin(jokeBean, SendMessageToWX.Req.WXSceneSession);
                    break;
                case SHARE_TYPE_WEIXIN_CIRCLE:
                    shareJokeToWeiXin(jokeBean, SendMessageToWX.Req.WXSceneTimeline);
                    break;
                case SHARE_TYPE_WEIBO:
                    shareToWeibo(activity, jokeBean);
                    break;
                case SHARE_TYPE_QQ:
                    shareJokeToQQ(activity, jokeBean);
                    break;
                case SHARE_TYPE_QZONE:
                    shareToQzone(activity, jokeBean);
                    break;
                default:
                    break;
            }
        }
    }

    private static void shareJokeToWeiXin(JokeBean jokeBean, int type) {

        String text = "【" + jokeBean.title + "】\n" + jokeBean.content;

        WXTextObject textObject = new WXTextObject();
        textObject.text = text;

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObject;
        msg.title = jokeBean.title;
        msg.description = jokeBean.content;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = type;

        if (wxapi.sendReq(req)) {
            shareSuccess();
            if (type == SendMessageToWX.Req.WXSceneSession) {
                StatUtils.onShareEvent(StatUtils.EVENT_SHARE_JOKE, StatUtils.SHARE_WEIXIN_FRIEND);
            } else {
                StatUtils.onShareEvent(StatUtils.EVENT_SHARE_JOKE, StatUtils.SHARE_WEIXIN_CIRCLE);
            }
        } else {
            shareFail();
        }
    }

    private static void shareJokeToQQ(Activity activity, JokeBean jokeBean) {
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, baseContext.getResources().getString(R.string.app_name));

        final String picture = PictureUtils.createJokePicture(activity, jokeBean);
        if (TextUtils.isEmpty(picture)) {
            Toast.makeText(activity, R.string.create_bitmap_error, Toast.LENGTH_LONG).show();
            return;
        }

        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, picture);

        tencent.shareToQQ(activity, params, new IUiListener() {
            @Override
            public void onComplete(Object o) {
                deleteFile(picture);
                shareSuccess();
                StatUtils.onShareEvent(StatUtils.EVENT_SHARE_JOKE, StatUtils.SHARE_QQ);
            }

            @Override
            public void onError(UiError uiError) {
                deleteFile(picture);
                shareFail();
            }

            @Override
            public void onCancel() {
                deleteFile(picture);
            }
        });
    }

    private static void shareToQzone(Activity activity, JokeBean jokeBean) {
        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, jokeBean.title);
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, buildJokeSummary(jokeBean.content));
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, APK_PAGE);

        // 二维码下载链接图片
        String qrPicture = PictureUtils.getLogoPicture();

        ArrayList<String> pictureList = new ArrayList<String>();
        if (!TextUtils.isEmpty(qrPicture)) {
            pictureList.add(qrPicture);
        }

        // 提供图片地址
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, pictureList);

        tencent.shareToQzone(activity, params, new IUiListener() {
            @Override
            public void onComplete(Object o) {
                shareSuccess();
                StatUtils.onShareEvent(StatUtils.EVENT_SHARE_JOKE, StatUtils.SHARE_QZONE);
            }

            @Override
            public void onError(UiError uiError) {
                shareFail();
            }

            @Override
            public void onCancel() {
            }
        });
    }

    private static String buildJokeSummary(String content) {
        if (content.length() > 20) {
            content = content.substring(0, 20);
        }
        return content + "...    " + baseContext.getString(R.string.share_joke_summary);
    }

    private static boolean deleteFile(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        File file = new File(fileName);
        return file.delete();
    }

    private static void shareToWeibo(Activity activity, JokeBean jokeBean) {
        Intent intent = new Intent(activity, WeiboAuthActivity.class);
        if (jokeBean != null) {
            intent.putExtra(WeiboAuthActivity.ARG_JOKE, jokeBean);
        }
        intent.putExtra(WeiboAuthActivity.ARG_START_BY_MYSELF, true);
        activity.startActivity(intent);
    }

    // 分享app
    public static void shareApp(Activity activity) {
        OnShareListener listener = new OnShareAppListener();
        AppQrDialog dialog = new AppQrDialog(activity, listener);
        dialog.show();
    }

    private static class OnShareAppListener implements OnShareListener {

        @Override
        public void onShare(Activity activity, int shareType) {
            switch (shareType) {
                case SHARE_TYPE_WEIXIN_FRIEND:
                    shareAppToWeiXin(SendMessageToWX.Req.WXSceneSession);
                    break;
                case SHARE_TYPE_WEIXIN_CIRCLE:
                    shareAppToWeiXin(SendMessageToWX.Req.WXSceneTimeline);
                    break;
                case SHARE_TYPE_WEIBO:
                    shareToWeibo(activity, null);
                    break;
                case SHARE_TYPE_QQ:
                    shareAppToQQ(activity);
                    break;
                case SHARE_TYPE_QZONE:
                    shareAppToQzone(activity);
                    break;
                default:
                    break;
            }
        }
    }

    private static void shareAppToWeiXin(int type) {
        WXWebpageObject webObject = new WXWebpageObject();
        webObject.webpageUrl = APK_PAGE;

        WXMediaMessage msg = new WXMediaMessage(webObject);
        msg.title = baseContext.getString(R.string.share_app_title);
        msg.description = baseContext.getString(R.string.app_introduce);

        Bitmap bitmap = BitmapFactory.decodeResource(baseContext.getResources(), R.drawable.logo);
        msg.setThumbImage(bitmap);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = type;

        if (wxapi.sendReq(req)) {
            shareSuccess();
            if (type == SendMessageToWX.Req.WXSceneSession) {
                StatUtils.onShareEvent(StatUtils.EVENT_SHARE_APP, StatUtils.SHARE_WEIXIN_FRIEND);
            } else {
                StatUtils.onShareEvent(StatUtils.EVENT_SHARE_APP, StatUtils.SHARE_WEIXIN_CIRCLE);
            }
        } else {
            shareFail();
        }
    }

    private static void shareAppToQQ(Activity activity) {
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_APP);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, baseContext.getString(R.string.share_app_title));
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, baseContext.getString(R.string.app_introduce));

        String logo = PictureUtils.getLogoPicture();
        if (!TextUtils.isEmpty(logo)) {
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, logo);
        }

        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, baseContext.getString(R.string.app_name));
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, APK_PAGE);

        tencent.shareToQQ(activity, params, new IUiListener() {
            @Override
            public void onComplete(Object o) {
                shareSuccess();
                StatUtils.onShareEvent(StatUtils.EVENT_SHARE_APP, StatUtils.SHARE_QQ);
            }

            @Override
            public void onError(UiError uiError) {
                shareFail();
            }

            @Override
            public void onCancel() {
            }
        });
    }


    private static void shareAppToQzone(Activity activity) {
        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, baseContext.getString(R.string.app_name));
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, baseContext.getString(R.string.share_app_title));
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, APK_PAGE);

        // 二维码下载链接图片
        String qrPicture = PictureUtils.getLogoPicture();

        ArrayList<String> pictureList = new ArrayList<String>();
        if (!TextUtils.isEmpty(qrPicture)) {
            pictureList.add(qrPicture);
        }

        // 提供图片地址
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, pictureList);

        tencent.shareToQzone(activity, params, new IUiListener() {
            @Override
            public void onComplete(Object o) {
                shareSuccess();
                StatUtils.onShareEvent(StatUtils.EVENT_SHARE_APP, StatUtils.SHARE_QZONE);
            }

            @Override
            public void onError(UiError uiError) {
                shareFail();
            }

            @Override
            public void onCancel() {
            }
        });
    }

    public static void shareSuccess() {
//        Toast.makeText(baseContext, R.string.share_success, Toast.LENGTH_SHORT).show();
    }

    public static void shareFail() {
//        Toast.makeText(baseContext, R.string.share_fail, Toast.LENGTH_SHORT).show();
    }
}
