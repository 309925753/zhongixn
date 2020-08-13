package com.vkzwbim.chat.ui.other;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.qrcode.utils.CommonUtils;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.bean.Friend;
import com.vkzwbim.chat.db.dao.FriendDao;
import com.vkzwbim.chat.helper.AvatarHelper;
import com.vkzwbim.chat.ui.base.BaseActivity;
import com.vkzwbim.chat.util.FileUtil;
import com.vkzwbim.chat.view.MessageAvatar;

/**
 * Created by Administrator on 2017/9/14 0014.
 * 二维码类
 */
public class QRcodeActivity extends BaseActivity {
    private ImageView qrcode;
    private ImageView mPAva;
    private ImageView head_person;
    private MessageAvatar mGAva;
    private MessageAvatar head_group;

    private RelativeLayout img_head;
    private TextView tv_nickname;
    private TextView tv_bottom_tip;


    private boolean isgroup;
    private String userId;
    private String userAvatar;
    private String roomJid;
    private String action;
    private String str;
    private String nickname;
private RelativeLayout my_qcode;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_im_code_image);
        if (getIntent() != null) {
            isgroup = getIntent().getBooleanExtra("isgroup", false);
            userId = getIntent().getStringExtra("userid");
            userAvatar = getIntent().getStringExtra("userAvatar");
            nickname = getIntent().getStringExtra("nickname");
            if (isgroup) {
                roomJid = getIntent().getStringExtra("roomJid");
            }
        }
        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        my_qcode=findViewById(R.id.my_qcode);
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.qrcode));
        ImageView mIvTitleRight = (ImageView) findViewById(R.id.iv_title_right);
        mIvTitleRight.setImageResource(R.mipmap.download_icon);
        mIvTitleRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileUtil.saveImageToGallery2(mContext, getBitmap(QRcodeActivity.this.getWindow().getDecorView().findViewById(R.id.my_qcode)));
            }
        });
    }

    private void initView() {
        qrcode = (ImageView) findViewById(R.id.qrcode);
        mPAva = (ImageView) findViewById(R.id.avatar_img);
        mGAva = (MessageAvatar) findViewById(R.id.avatar_imgS);
        tv_bottom_tip=findViewById(R.id.tv_bottom_tip);
        tv_nickname=findViewById(R.id.tv_nick_name);
        head_person = (ImageView) findViewById(R.id.head_person);
        head_group = (MessageAvatar) findViewById(R.id.head_group);



        if (isgroup) {
            action = "group";
            head_group.setVisibility(View.VISIBLE);
            mGAva.setVisibility(View.VISIBLE);
            tv_bottom_tip.setText("扫一扫上面的二维码图片,加群");
        } else {
            action = "user";
            tv_bottom_tip.setText("扫一扫上面的二维码图片,加我好友");
            head_person.setVisibility(View.VISIBLE);
            mPAva.setVisibility(View.VISIBLE);
        }

        tv_nickname.setText(nickname);
        str = coreManager.getConfig().website + "?action=" + action + "&shikuId=" + userId;

        Log.e("zq", "二维码链接：" + str);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        // 生成二维码
        bitmap = CommonUtils.createQRCode(str, screenWidth -200, screenWidth -200);

        // 显示 二维码 与 头像
        if (isgroup) {// 群组头像
            Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), roomJid);
            if (friend != null) {
                mGAva.fillData(friend);
                head_group.fillData(friend);

            }
        } else {// 用户头像
           /* Glide.with(mContext)
                    .load(AvatarHelper.getInstance().getAvatarUrl(userId, false))
                    .asBitmap()
                    .signature(new StringSignature(UserAvatarDao.getInstance().getUpdateTime(userId)))
                    .dontAnimate()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            bitmap = EncodingUtils.createQRCode(str, screenWidth - 200, screenWidth - 200,
                                    resource);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            super.onLoadFailed(e, errorDrawable);
                            bitmap = EncodingUtils.createQRCode(str, screenWidth - 200, screenWidth - 200,
                                    BitmapFactory.decodeResource(getResources(), R.drawable.avatar_normal));// 默认头像
                        }
                    });*/

            AvatarHelper.getInstance().displayAvatar(userAvatar, mPAva);
            AvatarHelper.getInstance().displayAvatar(userAvatar, head_person);
        }
        qrcode.setImageBitmap(bitmap);

    }

    /**
     * 获取这个view的缓存bitmap,
     */
    private Bitmap getBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap result = Bitmap.createBitmap(view.getDrawingCache());
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);
        return result;
    }
}