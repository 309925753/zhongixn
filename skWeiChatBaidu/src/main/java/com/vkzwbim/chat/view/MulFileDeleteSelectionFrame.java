package com.vkzwbim.chat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.vkzwbim.chat.R;
import com.vkzwbim.chat.util.ScreenUtil;

/**
 * 群文件共享 删除群文件 弹窗
 */
public class MulFileDeleteSelectionFrame extends Dialog {
    private TextView
            titleTv,
            describeTv,
            cancelTv,
            confirmTv;
    private View view;

    private FrameInitSuccessListener frameInitSuccessListener;

    public MulFileDeleteSelectionFrame(Context context) {
        super(context, R.style.BottomDialog);
    }

    public void setFrameInitSuccessListener(FrameInitSuccessListener frameInitSuccessListener) {
        this.frameInitSuccessListener = frameInitSuccessListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_delete_muc_file);
        setCanceledOnTouchOutside(true);
        initView();
        if (frameInitSuccessListener != null) {
            frameInitSuccessListener.initSuccess();
        }
    }

    private void initView() {
        titleTv = (TextView) findViewById(R.id.title);
        describeTv = (TextView) findViewById(R.id.describe);
        cancelTv = (TextView) findViewById(R.id.cancel);
        confirmTv = (TextView) findViewById(R.id.confirm);
        view = findViewById(R.id.v_dark);

        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        lp.width = (int) (ScreenUtil.getScreenWidth(getContext()) * 0.9);
        lp.gravity = Gravity.CENTER;
        o.setAttributes(lp);
    }

    public TextView getTitleTv() {
        return titleTv;
    }

    public TextView getDescribeTv() {
        return describeTv;
    }

    public TextView getCancelTv() {
        return cancelTv;
    }

    public TextView getConfirmTv() {
        return confirmTv;
    }

    public View getView() {
        return view;
    }

    public interface FrameInitSuccessListener {

        void initSuccess();

    }
}
