package com.vkzwbim.chat.helper;

import android.text.TextUtils;

import com.vkzwbim.chat.MyApplication;
import com.vkzwbim.chat.bean.AttentionUser;
import com.vkzwbim.chat.bean.Friend;
import com.vkzwbim.chat.bean.User;
import com.vkzwbim.chat.broadcast.CardcastUiUpdateUtil;
import com.vkzwbim.chat.broadcast.MsgBroadcast;
import com.vkzwbim.chat.db.dao.ChatMessageDao;
import com.vkzwbim.chat.db.dao.CircleMessageDao;
import com.vkzwbim.chat.db.dao.FriendDao;
import com.vkzwbim.chat.sp.TableVersionSp;
import com.vkzwbim.chat.util.TimeUtils;

/**
 * @author Dean Tao
 */
public class FriendHelper {

    public static boolean updateFriendRelationship(String loginUserId, User user) {// 更新两者的关系，因为本地数据可能不正确
        AttentionUser attentionUser = user.getFriends();
        String friendId = user.getUserId();
        boolean changed = false;
        Friend friend = FriendDao.getInstance().getFriend(loginUserId, friendId);// 本地好友
        if (attentionUser == null) {// 服务器上不存在关系
            changed = true;
            if (friend != null) {// 服务器上不存在关系，本地却有
                if (friend.getStatus() != Friend.STATUS_23) {
                    // 从好友表中改变状态
                    // 只改变状态，和被删除一致，避免在其他页面被查询出来，
                    FriendDao.getInstance().updateFriendStatus(loginUserId, friendId, Friend.STATUS_23);
                }
            } else {
                // 现在服务器上不存在的情况本地也需要有，才能在一些页面获取到数据，
                // 因为陌生人支持设置备注了，
                friend = new Friend();
                friend.setOwnerId(loginUserId);
                friend.setUserId(user.getUserId());
                friend.setRoomFlag(0);// 0朋友 1群组
                friend.setStatus(Friend.STATUS_UNKNOW);
                friend.setVersion(TableVersionSp.getInstance(MyApplication.getInstance()).getFriendTableVersion(loginUserId));// 更新版本
                FriendDao.getInstance().createOrUpdateFriend(friend);
            }
        } else {// 服务器上存在关系
            if (friend == null) {// 本地不存在关系，那么就要插入一条好友记录
                friend = new Friend();
                friend.setOwnerId(attentionUser.getUserId());
                friend.setUserId(attentionUser.getToUserId());
                friend.setNickName(attentionUser.getToNickName());
                friend.setRemarkName(attentionUser.getRemarkName());
                friend.setDescribe(attentionUser.getDescribe());
                friend.setTimeCreate(attentionUser.getCreateTime());
                friend.setTimeSend(TimeUtils.sk_time_current_time());
                friend.setRoomFlag(0);// 0朋友 1群组
                friend.setCompanyId(attentionUser.getCompanyId());// 公司
                int status = (attentionUser.getBlacklist() == 0) ? attentionUser.getStatus() : Friend.STATUS_BLACKLIST;
                friend.setStatus(status);
                friend.setVersion(TableVersionSp.getInstance(MyApplication.getInstance()).getFriendTableVersion(loginUserId));// 更新版本
                FriendDao.getInstance().createOrUpdateFriend(friend);

                if (status == Friend.STATUS_ATTENTION) {// 如果是关注（理论上不可能）
                } else if (status == Friend.STATUS_FRIEND) {   // 如果是好友
                    addFriendExtraOperation(loginUserId, friendId);
                }
                changed = true;
            } else {
                if (!TextUtils.equals(attentionUser.getRemarkName(), friend.getRemarkName())
                        || !TextUtils.equals(attentionUser.getDescribe(), friend.getDescribe())) {
                    FriendDao.getInstance().updateRemarkNameAndDescribe(loginUserId,
                            attentionUser.getToUserId(), user.getFriends().getRemarkName(),
                            user.getFriends().getDescribe());
                    changed = true;
                }
                int status = attentionUser.getBlacklist() == 0 ? attentionUser.getStatus() : Friend.STATUS_BLACKLIST;
                if (status == friend.getStatus()) {
                    // do no thing
                } else {
                    FriendDao.getInstance().updateFriendStatus(loginUserId, friendId, status);
                    if (status == Friend.STATUS_BLACKLIST) {// 如果之前在黑名单中，现在是STATUS_ATTENTION或者STATUS_FRIEND
                        if (friend.getStatus() == Friend.STATUS_ATTENTION) {
                        } else if (friend.getStatus() == Friend.STATUS_FRIEND) {
                            addFriendExtraOperation(loginUserId, friendId);
                        }
                    } else if (status == Friend.STATUS_ATTENTION) {// 如果之前是关注，现在是黑名单或者好友
                        if (friend.getStatus() == Friend.STATUS_BLACKLIST) {
                            addBlacklistExtraOperation(loginUserId, friendId);
                        } else if (friend.getStatus() == Friend.STATUS_FRIEND) {
                            addFriendExtraOperation(loginUserId, friendId);
                        }
                    } else if (status == Friend.STATUS_FRIEND) {
                        if (friend.getStatus() == Friend.STATUS_BLACKLIST) {
                            addBlacklistExtraOperation(loginUserId, friendId);
                        } else if (friend.getStatus() == Friend.STATUS_ATTENTION) {// 本来是好友，现在变成关注
                            // 消息表中删除
                            ChatMessageDao.getInstance().deleteMessageTable(loginUserId, friendId);
                            // 2、更新消息界面（消息界面可能之前存在和该用户的聊天记录，要删除掉）
                            MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                            // 3、更新主界面未读数量（消息界面可能之前存在和该用户的聊天记录，要删除掉，未读数量可能改变）
                            MsgBroadcast.broadcastMsgNumReset(MyApplication.getInstance());
                        }
                    }
                    changed = true;
                }
            }
        }
        return changed;
    }

    /**
     * 加入黑名单，额外需要做的操作
     */
    public static void addBlacklistExtraOperation(String loginUserId, String friendId) {
        // 消息表中删除
        ChatMessageDao.getInstance().deleteMessageTable(loginUserId, friendId);
        // 商务圈消息表删除
        CircleMessageDao.getInstance().deleteMessage(loginUserId, friendId);
        // 2、更新消息界面（消息界面可能之前存在和该用户的聊天记录，要删除掉）
        MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
        // 3、更新主界面未读数量（消息界面可能之前存在和该用户的聊天记录，要删除掉，未读数量可能改变）
        MsgBroadcast.broadcastMsgNumReset(MyApplication.getInstance());
    }

    /**
     * 在本地数据库表中出入一条好友记录，额外需要做的操作
     */
    public static void addFriendExtraOperation(String loginUserId, String friendId) {
        // 插入一条系统提示消息
        FriendDao.getInstance().addNewFriendInMsgTable(loginUserId, friendId);
        // 更新Message Ui
        MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
        // 更新Main Ui message 未读数量
        MsgBroadcast.broadcastMsgNumUpdate(MyApplication.getInstance(), true, 1);
    }

    /**
     * 在本地数据库表中出入一条好友记录，额外需要做的操作
     */
    public static void addFriendExtraOperation(String loginUserId, String friendId, int userType) {
        if (userType != 2) {
            // 非公众号，插入一条系统提示消息
            FriendDao.getInstance().addNewFriendInMsgTable(loginUserId, friendId);
        }
        // 更新Message Ui
        MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
        // 更新Main Ui message 未读数量
        MsgBroadcast.broadcastMsgNumUpdate(MyApplication.getInstance(), true, 1);
    }

    /**
     * 移除一个关注级别的用户，可能是关注或者好友
     */
    public static void removeAttentionOrFriend(String ownerId, String friendId) {
        // 从好友表中改变状态
        FriendDao.getInstance().updateFriendStatus(ownerId, friendId, Friend.STATUS_23);//对方我把加入了黑名单 不删除好友
        FriendDao.getInstance().updateFriendContent(ownerId, friendId, null, 0, 0);

        FriendDao.getInstance().updateRemarkName(ownerId, friendId, "");

        // 消息表中删除
        ChatMessageDao.getInstance().deleteMessageTable(ownerId, friendId);
        // 商务圈消息表删除
        CircleMessageDao.getInstance().deleteMessage(ownerId, friendId);
        // 更新消息界面（消息界面可能之前存在和该用户的聊天记录，要删除掉）
        MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
        // 更新主界面未读数量（消息界面可能之前存在和该用户的聊天记录，要删除掉，未读数量可能改变）
        MsgBroadcast.broadcastMsgNumReset(MyApplication.getInstance());
    }

    // ////////////////////上面是我的主动操作/////////////////
    // ////////////////////下面是我的被动操作/////////////////


    /**
     * 取消了对我的单向关注
     */
    /*public static void beDeleteSeeNewFriend(String loginUserId, String friendId) {
        Friend friend = FriendDao.getInstance().getFriend(loginUserId, friendId);
        if (friend != null) {
            if (friend.getStatus() == Friend.STATUS_FRIEND) {
                friend.setStatus(Friend.STATUS_ATTENTION);
                friend.setContent("");
                // 由好友变为关注，更新一些数据
                FriendDao.getInstance().createOrUpdateFriend(friend);
                // 消息表中删除
                ChatMessageDao.getInstance().deleteMessageTable(loginUserId, friendId);
                // 更新消息界面（消息界面可能之前存在和该用户的聊天记录，要删除掉）
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());

                MsgBroadcast.broadcastMsgNumReset(MyApplication.getInstance());

                CardcastUiUpdateUtil.broadcastUpdateUi(MyApplication.getInstance());
            }
        }
    }*/

    /**
     * 对方 拉黑 || 删除 我
     */
    public static void beDeleteAllNewFriend(String loginUserId, String friendId) {
        removeAttentionOrFriend(loginUserId, friendId);
        // 可能正在看通讯录，那么通讯录也要更新
        CardcastUiUpdateUtil.broadcastUpdateUi(MyApplication.getInstance());
    }

    /**
     * 在本地数据库表中出入一条好友记录，额外需要做的操作
     */
    public static void beAddFriendExtraOperation(String loginUserId, String friendId) {
        FriendDao.getInstance().addNewFriendInMsgTable(loginUserId, friendId);
        // 更新Message Ui
        MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
        // 更新Main Ui message 未读数量
        MsgBroadcast.broadcastMsgNumUpdate(MyApplication.getInstance(), true, 1);
    }

    public static void friendAccountRemoved(String ownerId, String friendId) {
        // 从好友表中改变状态
        FriendDao.getInstance().deleteFriend(ownerId, friendId);
        // 消息表中删除
        ChatMessageDao.getInstance().deleteMessageTable(ownerId, friendId);
        // 商务圈消息表删除
        CircleMessageDao.getInstance().deleteMessage(ownerId, friendId);
        // 更新消息界面（消息界面可能之前存在和该用户的聊天记录，要删除掉）
        MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
        // 更新主界面未读数量（消息界面可能之前存在和该用户的聊天记录，要删除掉，未读数量可能改变）
        MsgBroadcast.broadcastMsgNumReset(MyApplication.getInstance());
        // 可能正在看通讯录，那么通讯录也要更新
        CardcastUiUpdateUtil.broadcastUpdateUi(MyApplication.getInstance());
    }
}
