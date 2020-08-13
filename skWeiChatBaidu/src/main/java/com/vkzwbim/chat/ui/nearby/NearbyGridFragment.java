package com.vkzwbim.chat.ui.nearby;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.vkzwbim.chat.AppConstant;
import com.vkzwbim.chat.MyApplication;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.bean.User;
import com.vkzwbim.chat.helper.AvatarHelper;
import com.vkzwbim.chat.ui.base.BaseNearGridFragment;
import com.vkzwbim.chat.ui.other.BasicInfoActivity;
import com.vkzwbim.chat.util.DisplayUtil;
import com.vkzwbim.chat.util.ScreenUtil;
import com.vkzwbim.chat.util.TimeUtils;
import com.vkzwbim.chat.util.ToastUtil;
import com.vkzwbim.chat.view.CircleImageView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * 附近的人-列表模式
 */
public class NearbyGridFragment extends BaseNearGridFragment<NearbyGridFragment.NearbyGridHolder> {
    double latitude;
    double longitude;
    private List<User> mUsers = new ArrayList<>();
    private boolean isPullDwonToRefersh;
    private String mSex;

    @Override
    public void initDatas(int pager) {
        if (pager == 0) {
            isPullDwonToRefersh = true;
        } else {
            isPullDwonToRefersh = false;
        }

        latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
        longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();

        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageIndex", String.valueOf(pager));
        params.put("pageSize", "20");
        params.put("latitude", String.valueOf(latitude));
        params.put("longitude", String.valueOf(longitude));
        //    String sex = getActivity().getIntent().getStringExtra("sex");
        if (!TextUtils.isEmpty(mSex)) {
            params.put("sex", mSex);
        }
        requestData(params);
    }

    private void requestData(HashMap<String, String> params) {
        HttpUtils.get().url(coreManager.getConfig().NEARBY_USER)
                .params(params)
                .build()
                .execute(new ListCallback<User>(User.class) {
                    @Override
                    public void onResponse(ArrayResult<User> result) {
                        if (isPullDwonToRefersh) {
                            mUsers.clear();
                        }

                        List<User> data = result.getData();
                        if (data != null && data.size() > 0) {
                            mUsers.addAll(data);
                        }

                        if (mUsers.size() > 0) {
                            update(mUsers);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }

    public void refreshData(String sex) {
        mSex = sex;
        initDatas(0);
    }

    @Override
    public NearbyGridHolder initHolder(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.item_nearby_grid, parent, false);
        return new NearbyGridHolder(v);
    }

    @Override
    public void fillData(NearbyGridHolder holder, int position) {
        if (mUsers != null && mUsers.size() > 0) {
            User data = mUsers.get(position);
            AvatarHelper.getInstance().displayRoundAvatar(data.getNickName(), data.getUserId(), holder.ivBgImg, false);
            holder.tvName.setText(data.getNickName());
            AvatarHelper.getInstance().displayAvatar(data.getNickName(), data.getUserId(), holder.ivHead, true);
            String distance = DisplayUtil.getDistance(latitude, longitude, data);
            holder.tvDistance.setText(distance);
            holder.tvTime.setText(TimeUtils.skNearbyTimeString(data.getCreateTime()));
        }
    }

    public void onItemClick(int position) {
        String userId = mUsers.get(position).getUserId();
        Intent intent = new Intent(getActivity(), BasicInfoActivity.class);
        intent.putExtra(AppConstant.EXTRA_USER_ID, userId);
        startActivity(intent);
    }

    class NearbyGridHolder extends RecyclerView.ViewHolder {
        LinearLayout rootView;
        RoundedImageView ivBgImg;
        TextView tvName;
        CircleImageView ivHead;
        TextView tvDistance;
        TextView tvTime;

        NearbyGridHolder(View itemView) {
            super(itemView);
            rootView = (LinearLayout) itemView.findViewById(R.id.ll_nearby_grid_root);
            ivBgImg = (RoundedImageView) itemView.findViewById(R.id.iv_nearby_img);
            ivBgImg.setCornerRadius(ScreenUtil.dip2px(requireContext(), 7), ScreenUtil.dip2px(requireContext(), 7), 0, 0);
            tvName = (TextView) itemView.findViewById(R.id.tv_nearby_name);
            ivHead = (CircleImageView) itemView.findViewById(R.id.iv_nearby_head);
            tvDistance = (TextView) itemView.findViewById(R.id.tv_nearby_distance);
            tvTime = (TextView) itemView.findViewById(R.id.tv_nearby_time);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(getLayoutPosition());
                }
            });
        }
    }
}
