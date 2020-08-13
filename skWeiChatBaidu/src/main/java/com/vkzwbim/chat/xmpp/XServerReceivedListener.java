package com.vkzwbim.chat.xmpp;

import android.text.TextUtils;

import com.vkzwbim.chat.MyApplication;
import com.vkzwbim.chat.bean.message.NewFriendMessage;
import com.vkzwbim.chat.db.dao.ChatMessageDao;
import com.vkzwbim.chat.ui.base.CoreManager;
import com.vkzwbim.chat.ui.me.sendgroupmessage.ChatActivityForSendGroup;
import com.vkzwbim.chat.util.log.LogUtils;
import com.vkzwbim.chat.xmpp.listener.ChatMessageListener;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;

import de.greenrobot.event.EventBus;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;

/**
 * 消息发送出去后，判断服务端是否收到，服务端收到后将消息置为送达
 */
public class XServerReceivedListener implements StanzaListener {

    private String mLoginUserId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();

    @Override
    public void processStanza(Stanza packet) {
        LogUtils.e("msg", "消息已送至服务器11");
        // Message
        if (packet instanceof Message) {
            Message message = (Message) packet;
            if (message.getType() == Message.Type.chat) {
                LogUtils.e("msg", "单聊：" + message.getPacketID());
                LogUtils.e("msg", "单聊：" + message.getBody());
            } else if (message.getType() == Message.Type.groupchat) {
                LogUtils.e("msg", "群聊：" + message.getPacketID());
                LogUtils.e("msg", "群聊：" + message.getBody());
            } else if (message.getType() == Message.Type.error) {
                LogUtils.e("msg", "error：" + message.getPacketID());
                LogUtils.e("msg", "error：" + message.getBody());
            } else {
                LogUtils.e("msg", "else：" + message.getPacketID());
                LogUtils.e("msg", "else：" + message.getBody());
            }
        }

        LogUtils.e("msg", "packet.getStanzaId()：" + packet.getStanzaId());
        if (TextUtils.isEmpty(packet.getStanzaId())) {
            LogUtils.e("msg", "packet.getStanzaId() == Null Return");
            return;
        }
        LogUtils.e("msg", "消息已送至服务器22");
        ReceiptManager.ReceiptObj mReceiptObj = ReceiptManager.mReceiptMap.get(packet.getStanzaId());
        if (mReceiptObj != null) {
            LogUtils.e("msg", "消息已送至服务器");
            if (mReceiptObj.Read == 1) {// 已读消息 Type==26
                if (mLoginUserId.equals(mReceiptObj.toUserId)) {
                    for (String s : MyApplication.machine) {
                        ChatMessageDao.getInstance().updateMessageRead(mLoginUserId, s, mReceiptObj.Read_msg_pid, true);
                    }
                } else {
                    ChatMessageDao.getInstance().updateMessageRead(mLoginUserId, mReceiptObj.toUserId, mReceiptObj.Read_msg_pid, true);
                }
            } else {// 普通消息 && 新朋友消息
                if (mReceiptObj.sendType == ReceiptManager.SendType.NORMAL) {
                    ListenerManager.getInstance().notifyMessageSendStateChange(mLoginUserId, mReceiptObj.toUserId, packet.getStanzaId(),
                            ChatMessageListener.MESSAGE_SEND_SUCCESS);

                    if (ChatActivityForSendGroup.isAlive) {
                        // 收到消息回执，通知消息群发页面
                        EventBus.getDefault().post(new MessageEvent(mReceiptObj.toUserId));
                    }
                } else {
                    ListenerManager.getInstance().notifyNewFriendSendStateChange(mReceiptObj.toUserId, ((NewFriendMessage) mReceiptObj.msg),
                            ChatMessageListener.MESSAGE_SEND_SUCCESS);
                }
            }
            ReceiptManager.mReceiptMap.remove(packet.getStanzaId());
        }
    }
}
