package com.vkzwbim.chat.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.Reporter;
import com.vkzwbim.chat.adapter.FriendSortAdapter;
import com.vkzwbim.chat.bean.AttentionUser;
import com.vkzwbim.chat.bean.Friend;
import com.vkzwbim.chat.broadcast.CardcastUiUpdateUtil;
import com.vkzwbim.chat.broadcast.MsgBroadcast;
import com.vkzwbim.chat.db.dao.FriendDao;
import com.vkzwbim.chat.db.dao.OnCompleteListener2;
import com.vkzwbim.chat.helper.DialogHelper;
import com.vkzwbim.chat.pay.PaymentActivity;
import com.vkzwbim.chat.sortlist.BaseComparator;
import com.vkzwbim.chat.sortlist.BaseSortModel;
import com.vkzwbim.chat.sortlist.SideBar;
import com.vkzwbim.chat.sortlist.SortHelper;
import com.vkzwbim.chat.ui.MainActivity;
import com.vkzwbim.chat.ui.base.EasyFragment;
import com.vkzwbim.chat.ui.contacts.BlackActivity;
import com.vkzwbim.chat.ui.contacts.ContactsActivity;
import com.vkzwbim.chat.ui.contacts.NewFriendActivity;
import com.vkzwbim.chat.ui.contacts.PublishNumberActivity;
import com.vkzwbim.chat.ui.contacts.RoomActivity;
import com.vkzwbim.chat.ui.groupchat.FaceToFaceGroup;
import com.vkzwbim.chat.ui.groupchat.SelectContactsActivity;
import com.vkzwbim.chat.ui.me.NearPersonActivity;
import com.vkzwbim.chat.ui.message.ChatActivity;
import com.vkzwbim.chat.ui.nearby.PublicNumberSearchActivity;
import com.vkzwbim.chat.ui.nearby.UserSearchActivity;
import com.vkzwbim.chat.ui.search.SearchAllActivity;
import com.vkzwbim.chat.util.AsyncUtils;
import com.vkzwbim.chat.util.Constants;
import com.vkzwbim.chat.util.PreferenceUtils;
import com.vkzwbim.chat.util.ToastUtil;
import com.vkzwbim.chat.util.UiUtils;
import com.vkzwbim.chat.view.MessagePopupWindow;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 通讯录
 */
public class FriendFragment extends EasyFragment {
    private static final String TAG = "FriendFragment";
    private TextView mTvTitle;
    private TextView tvFriendCount;
    private ImageView mIvTitleRight;
    private PullToRefreshListView mPullToRefreshListView;
    private FriendSortAdapter mAdapter;
    private SideBar mSideBar;
    private TextView mTextDialog;
    private List<BaseSortModel<Friend>> mSortFriends;
    private List<BaseSortModel<Friend>> mSearchSortFriends;
    private BaseComparator<Friend> mBaseComparator;
    private View mHeadView;
    /*
        private EditText mEditText;
        private boolean isSearch;
    */
    private TextView mNotifyCountTv;
    private boolean isSearch;
    /*private RelativeLayout mNewFriendRl;
    private RelativeLayout mGroupRl;
    private RelativeLayout mNoticeRl;*/
    private TextView mNotifyCountTv2;
    private String mLoginUserId;
    private String mLoginUserName;
    private Handler mHandler = new Handler();
    private MessagePopupWindow mMessagePopupWindow;
    private LinearLayout mAllView;
    private TextView mLoadView;
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CardcastUiUpdateUtil.ACTION_UPDATE_UI)) {
                // update();
                loadData();
            } else if (action.equals(MsgBroadcast.ACTION_MSG_NUM_UPDATE_NEW_FRIEND)) {// 更新消息数量
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE);
                if (friend != null && friend.getUnReadNum() > 0) {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.updateNewFriendMsgNum(friend.getUnReadNum());// 更新底部Tab栏通讯录角标

                    mNotifyCountTv.setText(friend.getUnReadNum() + "");
                    mNotifyCountTv.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    public FriendFragment() {
        mSortFriends = new ArrayList<BaseSortModel<Friend>>();
        mBaseComparator = new BaseComparator<Friend>();
    }

    /*private boolean mNeedUpdate = true;

    public void update() {
        if (isResumed()) {
            loadData();
        } else {
            mNeedUpdate = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNeedUpdate) {
            loadData();
            mNeedUpdate = false;
        }
    }*/

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_friend;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        initActionBar();
        mLoginUserId = coreManager.getSelf().getUserId();
        mLoginUserName = coreManager.getSelf().getNickName();
        initView();
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE);
        if (friend != null && friend.getUnReadNum() > 0) {
            mNotifyCountTv.setText(friend.getUnReadNum() + "");
            mNotifyCountTv.setVisibility(View.VISIBLE);
        }

        int mNewContactsNumber = PreferenceUtils.getInt(getActivity(), Constants.NEW_CONTACTS_NUMBER + mLoginUserId, 0);
        if (mNewContactsNumber > 0) {
            mNotifyCountTv2.setText(mNewContactsNumber + "");
            mNotifyCountTv2.setVisibility(View.VISIBLE);
        } else {
            mNotifyCountTv2.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mUpdateReceiver);
    }

    private void initActionBar() {
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(getString(R.string.contacts));
        mTvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        mIvTitleRight = (ImageView) findViewById(R.id.iv_title_right);
        mIvTitleRight.setImageResource(R.mipmap.folding_icon);
        mIvTitleRight.setVisibility(View.GONE);
        ImageView iv_title_right_right = findViewById(R.id.iv_title_right_right);
        iv_title_right_right.setVisibility(View.GONE);
        iv_title_right_right.setImageResource(R.mipmap.search_icon);
        iv_title_right_right.setOnClickListener(v -> SearchAllActivity.start(requireActivity(), "chatHistory"));
        appendClick(mIvTitleRight);
    }

    private void initView() {
        mAllView = (LinearLayout) findViewById(R.id.friend_rl);
        mLoadView = (TextView) findViewById(R.id.load_fragment);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        mHeadView = inflater.inflate(R.layout.fragment_contacts, null);

        TextView mEditText = mHeadView.findViewById(R.id.search_edit);
        mEditText.setOnClickListener(v -> SearchAllActivity.start(requireActivity(), "chatHistory"));
        mNotifyCountTv = (TextView) mHeadView.findViewById(R.id.num_tv);
        mNotifyCountTv2 = (TextView) mHeadView.findViewById(R.id.num_tv2);
        mHeadView.findViewById(R.id.new_friend_rl).setOnClickListener(this);
        mHeadView.findViewById(R.id.group_rl).setOnClickListener(this);
        mHeadView.findViewById(R.id.notice_rl).setOnClickListener(this);
        mHeadView.findViewById(R.id.black_rl).setOnClickListener(this);
        mHeadView.findViewById(R.id.contacts_rl).setOnClickListener(this);

        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        mPullToRefreshListView.setMode(Mode.PULL_FROM_START);
        mPullToRefreshListView.getRefreshableView().addHeaderView(mHeadView, null, false);
        View footerView = inflater.inflate(R.layout.footer_friend_fragment, mPullToRefreshListView.getRefreshableView(), false);
        tvFriendCount = footerView.findViewById(R.id.tvFriendCount);
        mPullToRefreshListView.getRefreshableView().addFooterView(footerView, null, false);
        mAdapter = new FriendSortAdapter(getActivity(), mSortFriends);
        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);

        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                upDataFriend();
            }
        });

        mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
/*
                Friend friend;
                if (isSearch) {
                    friend = mSearchSortFriends.get((int) id).getBean();
                } else {
                    friend = mSortFriends.get((int) id).getBean();
                }
*/
                Friend friend = mSortFriends.get((int) id).getBean();
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatActivity.FRIEND, friend);
                intent.putExtra("isserch", false);
                startActivity(intent);
            }
        });

        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar.setTextView(mTextDialog);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mPullToRefreshListView.getRefreshableView().setSelection(position);
                }
            }
        });

        /*
        Add Search Friend
         */
        // Todo 跳转至新页面搜索
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                isSearch = true;
                String mContent = mEditText.getText().toString();
                mSearchSortFriends = new ArrayList<>();
                if (TextUtils.isEmpty(mContent)) {
                    isSearch = false;
                    mAdapter.setData(mSortFriends);
                }
                for (int i = 0; i < mSortFriends.size(); i++) {
                    final Friend friend = mSortFriends.get(i).getBean();
                    String name = friend.getRemarkName();
                    if (TextUtils.isEmpty(name)) {
                        name = friend.getNickName();
                    }
                    if (name.contains(mContent)) {
                        // 符合搜索条件的好友
                        mSearchSortFriends.add((mSortFriends.get(i)));
                    }
                }
                mAdapter.setData(mSearchSortFriends);
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CardcastUiUpdateUtil.ACTION_UPDATE_UI);
        intentFilter.addAction(MsgBroadcast.ACTION_MSG_NUM_UPDATE_NEW_FRIEND);
        getActivity().registerReceiver(mUpdateReceiver, intentFilter);
    }

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        switch (v.getId()) {
            // Title And Window Click Listener
            case R.id.iv_title_right:
                mMessagePopupWindow = new MessagePopupWindow(getActivity(), this, coreManager);
                mMessagePopupWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                mMessagePopupWindow.showAsDropDown(v,
                        -(mMessagePopupWindow.getContentView().getMeasuredWidth() - v.getWidth() / 2 - 40),
                        0);
                break;
            case R.id.search_public_number:
                // 搜索公众号，
                mMessagePopupWindow.dismiss();
                PublicNumberSearchActivity.start(requireContext());
                break;
            case R.id.create_group:
                // 发起群聊
                mMessagePopupWindow.dismiss();
                startActivity(new Intent(getActivity(), SelectContactsActivity.class));
                break;
            case R.id.face_group:
                // 面对面建群
                mMessagePopupWindow.dismiss();
                startActivity(new Intent(getActivity(), FaceToFaceGroup.class));
                break;
            case R.id.add_friends:
                // 添加朋友
                mMessagePopupWindow.dismiss();
                startActivity(new Intent(getActivity(), UserSearchActivity.class));
                break;
            case R.id.scanning:
                // 扫一扫
                mMessagePopupWindow.dismiss();
                MainActivity.requestQrCodeScan(getActivity());
                break;
            case R.id.receipt_payment:
                // 收付款
                mMessagePopupWindow.dismiss();
                startActivity(new Intent(getActivity(), PaymentActivity.class));
                break;
            case R.id.near_person:
                // 附近的人
                mMessagePopupWindow.dismiss();
                startActivity(new Intent(getActivity(), NearPersonActivity.class));
                break;

            // Head Click Listener
            case R.id.new_friend_rl:
                Friend mNewFriend = FriendDao.getInstance().getFriend(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE);
                if (mNewFriend != null) {
                    mNotifyCountTv.setVisibility(View.GONE);
                    mNewFriend.setUnReadNum(0);

                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.updateNewFriendMsgNum(0);// 更新底部Tab栏通讯录角标
                    }
                }
                Intent intentNewFriend = new Intent(getActivity(), NewFriendActivity.class);
                getActivity().startActivity(intentNewFriend);
                break;
            case R.id.group_rl:
                RoomActivity.start(requireContext());
                break;
            case R.id.notice_rl:
                Intent intentNotice = new Intent(getActivity(), PublishNumberActivity.class);
                getActivity().startActivity(intentNotice);
                break;
            case R.id.black_rl:
                Intent intentBlack = new Intent(getActivity(), BlackActivity.class);
                getActivity().startActivity(intentBlack);
                break;
            case R.id.contacts_rl:
                // 清空新联系人数量
                PreferenceUtils.putInt(getActivity(), Constants.NEW_CONTACTS_NUMBER + mLoginUserId, 0);
                mNotifyCountTv2.setVisibility(View.GONE);

                Friend mNewFriend2 = FriendDao.getInstance().getFriend(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE);
                MainActivity activity = (MainActivity) getActivity();
                if (mNewFriend2 != null && activity != null) {
                    activity.updateNewFriendMsgNum(mNewFriend2.getUnReadNum());// 更新底部Tab栏通讯录角标
                }
                Intent intentGroup = new Intent(getActivity(), ContactsActivity.class);
                getActivity().startActivity(intentGroup);
                break;
        }
    }

    private void loadData() {
//        if (!DialogHelper.isShowing()) {
//            DialogHelper.showDefaulteMessageProgressDialog(getActivity());
//        }
        AsyncUtils.doAsync(this, e -> {
            Reporter.post("加载数据失败，", e);
            AsyncUtils.runOnUiThread(requireContext(), ctx -> {
//                DialogHelper.dismissProgressDialog();
                Log.e("zx", "onResponse411 : "+e.toString() );
                ToastUtil.showToast(ctx, R.string.data_exception);
            });
        }, c -> {
            final List<Friend> friends = FriendDao.getInstance().getAllFriends(mLoginUserId);
            Map<String, Integer> existMap = new HashMap<>();
            List<BaseSortModel<Friend>> sortedList = SortHelper.toSortedModelList(friends, existMap, Friend::getShowName);
            c.uiThread(r -> {
//                DialogHelper.dismissProgressDialog();
                tvFriendCount.setText(String.valueOf(sortedList.size()));
                mSideBar.setExistMap(existMap);
                mSortFriends = sortedList;
                mAdapter.setData(sortedList);
                mPullToRefreshListView.onRefreshComplete();
            });
        });
    }

    /**
     * 从服务端获取好友列表，更新数据库
     */
    private void upDataFriend() {
        // 这鬼库马上停止刷新会停不了，只能post一下，
        mPullToRefreshListView.post(() -> {
            mPullToRefreshListView.onRefreshComplete();
        });
        // 使用这个对话框阻止其他操作，以免主线程读写数据库被阻塞anr,
//        DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        HttpUtils.get().url(coreManager.getConfig().FRIENDS_ATTENTION_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<AttentionUser>(AttentionUser.class) {
                    @Override
                    public void onResponse(ArrayResult<AttentionUser> result) {
                        if (result.getResultCode() == 1) {
                            AsyncUtils.doAsync(FriendFragment.this, e -> {
                                Reporter.post("保存好友失败，", e);
                                AsyncUtils.runOnUiThread(requireContext(), ctx -> {
//                                    DialogHelper.dismissProgressDialog();
                                    Log.e("zx", "onResponse: "+e.toString() );
                                    ToastUtil.showToast(ctx, R.string.data_exception);
                                });
                            }, c -> {
                                FriendDao.getInstance().addAttentionUsers(coreManager.getSelf().getUserId(), result.getData(),
                                        new OnCompleteListener2() {

                                            @Override
                                            public void onLoading(int progressRate, int sum) {

                                            }

                                            @Override
                                            public void onCompleted() {
                                                c.uiThread(r -> {
                                                    r.loadData();
                                                });
                                            }
                                        });
                            });
                        } else {
//                            DialogHelper.dismissProgressDialog();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
//                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }
}
