package com.vkzwbim.chat.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.bean.redpacket.OpenRedpacket;
import com.vkzwbim.chat.bean.redpacket.RedListItemRecive;
import com.vkzwbim.chat.bean.redpacket.RedListItemSend;
import com.vkzwbim.chat.ui.base.BaseActivity;
import com.vkzwbim.chat.ui.base.CoreManager;
import com.vkzwbim.chat.ui.smarttab.SmartTabLayout;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * 红包记录界面Activity
 * Created by 魏正旺 on 2016/9/23.
 */
public class RedListActivity extends BaseActivity implements View.OnClickListener, PullToRefreshBase.OnRefreshListener2 {
    // 数据格式，保留两位小数
    DecimalFormat df;
    // 显示时间的格式
    SimpleDateFormat sdf;
    private SmartTabLayout smartTabLayout;
    private ViewPager redlistPager;
    private List<View> views;
    private List<String> mTitleList;
    private LayoutInflater inflater;

    private ListView pullToRefreshListViewRedrecive;
    private ListView pullToRefreshListViewRedsend;
    // 数据集合
    private List<RedListItemSend> sendRedList;
    private List<RedListItemRecive> reciveRedList;
    // 表示发出的还是接受的红包list  0代表收到  1代表发出
    private int adapterType;
    // 两个listview数据加载的页数
    private int pageIndexSend, getPageIndexRecive;
    private RedListItemAdapter redListItemAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.redpacket_list);
        inflater = LayoutInflater.from(this);
        df = new DecimalFormat("######0.00");
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        initView();
        initData();
    }

    private void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.tv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.red_packet_history));

        mTitleList = new ArrayList<String>();
        views = new ArrayList<View>();

        sendRedList = new ArrayList<RedListItemSend>();
        reciveRedList = new ArrayList<RedListItemRecive>();
        mTitleList.add(getString(R.string.packets_received));
        mTitleList.add(getString(R.string.envelopes_issued));

        views.add(inflater.inflate(R.layout.redpacket_recivelist, null));
        views.add(inflater.inflate(R.layout.redpacket_sendlist, null));

        redlistPager = (ViewPager) findViewById(R.id.viewpagert_redlist);
        smartTabLayout = (SmartTabLayout) findViewById(R.id.smarttablayout_redlist);
        pullToRefreshListViewRedrecive =
                (ListView) views.get(0).findViewById(R.id.pull_refresh_list_redrecive);
        pullToRefreshListViewRedsend =
                (ListView) views.get(1).findViewById(R.id.pull_refresh_list_redsend);
        redlistPager.setAdapter(new PagerAdapter());
        //把一个viewA 作为ListViewA和ListViewB的headerView|FooterView，当在ListViewB中操作viewA时，就会报parameter must be a descendant of this view这个错误，解决方案是：
        //为每一个ListView单独添加只属于本身的View
        smartTabLayout.setViewPager(redlistPager);

        // 设置Adapter
        redListItemAdapter = new RedListItemAdapter();
        pullToRefreshListViewRedsend.setAdapter(redListItemAdapter);
        pullToRefreshListViewRedrecive.setAdapter(redListItemAdapter);




        pullToRefreshListViewRedsend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String token = CoreManager.requireSelfStatus(mContext).accessToken;
                final String mToUserId = sendRedList.get(position).getUserId();


                HashMap<String, String> params = new HashMap<>();
                params.put("access_token", token);
                params.put("id", sendRedList.get(position).getId());

                HttpUtils.get().url(CoreManager.requireConfig(mContext).RENDPACKET_GET)
                        .params(params)
                        .build()
                        .execute(new BaseCallback<OpenRedpacket>(OpenRedpacket.class) {

                            @Override
                            public void onResponse(ObjectResult<OpenRedpacket> result) {
                                if (result.getData() != null) {
                                    // 当resultCode==1时，表示可领取
                                    // 当resultCode==0时，表示红包已过期、红包已退回、红包已领完
                                    int resultCode = result.getResultCode();
                                    OpenRedpacket openRedpacket = result.getData();
                                    Bundle bundle = new Bundle();
                                    Intent intent = new Intent(mContext, RedDetailsActivity.class);
                                    bundle.putSerializable("openRedpacket", openRedpacket);
                                    bundle.putInt("redAction", 0);
                                    if (!TextUtils.isEmpty(result.getResultMsg())) //resultMsg不为空表示红包已过期
                                    {
                                        bundle.putInt("timeOut", 1);
                                    } else {
                                        bundle.putInt("timeOut", 0);
                                    }
                                    bundle.putBoolean("isGroup", true);
                                    bundle.putString("mToUserId", mToUserId);
                                    intent.putExtras(bundle);

                                        mContext.startActivity(intent);

                                } else {
                                    Toast.makeText(mContext, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onError(Call call, Exception e) {
                            }
                        });
            }
        });
        pullToRefreshListViewRedrecive.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String token = CoreManager.requireSelfStatus(mContext).accessToken;
                final String mToUserId = reciveRedList.get(position).getUserId();


                HashMap<String, String> params = new HashMap<>();
                params.put("access_token", token);
                params.put("id", reciveRedList.get(position).getRedId());

                HttpUtils.get().url(CoreManager.requireConfig(mContext).RENDPACKET_GET)
                        .params(params)
                        .build()
                        .execute(new BaseCallback<OpenRedpacket>(OpenRedpacket.class) {

                            @Override
                            public void onResponse(ObjectResult<OpenRedpacket> result) {
                                if (result.getData() != null) {
                                    // 当resultCode==1时，表示可领取
                                    // 当resultCode==0时，表示红包已过期、红包已退回、红包已领完
                                    int resultCode = result.getResultCode();
                                    OpenRedpacket openRedpacket = result.getData();
                                    Bundle bundle = new Bundle();
                                    Intent intent = new Intent(mContext, RedDetailsActivity.class);
                                    bundle.putSerializable("openRedpacket", openRedpacket);
                                    bundle.putInt("redAction", 0);
                                    if (!TextUtils.isEmpty(result.getResultMsg())) //resultMsg不为空表示红包已过期
                                    {
                                        bundle.putInt("timeOut", 1);
                                    } else {
                                        bundle.putInt("timeOut", 0);
                                    }
                                    bundle.putBoolean("isGroup", true);
                                    bundle.putString("mToUserId", mToUserId);
                                    intent.putExtras(bundle);

                                        mContext.startActivity(intent);

                                } else {
                                    Toast.makeText(mContext, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onError(Call call, Exception e) {
                            }
                        });
            }
        });


        redlistPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // 切换listview的时候。Adapter类型也要换
                adapterType = position;
                redListItemAdapter.notifyDataSetChanged(); // 切换Tab的时候要重新适配一下
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        /**
         * 为了实现点击Tab栏切换的时候不出现动画
         * 为每个Tab重新设置点击事件
         */
        for (int i = 0; i < mTitleList.size(); i++) {
            View view = smartTabLayout.getTabAt(i);
            view.setTag(i + "");
            view.setOnClickListener(this);
        }
    }

    private void initData() {
        loadReciveRed();
        loadSendRed();
    }

    private void loadSendRed() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageIndex", pageIndexSend + "");
        params.put("pageSize", 25 + "");

        HttpUtils.get().url(coreManager.getConfig().SEND_REDPACKET_LIST_GET)
                .params(params)
                .build()
                .execute(new ListCallback<RedListItemSend>(RedListItemSend.class) {
                    @Override
                    public void onResponse(ArrayResult<RedListItemSend> result) {
                        if (result.getData().size() > 0) {
                            for (RedListItemSend redListItemSend : result.getData()) {
                                sendRedList.add(redListItemSend);
                            }
                            redListItemAdapter.notifyDataSetChanged();
                            pageIndexSend++;
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    private void loadReciveRed() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageIndex", getPageIndexRecive + "");
        params.put("pageSize", 25 + "");

        HttpUtils.get().url(coreManager.getConfig().RECIVE_REDPACKET_LIST_GET)
                .params(params)
                .build()
                .execute(new ListCallback<RedListItemRecive>(RedListItemRecive.class) {
                    @Override
                    public void onResponse(ArrayResult<RedListItemRecive> result) {
                        if (result.getData().size() > 0) {
                            for (RedListItemRecive redListItemRecive : result.getData()) {
                                reciveRedList.add(redListItemRecive);
                            }
                            redListItemAdapter.notifyDataSetChanged();
                            getPageIndexRecive++;
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        if (refreshView.getId() == R.id.pull_refresh_list_redrecive) {
            loadReciveRed();
        } else if (refreshView.getId() == R.id.pull_refresh_list_redsend) {
            loadSendRed();
        }
    }

    @Override
    public void onClick(View v) {
        // 根据Tab按钮传递的Tag来判断是那个页面，设置到相应的界面并且去掉动画
        int index = Integer.parseInt(v.getTag().toString());
        redlistPager.setCurrentItem(index, false);
    }

    private class PagerAdapter extends android.support.v4.view.PagerAdapter {

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


        @Override
        public Object instantiateItem(View container, int position) {
            ((ViewGroup) container).addView(views.get(position));

            return views.get(position);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);
        }
    }

    /**
     * listview Adapter
     */
    public class RedListItemAdapter extends BaseAdapter {
        View view;

        @Override
        public int getCount() {
            if (adapterType == 0) {
                return reciveRedList.size();
            } else {
                return sendRedList.size();
            }
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RedViewHolder redViewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.red_item, null);
                redViewHolder = new RedViewHolder();

                redViewHolder.userNameTv = (TextView) convertView.findViewById(R.id.username_tv);
                redViewHolder.moneyTv = (TextView) convertView.findViewById(R.id.money_tv);
                redViewHolder.timeTv = (TextView) convertView.findViewById(R.id.time_tv);
                convertView.setTag(redViewHolder);
            } else {
                redViewHolder = (RedViewHolder) convertView.getTag();
            }

            if (adapterType == 0) {
                long lcc_time = Long.valueOf(reciveRedList.get(position).getTime());
                String StrTime = sdf.format(new Date(lcc_time * 1000L));
                redViewHolder.userNameTv.setText(reciveRedList.get(position).getSendName());
                redViewHolder.timeTv.setText(StrTime);
                redViewHolder.moneyTv.setText(df.format(reciveRedList.get(position).getMoney()) + getString(R.string.yuan));
            } else if (adapterType == 1) {
                switch (sendRedList.get(position).getType()) {
                    case 1: {
                        redViewHolder.userNameTv.setText(getString(R.string.Usual_Gift));
                    }
                    break;

                    case 2: {
                        redViewHolder.userNameTv.setText(getString(R.string.red_envelope));
                    }
                    break;

                    case 3: {
                        redViewHolder.userNameTv.setText(getString(R.string.chat_kl_red));
                    }
                    break;
                }
                long lcc_time = Long.valueOf(sendRedList.get(position).getSendTime());
                String StrTime = sdf.format(new Date(lcc_time * 1000L));
                redViewHolder.timeTv.setText(StrTime);
                redViewHolder.moneyTv.setText(df.format(sendRedList.get(position).getMoney()) + getString(R.string.yuan));
            }
            return convertView;
        }

        public class RedViewHolder {
            public TextView userNameTv;
            public TextView timeTv;
            public TextView moneyTv;
        }
    }
}
