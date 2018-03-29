package com.example.bearapp.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.bearapp.R;
import com.example.bearapp.model.ChatMsgInfo;
import com.example.bearapp.utils.ImgUtils;

/**
 * Created by zhengyg on 2018/3/22.
 */

public class DanmuItemView extends RelativeLayout {


    private static final String TAG = DanmuItemView.class.getSimpleName();
    private ImageView mSenderAvatar;
    private TextView mSenderName;
    private TextView mChatContent;
    private TranslateAnimation mTranslateAnimation=null;
    public DanmuItemView(Context context) {
        super(context);
        init();
    }
    public DanmuItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public DanmuItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_danmu_item,this,true);
        findAllViews();
        mTranslateAnimation = (TranslateAnimation) AnimationUtils.loadAnimation(getContext(),R.anim.danmu_item_enter);
        mTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d(TAG,DanmuItemView.this + " onAnimationStart VISIBLE");
                setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG,DanmuItemView.this + " onAnimationEnd INVISIBLE");
                setVisibility(INVISIBLE);
                if (onAvaliableListener != null) {
                    onAvaliableListener.onAvaliable();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void showMsgInfo(ChatMsgInfo danmuInfo) {
        String avatar = danmuInfo.getAvatar();
        if (TextUtils.isEmpty(avatar)) {
            ImgUtils.loadRound(R.drawable.default_avatar, mSenderAvatar);
        } else {
            ImgUtils.loadRound(avatar, mSenderAvatar);
        }

        mSenderName.setText(danmuInfo.getSenderName());
        mChatContent.setText(danmuInfo.getContent());

        //在动画监听里面做处理，调用post保证在动画结束之后再start
        //解决start之后直接end的情况。
        post(new Runnable() {
            @Override
            public void run() {
                DanmuItemView.this.startAnimation(mTranslateAnimation);
            }
        });

    }

    private void findAllViews() {
        mSenderAvatar = (ImageView) findViewById(R.id.user_avatar);
        mSenderName = (TextView) findViewById(R.id.user_name);

        mChatContent = (TextView) findViewById(R.id.chat_content);
    }

   private Animation.AnimationListener mAnimationListener =new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            Log.d(TAG,DanmuItemView.this + " onAnimationStart VISIBLE");
            setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            Log.d(TAG,DanmuItemView.this + " onAnimationEnd INVISIBLE");
            setVisibility(INVISIBLE);
            if (onAvaliableListener != null) {
                onAvaliableListener.onAvaliable();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private OnAvaliableListener onAvaliableListener;
    public void setOnAvaliableListener(OnAvaliableListener l){
        onAvaliableListener = l;
    }
  public interface OnAvaliableListener{
      public void onAvaliable();
  }
}
