package com.vkzwbim.chat.pay.new_ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.vkzwbim.chat.R;
import com.vkzwbim.chat.ui.base.BaseActivity;
import com.vkzwbim.chat.ui.me.redpacket.ChangePayPasswordActivity;
import com.vkzwbim.chat.ui.smarttab.SmartTabLayout;
import com.vkzwbim.chat.util.Constants;
import com.vkzwbim.chat.util.PreferenceUtils;
import com.vkzwbim.chat.util.ToastUtil;
import com.vkzwbim.chat.view.CustomScrollViewPager;

import java.util.ArrayList;
import java.util.List;

public class PaymentOrReceiptActivity extends BaseActivity {
    private SmartTabLayout smartTabLayout;
    private CustomScrollViewPager viewPager;
    private List<String> mTitleList;
    private Fragment mCurrentFragment;

    public static void start(Context ctx, String userId) {
        boolean hasPayPassword = PreferenceUtils.getBoolean(ctx, Constants.IS_PAY_PASSWORD_SET + userId, true);
        if (!hasPayPassword) {
            ToastUtil.showToast(ctx, R.string.tip_no_pay_password);
            Intent intent = new Intent(ctx, ChangePayPasswordActivity.class);
            ctx.startActivity(intent);
        } else {
            Intent intent = new Intent(ctx, PaymentOrReceiptActivity.class);
            ctx.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_new_ui);
        smartTabLayout = (SmartTabLayout) findViewById(R.id.muc_smarttablayout_redpacket);
        viewPager = (CustomScrollViewPager) findViewById(R.id.muc_viewpagert_redpacket);
        mTitleList = new ArrayList<String>();
        mTitleList.add(getString(R.string.payment_pay));
        mTitleList.add(getString(R.string.receipt));
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new PaymentFragment());
        fragments.add(new ReceiptFragment());
        MyTabAdapter adapter = new MyTabAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        notifyImage(0);
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                notifyImage(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
        smartTabLayout.setViewPager(viewPager);

        findViewById(R.id.payment_code).setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        findViewById(R.id.collection_code).setOnClickListener(v -> viewPager.setCurrentItem(1, true));
        findViewById(R.id.sweep_code).setOnClickListener(v -> viewPager.setCurrentItem(2, true));
    }

    private void notifyImage(int index) {
        if (index == 0) {
            findViewById(R.id.rl_pay).setBackground(getResources().getDrawable(R.mipmap.biue_background));
            findViewById(R.id.payment_code).setBackgroundResource(R.mipmap.payment_code_select_icon);
            findViewById(R.id.collection_code).setBackgroundResource(R.mipmap.collection_code_icon);
            findViewById(R.id.sweep_code).setBackgroundResource(R.mipmap.sweep_code_icon);
        } else if (index == 1) {
            findViewById(R.id.rl_pay).setBackground(getResources().getDrawable(R.mipmap.yellow_background));
            findViewById(R.id.payment_code).setBackgroundResource(R.mipmap.payment_code_icon);
            findViewById(R.id.collection_code).setBackgroundResource(R.mipmap.collection_code_selecet_icon);
            findViewById(R.id.sweep_code).setBackgroundResource(R.mipmap.sweep_code_icon);
        } else if (index == 2) {
            findViewById(R.id.payment_code).setBackgroundResource(R.mipmap.payment_code_icon);
            findViewById(R.id.collection_code).setBackgroundResource(R.mipmap.collection_code_icon);
            findViewById(R.id.sweep_code).setBackgroundResource(R.mipmap.sweep_code_select_icon);
        }
    }

    class MyTabAdapter extends FragmentPagerAdapter {
        private List<Fragment> mFragments;

        MyTabAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            mFragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            if (mFragments != null) {
                return mFragments.size();
            }
            return 0;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);
        }

        //用于区分具体属于哪个fragment
        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            mCurrentFragment = (Fragment) object;
            super.setPrimaryItem(container, position, object);
        }

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }
    }
}