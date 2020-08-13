package com.vkzwbim.chat.ui.other;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.qrcode.utils.CommonUtils;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.bean.Friend;
import com.vkzwbim.chat.db.dao.FriendDao;
import com.vkzwbim.chat.helper.AvatarHelper;
import com.vkzwbim.chat.ui.base.BaseActivity;
import com.vkzwbim.chat.util.FileUtil;
import com.vkzwbim.chat.util.ScreenUtil;
import com.vkzwbim.chat.view.MessageAvatar;

/**
 * Created by Administrator on 2017/9/14 0014.
 * 二维码类
 */
public class LinkQRcodeActivity extends BaseActivity {
    private ImageView qrcode;

    private String userId;// 因为有变动，userId为通讯号
    private String userAvatar;// 真正的userId
    LinearLayout rl_code;
    private int sizePix;
    private Bitmap bitmapAva;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_link_image);
        sizePix = ScreenUtil.getScreenWidth(mContext) - 200;
        initActionBar();
        initView();
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
        tvTitle.setText(getString(R.string.qrcode));
    }

    private void initView() {
        rl_code = findViewById(R.id.rl_code);
        qrcode = findViewById(R.id.qrcode);
        String url =  coreManager.getConfig().website;

        // 生成二维码
        Bitmap bitmap = CommonUtils.createQRCode(url, sizePix, sizePix);
        // 此bitmap只为无头像的二维码
        qrcode.setImageBitmap(bitmap);
    }



    public void saveImageToGallery(View view) {
        FileUtil.saveImageToGallery2(mContext, getBitmap(rl_code));
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