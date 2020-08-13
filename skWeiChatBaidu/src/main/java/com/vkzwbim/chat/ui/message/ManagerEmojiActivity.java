package com.vkzwbim.chat.ui.message;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cjt2325.cameralibrary.util.LogUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;
import com.vkzwbim.chat.AppConstant;
import com.vkzwbim.chat.MyApplication;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.bean.UploadFileResult;
import com.vkzwbim.chat.bean.collection.Collectiion;
import com.vkzwbim.chat.broadcast.OtherBroadcast;
import com.vkzwbim.chat.helper.DialogHelper;
import com.vkzwbim.chat.helper.LoginHelper;
import com.vkzwbim.chat.helper.UploadService;
import com.vkzwbim.chat.ui.account.LoginHistoryActivity;
import com.vkzwbim.chat.ui.base.BaseActivity;
import com.vkzwbim.chat.ui.tool.SingleImagePreviewActivity;
import com.vkzwbim.chat.util.GlideImageUtils;
import com.vkzwbim.chat.util.ToastUtil;
import com.vkzwbim.chat.view.photopicker.PhotoPickerActivity;
import com.vkzwbim.chat.view.photopicker.SelectModel;
import com.vkzwbim.chat.view.photopicker.intent.PhotoPickerIntent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * Created by Administrator on 2018/3/9 0009.
 */

public class ManagerEmojiActivity extends BaseActivity {
    boolean isEditOrSee;
    private RecyclerView rcyv;
    private MyAdapter mAdapter;
    private List<Collectiion> mData;
    private List<String> mReadyData;
    private TextView tv1, tv2, tv3;
    private ArrayList<String> mPhotoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_emoji);
        mData = (List<Collectiion>) getIntent().getSerializableExtra("list");
        LogUtil.e("cjh 表情 "+mData.size());
        mReadyData = new ArrayList<>();

        initActionBar();
        initView();
        initEvent();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mPhotoList=new ArrayList<>();
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.title_my_collection_emoji);
        final TextView tvTitleRight = (TextView) findViewById(R.id.tv_title_right);
        tvTitleRight.setText(R.string.edit);
        tvTitleRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditOrSee = !isEditOrSee;
                if (isEditOrSee) {
                    findViewById(R.id.rl_rl).setVisibility(View.VISIBLE);
                    tvTitleRight.setText(R.string.cancel);
                } else {
                    findViewById(R.id.rl_rl).setVisibility(View.GONE);
                    tvTitleRight.setText(R.string.edit);
                    updateData(false);
                }
            }
        });
    }

    private void initView() {
        tv1 = (TextView) findViewById(R.id.al_tv);
        tv2 = (TextView) findViewById(R.id.sl_tv);
        tv3 = (TextView) findViewById(R.id.dl_tv);

        mAdapter = new MyAdapter();
        rcyv = (RecyclerView) findViewById(R.id.emoji_recycle);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 4);
        rcyv.setLayoutManager(layoutManager);
        rcyv.setAdapter(mAdapter);
        mAdapter.setmOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Collectiion C = mData.get(position);
                if(C.getType()==7){
                    selectPhoto();
                }else if (!isEditOrSee) {
                    Intent intent = new Intent(mContext, SingleImagePreviewActivity.class);
                    intent.putExtra(AppConstant.EXTRA_IMAGE_URI, C.getUrl());
                    mContext.startActivity(intent);
                }
                else {
                    updateSingleData(C, position);
                }
            }
        });
    }

    private void initEvent() {
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData(true);
            }
        });

        tv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.showDefaulteMessageProgressDialog(ManagerEmojiActivity.this);
                String idList = "";
                for (int i = 0; i < mReadyData.size(); i++) {
                    if (i == mReadyData.size() - 1) {
                        idList += mReadyData.get(i);
                    } else {
                        idList += mReadyData.get(i) + ",";
                    }
                }
                deleteMyCollection(idList);
            }
        });
    }

    // 更新单个item
    public void updateSingleData(Collectiion C, int position) {
        if (C.getType() == 8) {
            C.setType(0);
            mReadyData.remove(C.getEmojiId());
        } else if (C.getType()!=7){
            C.setType(8);
            mReadyData.add(C.getEmojiId());
        }
        LogUtil.e("cjh pic  "+position);
        mData.remove(position);
        mData.add(position, C);
        mAdapter.notifyItemChanged(position);
        updateUI();
    }

    // 更新所有item 全选 || 取消
    public void updateData(boolean isAllSelectOrCancel) {
        mReadyData.clear();
        for (int i = 0; i < mData.size(); i++) {
            Collectiion C = mData.get(i);
            if (C.getType() != 7) {
                if (isAllSelectOrCancel) {
                    C.setType(8);
                    mReadyData.add(mData.get(i).getEmojiId());
                } else {
                    C.setType(0);
                }
                mData.remove(i);
                mData.add(i, C);
            }
        }
            mAdapter.notifyDataSetChanged();
            updateUI();

    }

    // 更新底部ui
    public void updateUI() {
        if (mReadyData != null) {
            if (mReadyData.size() > 0) {
                tv3.setVisibility(View.VISIBLE);
                tv2.setText("选中表情 (" + mReadyData.size() + ")");
            } else {
                tv3.setVisibility(View.GONE);
                tv2.setText("选中表情 (" + 0 + ")");
            }
        }
    }

    public void deleteMyCollection(String emojiId) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("emojiId", emojiId);

        HttpUtils.get().url(coreManager.getConfig().Collection_REMOVE)
                .params(params)
                .build()
                .execute(new BaseCallback<Collectiion>(Collectiion.class) {

                    @Override
                    public void onResponse(ObjectResult<Collectiion> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            Toast.makeText(mContext, mContext.getString(R.string.delete_all_succ), Toast.LENGTH_SHORT).show();
                            for (int i = 0; i < mReadyData.size(); i++) {
                                for (int i1 = 0; i1 < mData.size(); i1++) {
                                    if (mReadyData.get(i).equals(mData.get(i1).getEmojiId())) {
                                        mData.remove(i1);
                                    }
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                            mReadyData.clear();
                            updateUI();
                            ManagerEmojiActivity.this.sendBroadcast(new Intent(com.vkzwbim.chat.broadcast.OtherBroadcast.CollectionRefresh));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private OnItemClickListener onItemClickListener;

        public void setmOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collection_ma_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Collectiion collection = mData.get(position);
            if (collection.getType() == 7) {
                GlideImageUtils.setImageDrawable(ManagerEmojiActivity.this, R.drawable.addpic, holder.iv);

            } else if (collection.getUrl().endsWith(".gif")) {
                holder.iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                Glide.with(ManagerEmojiActivity.this)
                        .load(collection.getUrl())
                        .asGif()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(holder.iv);
            } else if (collection.getUrl().endsWith("jpg") || collection.getUrl().endsWith("png")) {
                holder.iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(ManagerEmojiActivity.this)
                        .load(collection.getUrl())
                        .placeholder(R.drawable.ffb)
                        .error(R.drawable.fez)
                        .dontAnimate()
                        .into(holder.iv);
            }

            if (collection.getType() == 8) {
                holder.ck.setVisibility(View.VISIBLE);
                holder.iv.setAlpha(0.4f);
            } else {
                holder.ck.setVisibility(View.GONE);
                holder.iv.setAlpha(1.0f);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int position = holder.getLayoutPosition();
                        onItemClickListener.onItemClick(holder.itemView, position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView iv;
            CheckBox ck;

            public ViewHolder(View itemView) {
                super(itemView);
                ck = (CheckBox) itemView.findViewById(R.id.cl_ck);
                iv = (ImageView) itemView.findViewById(R.id.cl_iv);
            }
        }
    }


    private static final int REQUEST_CODE_PICK_PHOTO = 2;     // 图库

    /**
     * 相册
     * 可以多选的图片选择器
     */
    private void selectPhoto() {
        ArrayList<String> imagePaths = new ArrayList<>();
        PhotoPickerIntent intent = new PhotoPickerIntent(ManagerEmojiActivity.this);
        intent.setSelectModel(SelectModel.MULTI);
        // 是否显示拍照， 默认false
        intent.setShowCarema(false);
        // 最多选择照片数量，默认为9
        intent.setMaxTotal(9 - mPhotoList.size());
        // 已选中的照片地址， 用于回显选中状态
        intent.setSelectedPaths(imagePaths);
        // intent.setImageConfig(config);
        startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_PHOTO) {
            // 选择图片返回
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    boolean isOriginal = data.getBooleanExtra(PhotoPickerActivity.EXTRA_RESULT_ORIGINAL, false);
                    album(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT), isOriginal);
                    new UploadPhoto().execute();
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        }
    }

    // 多张图片压缩 相册
    private void album(ArrayList<String> stringArrayListExtra, boolean isOriginal) {
        LogUtil.e("cjh album"+stringArrayListExtra.size()+"  "+isOriginal);
        if (isOriginal) {// 原图发送，不压缩
            for (int i = 0; i < stringArrayListExtra.size(); i++) {
                mPhotoList.add(stringArrayListExtra.get(i));
            }

            return;
        }

        List<String> list = new ArrayList<>();
        for (int i = 0; i < stringArrayListExtra.size(); i++) {
            // Luban只处理特定后缀的图片，不满足的不处理也不走回调，
            // 只能挑出来不压缩，
            // todo luban支持压缩.gif图，但是压缩之后的.gif图用glide加载与转换为gifDrawable都会出问题，所以,gif图不压缩了
            List<String> lubanSupportFormatList = Arrays.asList("jpg", "jpeg", "png", "webp");
            boolean support = false;
            for (int j = 0; j < lubanSupportFormatList.size(); j++) {
                if (stringArrayListExtra.get(i).endsWith(lubanSupportFormatList.get(j))) {
                    support = true;
                    break;
                }
            }
            if (!support) {
                list.add(stringArrayListExtra.get(i));
            }
        }

        if (list.size() > 0) {
            for (String s : list) {// 不压缩的部分，直接发送
                mPhotoList.add(s);

            }
        }

        // 移除掉不压缩的图片
        stringArrayListExtra.removeAll(mPhotoList);

        Luban.with(this)
                .load(stringArrayListExtra)
                .ignoreBy(100)// 原图小于100kb 不压缩
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(File file) {
                        mPhotoList.add(file.getPath());

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }).launch();// 启动压缩
    }


    private String mImageData;

    private class UploadPhoto extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DialogHelper.showDefaulteMessageProgressDialog(ManagerEmojiActivity.this);
        }

        /**
         * 上传的结果： <br/>
         * return 1 Token过期，请重新登陆 <br/>
         * return 2 上传出错<br/>
         * return 3 上传成功<br/>
         */
        @Override
        protected Integer doInBackground(Void... params) {
            if (!LoginHelper.isTokenValidation()) {
                return 1;
            }
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("access_token", coreManager.getSelfStatus().accessToken);
            mapParams.put("userId", coreManager.getSelf().getUserId() + "");
            mapParams.put("validTime", "-1");// 文件有效期

            String result = new UploadService().uploadFile(coreManager.getConfig().UPLOAD_URL, mapParams, mPhotoList);

            if (TextUtils.isEmpty(result)) {
                return 2;
            }

            UploadFileResult recordResult = JSON.parseObject(result, UploadFileResult.class);
            boolean success = Result.defaultParser(ManagerEmojiActivity.this, recordResult, true);
            if (success) {
                if (recordResult.getSuccess() != recordResult.getTotal()) {
                    // 上传丢失了某些文件
                    return 2;
                }
                if (recordResult.getData() != null) {
                    UploadFileResult.Data data = recordResult.getData();
                    if (data.getImages() != null && data.getImages().size() > 0) {
                        mImageData = JSON.toJSONString(data.getImages(), UploadFileResult.sImagesFilter);
                    } else {
                        return 2;
                    }
                    return 3;
                } else {
                    // 没有文件数据源，失败
                    return 2;
                }
            } else {
                return 2;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == 1) {
                DialogHelper.dismissProgressDialog();
                startActivity(new Intent(ManagerEmojiActivity.this, LoginHistoryActivity.class));
            } else if (result == 2) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(ManagerEmojiActivity.this, getString(R.string.upload_failed));
            } else {
                mPhotoList.clear();
                addPic(mImageData);
            }
        }
    }

    private void addPic(String picUrl) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("imgs", picUrl);
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().BATADDCUSTOMIZE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        LogUtil.e("cjh接口 " + result.getResultCode());
                        if (result.getResultCode() == 1) {

                            try {
                                JSONArray array = new JSONArray(mImageData);
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject jsonObject = new JSONObject(array.get(i).toString());
                                    String orl = jsonObject.getString("oUrl");
                                    Collectiion collectiion = new Collectiion();
                                    collectiion.setUrl(orl);
                                    mData.add(collectiion);
                                }
                                mAdapter.notifyDataSetChanged();
                                MyApplication.getInstance().sendBroadcast(new Intent(OtherBroadcast.CollectionRefresh));
                            } catch (Exception e) {

                            }


                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }


}
