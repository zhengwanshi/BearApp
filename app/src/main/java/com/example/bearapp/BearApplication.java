package com.example.bearapp;

import android.app.Application;
import android.content.Context;

import com.example.bearapp.editprofile.CustomProfile;
import com.example.bearapp.utils.QnUploadHelper;
import com.tencent.TIMManager;
import com.tencent.TIMUserProfile;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.livesdk.ILVLiveConfig;
import com.tencent.livesdk.ILVLiveManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengyg on 2018/3/11.
 */

public class BearApplication extends Application {
    private static BearApplication app;
    private static Context appContext;
    private TIMUserProfile mSelfProfile;
    private ILVLiveConfig mLiveConfig;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        appContext = getApplicationContext();
     //ILiveSDK.getInstance().initSdk(getApplicationContext(),1400073170,23528);
        ILiveSDK.getInstance().initSdk(getApplicationContext(), 1400026811, 11334);

        List<String> customInfos = new ArrayList<String>();
        customInfos.add(CustomProfile.CUSTOM_GET);
        customInfos.add(CustomProfile.CUSTOM_SEND);
        customInfos.add(CustomProfile.CUSTOM_LEVEL);
        customInfos.add(CustomProfile.CUSTOM_RENZHENG);
        TIMManager.getInstance().initFriendshipSettings(CustomProfile.allBaseInfo,customInfos);

        //初始化直播场景
        mLiveConfig = new ILVLiveConfig();
        ILVLiveManager.getInstance().init(mLiveConfig);



        QnUploadHelper.init("jicVgcRbuwrRP8HQN2TygMX-7ubB5BcmDm0FwOKq",
                "cZeZmNdiFH5eHy-c-mEUNukTapF-DzIojW9EQ5qn",
                "http://p5ic3t39m.bkt.clouddn.com/",
                "wanshitong");

    }

    public static BearApplication getApplication() {
        return app;
    }
    public static Context getContext() {
        return appContext;
    }

    public void setSelfProfile(TIMUserProfile selfProfile) {
        mSelfProfile = selfProfile;
    }

    public TIMUserProfile getSelfProfile() {
        return mSelfProfile;
    }
    public ILVLiveConfig getLiveConfig() {
        return mLiveConfig;
    }
}
