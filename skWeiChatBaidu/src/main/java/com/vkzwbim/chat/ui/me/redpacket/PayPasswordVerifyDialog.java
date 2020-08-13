package com.vkzwbim.chat.ui.me.redpacket;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.jungly.gridpasswordview.GridPasswordView;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.util.ScreenUtil;

public class PayPasswordVerifyDialog extends Dialog {
    private TextView tvAction;
    private View llMoney;
    private TextView tvMoney;
    private GridPasswordView gpvPassword;

    private String action;
    private String money;

    private OnInputFinishListener onInputFinishListener;

    public PayPasswordVerifyDialog(@NonNull Context context) {
        super(context, R.style.MyDialog);
    }

    public PayPasswordVerifyDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected PayPasswordVerifyDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_password_verify_dialog);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView() {
        findViewById(R.id.ivClose).setOnClickListener(v -> {
            cancel();
        });
        tvAction = findViewById(R.id.tvAction);
        if (action != null) {
            tvAction.setText(action);
        }
        llMoney = findViewById(R.id.llMoney);
        tvMoney = findViewById(R.id.tvMoney);
        if (!TextUtils.isEmpty(money)) {
            tvMoney.setText(money);
            llMoney.setVisibility(View.VISIBLE);
        } else {
            llMoney.setVisibility(View.GONE);
        }
        gpvPassword = findViewById(R.id.gpvPassword);

        gpvPassword.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {

            }

            @Override
            public void onInputFinish(String psw) {
                dismiss();
                if (onInputFinishListener != null) {
                    onInputFinishListener.onInputFinish(psw);
                }
            }
        });
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = (int) (ScreenUtil.getScreenWidth(getContext()) * 0.8);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void setAction(String action) {
        this.action = action;
        if (tvAction != null) {
            tvAction.setText(action);
        }
    }

    public void setMoney(String money) {
        this.money = money;
        if (tvMoney != null) {
            tvMoney.setText(money);
        }
        if (llMoney != null) {
            if (!TextUtils.isEmpty(money)) {
                llMoney.setVisibility(View.VISIBLE);
            } else {
                llMoney.setVisibility(View.GONE);
            }
        }
    }

    public void setOnInputFinishListener(OnInputFinishListener onInputFinishListener) {
        this.onInputFinishListener = onInputFinishListener;
    }

    public interface OnInputFinishListener {
        void onInputFinish(String password);
    }
}
