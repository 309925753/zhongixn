package com.vkzwbim.chat.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cjt2325.cameralibrary.util.LogUtil;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.bean.EventNotifyByTag;
import com.vkzwbim.chat.bean.redpacket.Balance;
import com.vkzwbim.chat.helper.DialogHelper;
import com.vkzwbim.chat.helper.PaySecureHelper;
import com.vkzwbim.chat.ui.base.BaseActivity;
import com.vkzwbim.chat.ui.base.CoreManager;
import com.vkzwbim.chat.ui.tool.ButtonColorChange;
import com.vkzwbim.chat.ui.tool.WebViewActivity;
import com.vkzwbim.chat.util.Constants;
import com.vkzwbim.chat.util.EventBusHelper;
import com.vkzwbim.chat.util.PreferenceUtils;
import com.vkzwbim.chat.util.SkinUtils;
import com.vkzwbim.chat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.JsonCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

import static com.vkzwbim.chat.ui.tool.WebViewActivity.EXTRA_URL;

public class WxPayBlance extends BaseActivity {

    public static final String RSA_PRIVATE = "";
    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_AUTH_FLAG = 2;
    private TextView mBalanceTv;
    private TextView mRechargeTv;
    private TextView mWithdrawTv;

    private TextView mTitleTv;
    private ImageView mTitleLeftIv;
    private ImageView mTitleRightIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wx_pay_blance);
        initActionBar();
        initView();
        checkHasPayPassword();
        EventBusHelper.register(this);
    }

    @Override
    protected void onResume() {
        // todo 提现之后回到该界面，服务端待微信响应之后才会更新余额，此时调用刷新余额的方法获取到的可能还是之前的余额，另加一个EventBus来刷新吧
        super.onResume();
        initData();
    }


    private void initActionBar() {
        getSupportActionBar().hide();

        mTitleTv = findViewById(R.id.tv_title_center);
        mTitleTv.setText("我的钱包");
        mTitleLeftIv = findViewById(R.id.iv_title_left);
        mTitleLeftIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mTitleRightIv = findViewById(R.id.iv_title_right);
        mTitleRightIv.setImageResource(R.mipmap.navigation);
        mTitleRightIv.setVisibility(View.VISIBLE);
        mTitleRightIv.setOnClickListener(v -> {
            // 访问接口 获取记录
            Intent intent = new Intent(WxPayBlance.this, PaymentCenterActivity.class);
            startActivity(intent);
        });
    }
    private void initView() {
        mBalanceTv = (TextView) findViewById(R.id.myblance);
        mRechargeTv = (TextView) findViewById(R.id.chongzhi);
        mWithdrawTv = (TextView) findViewById(R.id.quxian);
        ButtonColorChange.rechargeChange(this, mWithdrawTv, R.drawable.recharge_icon);
        ButtonColorChange.rechargeChange(this, mRechargeTv, R.drawable.chongzhi_icon);
        mWithdrawTv.setTextColor(SkinUtils.getSkin(this).getAccentColor());

        if (!coreManager.getConfig().enablePayModule) {
            mRechargeTv.setVisibility(View.GONE);
            mWithdrawTv.setVisibility(View.GONE);
        }
        mRechargeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmUserInfo("1");
//                Intent intent = new Intent(WxPayBlance.this, WxPayAdd.class);
//                startActivity(intent);
            }
        });

        mWithdrawTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PaySecureHelper.inputPayPwd(WxPayBlance.this,password -> {
                    sendRed(password);
//                    confirmUserPwd(password);
                });
            }
        });




        findViewById(R.id.tvPayPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WxPayBlance.this, ChangePayPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkHasPayPassword() {
        boolean hasPayPassword = PreferenceUtils.getBoolean(this, Constants.IS_PAY_PASSWORD_SET + coreManager.getSelf().getUserId(), true);
        if (!hasPayPassword) {
            ToastUtil.showToast(this, R.string.tip_no_pay_password);
            Intent intent = new Intent(this, ChangePayPasswordActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initData() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        HttpUtils.get().url(coreManager.getConfig().RECHARGE_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<Balance>(Balance.class) {

                    @Override
                    public void onResponse(ObjectResult<Balance> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            DecimalFormat decimalFormat = new DecimalFormat("0.00");
                            Balance balance = result.getData();
                            coreManager.getSelf().setBalance(Double.parseDouble(decimalFormat.format(balance.getBalance())));
                            mBalanceTv.setText("￥" + decimalFormat.format(Double.parseDouble(decimalFormat.format(balance.getBalance()))));
                        } else {
                            ToastUtil.showErrorData(WxPayBlance.this);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(WxPayBlance.this);
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventNotifyByTag message) {
        if (TextUtils.equals(message.tag, EventNotifyByTag.Withdraw)) {
            initData();
        }
    }


    public void confirmUserInfo(String type) {
        HashMap<String, String> params = new HashMap<String, String>();
        String userId = CoreManager.getInstance(WxPayBlance.this).getSelf().getUserId();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", userId);
        params.put("type", type);

        HttpUtils.get().url(coreManager.getConfig().CHECKCERTIFY)
                .params(params)
                .build(true).execute(new JsonCallback() {
            @Override
            public void onResponse(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String success = jsonObject.getString("success");
                    String msg = jsonObject.getString("msg");
                    if (success.equals("0") || success.equals("1")) {
                        String dataUrl = jsonObject.getString("data");
                        String REGEX_URL = "((((ht|f)tp(s?))\\:\\/\\/)([\\w\\-]+)(\\.[\\w\\-]+)+|([\\w\\-]+\\.)+(com|cn|cc|top|xyz|edu|gov|mil|net|org|biz|info|name|museum|us|ca|uk))(\\:\\d+)?(\\/([\\w_\\-\\.~!*\\'()\\;\\:@&=+&$,/?#%]*))*";
                        Pattern pattern = Pattern.compile(REGEX_URL);
                        Matcher m = pattern.matcher(dataUrl);
                        if (!m.matches()) {
                            ToastUtil.showToast(WxPayBlance.this, "无效的链接");
                            return;
                        } else {

                            Intent intent = new Intent(WxPayBlance.this, WebViewActivity.class);
                            intent.putExtra(EXTRA_URL, dataUrl);
                            WxPayBlance.this.startActivity(intent);
                        }
                    } else {
                        ToastUtil.showToast(WxPayBlance.this, msg);
                    }
                } catch (Exception e) {

                }
            }

            @Override
            public void onError(Call call, Exception e) {
                LogUtil.e("ckjj " + e.getMessage());
            }
        });


    }


    public void confirmUserPwd(String password) {
        HashMap<String, String> params = new HashMap<String, String>();
        String userId = CoreManager.getInstance(WxPayBlance.this).getSelf().getUserId();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", userId);
        params.put("payPassword", password);

        HttpUtils.get().url(coreManager.getConfig().VERIFYPAYPASSWORD)
                .params(params)
                .build(true).execute(new JsonCallback() {
            @Override
            public void onResponse(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String success = jsonObject.getString("success");
                    String msg = jsonObject.getString("msg");
                    if (success.equals("0")) {
                        confirmUserInfo("2");
                    } else {
                        ToastUtil.showToast(WxPayBlance.this, msg);
                    }
                } catch (Exception e) {

                }
            }

            @Override
            public void onError(Call call, Exception e) {
                LogUtil.e("ckjj " + e.getMessage());
            }
        });

    }


    public void sendRed(  String payPassword) {
        if (!coreManager.isLogin()) {
            return;
        }
        DialogHelper.showDefaulteMessageProgressDialog(mContext);

        Map<String, String> params = new HashMap<>();
        params.put("amount", "0.01");
        PaySecureHelper.generateParam(mContext, payPassword, params,"",
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(mContext, mContext.getString(R.string.tip_pay_secure_place_holder,t.getMessage()));
                }, (p, code) -> {

                    confirmUserInfo("2");
                    DialogHelper.dismissProgressDialog();

                }
        );
    }
}
