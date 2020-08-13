package com.vkzwbim.chat.ui.nearby;

import android.os.Bundle;

import com.vkzwbim.chat.R;
import com.vkzwbim.chat.fragment.DiscoverFragment;
import com.vkzwbim.chat.ui.base.BaseActivity;

public class FriendCircleActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendcircle);
        getSupportActionBar().hide();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container,new DiscoverFragment())   // 此处的R.id.fragment_container是要盛放fragment的父容器
                .commit();
    }
}
