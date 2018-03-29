package com.example.bearapp.view;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.bearapp.R;
import com.tencent.TIMUserProfile;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhengyg on 2018/3/22.
 */

public class VipEnterView extends RelativeLayout{

    private TextView userNameView;
    private ImageView splashView;

    private Animation viewAnimation;
    private Animation nameAnimation;
    private Animation splashAnimation;
    private boolean isAvaliable = true;
    private List<TIMUserProfile> vipProfile = new LinkedList<TIMUserProfile>();

    public VipEnterView(Context context) {
        super(context);
        init();
    }
    public VipEnterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public VipEnterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundResource(R.drawable.vip_enter_bkg);
        LayoutInflater.from(getContext()).inflate(R.layout.view_vip_enter,this,true);
        findAllViews();
        loadAnim();
        setVisibility(INVISIBLE);

    }

    private void loadAnim() {
        viewAnimation = AnimationUtils.loadAnimation(getContext(),R.anim.vip_enter_view);
        nameAnimation = AnimationUtils.loadAnimation(getContext(),R.anim.vip_enter_name);
        splashAnimation = AnimationUtils.loadAnimation(getContext(),R.anim.vip_enter_splash);
        viewAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                VipEnterView.this.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                userNameView.post(new Runnable() {
                    @Override
                    public void run() {
                        userNameView.startAnimation(nameAnimation);
                    }
                });

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        nameAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                userNameView.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                splashView.setAnimation(splashAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        splashAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                splashView.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                        //判断有无下一个用户进入
                isAvaliable = true;
                if (vipProfile.size()>0){
                    showVipEnter(vipProfile.remove(0));
                }else{
                 VipEnterView.this.setVisibility(INVISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }



    private void findAllViews() {
        userNameView = (TextView) findViewById(R.id.user_name);
        splashView = (ImageView) findViewById(R.id.splash);

    }

   public void showVipEnter(TIMUserProfile userProfile) {
        if (isAvaliable){

            String name = userProfile.getNickName();
            if (TextUtils.isEmpty(name)){
                name = userProfile.getIdentifier();
            }

            SpannableStringBuilder nameBuilder = new SpannableStringBuilder("");
            SpannableString nameSpanStr = new SpannableString(name);
            int nameStartIndex = 0;
            int nameEndIndex = name.length();
            nameSpanStr.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)),
                    nameStartIndex,nameEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            nameBuilder.append(nameSpanStr);

            SpannableString tipSpanStr = new SpannableString("进入房间");
            int tipStartIndex = 0;
            int tipEndIndex = tipSpanStr.length();
            tipSpanStr.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.white)),
                    tipStartIndex, tipEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            nameBuilder.append(tipSpanStr);

            userNameView.setText(nameBuilder);
            setVisibility(INVISIBLE);
            userNameView.setVisibility(INVISIBLE);
            splashView.setVisibility(INVISIBLE);
            VipEnterView.this.post(new Runnable() {
                @Override
                public void run() {
                    VipEnterView.this.startAnimation(viewAnimation);
                }
            });


        }else{
            vipProfile.add(userProfile);
        }
    }
}
