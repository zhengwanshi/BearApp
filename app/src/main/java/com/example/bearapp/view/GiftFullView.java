package com.example.bearapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.bearapp.model.GiftInfo;
import com.tencent.TIMUserProfile;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhengyg on 2018/3/24.
 */

public class GiftFullView extends RelativeLayout {
    private PorcheView mPorcheView;
    private boolean isAvaliable = false;

    private class GiftUserInfo{
        GiftInfo giftinfo;
        TIMUserProfile userProfile;
    }
    private List<GiftUserInfo> giftUserInfoList = new LinkedList<GiftUserInfo>();

    public GiftFullView(Context context) {
        super(context);
        init();
    }
    public GiftFullView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public GiftFullView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        isAvaliable = true;
    }
    public void showGift(GiftInfo giftInfo,TIMUserProfile userProfile){
        if (giftInfo ==null || giftInfo.type !=GiftInfo.Type.FullScreenGift){
            return;
        }
        if (isAvaliable){
            isAvaliable = false;
            if (giftInfo.giftId ==GiftInfo.Gift_BaoShiJie.giftId){
                showProcheView(userProfile);

            }else{
                //其他礼物
            }
        }
        else {//如果正在显示礼物
            GiftUserInfo giftUserInfo = new GiftUserInfo();
            giftUserInfo.userProfile = userProfile;
            giftUserInfo.giftinfo = giftInfo;
            giftUserInfoList.add(giftUserInfo);

        }

    }

    private void showProcheView(TIMUserProfile userProfile) {
        if (mPorcheView == null){
            mPorcheView = new PorcheView(getContext());

            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
            addView(mPorcheView,rlp);

            mPorcheView.setOnAvaliableLister(new PorcheView.OnAvaliableListener() {
                @Override
                public void OnAvaliable() {
                    isAvaliable = true;
                    int size = giftUserInfoList.size();
                    if (size>0){
                        GiftUserInfo giftUserInfo = giftUserInfoList.remove(0);
                        GiftInfo giftInfo = giftUserInfo.giftinfo;
                        TIMUserProfile userProfile = giftUserInfo.userProfile;
                        showGift(giftInfo,userProfile);
                    }
                }
            });

        }
        mPorcheView.show(userProfile);
    }
}
