package com.vkzwbim.chat.ui.me;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.vkzwbim.chat.R;
import com.vkzwbim.chat.ui.base.BaseActivity;
import com.vkzwbim.chat.util.PreferenceUtils;
import com.vkzwbim.chat.util.SkinUtils;
import com.vkzwbim.chat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.JsonCallback;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

public class DescriptionActivity extends BaseActivity {
    private static boolean isEnable;
    private String description;
    private  TextView tv_textcount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);
        initActionBar();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        tv_textcount=findViewById(R.id.tv_textcount);
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.personalized_signature);
        EditText et_desc = findViewById(R.id.et_desc);
        String defaulDesc = PreferenceUtils.getString(DescriptionActivity.this, "description", coreManager.getSelf().getDescription());
        et_desc.setHint(defaulDesc);
        TextView tv_title_right = (TextView) findViewById(R.id.tv_title_right);
        if (TextUtils.isEmpty(description)) {
            isEnable = false;
            tv_title_right.setAlpha(0.6f);
        } else {
            isEnable = true;
            tv_title_right.setAlpha(1f);
        }
        et_desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e("zx", "onTextChanged: " + start + " before:  " + before + " count: " + count);
                if (count == 0 && start == 0 && et_desc.getText().toString().trim().equals(coreManager.getSelf().getDescription())) {
                    isEnable = false;
                    tv_title_right.setAlpha(0.6f);
                } else {
                    isEnable = true;
                    tv_title_right.setAlpha(1f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
              tv_textcount.setText((30-s.length())+"");
            }
        });
        tv_title_right.setText(getResources().getString(R.string.save));
        tv_title_right.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_grey_circle));
        ViewCompat.setBackgroundTintList(tv_title_right, ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
        tv_title_right.setTextColor(getResources().getColor(R.color.white));
        tv_title_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("zx", "onClickv: " + et_desc.getText().toString().trim());
                description = et_desc.getText().toString().trim();
                if (!TextUtils.isEmpty(description) && !description.equals(coreManager.getSelf().getDescription())) {
                    Log.e("zx", "onClick: " + description);
                    loadDescription(description);
                }
            }
        });

    }

    private void loadDescription(String description) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("description", description);

        HttpUtils.get().url(coreManager.getConfig().USER_DESCRIPTION)
                .params(params)
                .build()
                .execute(new JsonCallback() {
                    @Override
                    public void onResponse(String result) {
                        Log.e("zx", "onResponse: " + result);
                        if (!TextUtils.isEmpty(result)) {
                            PreferenceUtils.putString(DescriptionActivity.this, "description", description);
                            ToastUtil.showToast(DescriptionActivity.this, getString(R.string.save_success));
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(DescriptionActivity.this);
                    }
                });
    }
}
