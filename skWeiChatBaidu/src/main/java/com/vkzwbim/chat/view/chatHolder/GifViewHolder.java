package com.vkzwbim.chat.view.chatHolder;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cjt2325.cameralibrary.util.LogUtil;
import com.vkzwbim.chat.R;
import com.vkzwbim.chat.bean.message.ChatMessage;
import com.vkzwbim.chat.db.dao.ChatMessageDao;
import com.vkzwbim.chat.util.DisplayUtil;
import com.vkzwbim.chat.util.SmileyParser;

import de.greenrobot.event.EventBus;
import fm.jiecao.jcvideoplayer_lib.MessageStateEvent;
import pl.droidsonroids.gif.GifImageView;

class GifViewHolder extends AChatHolderInterface {


    GifImageView mGifView;
    GifImageView chat_gif_dice;
    GifImageView chat_gif_jsb;
    RelativeLayout rl_dice;
    RelativeLayout rl_jsb;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_gif : R.layout.chat_to_item_gif;
    }

    @Override
    public void initView(View view) {
        mGifView = view.findViewById(R.id.chat_gif_view);
        chat_gif_dice = view.findViewById(R.id.chat_gif_dice);
        chat_gif_jsb = view.findViewById(R.id.chat_gif_jsb);
        rl_dice = view.findViewById(R.id.rl_dice);
        rl_jsb = view.findViewById(R.id.rl_jsb);


    }

    @Override
    public void fillData(ChatMessage message) {
        String gifName = message.getContent();
        LogUtil.e("cjh消息更新 dice " + gifName + "  " + message.getIsDiceSelect());

        if (gifName.contains("quyang-dice")) {
            mRootView = chat_gif_dice;
            rl_dice.setVisibility(View.VISIBLE);
            mGifView.setVisibility(View.GONE);
            rl_jsb.setVisibility(View.GONE);
            if (message.getIsDiceSelect() == 0) {
                start(chat_gif_dice, 1, message);
            } else {
                if (message.getContent().contains("卍")) {
                    String diceIndex = message.getContent().split("卍")[1];
                    chat_gif_dice.setImageDrawable(mContext.getResources().getDrawable(mContext.getResources().getIdentifier("dice_" + diceIndex, "drawable", mContext.getPackageName())));
                }
            }
        } else if (gifName.contains("quyang-jsb")) {
            mRootView = chat_gif_jsb;
            rl_jsb.setVisibility(View.VISIBLE);
            rl_dice.setVisibility(View.GONE);
            mGifView.setVisibility(View.GONE);
            if (message.getIsDiceSelect() == 0) {
                start(chat_gif_jsb, 2, message);
            } else {
                if (message.getContent().contains("卍")) {
                    String diceIndex = message.getContent().split("卍")[1];
                    chat_gif_jsb.setImageDrawable(mContext.getResources().getDrawable(mContext.getResources().getIdentifier("jsb_" + diceIndex, "drawable", mContext.getPackageName())));
                }
            }
        } else {
            mRootView = mGifView;
            rl_jsb.setVisibility(View.GONE);
            rl_dice.setVisibility(View.GONE);
            mGifView.setVisibility(View.VISIBLE);
            int resId = SmileyParser.Gifs.textMapId(gifName);
            if (resId != -1) {
                int margin = DisplayUtil.dip2px(mContext, 20);
                RelativeLayout.LayoutParams paramsL = (RelativeLayout.LayoutParams) mGifView.getLayoutParams();
                paramsL.setMargins(margin, 0, margin, 0);
                mGifView.setImageResource(resId);
            } else {
                mGifView.setImageBitmap(null);
            }
        }
    }

    @Override
    protected void onRootClick(View v) {

    }

    @Override
    public boolean enableSendRead() {
        return true;
    }

    AnimationDrawable animationDrawable;

    public void start(ImageView imageDice, int flag, ChatMessage message) {
        LogUtil.e("cjh消息更新 quyang-dice3333  " + message.getContent());
        if (message.isMySend()) {
            ChatMessageDao.getInstance().updateMessageDice(mLoginUserId, message.getToUserId(), message.getPacketId(), true);
        } else {
            ChatMessageDao.getInstance().updateMessageDice(mLoginUserId, message.getFromUserId(), message.getPacketId(), true);
        }

        if (flag == 1) {
            imageDice.setImageDrawable(mContext.getResources().getDrawable(R.drawable.anim_dice));

        } else {
            imageDice.setImageDrawable(mContext.getResources().getDrawable(R.drawable.anim_guess));
        }
        animationDrawable = (AnimationDrawable) imageDice.getDrawable();
        animationDrawable.start();

        if (message.getContent().contains("卍")) {
            String index = message.getContent().split("卍")[1];
            int delayTime = 2000;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (flag == 1) {
                        imageDice.setImageDrawable(mContext.getResources().getDrawable(mContext.getResources().getIdentifier("dice_" + index, "drawable", mContext.getPackageName())));
                    } else if (flag==2){
                        imageDice.setImageDrawable(mContext.getResources().getDrawable(mContext.getResources().getIdentifier("jsb_" + index, "drawable", mContext.getPackageName())));
                    }
                    EventBus.getDefault().post(new MessageStateEvent(1, message.getPacketId()));
                        if (animationDrawable == null) {
                            animationDrawable = (AnimationDrawable) imageDice.getDrawable();
                        }
                        animationDrawable.stop();
                }
            }, delayTime);

        }

    }


    Handler handler = new Handler();

}
