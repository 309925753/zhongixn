package com.vkzwbim.chat.course;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vkzwbim.chat.AppConfig;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.Reporter;
import com.vkzwbim.chat.adapter.MessageSendChat;
import com.vkzwbim.chat.bean.message.ChatMessage;
import com.vkzwbim.chat.bean.message.CourseChatBean;
import com.vkzwbim.chat.bean.message.XmppMessage;
import com.vkzwbim.chat.broadcast.MsgBroadcast;
import com.vkzwbim.chat.db.dao.ChatMessageDao;
import com.vkzwbim.chat.helper.DialogHelper;
import com.vkzwbim.chat.helper.PrivacySettingHelper;
import com.vkzwbim.chat.ui.base.BaseActivity;
import com.vkzwbim.chat.ui.tool.ButtonColorChange;
import com.vkzwbim.chat.util.AppUtils;
import com.vkzwbim.chat.util.Constants;
import com.vkzwbim.chat.util.DES;
import com.vkzwbim.chat.util.FloatWindowPermissionChecker;
import com.vkzwbim.chat.util.Md5Util;
import com.vkzwbim.chat.util.PreferenceUtils;
import com.vkzwbim.chat.util.TimeUtils;
import com.vkzwbim.chat.util.ToastUtil;
import com.vkzwbim.chat.view.ChatContentView;
import com.vkzwbim.chat.view.SelectionFrame;
import com.vkzwbim.chat.xmpp.listener.ChatMessageListener;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 我的课件
 *
 * @author Dean Tao
 * @version 1.0
 */
public class CourseDateilsActivity extends BaseActivity {
    public static boolean isCourseDetailClassSendHandler;
    boolean isRun;
    int mPos;
    private String title;
    private String courseId;
    private ChatContentView mChatContentView;
    private List<ChatMessage> mChatMessages;
    Runnable sendMessageTask = new Runnable() {
        @Override
        public void run() {
            while (isRun) {
                mHandler.sendEmptyMessage(mPos);
                mPos++;
                if (mPos == mChatMessages.size()) {
                    // 最后一条已发送完成
                    isRun = false;
                    SystemClock.sleep(400);
                    mHandler.sendEmptyMessage(-1);
                } else {
                    long sleepTime = 1000;
                    SystemClock.sleep(sleepTime);
                }
            }
        }
    };
    private String toUserId;
    private String mLoginUserId;
    private int isReadDel;
    private boolean isGroup;
    private SuspenionWondow windowManager;
    private TextView mTvSuspen;
    private boolean isEncrypt;
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            // todo 因为LocalCourseActivity也有一个mHandlerChat，需要坐下兼容处理
            if (msg.what == -1) {
                isCourseDetailClassSendHandler = false;
                // loadingDialog.dismiss();
                Constants.IS_SENDONG_COURSE_NOW = false;// 课程发送完成，修改发送状态
                hideSuspensionWondow();
                ToastUtil.showToast(CourseDateilsActivity.this, getString(R.string.tip_course_send_success));
                // 更新消息界面
                MsgBroadcast.broadcastMsgUiUpdate(CourseDateilsActivity.this);
                return;
            }
            isCourseDetailClassSendHandler = true;
            mTvSuspen.setText(getString(R.string.sending_message_index_place_holder, msg.what + 1));

            ChatMessage chatMessage = mChatMessages.get(msg.what);
            // 数据解密
            if (chatMessage.getIsEncrypt() == 1) { // == 1
                try {
                    String decryptKey = Md5Util.toMD5(AppConfig.apiKey + chatMessage.getTimeSend() + chatMessage.getPacketId());
                    String decryptContent = DES.decryptDES(chatMessage.getContent(), decryptKey);
                    // 为chatMessage重新设值
                    chatMessage.setContent(decryptContent);
                    chatMessage.setIsEncrypt(0);
                } catch (Exception e) {
                    Reporter.unreachable(e);
                }
            }
            // 重新加密，
            if (isEncrypt) {
                chatMessage.setIsEncrypt(1);
            } else {
                chatMessage.setIsEncrypt(0);
            }
            chatMessage.setFromUserId(mLoginUserId);
            chatMessage.setToUserId(toUserId);
            chatMessage.setIsReadDel(isReadDel);
            chatMessage.setMySend(true);
            chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
            chatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());

            ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, toUserId, chatMessage);
            MessageSendChat chat = new MessageSendChat(isGroup, toUserId, chatMessage);
            EventBus.getDefault().post(chat);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_course_dateils);
        courseId = getIntent().getStringExtra("data");
        title = getIntent().getStringExtra("title");
        mChatMessages = new ArrayList<>();
        mLoginUserId = coreManager.getSelf().getUserId();
        initActionBar();
        initView();

        isEncrypt = PrivacySettingHelper.getPrivacySettings(this).getIsEncrypt() == 1;

        EventBus.getDefault().register(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(title);
    }

    private void initView() {
        mChatContentView = (ChatContentView) findViewById(R.id.chat_content_view);
        mChatContentView.setToUserId("123");
        mChatContentView.setMessageEventListener(new ChatContentView.MessageEventListener() {

            @Override
            public void onEmptyTouch() {

            }

            @Override
            public void onTipMessageClick(ChatMessage message) {

            }

            @Override
            public void onMyAvatarClick() {

            }

            @Override
            public void onFriendAvatarClick(String friendUserId) {

            }

            @Override
            public void onMessageClick(ChatMessage chatMessage) {
                // 删除
                deleteMessage(chatMessage);
            }

            @Override
            public void onMessageLongClick(ChatMessage chatMessage) {

            }

            @Override
            public void onSendAgain(ChatMessage chatMessage) {

            }

            @Override
            public void onMessageBack(ChatMessage chatMessage, int position) {

            }

            @Override
            public void onMessageForbid(ChatMessage chatMessage) {

            }

            @Override
            public void onMessageDelete(ChatMessage chatMessage) {

            }

            @Override
            public void onNickNameClick(String friendUserId) {

            }

            @Override
            public void onCallListener(int type) {

            }

            @Override
            public void LongAvatarClick(ChatMessage chatMessage) {

            }
        });
        mChatContentView.setNeedRefresh(false);

        mChatContentView.setChatListType(ChatContentView.ChatListType.COURSE);

        ButtonColorChange.colorChange(mContext, findViewById(R.id.sure_btn));
        findViewById(R.id.sure_btn).setOnClickListener(v -> {
            if (mChatMessages == null || mChatMessages.size() == 0) {
                Toast.makeText(CourseDateilsActivity.this, getString(R.string.tip_get_course_detail_faled), Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(mContext, SelectFriendsActivity.class));
            SelectFriendsActivity.isIntentLocalCourseActivity = false;
        });
        loadData();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventSendCourse message) {
        toUserId = message.getToUserId();
        isGroup = message.isGroup();
        isReadDel = PreferenceUtils.getInt(mContext, Constants.MESSAGE_READ_FIRE + toUserId + mLoginUserId, 0);
        sendRecordMessage();
    }

    /**
     * 隐藏悬浮窗
     */
    private void hideSuspensionWondow() {
        windowManager.hide();
    }

    /**
     * 显示悬浮窗
     */
    private void showSuspensionWondow() {
        mTvSuspen = new TextView(this);
        mTvSuspen.setGravity(Gravity.CENTER);
        mTvSuspen.setBackgroundResource(R.drawable.course_connors);
        mTvSuspen.setTextAppearance(this, R.style.TextStyle);
        mTvSuspen.setText(R.string.sending_course);
        windowManager = new SuspenionWondow(CourseDateilsActivity.this);
        windowManager.show(mTvSuspen);
    }

    /**
     * 发送课程
     */
    private void sendRecordMessage() {
        if (AppUtils.checkAlertWindowsPermission(this)) { // 已开启悬浮窗权限
            showSuspensionWondow();
            mPos = 0;
            isRun = true;
            new Thread(sendMessageTask).start();
        } else {
            Constants.IS_SENDONG_COURSE_NOW = false;

            SelectionFrame selectionFrame = new SelectionFrame(this);
            selectionFrame.setSomething(null, getString(R.string.av_no_float), new SelectionFrame.OnSelectionFrameClickListener() {
                @Override
                public void cancelClick() {

                }

                @Override
                public void confirmClick() {
                    FloatWindowPermissionChecker.tryJumpToPermissionPage(CourseDateilsActivity.this);
                }
            });
            selectionFrame.show();
        }
    }

    /**
     * 从课件里删除某条消息
     *
     * @param chatMessage 要删除的消息
     */
    private void deleteMessage(final ChatMessage chatMessage) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("courseMessageId", chatMessage.getPacketId());
        params.put("courseId", courseId);
        params.put("updateTime", TimeUtils.sk_time_current_time() + "");
        HttpUtils.get().url(coreManager.getConfig().USER_EDIT_COURSE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(CourseDateilsActivity.this, getString(R.string.delete_success));
                        deleteAdapterData(chatMessage);
                        mChatContentView.setData(mChatMessages);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(CourseDateilsActivity.this);
                    }
                });
    }

    private void deleteAdapterData(ChatMessage chatMessage) {
        for (int i = 0; i < mChatMessages.size(); i++) {
            ChatMessage chat = mChatMessages.get(i);
            if (chatMessage.getPacketId().equals(chat.getPacketId())) {
                mChatMessages.remove(i);
                return;
            }
        }
    }

    /**
     * 加载数据
     */
    private void loadData() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("courseId", courseId);
        HttpUtils.get().url(coreManager.getConfig().USER_COURSE_DATAILS)
                .params(params)
                .build()
                .execute(new ListCallback<CourseChatBean>(CourseChatBean.class) {
                    @Override
                    public void onResponse(ArrayResult<CourseChatBean> result) {
                        DialogHelper.dismissProgressDialog();
                        fromatDatas(result.getData());
                        mChatContentView.setData(mChatMessages);
                        mChatContentView.headerRefreshingCompleted();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(CourseDateilsActivity.this);
                        mChatContentView.headerRefreshingCompleted();
                    }
                });
    }

    private void fromatDatas(List<CourseChatBean> result) {
        mChatMessages.clear();
        for (int i = 0; i < result.size(); i++) {
            try {
                CourseChatBean data = result.get(i);
                String messageBody = data.getMessage();
                org.json.JSONObject json = new org.json.JSONObject(messageBody);
                String body = json.getString("body");
                body = Html.fromHtml(body).toString();
                String packedId = data.getCourseMessageId();
                Log.e("xuan", "fromatDatas: " + body);
                ChatMessage chatMessage = new ChatMessage(body);
                if (chatMessage.getType() == XmppMessage.TYPE_IMAGE
                        || chatMessage.getType() == XmppMessage.TYPE_VIDEO
                        || chatMessage.getType() == XmppMessage.TYPE_FILE) {
                    chatMessage.setUploadSchedule(100);
                    chatMessage.setUpload(true);
                }
                chatMessage.setMySend(true);
                chatMessage.setMessageState(ChatMessageListener.MESSAGE_SEND_SUCCESS);
                mChatMessages.add(chatMessage);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
