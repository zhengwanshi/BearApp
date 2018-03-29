package com.example.bearapp.view;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bearapp.R;
import com.example.bearapp.utils.ImgUtils;
import com.example.bearapp.utils.LogUtil;
import com.example.bearapp.widget.UserInfoDialog;
import com.tencent.TIMFriendshipManager;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengyg on 2018/3/19.
 */

public class TitleView1 extends LinearLayout {

    private ImageView hostAvatarImageView;
    private TextView watchersNumView;
    private int watcherNum = 0;

    private RecyclerView watcherListView;
    private  WatcherAdapter watcherAdapter;
    private String hostId;//主播Id
    public TitleView1(Context context) {
        super(context);
        init();
    }
    public TitleView1(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public TitleView1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.HORIZONTAL);
        LayoutInflater.from(getContext()).inflate(R.layout.view_title,this,true);
        findAllViews();
    }

    private void findAllViews() {
        hostAvatarImageView = (ImageView) findViewById(R.id.host_avatar);
        watchersNumView = (TextView) findViewById(R.id.watcher_num);
        hostAvatarImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击头像，显示详情对话框
                showUserInfoDialog(hostId);
            }
        });

        watcherListView= (RecyclerView) findViewById(R.id.watch_list);
        watcherListView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        watcherListView.setLayoutManager(layoutManager);

        //设置Adapter
        watcherAdapter = new WatcherAdapter(getContext());
        watcherListView.setAdapter(watcherAdapter);
    }

    private void showUserInfoDialog(String senderId) {
        List<String> ids = new ArrayList<String>();
        ids.add(senderId);
        TIMFriendshipManager.getInstance().getUsersProfile(ids, new TIMValueCallBack<List<TIMUserProfile>>() {
            @Override
            public void onError(int i, String s) {
                LogUtil.e("请求用户信息失败");
                Toast.makeText(TitleView1.this.getContext(), "请求用户信息失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                Context context = TitleView1.this.getContext();
                if (context instanceof Activity){
                    UserInfoDialog userInfoDialog = new UserInfoDialog((Activity) context,timUserProfiles.get(0));
                    userInfoDialog.show();
                }
            }
        });
    }

    /**
     * 设置主播头像
     * @param userProfile
     */
    public void setHost(TIMUserProfile userProfile){
        if (userProfile ==null){
            ImgUtils.loadRound(R.drawable.default_avatar,hostAvatarImageView);
        }else{
            hostId = userProfile.getIdentifier();
            String avatarUrl = userProfile.getFaceUrl();
            if (TextUtils.isEmpty(avatarUrl)){
                ImgUtils.loadRound(R.drawable.default_avatar,hostAvatarImageView);
            }else{
                ImgUtils.loadRound(avatarUrl,hostAvatarImageView);
            }
        }
    }

    /**
     * 有观众进入房间时候
     * @param userProfile
     */
    public void addWatcher(TIMUserProfile userProfile){
        if (userProfile != null){
            watcherAdapter.addWatcher(userProfile);
            watcherNum++;
            watchersNumView.setText("观众："+watcherNum);
        }
    }

    public void addWatchers(List<TIMUserProfile> userProfileList){
        if (userProfileList != null){
            watcherAdapter.addWatchers(userProfileList);
            watcherNum +=userProfileList.size();
            watchersNumView.setText("观众："+ watcherNum);
        }
    }

    public void removeWatcher(TIMUserProfile userProfile){
        if (userProfile != null){
            watcherAdapter.removeWatcher(userProfile);
            watcherNum--;
            watchersNumView.setText("观众："+ watcherNum);
        }
    }

    private class WatcherAdapter extends RecyclerView.Adapter {
        private Context mContext;
        private List<TIMUserProfile> watcherList = new ArrayList<TIMUserProfile>();
        private Map<String,TIMUserProfile> watcherMap = new HashMap<String, TIMUserProfile>();

        public WatcherAdapter(Context context) {
            mContext = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.adapter_watcher,parent,false);
            WatcherHolder holder = new WatcherHolder(itemView);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            if (holder instanceof  WatcherHolder){
                ((WatcherHolder)holder).bindData(watcherList.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return watcherList.size();
        }

        public void addWatcher(TIMUserProfile userProfile) {
            if (userProfile !=null){
                boolean inWatcher = watcherMap.containsKey(userProfile.getIdentifier());
                if (!inWatcher){
                    watcherList.add(userProfile);
                    watcherMap.put(userProfile.getIdentifier(),userProfile);
                    notifyDataSetChanged();
                }
            }
        }

        public void addWatchers(List<TIMUserProfile> userProfileList) {
            if (userProfileList ==null){
                return;
            }
            for (TIMUserProfile userProfile : userProfileList){
                if (userProfile != null){
                    boolean inWatcher = watcherMap.containsKey(userProfile.getIdentifier());
                    if (!inWatcher){
                        watcherList.add(userProfile);
                        watcherMap.put(userProfile.getIdentifier(),userProfile);
                    }
                }
            }
            notifyDataSetChanged();
        }

        public void removeWatcher(TIMUserProfile userProfile) {
            if (userProfile == null){
                return;
            }
            TIMUserProfile targetUser = watcherMap.get(userProfile.getIdentifier());
            if (targetUser != null){
                watcherList.remove(targetUser);
                watcherMap.remove(targetUser.getIdentifier());
                notifyDataSetChanged();
            }
        }

        private class WatcherHolder extends RecyclerView.ViewHolder{

            private ImageView avatarImag;
            public WatcherHolder(View itemView) {
                super(itemView);
                avatarImag = (ImageView) itemView.findViewById(R.id.user_avatar);
            }
            public void bindData(final TIMUserProfile userProfile){
                String avatarUrl = userProfile.getFaceUrl();
                if (TextUtils.isEmpty(avatarUrl)){
                    ImgUtils.loadRound(R.drawable.default_avatar,avatarImag);
                }else{
                    ImgUtils.loadRound(avatarUrl,avatarImag);
                }
                avatarImag.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //点击观众头像进入显示观众的详情信息Dialog
                        showUserInfoDialog(userProfile.getIdentifier());
                    }
                });
            }
        }
    }


}
