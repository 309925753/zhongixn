package com.vkzwbim.chat.ui.me.redpacket;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vkzwbim.chat.R;
import com.vkzwbim.chat.bean.redpacket.ConsumeRecordItem;
import com.vkzwbim.chat.helper.DialogHelper;
import com.vkzwbim.chat.ui.base.BaseListActivity;
import com.vkzwbim.chat.ui.mucfile.XfileUtils;
import com.vkzwbim.chat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * Created by wzw on 2016/9/26.
 */
public class MyConsumeRecord extends BaseListActivity<MyConsumeRecord.MyConsumeHolder> {
    private static final String TAG = "MyConsumeRecord";
    List<ConsumeRecordItem.PageDataEntity> datas = new ArrayList<>();

    @Nullable
    @Override
    protected Integer getMiddleDivider() {
        return R.drawable.divider_consume_record;
    }

    @Override
    public void initView() {
        super.initView();
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getResources().getString(R.string.bill));
    }

    @Override
    public void initDatas(int pager) {
        if (pager == 0) {
            datas.clear();
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        // 如果是下拉刷新就重新加载第一页
        params.put("pageIndex", pager + "");
        params.put("pageSize", "30");
        HttpUtils.get().url(coreManager.getConfig().CONSUMERECORD_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<ConsumeRecordItem>(ConsumeRecordItem.class) {

                    @Override
                    public void onResponse(ObjectResult<ConsumeRecordItem> result) {
                        if (result.getData().getPageData() != null) {
                            for (ConsumeRecordItem.PageDataEntity data : result.getData().getPageData()) {
                                final double money = data.getMoney();
                                boolean isZero = Double.toString(money).equals("0.0");
                                Log.d(TAG, "bool : " + isZero + " \t" + money);
                                if (!isZero) {
                                    datas.add(data);
                                }
                            }
                            if (result.getData().getPageData().size() != 30) {
                                more = false;
                            } else {
                                more = true;
                            }
                        } else {
                            more = false;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                update(datas);
                            }
                        });
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(MyConsumeRecord.this);
                    }
                });
    }

    @Override
    public MyConsumeHolder initHolder(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.consumerecord_item, parent, false);
        MyConsumeHolder holder = new MyConsumeHolder(v);
        return holder;
    }

    @Override
    public void fillData(MyConsumeHolder holder, int position) {
        ConsumeRecordItem.PageDataEntity info = datas.get(position);

        long time = Long.valueOf(info.getTime());
        String StrTime = XfileUtils.fromatTime(time * 1000, "MM-dd HH:mm");
        holder.nameTv.setText(info.getDesc());
        holder.timeTv.setText(StrTime);

        switch (info.getType()) {
            case 2:
            case 4:
            case 7:
            case 10:
            case 14:
            case 12:
                holder.moneyTv.setTextColor(getResources().getColor(R.color.records_of_consumption));
                holder.moneyTv.setText("-" + XfileUtils.fromatFloat(info.getMoney()));
                break;
            case 1:
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 13:
            case 11:
                holder.moneyTv.setTextColor(getResources().getColor(R.color.ji_jian_lan));
                holder.moneyTv.setText("+" + XfileUtils.fromatFloat(info.getMoney()));
                break;
        }
    }

    public class MyConsumeHolder extends RecyclerView.ViewHolder {
        public TextView nameTv, timeTv, moneyTv;

        public MyConsumeHolder(View itemView) {
            super(itemView);
            nameTv = (TextView) itemView.findViewById(R.id.textview_name);
            timeTv = (TextView) itemView.findViewById(R.id.textview_time);
            moneyTv = (TextView) itemView.findViewById(R.id.textview_money);
        }
    }
}
