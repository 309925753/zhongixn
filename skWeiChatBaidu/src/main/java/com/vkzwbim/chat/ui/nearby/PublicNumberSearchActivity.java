package com.vkzwbim.chat.ui.nearby;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.vkzwbim.chat.R;
import com.vkzwbim.chat.ui.base.BaseActivity;
import com.vkzwbim.chat.ui.tool.ButtonColorChange;

public class PublicNumberSearchActivity extends BaseActivity {
    private EditText mKeyWordEdit;
    private Button mSearchBtn;

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, PublicNumberSearchActivity.class);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_number_search);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.search_public_number);
        initView();
    }

    private void initView() {
        mKeyWordEdit = (EditText) findViewById(R.id.keyword_edit);
        // 获取焦点，键盘弹出
        mKeyWordEdit.requestFocus();

        mSearchBtn = (Button) findViewById(R.id.search_btn);
        ButtonColorChange.colorChange(this, mSearchBtn);
        mSearchBtn.setOnClickListener(v -> {
            PublicNumberListActivity.start(mContext, mKeyWordEdit.getText().toString());
        });
    }
}
