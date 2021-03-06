package com.vkzwbim.chat.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vkzwbim.chat.AppConstant;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.adapter.MessageEventHongdian;
import com.vkzwbim.chat.broadcast.OtherBroadcast;
import com.vkzwbim.chat.helper.AvatarHelper;
import com.vkzwbim.chat.ui.base.EasyFragment;
import com.vkzwbim.chat.ui.circle.BusinessCircleActivity;
import com.vkzwbim.chat.ui.circle.SelectPicPopupWindow;
import com.vkzwbim.chat.ui.circle.range.NewZanActivity;
import com.vkzwbim.chat.ui.circle.range.SendAudioActivity;
import com.vkzwbim.chat.ui.circle.range.SendFileActivity;
import com.vkzwbim.chat.ui.circle.range.SendShuoshuoActivity;
import com.vkzwbim.chat.ui.circle.range.SendVideoActivity;
import com.vkzwbim.chat.ui.me.BasicInfoEditActivity;
import com.vkzwbim.chat.ui.me.GuanyuActivity;
import com.vkzwbim.chat.ui.me.MyCollection;
import com.vkzwbim.chat.ui.me.SettingActivity;
import com.vkzwbim.chat.ui.me.redpacket.WxPayBlance;
import com.vkzwbim.chat.ui.other.LinkQRcodeActivity;
import com.vkzwbim.chat.ui.search.SearchAllActivity;
import com.vkzwbim.chat.ui.tool.SingleImagePreviewActivity;
import com.vkzwbim.chat.util.UiUtils;

import de.greenrobot.event.EventBus;

public class MeFragment extends EasyFragment implements View.OnClickListener {

    private ImageView mAvatarImg;
    private TextView mNickNameTv;
    private TextView mPhoneNumTv;
    private TextView skyTv, setTv;

    private TextView mTvTitle;
    private ImageView mIvTitleRight;
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, OtherBroadcast.SYNC_SELF_DATE_NOTIFY)) {
                updateUI();
            }
        }
    };
    private SelectPicPopupWindow menuWindow;
    // 为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            if (menuWindow != null) {
                // 顶部一排按钮复用这个listener, 没有menuWindow,
                menuWindow.dismiss();
            }
            Intent intent = new Intent();
            switch (v.getId()) {
                case R.id.btn_send_picture:
                    // 发表图文，
                    intent.setClass(getActivity(), SendShuoshuoActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_send_voice:
                    // 发表语音
                    intent.setClass(getActivity(), SendAudioActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_send_video:
                    // 发表视频
                    intent.setClass(getActivity(), SendVideoActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_send_file:
                    // 发表文件
                    intent.setClass(getActivity(), SendFileActivity.class);
                    startActivity(intent);
                    break;
                case R.id.new_comment:
                    // 最新评论&赞
                    Intent intent2 = new Intent(getActivity(), NewZanActivity.class);
                    intent2.putExtra("OpenALL", true);
                    startActivity(intent2);
                    EventBus.getDefault().post(new MessageEventHongdian(0));
                    break;
                default:
                    break;
            }
        }
    };

    public MeFragment() {
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_me;
    }


    private void initActionBar() {
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText("我的");
        mTvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        mIvTitleRight = (ImageView) findViewById(R.id.iv_title_right);

        ImageView iv_title_right_right = findViewById(R.id.iv_title_right_right);
        iv_title_right_right.setVisibility(View.GONE);
        iv_title_right_right.setImageResource(R.mipmap.me_camera);
        iv_title_right_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuWindow = new SelectPicPopupWindow(getActivity(), itemsOnClick);
                menuWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                menuWindow.showAsDropDown(v,
                        -(menuWindow.getContentView().getMeasuredWidth() - v.getWidth() / 2 - 40),
                        0);
            }
        });
        appendClick(mIvTitleRight);
    }
    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            initActionBar();
            initView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mUpdateReceiver);
    }

    private void initView() {
        skyTv = (TextView) findViewById(R.id.MySky);
        setTv = (TextView) findViewById(R.id.SettingTv);
        skyTv.setText(getString(R.string.my_moments));
        setTv.setText(getString(R.string.settings));
        findViewById(R.id.info_rl).setOnClickListener(this);
        findViewById(R.id.live_rl).setOnClickListener(this);
        findViewById(R.id.douyin_rl).setOnClickListener(this);

        findViewById(R.id.ll_more).setVisibility(View.GONE);

        findViewById(R.id.my_monry).setOnClickListener(this);
        // 关闭红包功能，隐藏我的钱包
        if (coreManager.getConfig().displayRedPacket||coreManager.getSelf().getPhone().equals("18899991111")) {// 切换新旧两种ui对应我的页面是否显示视频会议、直播、短视频，

            findViewById(R.id.my_monry).setVisibility(View.GONE);
        }

        findViewById(R.id.my_space_rl).setOnClickListener(this);
        findViewById(R.id.my_collection_rl).setOnClickListener(this);
        findViewById(R.id.local_course_rl).setOnClickListener(this);
        findViewById(R.id.local_code_rl).setOnClickListener(this);
        findViewById(R.id.setting_rl).setOnClickListener(this);

        mAvatarImg = (ImageView) findViewById(R.id.avatar_img);
        mNickNameTv = (TextView) findViewById(R.id.nick_name_tv);
        mPhoneNumTv = (TextView) findViewById(R.id.phone_number_tv);
        String loginUserId = coreManager.getSelf().getUserId();
        AvatarHelper.getInstance().displayAvatar(coreManager.getSelf().getNickName(), loginUserId, mAvatarImg, false);
        mNickNameTv.setText(coreManager.getSelf().getNickName());

        mAvatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SingleImagePreviewActivity.class);
                intent.putExtra(AppConstant.EXTRA_IMAGE_URI, coreManager.getSelf().getUserId());
                startActivity(intent);
            }
        });


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OtherBroadcast.SYNC_SELF_DATE_NOTIFY);
        getActivity().registerReceiver(mUpdateReceiver, intentFilter);

    }


    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        int id = v.getId();
        switch (id) {
            case R.id.info_rl:
                // 我的资料
                startActivityForResult(new Intent(getActivity(), BasicInfoEditActivity.class), 1);
                break;
            case R.id.my_monry:
                // 我的钱包
                startActivity(new Intent(getActivity(), WxPayBlance.class));
                break;
            case R.id.my_space_rl:
                // 我的动态
                Intent intent = new Intent(getActivity(), BusinessCircleActivity.class);
                intent.putExtra(AppConstant.EXTRA_CIRCLE_TYPE, AppConstant.CIRCLE_TYPE_PERSONAL_SPACE);
                startActivity(intent);
                break;
            case R.id.my_collection_rl:
                // 我的收藏
                startActivity(new Intent(getActivity(), MyCollection.class));
                break;
            case R.id.local_course_rl:
                // 我的课件
                startActivity(new Intent(getActivity(), GuanyuActivity.class));
                break;

                case R.id.local_code_rl:
                // 我的下载
                startActivity(new Intent(getActivity(), LinkQRcodeActivity.class));
                break;
            case R.id.setting_rl:
                // 设置
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 || resultCode == Activity.RESULT_OK) {// 个人资料更新了
            updateUI();
        }
    }

    /**
     * 用户的信息更改的时候，ui更新
     */
    private void updateUI() {
        if (mAvatarImg != null) {
            AvatarHelper.getInstance().displayAvatar(coreManager.getSelf().getUserId(), mAvatarImg, true);
        }
        if (mNickNameTv != null) {
            mNickNameTv.setText(coreManager.getSelf().getNickName());
        }

        if (mPhoneNumTv != null) {
            String phoneNumber = coreManager.getSelf().getAccount();
//            int mobilePrefix = PreferenceUtils.getInt(getContext(), Constants.AREA_CODE_KEY, -1);
//            String sPrefix = String.valueOf(mobilePrefix);
//            // 删除开头的区号，
//            if (phoneNumber.startsWith(sPrefix)) {
//                phoneNumber = phoneNumber.substring(sPrefix.length());
//            }
            mPhoneNumTv.setText("众信聊天号: "+phoneNumber);
        }
    }
}
