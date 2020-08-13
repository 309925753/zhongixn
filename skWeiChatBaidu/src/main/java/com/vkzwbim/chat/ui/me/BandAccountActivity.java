package com.vkzwbim.chat.ui.me;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.helper.DialogHelper;
import com.vkzwbim.chat.ui.base.BaseActivity;
import com.vkzwbim.chat.util.EventBusHelper;
import com.vkzwbim.chat.view.SelectionFrame;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.JsonCallback;

import okhttp3.Call;

/**
 * 绑定账号
 */
public class BandAccountActivity extends BaseActivity implements View.OnClickListener {

    private TextView tvBindWx;
    private boolean isBandWx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_band_account);
        EventBusHelper.register(this);
        initActionBar();
        initView();
        getBindInfo();
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
        tvTitle.setText(getString(R.string.bind_account_set));
    }

    private void initView() {
        tvBindWx = findViewById(R.id.tv_bind_wx);
        findViewById(R.id.wx_band_rl).setOnClickListener(this);
    }

    private void updateUi() {
        String str = getString(isBandWx ? R.string.banded : R.string.no_band);
        tvBindWx.setText(str);
    }

    // 获取用户的设置状态
    private void getBindInfo() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.get().url(coreManager.getConfig().USER_GET_BAND_ACCOUNT)
                .params("access_token", coreManager.getSelfStatus().accessToken)
                .build()
                .execute(new JsonCallback() {

                    @Override
                    public void onResponse(String result) {
                        DialogHelper.dismissProgressDialog();

                        JSONObject json = JSONObject.parseObject(result);
                        JSONArray array = json.getJSONArray("data");
                        isBandWx = array != null && array.size() > 0;
                        updateUi();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        updateUi();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        showSelectDialog();
    }

    private void showSelectDialog() {
        String content = isBandWx ? getResources().getString(R.string.dialog_toast) : getResources().getString(R.string.dialog_being_go);
        String buttonText = isBandWx ? getResources().getString(R.string.dialog_Relieve) : getResources().getString(R.string.dialog_go);
        SelectionFrame selectionFrame = new SelectionFrame(mContext);
        selectionFrame.setSomething(null, content, getString(R.string.cancel), buttonText,
                new SelectionFrame.OnSelectionFrameClickListener() {

                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void confirmClick() {
                    }
                });
        selectionFrame.show();
    }

    // 修改用户绑定
    private void unBindInfo() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.get().url(coreManager.getConfig().USER_UN_BAND_ACCOUNT)
                .params("access_token", coreManager.getSelfStatus().accessToken)
                .params("type", "2")
                .build()
                .execute(new JsonCallback() {
                    @Override
                    public void onResponse(String result) {
                        DialogHelper.dismissProgressDialog();

                        isBandWx = false;
                        updateUi();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        updateUi();
                    }
                });
    }

}
