package com.example.bearapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.example.bearapp.R;
import com.example.bearapp.model.GiftInfo;
import com.tencent.TIMUserProfile;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhengyg on 2018/3/22.
 */

public class GiftRrepeatView extends LinearLayout {
    private GiftRepeatItemView item0, item1,item2;
    
    private class GiftSenderAndInfo {
        public GiftInfo giftInfo;
        public String repeatId;
        public TIMUserProfile senderProfile;
    }

    private List<GiftSenderAndInfo> giftSenderAndInfoList = new LinkedList<GiftSenderAndInfo>();

    public GiftRrepeatView(Context context) {
        super(context);
        init();
    }
    public GiftRrepeatView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }
    public GiftRrepeatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_gift_repeat, this, true);
        findAllViews();
        item0.setVisibility(INVISIBLE);
        item1.setVisibility(INVISIBLE);
        item2.setVisibility(INVISIBLE);
    }

    private void findAllViews() {
        item0 = (GiftRepeatItemView) findViewById(R.id.item0);
        item1 = (GiftRepeatItemView) findViewById(R.id.item1);
        item2 = (GiftRepeatItemView) findViewById(R.id.item2);

        item0.setOnGiftItemAvaliableListener(avaliableListener);
        item1.setOnGiftItemAvaliableListener(avaliableListener);
        item2.setOnGiftItemAvaliableListener(avaliableListener);
    }
    private GiftRepeatItemView.OnGiftItemAvaliableListener avaliableListener = new GiftRepeatItemView.OnGiftItemAvaliableListener() {
        @Override
        public void onAvaliable() {
            if (giftSenderAndInfoList.size() > 0) {
                GiftSenderAndInfo info = giftSenderAndInfoList.remove(0);
                showGift(info.giftInfo, info.repeatId, info.senderProfile);
            }
        }
    };
    public void showGift(GiftInfo giftInfo, String repeatId, TIMUserProfile profile) {
        GiftRepeatItemView avaliableView = getAvaliableView(giftInfo, repeatId, profile);
        if (avaliableView == null) {
            GiftSenderAndInfo info = new GiftSenderAndInfo();
            info.giftInfo = giftInfo;
            info.senderProfile = profile;
            info.repeatId = repeatId;
            giftSenderAndInfoList.add(info);
        } else {
            avaliableView.showGift(giftInfo, repeatId, profile);
        }
    }
    private GiftRepeatItemView getAvaliableView(GiftInfo giftInfo, String repeatId, TIMUserProfile profile) {

        if (item0.isAvaliable(giftInfo, repeatId, profile)) {
            return item0;
        }

        if (item1.isAvaliable(giftInfo, repeatId, profile)) {
            return item1;
        }

        if (item0.getVisibility() == INVISIBLE) {
            return item0;
        }
        if (item1.getVisibility() == INVISIBLE) {
            return item1;
        }
        return null;
    }
}
