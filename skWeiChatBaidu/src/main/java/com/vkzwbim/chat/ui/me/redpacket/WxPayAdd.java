package com.vkzwbim.chat.ui.me.redpacket;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.vkzwbim.chat.R;
import com.vkzwbim.chat.ui.base.BaseActivity;
import com.vkzwbim.chat.util.EventBusHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 微信充值
 */
public class WxPayAdd extends BaseActivity {

    private List<BigDecimal> mRechargeList = new ArrayList<>();
    private List<CheckedTextView> mRechargeMoneyViewList = new ArrayList<>();

    private TextView mSelectMoneyTv;
    private int mSelectedPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wx_pay_add);

        initActionBar();

        EventBusHelper.register(this);
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
        tvTitle.setText(getString(R.string.recharge));
    }


}
