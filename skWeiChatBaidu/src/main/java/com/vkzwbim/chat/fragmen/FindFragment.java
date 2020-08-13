package com.vkzwbim.chat.fragmen;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cjt2325.cameralibrary.util.LogUtil;
import com.makeramen.roundedimageview.RoundedImageView;
import com.vkzwbim.chat.bean.AdvEntivity;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.ui.MainActivity;
import com.vkzwbim.chat.ui.base.EasyFragment;
import com.vkzwbim.chat.ui.nearby.FriendCircleActivity;
import com.vkzwbim.chat.ui.tool.WebViewActivity;
import com.vkzwbim.chat.util.GlideImageUtils;
import com.vkzwbim.chat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;


public class FindFragment extends EasyFragment {
    private RelativeLayout rl_find_circle;
    private RelativeLayout rl_find_scans;
    private TextView mTvTitle;
    private ImageView mIvTitleRight;
    private RecyclerView rv_adv;
    List<AdvEntivity.DataBean> advList;
    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_find;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        initActionBar();
        initView();
    }

    private void initActionBar() {
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(getString(R.string.find));
        mTvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        mIvTitleRight = findViewById(R.id.iv_title_right);
        mIvTitleRight.setVisibility(View.GONE);

    }

    public void initView() {
        rv_adv = findViewById(R.id.rv_adv);
        rl_find_circle = findViewById(R.id.rl_frinend_circle);
        rl_find_scans = findViewById(R.id.rl_frinend_scan);

        rl_find_circle.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FriendCircleActivity.class);
            startActivity(intent);
        });

        rl_find_scans.setOnClickListener(v -> MainActivity.requestQrCodeScan(getActivity()));
    }


    @Override
    public void onResume() {
        super.onResume();
        getData();
    }



    public void getData() {
        Map<String, String> params = new HashMap<>();
        params.put("userId", coreManager.getSelf().getUserId());
        HttpUtils.get().url(coreManager.getConfig().NEARBY_FIND)
                .params(params)
                .build(true, true)
                .execute(new ListCallback<AdvEntivity.DataBean>(AdvEntivity.DataBean.class) {

                    @Override
                    public void onResponse(ArrayResult<AdvEntivity.DataBean> result) {

                            advList = result.getData();
                            if (advList != null && advList.size() > 0) {
                                rv_adv.setVisibility(View.VISIBLE);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                                rv_adv.setLayoutManager(linearLayoutManager);
                                rv_adv.setAdapter(new AdvAdapter(advList));
                            } else {
                                rv_adv.setVisibility(View.GONE);
                            }
                        }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });

    }


    class AdvAdapter extends RecyclerView.Adapter<ViewHolder> {

        public List<AdvEntivity.DataBean> list;

        public AdvAdapter(List<AdvEntivity.DataBean> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.activity_text, viewGroup, false);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {


            AdvEntivity.DataBean dataBean = list.get(i);
            GlideImageUtils.setImageView(getContext(), dataBean.getImg(), viewHolder.img_adv);
            viewHolder.tv_adv.setText(dataBean.getTitle());
            viewHolder.rl_adv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String REGEX_URL = "((((ht|f)tp(s?))\\:\\/\\/)([\\w\\-]+)(\\.[\\w\\-]+)+|([\\w\\-]+\\.)+(com|cn|cc|top|xyz|edu|gov|mil|net|org|biz|info|name|museum|us|ca|uk))(\\:\\d+)?(\\/([\\w_\\-\\.~!*\\'()\\;\\:@&=+&$,/?#%]*))*";
                    Pattern pattern = Pattern.compile(REGEX_URL);
                    Matcher m = pattern.matcher(dataBean.getUrl());
                    if (!m.matches()) {
                        ToastUtil.showToast(getActivity(), "无效的链接");
                        return;
                    } else {

                        Intent intent = new Intent(getActivity(), WebViewActivity.class);
                        intent.putExtra(WebViewActivity.EXTRA_URL, dataBean.getUrl());
                        getActivity().startActivity(intent);
                    }

                }
            });


        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout rl_adv;
        private TextView tv_adv;
        private RoundedImageView img_adv;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rl_adv = itemView.findViewById(R.id.rl_adv);
            tv_adv = itemView.findViewById(R.id.tv_adv);
            img_adv = itemView.findViewById(R.id.img_adv);
        }
    }


}





