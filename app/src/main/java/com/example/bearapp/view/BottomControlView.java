package com.example.bearapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.bearapp.R;

/**
 * Created by zhengyg on 2018/3/21.
 */

public class BottomControlView extends RelativeLayout {

    private ImageView optionView;
    private ImageView giftView;
    public BottomControlView(Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_bottom_control,this,true);
        findAllViews();
    }


    public BottomControlView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }
    public BottomControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setIsHost(boolean isHost){
        if (isHost){
            giftView.setVisibility(GONE);
            optionView.setVisibility(VISIBLE);
        }else{
            giftView.setVisibility(VISIBLE);
            optionView.setVisibility(GONE);
        }
    }
    private void findAllViews() {
        findViewById(R.id.chat).setOnClickListener(clickListener);
        findViewById(R.id.close).setOnClickListener(clickListener);
        optionView = (ImageView) findViewById(R.id.option);
        optionView.setOnClickListener(clickListener);
        giftView = (ImageView) findViewById(R.id.gift);
        giftView.setOnClickListener(clickListener);
    }

       private OnClickListener clickListener=new OnClickListener() {
        @Override
        public void onClick(View v) {
           switch (v.getId()){
               case R.id.chat:
                   //显示聊天操作栏
                   if (mOnControlListener != null){
                       mOnControlListener.onChatClick();
                   }
                   break;
               case R.id.close:
                   //关闭直播
                   if (mOnControlListener != null){
                       mOnControlListener.onCloseClick();
                   }
                   break;
               case R.id.option:
                   //主播操作选项
                   if (mOnControlListener != null){
                       mOnControlListener.onOptionClick(v);
                   }
                   break;
               case R.id.gift:
                   //显示礼物九宫格
                   if (mOnControlListener != null){
                       mOnControlListener.onGiftClick();
                   }
                   break;
           }

        }
        };
    private OnControlListener mOnControlListener;
    public void setOnClickControlListener(OnControlListener l){
        mOnControlListener = l;
    }
    public interface OnControlListener{
        public void onChatClick();
        public void onCloseClick();
        public void onGiftClick();
        public void onOptionClick(View view);
    }
}
