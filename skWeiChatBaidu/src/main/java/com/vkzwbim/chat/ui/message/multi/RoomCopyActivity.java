package com.vkzwbim.chat.ui.message.multi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.vkzwbim.chat.AppConstant;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.bean.Friend;
import com.vkzwbim.chat.bean.RoomMember;
import com.vkzwbim.chat.bean.RoomMessage;
import com.vkzwbim.chat.bean.message.ChatMessage;
import com.vkzwbim.chat.bean.message.XmppMessage;
import com.vkzwbim.chat.broadcast.MsgBroadcast;
import com.vkzwbim.chat.broadcast.MucgroupUpdateUtil;
import com.vkzwbim.chat.db.dao.ChatMessageDao;
import com.vkzwbim.chat.db.dao.FriendDao;
import com.vkzwbim.chat.db.dao.RoomMemberDao;
import com.vkzwbim.chat.helper.AvatarHelper;
import com.vkzwbim.chat.ui.base.BaseActivity;
import com.vkzwbim.chat.ui.message.MucChatActivity;
import com.vkzwbim.chat.ui.tool.ButtonColorChange;
import com.vkzwbim.chat.util.TimeUtils;
import com.vkzwbim.chat.view.HeadView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.JsonCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class RoomCopyActivity extends BaseActivity {

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (msg.obj != null) {
                        RoomMessage roomMessage = (RoomMessage) msg.obj;
                        createRoomSuccess(roomMessage.getData().getId(),
                                roomMessage.getData().getJid(),
                                roomMessage.getData().getName(),
                                roomMessage.getData().getDesc());
                        Intent intent = new Intent(RoomCopyActivity.this, MucChatActivity.class);
                        intent.putExtra(AppConstant.EXTRA_USER_ID, roomMessage.getData().getJid());
                        intent.putExtra(AppConstant.EXTRA_NICK_NAME, roomMessage.getData().getNickname());
                        intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
                        startActivity(intent);
                        finish();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_room_copy);
        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.copy_group));
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
    }

    private void initView() {
        String rId = getIntent().getStringExtra("roomId");
        TextView tv_people_num = findViewById(R.id.tv_people_num);
        TextView tv_people = (TextView) findViewById(R.id.tv_people);
        List<RoomMember> roomMembers = RoomMemberDao.getInstance().getRoomMember(rId);
        int num = roomMembers.size();
        if (num > 3) {
            StringBuffer stringBufferD = null;
            for (int i = 0; i < 3; i++) {
                stringBufferD = new StringBuffer();
                stringBufferD.append(roomMembers.get(i).getCardName() + ",");
            }
            tv_people.setText(stringBufferD + coreManager.getSelf().getNickName());

        } else {
            StringBuffer stringBuffer = null;

            for (int i = 0; i < num; i++) {
                stringBuffer = new StringBuffer();
                stringBuffer.append(roomMembers.get(i).getCardName() + ",");
            }
            tv_people.setText(stringBuffer + coreManager.getSelf().getNickName());

        }
        tv_people_num.setText(num + getString(R.string.people));

        HeadView headView = findViewById(R.id.avatar_imgS);
        Friend friend = FriendDao.getInstance().getMucFriendByRoomId(coreManager.getSelf().getUserId(), rId);
        AvatarHelper.getInstance().displayAvatar(coreManager.getSelf().getUserId(), friend, headView);

        Button bt_copy_room = findViewById(R.id.bt_copy_room);
        ButtonColorChange.colorChange(this, bt_copy_room);
        bt_copy_room.setText(getResources().getString(R.string.copy_sure));
        bt_copy_room.setOnClickListener(v -> runOnUiThread(() -> copyRoom(rId)));
    }

    // 创建成功的时候将会调用此方法，将房间也存为好友
    private void createRoomSuccess(String roomId,
                                   String roomJid,
                                   String roomName,
                                   String roomDesc) {
        Friend friend = new Friend();
        friend.setOwnerId(coreManager.getSelf().getUserId());
        friend.setUserId(roomJid);
        friend.setNickName(roomName);
        friend.setDescription(roomDesc);
        friend.setRoomFlag(1);
        friend.setRoomId(roomId);
        friend.setRoomCreateUserId(coreManager.getSelf().getUserId());
        // timeSend作为取群聊离线消息的标志，所以要在这里设置一个初始值
        friend.setTimeSend(TimeUtils.sk_time_current_time());
        friend.setStatus(Friend.STATUS_FRIEND);
        FriendDao.getInstance().createOrUpdateFriend(friend);

        // 更新群组
        MucgroupUpdateUtil.broadcastUpdateUi(this);

        // 本地发送一条消息至该群 否则未邀请其他人时在消息列表不会显示
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(XmppMessage.TYPE_TIP);
        chatMessage.setFromUserId(coreManager.getSelf().getUserId());
        chatMessage.setFromUserName(coreManager.getSelf().getNickName());
        chatMessage.setToUserId(roomJid);
        chatMessage.setContent(getString(R.string.new_friend_chat));
        chatMessage.setPacketId(coreManager.getSelf().getNickName());
        chatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(coreManager.getSelf().getUserId(), roomJid, chatMessage)) {
            // 更新聊天界面
            MsgBroadcast.broadcastMsgUiUpdate(RoomCopyActivity.this);
        }
    }

    private void copyRoom(String mRoomId) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);

        HttpUtils.get().url(coreManager.getConfig().ROOM_COPY)
                .params(params)
                .build()
                .execute(new JsonCallback() {
                    @Override
                    public void onResponse(String result) {
                        Log.e("zx", "onResponse: " + result);
                        RoomMessage roomMessage = JSON.parseObject(result, RoomMessage.class);
                        if (1 == roomMessage.getResultCode()) {
                            Message message = new Message();
                            message.what = 1;
                            message.obj = roomMessage;
                            handler.sendMessage(message);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }
}
