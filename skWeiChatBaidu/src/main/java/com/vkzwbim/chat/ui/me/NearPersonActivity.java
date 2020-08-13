package com.vkzwbim.chat.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vkzwbim.chat.R;
import com.vkzwbim.chat.ui.base.BaseActivity;
import com.vkzwbim.chat.ui.nearby.NearbyGridFragment;
import com.vkzwbim.chat.ui.nearby.NearbyMapFragment;
import com.vkzwbim.chat.ui.nearby.UserSearchActivity;
import com.vkzwbim.chat.util.MyFragmentManager;
import com.vkzwbim.chat.view.NearSeachDialog;
import com.vkzwbim.chat.view.TabView;

/**
 * 附近的人
 */

public class NearPersonActivity extends BaseActivity {
    private TabView tabView;
    private MyFragmentManager mMyFragmentManager;
    // 列表
    private NearbyGridFragment mGridFragment;
    // 地图
    private NearbyMapFragment mMapFragment;
    private NearSeachDialog nearSeachDialog;
    NearSeachDialog.OnNearSeachDialogClickListener onNearSeachDialogClickListener = new NearSeachDialog.OnNearSeachDialogClickListener() {

        @Override
        public void tv1Click() {
            mGridFragment.refreshData("");
            mMapFragment.refreshData("");
        }

        @Override
        public void tv2Click() {
            mGridFragment.refreshData("1");
            mMapFragment.refreshData("1");

        }

        @Override
        public void tv3Click() {
            mGridFragment.refreshData("0");
            mMapFragment.refreshData("0");
        }

        @Override
        public void tv4Click() {
            nearSeachDialog.dismiss();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardcast);

        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.near_person));
        ImageView ivRight = (ImageView) findViewById(R.id.iv_title_right_right);
        ivRight.setVisibility(View.GONE);
//        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ivRight.getLayoutParams();
//        lp.setMargins(0, DisplayUtil.dip2px(mContext, 2), DisplayUtil.dip2px(mContext, 55), 0);
        ivRight.setImageResource(R.mipmap.search_icon);
        ivRight.setOnClickListener((view) -> {
            Intent intent = new Intent(NearPersonActivity.this, UserSearchActivity.class);
            startActivity(intent);
        });

        ImageView iv_title_right = (ImageView) findViewById(R.id.iv_title_right);
        iv_title_right.setVisibility(View.VISIBLE);
        iv_title_right.setImageResource(R.mipmap.folding_icon);
        iv_title_right.setOnClickListener((view) -> {
            nearSeachDialog = new NearSeachDialog(NearPersonActivity.this, onNearSeachDialogClickListener);
            nearSeachDialog.show();
        });

        tabView = new TabView(this);
        tabView.getAttention_each_tv().setText(getString(R.string.near_person));
        tabView.getAttention_single_tv().setText(getString(R.string.map));
        ((LinearLayout) findViewById(R.id.ll_content)).addView(tabView.getView(), 0);

        mGridFragment = new NearbyGridFragment();
        mMapFragment = new NearbyMapFragment();
        mMyFragmentManager = new MyFragmentManager(this, R.id.fl_fragments);
        mMyFragmentManager.add(mGridFragment, mMapFragment);
        tabView.setOnTabSelectedLisenter(index -> mMyFragmentManager.show(index));
        tabView.callOnSelect(1);
    }

}
