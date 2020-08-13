package com.vkzwbim.chat.voicetalk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cjt2325.cameralibrary.util.LogUtil;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.bean.message.ChatMessage;
import com.vkzwbim.chat.bean.message.XmppMessage;
import com.vkzwbim.chat.broadcast.OtherBroadcast;
import com.vkzwbim.chat.helper.AvatarHelper;
import com.vkzwbim.chat.ui.base.BaseActivity;
import com.vkzwbim.chat.util.Constants;
import com.vkzwbim.chat.util.GlideImageUtils;
import com.vkzwbim.chat.util.PreferenceUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

public class TalkRecieveActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout ll_un_talk, ll_voice_guaduan, ll_voice_jieting;
    private LinearLayout ll_talk, ll_voice_jingyin, ll_voice_gd, ll_voice_mianti;
    private MsgOperReciver msgOperReciver;
    private ImageView photo;
    private ImageView voice_mt;
    private TextView nickname;
    private TextView voice_gk_time;
    private boolean nojingyin=true;
    private boolean nomianti=false;


    private MediaPlayer mediaPlayer1;
    AgoraVoiceChat chat;

    private String mLoginUserId;
    private String mLoginNickName;
    private String toUserName;
    private String toUserId;
    private Timer timer = null;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dengdaijieshouvoice);
getSupportActionBar().hide();
    initView();
        mLoginNickName=getIntent().getStringExtra("mLoginNickName");
        mLoginUserId = getIntent().getStringExtra("mLoginUserId");
        toUserId = getIntent().getStringExtra("toUserId");
        toUserName = getIntent().getStringExtra("toUserName");


        nickname.setText(toUserName);
        String url = AvatarHelper.getAvatarUrl(toUserId,false);
        GlideImageUtils.setImageView(TalkRecieveActivity.this,url,photo);


        chat = new AgoraVoiceChat(TalkRecieveActivity.this, null);
        msgOperReciver = new MsgOperReciver();
        IntentFilter intentFilter = new IntentFilter(OtherBroadcast.ACTION_MSG_FINISH);
        registerReceiver(msgOperReciver, intentFilter);

        //开始播放音乐
        openRawMusicS();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void initView() {
        ll_talk = findViewById(R.id.ll_talk);
        ll_voice_jingyin = findViewById(R.id.ll_voice_jingyin);
        ll_voice_gd = findViewById(R.id.ll_voice_gd);
        ll_voice_mianti = findViewById(R.id.ll_voice_mianti);
        voice_mt = findViewById(R.id.voice_mt);
        ll_un_talk = findViewById(R.id.ll_un_talk);
        ll_voice_guaduan = findViewById(R.id.ll_voice_guaduan);
        ll_voice_jieting = findViewById(R.id.ll_voice_jieting);
        photo = findViewById(R.id.photo);
        voice_gk_time = findViewById(R.id.voice_gk_time);
        nickname = findViewById(R.id.nickname);

        ll_voice_gd.setOnClickListener(this);
        ll_voice_jingyin.setOnClickListener(this);
        ll_voice_mianti.setOnClickListener(this);
        ll_voice_jieting.setOnClickListener(this);
        ll_voice_guaduan.setOnClickListener(this);

    }

    /**
     * 打开raw目录下的音乐mp3文件
     */
    private void openRawMusicS() {
        mediaPlayer1 = MediaPlayer.create(this, R.raw.music1);
        //用prepare方法，会报错误java.lang.IllegalStateExceptio
        //mediaPlayer1.prepare();
        mediaPlayer1.start();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            //未接通前挂断
            case R.id.ll_voice_guaduan:
                mediaPlayer1.pause();
//                EventBus.getDefault().post(new MessageTalkEvent("",2));
                sendMessage();
                finish();
                break;

            case R.id.ll_voice_jieting:
                mediaPlayer1.stop();
                chat.initAndJoinChannel(toUserId + "_" + mLoginUserId);
                ll_talk.setVisibility(View.VISIBLE);
                ll_un_talk.setVisibility(View.GONE);
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

            case R.id.ll_voice_jingyin:

                break;


            case R.id.ll_voice_mianti:
                //免提
                if (nomianti){
                    nomianti=false;
                    voice_mt.setImageResource(R.drawable.voice_nomianti);
                }else{
                    nomianti=true;
                    voice_mt.setImageResource(R.drawable.voice_yimianti);
                }
                chat.onSwitchSpeakerphone(nomianti);
                break;

            case R.id.ll_voice_gd:
                mediaPlayer1.pause();
                chat.onHangUp();
                String durinTime=voice_gk_time.getText().toString();
                EventBus.getDefault().post(new MessageTalkEvent(durinTime,4));
                finish();
                break;
        }
    }

    private class MsgOperReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.e("cjh yuyim retak44 "+mLoginUserId);

            PreferenceUtils.putBoolean(TalkRecieveActivity.this, Constants.VOICE_CALL + mLoginUserId, false);
            if (intent.getAction().equals(OtherBroadcast.ACTION_MSG_FINISH)) {
                mediaPlayer1.pause();
                finish();
            }
        }
    }


    public void sendMessage(){

        String durinTime=voice_gk_time.getText().toString();



        ChatMessage message = new ChatMessage();

        message.setType(XmppMessage.TYPE_END_FROM_CONNECT_VOICE);
        message.setVideoLength(durinTime);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setIsReadDel(0);
        coreManager.sendChatMessage(toUserId,message);

    }

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
                        voice_gk_time.setText(strtime);
                    } else {
                        voice_gk_time.setText("");
                    }
                    break;
            }
        }
    };


    @Override
    protected void onDestroy() {
        if (timer!=null){
            timer.cancel();
        }
        if (mediaPlayer1 != null) {
            mediaPlayer1.release();
        }
        unregisterReceiver(msgOperReciver);
        super.onDestroy();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//按下返回键
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
