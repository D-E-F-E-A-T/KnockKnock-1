package com.github.cyanflxy.knockknock.push;

import android.content.Context;
import android.content.Intent;

import com.baidu.android.pushservice.PushMessageReceiver;
import com.cyanflxy.dapenti.htmlparser.JokeBean;
import com.github.cyanflxy.knockknock.ui.SingleJokeActivity;

import java.util.List;

public class BaiduPushReceiver extends PushMessageReceiver {

//    public static final String BAIDU_PUSH_RECEIVER = "BaiduPushReceiver";

    @Override
    public void onBind(Context context, int errorCode, String appId, String userId, String channelId, String requestId) {
//        Log.i(BAIDU_PUSH_RECEIVER, "errorCode: " + errorCode + "; appId: " + appId + "; userId: " + userId + "; channelId: " + channelId + "; requestId: " + requestId);

    }

    /**
     * 接收透传消息的函数。
     *
     * @param context             上下文
     * @param message             推送的消息
     * @param customContentString 自定义内容,为空或者json字符串
     */
    @Override
    public void onMessage(Context context, String message, String customContentString) {
//        Log.i(BAIDU_PUSH_RECEIVER, "message:" + message + ", customString:" + customContentString);
    }

    /**
     * 接收通知点击的函数。注：推送通知被用户点击前，应用无法通过接口获取通知的内容。
     *
     * @param context             上下文
     * @param title               推送的通知的标题
     * @param description         推送的通知的描述
     * @param customContentString 自定义内容，为空或者json字符串
     */
    @Override
    public void onNotificationClicked(Context context, String title, String description, String customContentString) {
//        Log.i(BAIDU_PUSH_RECEIVER, "title: " + title + "; description: " + description + "; customContentString: " + customContentString);

        JokeBean bean = new JokeBean(0, title, description);
        Intent intent = new Intent(context, SingleJokeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SingleJokeActivity.ARG_JOKE, bean);
        context.startActivity(intent);
    }

    /**
     * setTags() 的回调函数。
     *
     * @param context     上下文
     * @param errorCode   错误码。0表示某些tag已经设置成功；非0表示所有tag的设置均失败。
     * @param successTags 设置成功的tag
     * @param failTags    设置失败的tag
     * @param requestId   分配给对云推送的请求的id
     */
    @Override
    public void onSetTags(Context context, int errorCode, List<String> successTags, List<String> failTags, String requestId) {
//        Log.i(BAIDU_PUSH_RECEIVER, "errorCode: " + errorCode + "; requestId: " + requestId);
    }

    /**
     * delTags() 的回调函数。
     *
     * @param context     上下文
     * @param errorCode   错误码。0表示某些tag已经删除成功；非0表示所有tag均删除失败。
     * @param successTags 成功删除的tag
     * @param failTags    删除失败的tag
     * @param requestId   分配给对云推送的请求的id
     */
    @Override
    public void onDelTags(Context context, int errorCode, List<String> successTags, List<String> failTags, String requestId) {
//        Log.i(BAIDU_PUSH_RECEIVER, "errorCode: " + errorCode + "; requestId: " + requestId);
    }

    /**
     * listTags() 的回调函数。
     *
     * @param context   上下文
     * @param errorCode 错误码。0表示列举tag成功；非0表示失败。
     * @param tags      当前应用设置的所有tag。
     * @param requestId 分配给对云推送的请求的id
     */
    @Override
    public void onListTags(Context context, int errorCode, List<String> tags, String requestId) {
//        Log.i(BAIDU_PUSH_RECEIVER, "errorCode: " + errorCode + "; requestId: " + requestId);
    }

    /**
     * PushManager.stopWork() 的回调函数。
     *
     * @param context   上下文
     * @param errorCode 错误码。0表示从云推送解绑定成功；非0表示失败。
     * @param requestId 分配给对云推送的请求的id
     */
    @Override
    public void onUnbind(Context context, int errorCode, String requestId) {
//        Log.i(BAIDU_PUSH_RECEIVER, "errorCode: " + errorCode + "; requestId: " + requestId);
    }

    @Override
    public void onNotificationArrived(Context arg0, String arg1, String arg2, String arg3) {
//        Log.i(BAIDU_PUSH_RECEIVER, "arg1: " + arg1 + "; arg2: " + arg2 + "; arg3:" + arg3);
    }
}
