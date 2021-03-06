package com.example.bearapp.hostlive;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.bearapp.BearApplication;
import com.example.bearapp.R;
import com.example.bearapp.model.ChatMsgInfo;
import com.example.bearapp.model.Constants;
import com.example.bearapp.model.GiftCmdInfo;
import com.example.bearapp.model.GiftInfo;
import com.example.bearapp.view.BottomControlView;
import com.example.bearapp.view.ChatMsgListView;
import com.example.bearapp.view.ChatView;
import com.example.bearapp.view.DanmuView;
import com.example.bearapp.view.GiftFullView;
import com.example.bearapp.view.GiftRrepeatView;
import com.example.bearapp.view.TitleView;
import com.example.bearapp.view.VipEnterView;
import com.example.bearapp.widget.HostControlDialog;
import com.example.bearapp.widget.SizeChangeRelativeLayout;
import com.google.gson.Gson;
import com.tencent.TIMMessage;
import com.tencent.TIMUserProfile;
import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.view.AVRootView;
import com.tencent.livesdk.ILVCustomCmd;
import com.tencent.livesdk.ILVLiveConfig;
import com.tencent.livesdk.ILVLiveConstants;
import com.tencent.livesdk.ILVLiveManager;
import com.tencent.livesdk.ILVLiveRoomOption;
import com.tencent.livesdk.ILVText;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import tyrantgit.widget.HeartLayout;

public class HostLiveActivity extends AppCompatActivity {
    private SizeChangeRelativeLayout mSizeChangeRelativeLayout;
    private TitleView mTitleView;
    private AVRootView mLiveView;
    private BottomControlView mControlView;
    private ChatView mChatView;
    private VipEnterView mVipEnterView;
    private ChatMsgListView mChatMsgListView;
    private DanmuView mDanmuView;

    private Timer heartBeatTimer = new Timer();
    private Timer heartTimer = new Timer();
    private Random heartRandom = new Random();
    private HeartLayout heartLayout;
    private GiftRrepeatView giftRepeatView;
    private GiftFullView giftFullView;

    private HostControlState hostControlState;
    private FlashlightHelper flashlightHelper;

    private int mRoomId;
    private HeartBeatRequest mHeartBeatRequest = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_live);
        findAllViews();
        createLive();
    }

    private void createLive() {
        hostControlState = new HostControlState();
        flashlightHelper = new FlashlightHelper();
        mRoomId = getIntent().getIntExtra("roomId",-1);
        if (mRoomId<0){
            return;
        }

        ILVLiveConfig liveConfig = BearApplication.getApplication().getLiveConfig();
        liveConfig.setLiveMsgListener(new ILVLiveConfig.ILVLiveMsgListener() {
            @Override
            public void onNewTextMsg(ILVText text, String SenderId, TIMUserProfile userProfile) {
                //接收到文本消息
            }

            @Override
            public void onNewCustomMsg(ILVCustomCmd cmd, String id, TIMUserProfile userProfile) {
//接收到自定义消息
                if (cmd.getCmd() == Constants.CMD_CHAT_MSG_LIST) {
                    String content = cmd.getParam();
                    ChatMsgInfo info = ChatMsgInfo.createListInfo(content, id, userProfile.getFaceUrl());
                   mChatMsgListView.addMsgInfo(info);
                } else if (cmd.getCmd() == Constants.CMD_CHAT_MSG_DANMU) {
                    String content = cmd.getParam();
//                    ChatMsgInfo info = ChatMsgInfo.createListInfo(content, id, userProfile.getFaceUrl());
//                    mChatMsgListView.addMsgInfo(info);

                    String name = userProfile.getNickName();
                    if (TextUtils.isEmpty(name)) {
                        name = userProfile.getIdentifier();
                    }
                    ChatMsgInfo danmuInfo = ChatMsgInfo.createDanmuInfo(content, id, userProfile.getFaceUrl(), name);
                    mDanmuView.addMsgInfo(danmuInfo);
                } else if (cmd.getCmd() == Constants.CMD_CHAT_GIFT) {
                    //界面显示礼物动画。
                    GiftCmdInfo giftCmdInfo = new Gson().fromJson(cmd.getParam(), GiftCmdInfo.class);
                    int giftId = giftCmdInfo.giftId;
                    String repeatId = giftCmdInfo.repeatId;
                    GiftInfo giftInfo = GiftInfo.getGiftById(giftId);
                    if (giftInfo == null) {
                        return;
                    }
                    if (giftInfo.giftId==GiftInfo.Gift_Heart.giftId){
                        //心形礼物
                        heartLayout.addHeart(getRandomColor());
                    }else if (giftInfo.type == GiftInfo.Type.ContinueGift) {
                        //重复礼物

                        giftRepeatView.showGift(giftInfo, repeatId, userProfile);
                    } else if (giftInfo.type == GiftInfo.Type.FullScreenGift) {
                        //全屏礼物
                        giftFullView.showGift(giftInfo, userProfile);
                    }
                } else if (cmd.getCmd() == ILVLiveConstants.ILVLIVE_CMD_ENTER) {
                    //用户进入直播
                    mTitleView.addWatcher(userProfile);
                    mVipEnterView.showVipEnter(userProfile);
                } else if (cmd.getCmd() == ILVLiveConstants.ILVLIVE_CMD_LEAVE) {
                    //用户离开消息
                    mTitleView.removeWatcher(userProfile);
                }

            }

            @Override
            public void onNewOtherMsg(TIMMessage message) {
                //接收到其他消息
            }
        });

        //创建房间配置项
        ILVLiveRoomOption hostOption = new ILVLiveRoomOption(ILiveLoginManager.getInstance().getMyUserId()).
                controlRole("LiveMaster")//角色设置
                .autoFocus(true)
                .autoMic(hostControlState.isVoiceOn())
                .authBits(AVRoomMulti.AUTH_BITS_DEFAULT)//权限设置
                .cameraId(hostControlState.getCameraid())//摄像头前置后置
                .videoRecvMode(AVRoomMulti.VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO);//是否开始半自动接收


        //创建房间
        ILVLiveManager.getInstance().createRoom(mRoomId, hostOption, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                //开始心形动画
                startHeartAnim();
                //开始发送心跳
                startHeartBeat();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                //失败的情况下，退出界面。
                Toast.makeText(HostLiveActivity.this, "创建直播失败！", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    private void startHeartBeat() {
        heartBeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //发送心跳包
                if (mHeartBeatRequest == null) {
                    mHeartBeatRequest = new HeartBeatRequest();
                }
                String roomId = mRoomId + "";
                String userId = BearApplication.getApplication().getSelfProfile().getIdentifier();
                String url = mHeartBeatRequest.getUrl(roomId, userId);
                mHeartBeatRequest.request(url);
            }
        }, 0, 4000); //4秒钟 。服务器是10秒钟去检测一次。
    }

    private void startHeartAnim() {
        heartTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                heartLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        heartLayout.addHeart(getRandomColor());
                    }
                });
            }
        }, 0, 1000); //1秒钟
    }

    private int getRandomColor() {
        return Color.rgb(heartRandom.nextInt(255), heartRandom.nextInt(255), heartRandom.nextInt(255));

    }

    private void findAllViews() {
        mSizeChangeRelativeLayout = (SizeChangeRelativeLayout) findViewById(R.id.size_change_layout);
        mSizeChangeRelativeLayout.setOnSizeChangeListener(new SizeChangeRelativeLayout.OnSizeChangeListener() {
            @Override
            public void onLarge() {
                //键盘隐藏
                mChatView.setVisibility(View.INVISIBLE);
                mControlView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSmall() {
                //
            }
        });

        mTitleView = (TitleView) findViewById(R.id.title_view);
        mTitleView.setHost(BearApplication.getApplication().getSelfProfile());

        mLiveView = (AVRootView) findViewById(R.id.live_view);
        ILVLiveManager.getInstance().setAvVideoView(mLiveView);

        mControlView = (BottomControlView) findViewById(R.id.control_view);
        mControlView.setIsHost(true);
        mControlView.setOnClickControlListener(new BottomControlView.OnControlListener() {
            @Override
            public void onChatClick() {
                //点击了聊天按钮
                mChatView.setVisibility(View.VISIBLE);
                mControlView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCloseClick() {
                //点击了关闭按钮
                quitLive();

            }

            @Override
            public void onGiftClick() {

            }

            @Override
            public void onOptionClick(View view) {
                //显示主播操作对话框
                boolean beautyOn = hostControlState.isBeautyOn();
                boolean flashOn = hostControlState.isFlashOn();
                boolean voiceOn = hostControlState.isVoiceOn();
                HostControlDialog hostControlDialog = new HostControlDialog(HostLiveActivity.this);
                hostControlDialog.setOnControlClickListener(controlClickListener);
                hostControlDialog.updateView(beautyOn,flashOn,voiceOn);
                hostControlDialog.show(view);

            }
        });

        mChatView = (ChatView) findViewById(R.id.chat_view);
        mChatView.setOnChatSendListener(new ChatView.OnChatSendListener() {
            @Override
            public void onChatSend(final ILVCustomCmd customCmd) {
                //发送消息
                customCmd.setDestId(ILiveRoomManager.getInstance().getIMGroupId());

                ILVLiveManager.getInstance().sendCustomCmd(customCmd, new ILiveCallBack<TIMMessage>() {


                    @Override
                    public void onSuccess(TIMMessage data) {
                        if (customCmd.getCmd() == Constants.CMD_CHAT_MSG_LIST){
                            //如果是列表类型的消息，发送给列表显示
                            String chatContent = customCmd.getParam();
                            String userId = BearApplication.getApplication().getSelfProfile().getIdentifier();
                            String avatar = BearApplication.getApplication().getSelfProfile().getFaceUrl();
                            ChatMsgInfo info = ChatMsgInfo.createListInfo(chatContent, userId, avatar);
                            mChatMsgListView.addMsgInfo(info);

                        }else if(customCmd.getCmd() == Constants.CMD_CHAT_MSG_DANMU){
                            String chatContent = customCmd.getParam();
                            String userId = BearApplication.getApplication().getSelfProfile().getIdentifier();
                            String avatar = BearApplication.getApplication().getSelfProfile().getFaceUrl();
//                            ChatMsgInfo info = ChatMsgInfo.createListInfo(chatContent, userId, avatar);
//                            mChatMsgListView.addMsgInfo(info);

                            String name = BearApplication.getApplication().getSelfProfile().getNickName();
                            if (TextUtils.isEmpty(name)) {
                                name = userId;
                            }
                            ChatMsgInfo danmuInfo = ChatMsgInfo.createDanmuInfo(chatContent, userId, avatar, name);
                            mDanmuView.addMsgInfo(danmuInfo);

                        }

                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {

                    }
                });

            }
        });

        mControlView.setVisibility(View.VISIBLE);
        mChatView.setVisibility(View.INVISIBLE);

        mChatMsgListView = (ChatMsgListView) findViewById(R.id.chat_list);
        mVipEnterView = (VipEnterView) findViewById(R.id.vip_enter);
        mDanmuView = (DanmuView) findViewById(R.id.danmu_view);

        heartLayout = (HeartLayout) findViewById(R.id.heart_layout);
        giftRepeatView = (GiftRrepeatView) findViewById(R.id.gift_repeat_view);
        giftFullView = (GiftFullView) findViewById(R.id.gift_full_view);
    }

    HostControlDialog.OnControlClickListener controlClickListener = new HostControlDialog.OnControlClickListener() {
        @Override
        public void onBeautyClick() {
            //点击美颜
            boolean isBeautyOn = hostControlState.isBeautyOn();
            if (isBeautyOn){
                //关闭美颜
                ILiveRoomManager.getInstance().enableBeauty(0);
                hostControlState.setBeautyOn(false);
            }else {
                //打开美颜
               ILiveRoomManager.getInstance().enableBeauty(50);
                hostControlState.setBeautyOn(true);

            }



        }

        @Override
        public void onFlashClick() {
            // 闪光灯
            boolean isFlashOn = flashlightHelper.isFlashLightOn();
            if (isFlashOn) {
                flashlightHelper.enableFlashLight(false);
            } else {
                flashlightHelper.enableFlashLight(true);
            }

        }

        @Override
        public void onVoiceClick() {
            //声音
            boolean isVoiceOn = hostControlState.isVoiceOn();
            if (isVoiceOn) {
                //静音
                ILiveRoomManager.getInstance().enableMic(false);
                hostControlState.setVoiceOn(false);
            } else {
                ILiveRoomManager.getInstance().enableMic(true);
                hostControlState.setVoiceOn(true);
            }

        }

        @Override
        public void onCameraClick() {
            int cameraId = hostControlState.getCameraid();
            if (cameraId == ILiveConstants.FRONT_CAMERA) {
                ILiveRoomManager.getInstance().switchCamera(ILiveConstants.BACK_CAMERA);
                hostControlState.setCameraid(ILiveConstants.BACK_CAMERA);
            } else if (cameraId == ILiveConstants.BACK_CAMERA) {
                ILiveRoomManager.getInstance().switchCamera(ILiveConstants.FRONT_CAMERA);
                hostControlState.setCameraid(ILiveConstants.FRONT_CAMERA);
            }


        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        ILVLiveManager.getInstance().onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ILVLiveManager.getInstance().onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        heartTimer.cancel();
        heartBeatTimer.cancel();
    }

    @Override
    public void onBackPressed() {
        quitLive();
    }

    private void quitLive() {
        ILVCustomCmd customCmd = new ILVCustomCmd();
        customCmd.setType(ILVText.ILVTextType.eGroupMsg);
        customCmd.setCmd(ILVLiveConstants.ILVLIVE_CMD_LEAVE);
        customCmd.setDestId(ILiveRoomManager.getInstance().getIMGroupId());
        ILVLiveManager.getInstance().sendCustomCmd(customCmd, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                ILiveRoomManager.getInstance().quitRoom(new ILiveCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                        logout();
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        logout();
                    }
                });
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {

            }
        });

        //发送退出消息给服务器
        QuitRoomRequest request = new QuitRoomRequest();
        String roomId = mRoomId +"";
        String userId = BearApplication.getApplication().getSelfProfile().getIdentifier();
        String url = request.getUrl(roomId, userId);
        request.request(url);

    }

    private void logout() {
        finish();
    }
}
