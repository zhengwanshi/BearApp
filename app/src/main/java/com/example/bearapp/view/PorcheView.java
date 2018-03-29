package com.example.bearapp.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.bearapp.R;
import com.example.bearapp.utils.ImgUtils;
import com.tencent.TIMUserProfile;

/**
 * Created by zhengyg on 2018/3/24.
 */

public class PorcheView extends LinearLayout {

    private AnimationDrawable drawb;
    private  AnimationDrawable drawf;
    private TextView senderName;
    private ImageView senderAvatar;
    private Animation inAnim;
    private Animation outAnim;
    private boolean avaliable=false;


    public PorcheView(Context context) {
        super(context);
        init();
    }
    public PorcheView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public PorcheView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_porche,this,true);
        findAllviews();
    }

    private void findAllviews() {
        senderAvatar = (ImageView) findViewById(R.id.sender_avatar);
        senderName = (TextView) findViewById(R.id.sender_name);
        ImageView wheel_b = (ImageView) findViewById(R.id.wheel_back);
        drawb= (AnimationDrawable) wheel_b.getDrawable();
        drawb.setOneShot(false);//设置执行数次
        ImageView wheel_f = (ImageView) findViewById(R.id.wheel_front);
        drawf = (AnimationDrawable) wheel_f.getDrawable();
        drawf.setOneShot(false);
        avaliable = true;
    }
    private boolean needShowAnim = false;
    private boolean layouted = false;//判断是否布局了

    public void show(TIMUserProfile userProfile){
        fillUserProfile(userProfile);
        if (layouted){
            startAnim();
        }
        else{
            needShowAnim = true;
        }
    }

    private void startAnim() {
        avaliable = false;//正在开始一个全局礼物，其他礼物先不显示

        int width= getWidth();
        int left = getLeft();
        inAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0-(width+left)*1.0f/width,//from x
                Animation.RELATIVE_TO_SELF,0,//to x
                Animation.RELATIVE_TO_SELF,-1,//from y
                Animation.RELATIVE_TO_SELF,0//to y
        );
        inAnim.setDuration(2000);
        inAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setVisibility(VISIBLE);
                drawb.start();
                drawf.start();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startAnimation(outAnim);

                    }
                },3000);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        outAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, //from X
                Animation.RELATIVE_TO_SELF, (width + left) * 1.0f / width,//to X
                Animation.RELATIVE_TO_SELF, 0,//fromY
                Animation.RELATIVE_TO_SELF, 1 //to Y
        );
        outAnim.setDuration(2000);//2秒
        outAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                setVisibility(INVISIBLE);
                drawb.stop();
                drawf.stop();
                needShowAnim = false;
                avaliable = true;
                if (onAvaliableListener != null){
                    onAvaliableListener.OnAvaliable();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        startAnimation(inAnim);
    }

    /**
     * 填充用户信息
     * @param userProfile
     */

    private void fillUserProfile(TIMUserProfile userProfile) {
        String avatarUrl = userProfile.getFaceUrl();
        if (TextUtils.isEmpty(avatarUrl)) {
            ImgUtils.loadRound(R.drawable.default_avatar, senderAvatar);
        } else {
            ImgUtils.loadRound(avatarUrl, senderAvatar);
        }

        String name = userProfile.getNickName();
        if (TextUtils.isEmpty(name)) {
            name = userProfile.getIdentifier();
        }

        senderName.setText(name);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        layouted = true;
        if (needShowAnim){
            startAnim();
        }
    }

    private OnAvaliableListener onAvaliableListener;
    public void setOnAvaliableLister( OnAvaliableListener l){
        onAvaliableListener = l;
    }
    public interface OnAvaliableListener{
      public   void OnAvaliable();
    }

}
