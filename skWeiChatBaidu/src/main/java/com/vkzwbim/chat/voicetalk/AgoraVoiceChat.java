package com.vkzwbim.chat.voicetalk;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;


import com.vkzwbim.chat.R;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

/**
 * Created by Administrator on 2018/6/15.
 */

public class AgoraVoiceChat
{
    public interface OnErrorMessageTrigged { void OnTrigged(int errCode, String msg); }
    public interface ChatStateEventListener
    {
        void OnOtherUserOffline();          // 对方离开
        void OnOtherUserOnline();
    }

    private static final String LOG_TAG = AgoraVoiceChat.class.getSimpleName();

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;

    public static final int ERROR_NO_PERMISSION = 1;               // 在初始化语聊环境时产生，表示用户拒绝了必须的系统权限
    public static final int ERROR_UNKNOW = 2;                       // 未知错误导致初始化语聊引擎失败。
    public static final int ERROR_INVALID_CHANNEL = 3;             // 要加入一个无效的频道
    public static final int ERROR_NOT_READY = 4;                    // 还没有成功的初始化就加入频道
    public static final int ERROR_REFUSED = 5;                      // 无法发起通话，可能是因为处于另一个通话中，或者创建失败

    private RtcEngine mRtcEngine;// Tutorial Step 1
    private String mChannel = "";
    private Activity activity;
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1

        @Override
        public void onUserOffline(final int uid, final int reason) { // Tutorial Step 4
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft(uid, reason);
                }
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserJoin(uid, elapsed);
                }
            });
        }

        @Override
        public void onUserMuteAudio(final int uid, final boolean muted) { // Tutorial Step 6
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserVoiceMuted(uid, muted);
                }
            });
        }

    };
    private OnErrorMessageTrigged mOnErrorTrigged = null;
    private ChatStateEventListener mChatStateEvent = null;

    /**
     * 构造一个语聊实例
     * @param activity  当前活跃的activity
     * @param errorTrigged 内部错误回调
     */
    public AgoraVoiceChat(Activity activity, OnErrorMessageTrigged errorTrigged)
    {
        if(activity == null)
            throw new NullPointerException("AgoraSDKCenter(Activity activity)::不能传递空的Activity对象！");
        this.activity = activity;
        mOnErrorTrigged = errorTrigged;
    }

    public void destroy()
    {
        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
    }

    /**
     * 初始化语聊环境并进入指定的频道
     * @param channel 初始化成功后要进入的频道，如果在此之前已经调用了setNextChannel函数,这里可以传递null
     */
    public void initAndJoinChannel(String channel)
    {
        if(channel != null)
            this.mChannel = channel;
        // 此处如果检测到已经有权限，则会直接返回true，直接进行初始化操作。
        // 如果发现没有获取到权限，则会发起申请，此处就不会进行初始化
        // 但是在权限申请的回调里如果发现权限申请到了，则会再次进行初始化操作
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
            initAgoraEngineAndJoinChannel();
        }
    }

    /**
     * 设置下一次要进入的频道
     * @param channel
     */
    public void setNextChannel(String channel)
    {
        mChannel = channel;
    }

    /**
     * 向用户请求权限的回调，需要由activity调用
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel();
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                    if(mOnErrorTrigged != null)
                        mOnErrorTrigged.OnTrigged(ERROR_NO_PERMISSION, "No permission for " + Manifest.permission.RECORD_AUDIO);
                }
                break;
            }
        }
    }

    /**
     * 注册内部错误回调
     * @param errorTrigged 错误回调
     */
    public void setErrorTriggedEvent(OnErrorMessageTrigged errorTrigged){ mOnErrorTrigged = errorTrigged; }

    // 设置通话状态事件
    public void setChatStateEvent(ChatStateEventListener chatEvent) {
        mChatStateEvent = chatEvent;
    }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();     // Tutorial Step 1
        joinChannel(mChannel);               // Tutorial Step 2
    }

    // 检测权限，如果已经有权限，则直接返回true，否则向用户申请
    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(activity,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }


    public final void showLongToast(final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity.getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }


    // Tutorial Step 7
    /**
     * 开启/关闭麦克风
     * @param opend true表示开启
     */
    public void onSwitchMic(Boolean opend) {
        mRtcEngine.muteLocalAudioStream(!opend);
    }

    // Tutorial Step 5

    /**
     * 切换扬声器/听筒
     * @param speakerPhone true表示扬声器，false表示听筒
     */
    public void onSwitchSpeakerphone(Boolean speakerPhone) {
        mRtcEngine.setEnableSpeakerphone(speakerPhone);
    }

    // Tutorial Step 3
    /**
     * 挂断
     */
    public void onHangUp() {
        leaveChannel();
    }

    // 主要流程控制 步骤 1
    /**
     * 初始化语聊引擎对象
     */
    private void initializeAgoraEngine() {
        try {

            mRtcEngine = RtcEngine.create(activity.getBaseContext(), activity.getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
            if(mOnErrorTrigged != null)
                mOnErrorTrigged.OnTrigged(ERROR_UNKNOW, Log.getStackTraceString(e));
        }
    }


    /**
     * 加入频道
     * @param channel 要加入的频道标志
     */
    public void joinChannel(String channel) {
        if(channel.isEmpty())
        {
            if(mOnErrorTrigged != null)
                mOnErrorTrigged.OnTrigged(ERROR_INVALID_CHANNEL, "频道名无法表示一个有效的频道");
            return;
        }
        int errCode = mRtcEngine.joinChannel(null, channel, "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
        if(errCode == 0)return;         // 成功，直接返回
        if(mOnErrorTrigged == null)return;
        String msg = "";
        switch(errCode)
        {
            case io.agora.rtc.Constants.ERR_INVALID_ARGUMENT:
                mOnErrorTrigged.OnTrigged(ERROR_INVALID_CHANNEL, "频道名无法表示一个有效的频道");
                break;

            case io.agora.rtc.Constants.ERR_NOT_READY:
                mOnErrorTrigged.OnTrigged(ERROR_NOT_READY, "还没有成功初始化过就尝试加入频道");
                break;

            case io.agora.rtc.Constants.ERR_REFUSED:
                mOnErrorTrigged.OnTrigged(ERROR_REFUSED, "在另一个通话中或者通话创建失败");
            break;

            default:
                mOnErrorTrigged.OnTrigged(ERROR_UNKNOW, "加入频道时产生未知的错误");
                break;
        }
    }

    // 主要流程控制 步骤 3
    /**
     * 离开频道
     */
    public void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    // 主要流程控制 步骤 4  // 对方用户离开
    private void onRemoteUserLeft(int uid, int reason) {
        //showLongToast(String.format(Locale.US, "user %d left %d", (uid & 0xFFFFFFFFL), reason));
        if(mChatStateEvent != null)
            mChatStateEvent.OnOtherUserOffline();
    }

    private void onRemoteUserJoin(int uid, int reason) {
        //showLongToast(String.format(Locale.US, "user %d left %d", (uid & 0xFFFFFFFFL), reason));
        if(mChatStateEvent != null)
            mChatStateEvent.OnOtherUserOnline();
    }


    // 主要流程控制 步骤 6  // 对方客户闭麦
    private void onRemoteUserVoiceMuted(int uid, boolean muted) {
        //showLongToast(  .format(Locale.US, "user %d muted or unmuted %b", (uid & 0xFFFFFFFFL), muted));
    }
}
