package com.vkzwbim.chat.ui.account;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.vkzwbim.chat.AppConfig;
import com.vkzwbim.chat.MyApplication;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.bean.Code;
import com.vkzwbim.chat.bean.User;
import com.vkzwbim.chat.db.dao.UserDao;
import com.vkzwbim.chat.helper.DialogHelper;
import com.vkzwbim.chat.helper.LoginHelper;
import com.vkzwbim.chat.helper.PasswordHelper;
import com.vkzwbim.chat.sp.UserSp;
import com.vkzwbim.chat.ui.base.BaseActivity;
import com.vkzwbim.chat.ui.tool.ButtonColorChange;
import com.vkzwbim.chat.util.Constants;
import com.vkzwbim.chat.util.PreferenceUtils;
import com.vkzwbim.chat.util.StringUtils;
import com.vkzwbim.chat.util.ToastUtil;
import com.vkzwbim.chat.util.ViewPiexlUtil;
import com.vkzwbim.chat.util.secure.LoginPassword;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;

import static com.vkzwbim.chat.AppConfig.BROADCASTTEST_ACTION;

/**
 * 忘记密码
 */
public class FindPwdActivity extends BaseActivity implements View.OnClickListener {
    private Button btn_getCode, btn_change;
    private EditText mPhoneNumberEdit;
    private EditText mPasswordEdit, mConfigPasswordEdit, mAuthCodeEdit;
    private TextView tv_prefix;
    private int mobilePrefix = 86;
    // 驗證碼
    private String randcode;
    // 图形验证码
    private EditText mImageCodeEdit;
    private ImageView mImageCodeIv;
    private ImageView mRefreshIv;
    private int reckonTime = 60;
    private Handler mReckonHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x1) {
                btn_getCode.setText("(" + reckonTime + ")");
                reckonTime--;
                if (reckonTime < 0) {
                    mReckonHandler.sendEmptyMessage(0x2);
                } else {
                    mReckonHandler.sendEmptyMessageDelayed(0x1, 1000);
                }
            } else if (msg.what == 0x2) {
                // 60秒结束
                btn_getCode.setText(getString(R.string.send));
                btn_getCode.setEnabled(true);
                reckonTime = 60;
            }
        }
    };

    public FindPwdActivity() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        initView();
    }

    private void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);

        tv_prefix = (TextView) findViewById(R.id.tv_prefix);
        tv_prefix.setOnClickListener(this);
        mobilePrefix = PreferenceUtils.getInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        tv_prefix.setText("+" + mobilePrefix);

        btn_getCode = (Button) findViewById(R.id.send_again_btn);
        ButtonColorChange.colorChange(this, btn_getCode);
        btn_getCode.setOnClickListener(this);
        btn_change = (Button) findViewById(R.id.login_btn);
        ButtonColorChange.colorChange(this, btn_change);
        btn_change.setOnClickListener(this);

        mPhoneNumberEdit = (EditText) findViewById(R.id.phone_numer_edit);
        tvTitle.setText(getString(R.string.forget_password));
        if (coreManager.getSelf() != null && !TextUtils.isEmpty(coreManager.getSelf().getTelephone())) {
            String telephone = coreManager.getSelf().getTelephone();
            String prefix = String.valueOf(mobilePrefix);
            if (telephone.startsWith(prefix)) {
                telephone = telephone.substring(prefix.length());
            }
            mPhoneNumberEdit.setText(telephone);
        } else {
            String userId = UserSp.getInstance(this).getUserId("");
            if (!TextUtils.isEmpty(userId)) {
                User mLastLoginUser = UserDao.getInstance().getUserByUserId(userId);
                if (mLastLoginUser != null) {
                    String phoneNumber = mLastLoginUser.getTelephone();
                    int mobilePrefix = PreferenceUtils.getInt(FindPwdActivity.this, Constants.AREA_CODE_KEY, -1);
                    String sPrefix = String.valueOf(mobilePrefix);
                    // 删除开头的区号，
                    if (phoneNumber.startsWith(sPrefix)) {
                        phoneNumber = phoneNumber.substring(sPrefix.length());
                    }
                    mPhoneNumberEdit.setText(phoneNumber);
                }
            }
        }

        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        PasswordHelper.bindPasswordEye(mPasswordEdit, findViewById(R.id.tbEye));
        mConfigPasswordEdit = (EditText) findViewById(R.id.confirm_password_edit);
        PasswordHelper.bindPasswordEye(mConfigPasswordEdit, findViewById(R.id.tbEyeConfirm));
        mImageCodeEdit = (EditText) findViewById(R.id.image_tv);
        mAuthCodeEdit = (EditText) findViewById(R.id.auth_code_edit);
        List<EditText> mEditList = new ArrayList<>();
        mEditList.add(mPasswordEdit);
        mEditList.add(mConfigPasswordEdit);
        mEditList.add(mImageCodeEdit);
        mEditList.add(mAuthCodeEdit);
        setBound(mEditList);

        mImageCodeIv = (ImageView) findViewById(R.id.image_iv);
        mRefreshIv = (ImageView) findViewById(R.id.image_iv_refresh);
        mRefreshIv.setOnClickListener(this);

        mPhoneNumberEdit.setHint(getString(R.string.hint_input_phone_number));
        mAuthCodeEdit.setHint(getString(R.string.please_input_auth_code));
        mPasswordEdit.setHint(getString(R.string.please_input_new_password));
        mConfigPasswordEdit.setHint(getString(R.string.please_confirm_new_password));
        btn_change.setText(getString(R.string.change_password));

        // 请求图形验证码
        if (!TextUtils.isEmpty(mPhoneNumberEdit.getText().toString())) {
            requestImageCode();
        }
    }

    public void setBound(List<EditText> mEditList) {// 为Edit内的drawableLeft设置大小
        for (int i = 0; i < mEditList.size(); i++) {
            Drawable[] compoundDrawable = mEditList.get(i).getCompoundDrawables();
            Drawable drawable = compoundDrawable[0];
            if (drawable != null) {
                drawable.setBounds(0, 0, ViewPiexlUtil.dp2px(this, 20), ViewPiexlUtil.dp2px(this, 20));
                mEditList.get(i).setCompoundDrawables(drawable, null, null, null);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_prefix:
                // 选择国家区号
                Intent intent = new Intent(this, SelectPrefixActivity.class);
                startActivityForResult(intent, SelectPrefixActivity.REQUEST_MOBILE_PREFIX_LOGIN);
                break;
            case R.id.image_iv_refresh:
                if (TextUtils.isEmpty(mPhoneNumberEdit.getText().toString())) {
                    ToastUtil.showToast(this, getString(R.string.tip_phone_number_empty_request_verification_code));
                } else {
                    requestImageCode();
                }
                break;
            case R.id.send_again_btn:
                // 获取验证码
                String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
                String imagecode = mImageCodeEdit.getText().toString().trim();
                if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(imagecode)) {
                    ToastUtil.showToast(mContext, getString(R.string.tip_phone_number_verification_code_empty));
                    return;
                }
                if (!configPassword()) {// 两次密码是否一致
                    return;
                }
                verifyTelephone(phoneNumber, imagecode);
                break;
            case R.id.login_btn:
                // 确认修改
                if (nextStep()) {
                    // 如果验证码正确，则可以重置密码
                    resetPassword();
                }
                break;
        }
    }

    /**
     * 修改密码
     */
    private void resetPassword() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        final String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
        final String password = mPasswordEdit.getText().toString().trim();
        String authCode = mAuthCodeEdit.getText().toString().trim();
        Map<String, String> params = new HashMap<>();
        params.put("telephone", phoneNumber);
        params.put("randcode", authCode);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("newPassword", LoginPassword.encodeMd5(password));

        HttpUtils.get().url(coreManager.getConfig().USER_PASSWORD_RESET)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(FindPwdActivity.this, result)) {
                            Toast.makeText(FindPwdActivity.this, getString(R.string.update_sccuess), Toast.LENGTH_SHORT).show();
                            if (coreManager.getSelf() != null
                                    && !TextUtils.isEmpty(coreManager.getSelf().getTelephone())) {
                                UserSp.getInstance(mContext).clearUserInfo();
                                MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_SIMPLE_TELPHONE;
                                coreManager.logout();
                                LoginHelper.broadcastLogout(mContext);
                                LoginHistoryActivity.start(FindPwdActivity.this);

                                //发送广播  重新拉起app
                                Intent intent = new Intent(BROADCASTTEST_ACTION);
                                intent.setComponent(new ComponentName(AppConfig.sPackageName, AppConfig.sPackageName + ".MyBroadcastReceiver"));
                                sendBroadcast(intent);
                            } else {// 本地连电话都没有，说明之前没有登录过 修改成功后直接跳转至登录界面
                                startActivity(new Intent(FindPwdActivity.this, LoginActivity.class));
                            }
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(FindPwdActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 请求图形验证码
     */
    private void requestImageCode() {
        Map<String, String> params = new HashMap<>();
        params.put("telephone", mobilePrefix + mPhoneNumberEdit.getText().toString().trim());
        String url = HttpUtils.get().url(coreManager.getConfig().USER_GETCODE_IMAGE)
                .params(params)
                .buildUrl();
        Glide.with(mContext).load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        mImageCodeIv.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        Toast.makeText(FindPwdActivity.this, R.string.tip_verification_code_load_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 请求验证码
     */
    private void verifyTelephone(String phoneNumber, String imageCode) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        String language = Locale.getDefault().getLanguage();
        params.put("language", language);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("telephone", phoneNumber);
        params.put("imgCode", imageCode);
        params.put("isRegister", String.valueOf(0));
        params.put("version", "1");

        /**
         * 只判断中国手机号格式
         */
        if (!StringUtils.isMobileNumber(phoneNumber) && mobilePrefix == 86) {
            Toast.makeText(this, getString(R.string.Input_11_phoneNumber), Toast.LENGTH_SHORT).show();
            return;
        }

        HttpUtils.get().url(coreManager.getConfig().SEND_AUTH_CODE)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Code>(Code.class) {
                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            Toast.makeText(FindPwdActivity.this, R.string.verification_code_send_success, Toast.LENGTH_SHORT).show();
                            btn_getCode.setEnabled(false);
                            // 开始计时
                            mReckonHandler.sendEmptyMessage(0x1);
                            if (result.getData() != null && result.getData().getCode() != null) {
                                // 得到验证码
                                randcode = result.getData().getCode();
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(FindPwdActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 确认两次输入的密码是否一致
     */
    private boolean configPassword() {
        String password = mPasswordEdit.getText().toString().trim();
        String confirmPassword = mConfigPasswordEdit.getText().toString().trim();
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            mPasswordEdit.requestFocus();
            mPasswordEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.password_empty_error));
            return false;
        }
        if (TextUtils.isEmpty(confirmPassword) || confirmPassword.length() < 6) {
            mConfigPasswordEdit.requestFocus();
            mConfigPasswordEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.confirm_password_empty_error));
            return false;
        }
        if (confirmPassword.equals(password)) {
            return true;
        } else {
            mConfigPasswordEdit.requestFocus();
            mConfigPasswordEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.password_confirm_password_not_match));
            return false;
        }
    }

    /**
     * 验证验证码
     */
    private boolean nextStep() {
        final String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, getString(R.string.hint_input_phone_number), Toast.LENGTH_SHORT).show();
            return false;
        }
        /**
         * 只判断中国手机号格式
         */
        if (!StringUtils.isMobileNumber(phoneNumber) && mobilePrefix == 86) {
            Toast.makeText(this, getString(R.string.Input_11_phoneNumber), Toast.LENGTH_SHORT).show();
            return false;
        }
        String authCode = mAuthCodeEdit.getText().toString().trim();
        if (TextUtils.isEmpty(authCode)) {
            Toast.makeText(this, getString(R.string.input_message_code), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!TextUtils.isEmpty(randcode)) {
            if (authCode.equals(randcode)) {
                // 验证码正确
                return true;
            } else {
                Toast.makeText(this, getString(R.string.msg_code_not_ok), Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != SelectPrefixActivity.RESULT_MOBILE_PREFIX_SUCCESS)
            return;
        mobilePrefix = data.getIntExtra(Constants.MOBILE_PREFIX, 86);
        tv_prefix.setText("+" + mobilePrefix);
        // 图形验证码可能因区号失效，
        // 请求图形验证码
        if (!TextUtils.isEmpty(mPhoneNumberEdit.getText().toString())) {
            requestImageCode();
        }
    }
}
