package com.vkzwbim.chat.voicetalk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.cjt2325.cameralibrary.util.LogUtil;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.broadcast.OtherBroadcast;
import com.vkzwbim.chat.helper.AvatarHelper;
import com.vkzwbim.chat.util.Constants;
import com.vkzwbim.chat.util.GlideImageUtils;
import com.vkzwbim.chat.util.PreferenceUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;


public class Gukevoice extends Activity {
    private SimpleDateFormat sd;
    private ImageView photo, voice_gkjy, voice_gkgd, voice_gkmt, btnGift, btnRecharge;
    private TextView nickname, voice_gkjishi;
    private View callingControl;
    private MsgOperReciver msgOperReciver;
    private int num = 0;

    private AgoraVoiceChat mVoiceChat = null;
    private int isTalkConnect = 0;
    private boolean nojingyin = true;
    private boolean nomianti = false;
    private CountDownTimer cdt;//倒计时
    private String mLoginUserId;
    private String mLoginNickName;
    private String toUserName;
    private String toUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dengdaijieshouvoice1);
        DestroyActivityUtil.addDestoryActivityToMap(Gukevoice.this, "Gukevoice");


        mLoginNickName = getIntent().getStringExtra("mLoginNickName");
        mLoginUserId = getIntent().getStringExtra("mLoginUserId");
        toUserId = getIntent().getStringExtra("toUserId");
        toUserName = getIntent().getStringExtra("toUserName");


        initView();
        bindListener();

        msgOperReciver = new MsgOperReciver();
        IntentFilter intentFilter = new IntentFilter(OtherBroadcast.ACTION_MSG_ONEINVITEVOICE);
        registerReceiver(msgOperReciver, intentFilter);

        mVoiceChat = new AgoraVoiceChat(this, null);
        mVoiceChat.setChatStateEvent(
                new AgoraVoiceChat.ChatStateEventListener() {
                    @Override
                    public void OnOtherUserOffline() {
                        onOtherUserLeve();
                    }

                    @Override
                    public void OnOtherUserOnline() {
                        handler.sendMessage(handler.obtainMessage(203, ""));
                    }
                });
        MusicUtil.init(Gukevoice.this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MusicUtil.playSound(1, 100);
            }
        }, 500);
        //倒计时50秒
        cdt = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                isTalkConnect = 3; //对方无应答
                exitCall();
            }
        };
        //启动倒计时
        cdt.start();
        mVoiceChat.initAndJoinChannel(mLoginUserId + "_" + toUserId);
    }

    public void initView() {
        photo = findViewById(R.id.photo);
        nickname = findViewById(R.id.nickname);
        voice_gkjy = findViewById(R.id.voice_gkjy);
        voice_gkgd = findViewById(R.id.voice_gkgd);
        voice_gkmt = findViewById(R.id.voice_gkmt);
        voice_gkjishi = findViewById(R.id.voice_gkjishi);
        callingControl = findViewById(R.id.callingControl);

        nickname.setText(toUserName);
        String url = AvatarHelper.getAvatarUrl(toUserId, false);
        GlideImageUtils.setImageView(Gukevoice.this, url, photo);
    }

    public void bindListener() {

        voice_gkjy.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                //静音
                if (nojingyin) {
                    nojingyin = false;
                    voice_gkjy.setImageResource(R.drawable.voice_yijingyin);
                } else {
                    nojingyin = true;
                    voice_gkjy.setImageResource(R.drawable.voice_nojingyin);
                }
                mVoiceChat.onSwitchMic(nojingyin);
            }
        });

        voice_gkmt.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                //免提
                if (nomianti) {
                    nomianti = false;
                    voice_gkmt.setImageResource(R.drawable.voice_nomianti);
                } else {
                    nomianti = true;
                    voice_gkmt.setImageResource(R.drawable.voice_yimianti);
                }
                mVoiceChat.onSwitchSpeakerphone(nomianti);
            }
        });


        voice_gkgd.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {

                exitCall();
            }
        });
    }

    /**
     * 挂断
     */
    private void exitCall() {

        LogUtil.e("cjh yuyimguke22 "+mLoginUserId);
        PreferenceUtils.putBoolean(this, Constants.VOICE_CALL + mLoginUserId, false);
        MusicUtil.stopPlay();
        mVoiceChat.onHangUp();
        EventBus.getDefault().post(new MessageTalkEvent(voice_gkjishi.getText().toString(), isTalkConnect));
        finish();
    }

    //对方挂断
    private void onOtherUserLeve() {
        MusicUtil.stopPlay();
        mVoiceChat.onHangUp();
        finish();
    }


    @Override
    protected void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }
        if (msgOperReciver != null) {
            unregisterReceiver(msgOperReciver);
        }
        super.onDestroy();

    }

    private class MsgOperReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(OtherBroadcast.ACTION_MSG_ONEINVITEVOICE)) {
                LogUtil.e("cjh yuyimguke33 "+mLoginUserId);
                    PreferenceUtils.putBoolean(Gukevoice.this, Constants.VOICE_CALL + mLoginUserId, false);

                if (isTalkConnect == 1) {
                    MusicUtil.stopPlay();
                    mVoiceChat.onHangUp();
                    finish();
                } else {
                    MusicUtil.stopPlay();
                    finish();
                }
            }

        }
    }

    private Timer timer = null;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:

                    String strtime = (String) msg.obj;
                    if (!strtime.equals("")) {
                        voice_gkjishi.setText(strtime);
                    } else {
                        voice_gkjishi.setText("");
                    }
                    break;

                case 203:
                    //停止倒计时
                    cdt.cancel();
                    //停止音乐
                    MusicUtil.stopPlay();
                    isTalkConnect = 1;
                    voice_gkgd.setImageResource(R.drawable.voice_guaduan);
                    callingControl.setVisibility(View.VISIBLE);
                    // 接通后开放静音和挂断按钮
                    voice_gkjy.setVisibility(View.VISIBLE);
                    voice_gkmt.setVisibility(View.VISIBLE);
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                        long t = System.currentTimeMillis();
                        long tl = t / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();

                        @Override
                        public void run() {
                            Timestamp tt = new Timestamp(tl);
                            String starttime = sdf.format(tt);
                            handler.sendMessage(handler.obtainMessage(1, (Object) starttime));
                            tl = tl + 1000;
                        }
                    }, 0, 1000);


                    break;


            }
        }
    };


    @Override
    protected void onStop() {
        super.onStop();
        if (timer != null) {
            timer.cancel();
        }

        MusicUtil.stopPlay();
        //停止倒计时
        cdt.cancel();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//按下返回键
            //exitCall();//挂断
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
