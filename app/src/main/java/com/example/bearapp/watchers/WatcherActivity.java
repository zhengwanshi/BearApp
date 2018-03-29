package com.example.bearapp.watchers;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.bearapp.BearApplication;
import com.example.bearapp.R;
import com.example.bearapp.hostlive.HeartBeatRequest;
import com.example.bearapp.hostlive.QuitRoomRequest;
import com.example.bearapp.model.ChatMsgInfo;
import com.example.bearapp.model.Constants;
import com.example.bearapp.model.GiftCmdInfo;
import com.example.bearapp.model.GiftInfo;
import com.example.bearapp.utils.request.BaseRequest;
import com.example.bearapp.view.BottomControlView;
import com.example.bearapp.view.ChatMsgListView;
import com.example.bearapp.view.ChatView;
import com.example.bearapp.view.DanmuView;
import com.example.bearapp.view.GiftFullView;
import com.example.bearapp.view.GiftRrepeatView;
import com.example.bearapp.view.TitleView1;
import com.example.bearapp.view.VipEnterView;
import com.example.bearapp.widget.GiftSelectDialog;
import com.example.bearapp.widget.SizeChangeRelativeLayout;
import com.google.gson.Gson;
import com.tencent.TIMFriendshipManager;
import com.tencent.TIMMessage;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;
import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.view.AVRootView;
import com.tencent.livesdk.ILVCustomCmd;
import com.tencent.livesdk.ILVLiveConfig;
import com.tencent.livesdk.ILVLiveConstants;
import com.tencent.livesdk.ILVLiveManager;
import com.tencent.livesdk.ILVLiveRoomOption;
import com.tencent.livesdk.ILVText;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import tyrantgit.widget.HeartLayout;

public class WatcherActivity extends AppCompatActivity {
    private static final String TAG = "gift";
    private SizeChangeRelativeLayout mSizeChangeLayout;
    private TitleView1 titleView;
    private AVRootView mLiveView;
    private BottomControlView mControlView;
    private ChatView mChatView;
    private ChatMsgListView mChatListView;
    private VipEnterView mVipEnterView;
    private DanmuView mDanmuView;
    private GiftSelectDialog giftSelectDialog;

    private Timer heartTimer = new Timer();
    private Random heartRandom = new Random();
    private HeartLayout heartLayout;
    private GiftRrepeatView giftRepeatView;
    private GiftFullView giftFullView;

    private String hostId;
    private int mRoomId;

    private HeartBeatRequest mHeartBeatRequest = null;
    private Timer heartBeatTimer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watcher);
        findAllViews();
        joinRoom();
    }

    private void joinRoom() {
        mRoomId = getIntent().getIntExtra("roomId",-1);
        hostId = getIntent().getStringExtra("hostId");
        if (mRoomId<0 || TextUtils.isEmpty(hostId)){
            return;
        }

        ILVLiveConfig liveConfig = BearApplication.getApplication().getLiveConfig();
        liveConfig.setLiveMsgListener(new ILVLiveConfig.ILVLiveMsgListener() {
            @Override
            public void onNewTextMsg(ILVText text, String SenderId, TIMUserProfile userProfile) {
                //接收到文本信息

            }

            @Override
            public void onNewCustomMsg(ILVCustomCmd cmd, String id, TIMUserProfile userProfile) {
                //接收到自定义消息
                if (cmd.getCmd() == Constants.CMD_CHAT_MSG_LIST) {
                    String content = cmd.getParam();
                    ChatMsgInfo info = ChatMsgInfo.createListInfo(content,id,userProfile.getFaceUrl());
                    mChatListView.addMsgInfo(info);
                }else if (cmd.getCmd() == Constants.CMD_CHAT_MSG_DANMU){
                    String content = cmd.getParam();
                    String name = userProfile.getNickName();
                    if (TextUtils.isEmpty(name)){
                        name = userProfile.getIdentifier();
                    }
                    ChatMsgInfo info = ChatMsgInfo.createDanmuInfo(content,id,userProfile.getFaceUrl(),name);
                    mDanmuView.addMsgInfo(info);
                }else if (cmd.getCmd() == ILVLiveConstants.ILVLIVE_CMD_LEAVE){
                    //用户离开消息
                    if (hostId.equals(userProfile.getIdentifier())){
                        //主播推出直播
                        quitRoom();
                    }else {
                        //观众退出直播
                        titleView.removeWatcher(userProfile);
                    }

                }else if (cmd.getCmd() == ILVLiveConstants.ILVLIVE_CMD_ENTER){
                    titleView.addWatcher(userProfile);
                    mVipEnterView.showVipEnter(userProfile);
                }
            }

            @Override
            public void onNewOtherMsg(TIMMessage message) {
            //接收到其他信息
            }
        });

        //加入房间配置项
        ILVLiveRoomOption memberOption = new ILVLiveRoomOption(hostId)
                .autoCamera(false)//是否打开摄像头
                .controlRole("Guest")//角色设置
                .authBits(AVRoomMulti.AUTH_BITS_JOIN_ROOM|AVRoomMulti.AUTH_BITS_RECV_AUDIO|
                        AVRoomMulti.AUTH_BITS_RECV_CAMERA_VIDEO|AVRoomMulti.AUTH_BITS_RECV_SCREEN_VIDEO)//权限设置
                .videoRecvMode(AVRoomMulti.VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO)//是否开始半自动接收
                .autoMic(false);//是否自主打开Mic
        //加入房间
        ILVLiveManager.getInstance().joinRoom(mRoomId, memberOption, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                //开始心形动画
                startHeartAnim();
                //同时发送进入直播消息
                sendEnterRoomMsg();
                //显示直播头像
                updateTitleView();
                //开始心跳包
                startHeartBeat();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Toast.makeText(WatcherActivity.this, "直播已结束", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void startHeartBeat() {
        heartBeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //发送心跳包
                if (mHeartBeatRequest == null){
                    mHeartBeatRequest = new HeartBeatRequest();
                }
                String roomId = mRoomId+"";
                String userId = BearApplication.getApplication().getSelfProfile().getIdentifier();
                String url = mHeartBeatRequest.getUrl(roomId,userId);
                mHeartBeatRequest.request(url);

            }
        },0,4000);
    }

    private void updateTitleView() {
        List<String> list = new ArrayList<String>();
        list.add(hostId);
        TIMFriendshipManager.getInstance().getUsersProfile(list, new TIMValueCallBack<List<TIMUserProfile>>() {
            @Override
            public void onError(int i, String s) {
                //失败
                titleView.setHost( null);

            }

            @Override
            public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                titleView.setHost(timUserProfiles.get(0));
            }
        });

        //添加自己的头像到titleView
        titleView.addWatcher(BearApplication.getApplication().getSelfProfile());

        //请求已经加入房间的成员信息
        GetWatcherRequest watcherRequest = new GetWatcherRequest();
        watcherRequest.setOnResultListener(new BaseRequest.OnResultListener<Set<String>>() {
            @Override
            public void onFail(int code, String msg) {

            }

            @Override
            public void onSuccess(Set<String> watchers) {
                if (watchers ==null){
                    return;
                }

                List<String> watcherList = new ArrayList<String>();
                watcherList.addAll(watchers);
                TIMFriendshipManager.getInstance().getUsersProfile(watcherList, new TIMValueCallBack<List<TIMUserProfile>>() {
                    @Override
                    public void onError(int i, String s) {
                        //失败
                    }

                    @Override
                    public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                    //成功添加已经在房间的观众信息
                        titleView.addWatchers(timUserProfiles);
                    }
                });
            }


        });

        String watcherRequestUrl = watcherRequest.getUrl(mRoomId+"");
        watcherRequest.request(watcherRequestUrl);
    }

    private void sendEnterRoomMsg() {
        ILVCustomCmd customCmd = new ILVCustomCmd();
        customCmd.setType(ILVText.ILVTextType.eGroupMsg);
        customCmd.setCmd(ILVLiveConstants.ILVLIVE_CMD_ENTER);
        customCmd.setDestId(ILiveRoomManager.getInstance().getIMGroupId());
        ILVLiveManager.getInstance().sendCustomCmd(customCmd, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {

            }

            @Override
            public void onError(String module, int errCode, String errMsg) {

            }
        });
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
        },0,1000);
    }

    private int getRandomColor() {
        return Color.rgb(heartRandom.nextInt(255),heartRandom.nextInt(255),heartRandom.nextInt(255));
    }

    private void findAllViews() {
        mSizeChangeLayout = (SizeChangeRelativeLayout) findViewById(R.id.activity_watcher);
        mSizeChangeLayout.setOnSizeChangeListener(new SizeChangeRelativeLayout.OnSizeChangeListener() {
            @Override
            public void onLarge() {
                //键盘隐藏
                mChatView.setVisibility(View.INVISIBLE);
                mControlView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onSmall() {
                //键盘显示
                mChatView.setVisibility(View.VISIBLE);
                mControlView.setVisibility(View.INVISIBLE);
            }
        });

        titleView = (TitleView1) findViewById(R.id.title_view);
        mLiveView = (AVRootView) findViewById(R.id.live_view);
        ILVLiveManager.getInstance().setAvVideoView(mLiveView);

        mControlView = (BottomControlView) findViewById(R.id.control_view);
        mControlView.setIsHost(false);
        mControlView.setOnClickControlListener(new BottomControlView.OnControlListener() {
            @Override
            public void onChatClick() {
                //点击了聊天按钮，显示聊天操作栏
                mChatView.setVisibility(View.VISIBLE);
                mControlView.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCloseClick() {
                //点击了关闭按钮
                quitRoom();
            }

            @Override
            public void onGiftClick() {
                //显示礼物九宫格
                if (giftSelectDialog == null){
                    giftSelectDialog = new GiftSelectDialog(WatcherActivity.this);
                    giftSelectDialog.setGiftSendListener(giftSendDialog);
                }
                giftSelectDialog.show();
            }

            @Override
            public void onOptionClick(View view) {

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
                        ChatMsgInfo info = ChatMsgInfo.createListInfo(chatContent,userId,avatar);
                        mChatListView.addMsgInfo(info);}
                        else if (customCmd.getCmd() == Constants.CMD_CHAT_MSG_DANMU){
                            String chatContent = customCmd.getParam();
                            String userId = BearApplication.getApplication().getSelfProfile().getIdentifier();
                            String avatar = BearApplication.getApplication().getSelfProfile().getFaceUrl();
                            String name = BearApplication.getApplication().getSelfProfile().getNickName();
                            if (TextUtils.isEmpty(name)){
                                name = userId;
                            }

                            ChatMsgInfo danmuInfo = ChatMsgInfo.createDanmuInfo(chatContent,userId,avatar,name);
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
        mChatListView = (ChatMsgListView) findViewById(R.id.chat_list);
        mVipEnterView = (VipEnterView) findViewById(R.id.vip_enter);
        mDanmuView = (DanmuView) findViewById(R.id.danmu_view);

        heartLayout = (HeartLayout) findViewById(R.id.heart_layout);
        heartLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送心形礼物
                ILVCustomCmd giftCmd = new ILVCustomCmd();
                giftCmd.setType(ILVText.ILVTextType.eGroupMsg);
                giftCmd.setCmd(Constants.CMD_CHAT_GIFT);
                GiftCmdInfo giftCmdInfo = new GiftCmdInfo();
                giftCmdInfo.giftId = GiftInfo.Gift_Heart.giftId;

                giftCmd.setParam(new Gson().toJson(giftCmdInfo));
                giftSendDialog.onGiftSendClick(giftCmd);

            }
        });
        giftRepeatView = (GiftRrepeatView) findViewById(R.id.gift_repeat_view);
        giftFullView = (GiftFullView) findViewById(R.id.gift_full_view);

    }

    GiftSelectDialog.OnGiftSendListener giftSendDialog = new GiftSelectDialog.OnGiftSendListener() {
        @Override
        public void onGiftSendClick(final ILVCustomCmd customCmd) {
            customCmd.setDestId(ILiveRoomManager.getInstance().getIMGroupId());

            ILVLiveManager.getInstance().sendCustomCmd(customCmd, new ILiveCallBack<TIMMessage>() {


                @Override
                public void onSuccess(TIMMessage data) {
                    if (customCmd.getCmd() == Constants.CMD_CHAT_GIFT){
                        //界面显示礼物动画
                        GiftCmdInfo giftCmdInfo = new Gson().fromJson(customCmd.getParam(),GiftCmdInfo.class);
                        int giftId = giftCmdInfo.giftId;
                        String repeatId = giftCmdInfo.repeatId;
                        GiftInfo giftInfo = GiftInfo.getGiftById(giftId);
                        if (giftInfo ==null){
                            return;
                        }
                        if (giftInfo.giftId==GiftInfo.Gift_Heart.giftId){
                            //心形礼物
                            heartLayout.addHeart(getRandomColor());
                        }else if (giftInfo.type == GiftInfo.Type.ContinueGift){
                            giftRepeatView.showGift(giftInfo,repeatId, BearApplication.getApplication().getSelfProfile());

                        }else if (giftInfo.type == GiftInfo.Type.FullScreenGift){
                            //全屏礼物
                            giftFullView.showGift(giftInfo,BearApplication.getApplication().getSelfProfile());
                        }
                    }
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {

                }
            });
        }
    };
    private void quitRoom() {
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
        String roomId = mRoomId+"";
        String userId = BearApplication.getApplication().getSelfProfile().getIdentifier();
        String url = request.getUrl(roomId,userId);
        request.request(url);
        logout();
    }

    private void logout() {
        finish();
    }

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
        quitRoom();
    }
}
