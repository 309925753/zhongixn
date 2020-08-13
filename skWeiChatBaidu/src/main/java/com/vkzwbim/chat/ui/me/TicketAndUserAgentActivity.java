package com.vkzwbim.chat.ui.me;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.vkzwbim.chat.R;
import com.vkzwbim.chat.ui.base.BaseActivity;
import com.vkzwbim.chat.util.UriPathUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2018/10/19.
 */

public class TicketAndUserAgentActivity extends BaseActivity {
TextView tv_user_agent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_agent);
        getSupportActionBar().hide();

        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText("用户协议");
        tv_user_agent=findViewById(R.id.tv_user_agent);
        setAgentText();
    }


    private void setAgentText() {
        InputStream is = null;
        try {
            is = getAssets().open("user_agree_doc.txt");
            String userAgent = UriPathUtil.getString(is);
            tv_user_agent.setText(userAgent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
