package com.example.bearapp.createroom;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bearapp.BearApplication;
import com.example.bearapp.R;
import com.example.bearapp.hostlive.HostLiveActivity;
import com.example.bearapp.model.RoomInfo;
import com.example.bearapp.utils.ImgUtils;
import com.example.bearapp.utils.LogUtil;
import com.example.bearapp.utils.PicChooserHelper;
import com.example.bearapp.utils.request.BaseRequest;
import com.tencent.TIMUserProfile;

public class CreateLiveActivity extends AppCompatActivity {
    private View mSetCoverView;
    private ImageView mCoverImg;
    private TextView mCoverTipTxt;
    private EditText mTitleEt;
    private TextView mCreateRoomBtn;
    private TextView mRoomNoText;
    private PicChooserHelper mPicChooserHelper;
    private String coverUrl=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_live);
        findAllViews();
        setListeners();
        setupTitlebar();
    }

    private void setupTitlebar() {
        Toolbar titlebar = (Toolbar) findViewById(R.id.titlebar);
        titlebar.setTitle("开始我的直播");
        titlebar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(titlebar);
    }

    private void setListeners() {
        mSetCoverView.setOnClickListener(clickListener);
        mCreateRoomBtn.setOnClickListener(clickListener);
    }

    private void findAllViews() {
        mSetCoverView = findViewById(R.id.set_cover);
        mCoverImg = (ImageView) findViewById(R.id.cover);
        mCoverTipTxt = (TextView) findViewById(R.id.tv_pic_tip);
        mTitleEt = (EditText) findViewById(R.id.title);
        mCreateRoomBtn = (TextView) findViewById(R.id.create);
        mRoomNoText = (TextView) findViewById(R.id.room_no);
    }

    private View.OnClickListener clickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.create) {
                //创建直播
                requestCreateRoom();
            } else if (id == R.id.set_cover) {
                //选择图片
                choosePic();
            }
        }
    };

    private void choosePic() {
        if (mPicChooserHelper == null) {
            mPicChooserHelper = new PicChooserHelper(this, PicChooserHelper.PicType.Cover);
            mPicChooserHelper.setOnChooseResultListener(new PicChooserHelper.OnChooseResultListener() {
                @Override
                public void onSuccess(String url) {
                    //获取图片成功
                    Toast.makeText(CreateLiveActivity.this, "选择成功：", Toast.LENGTH_SHORT).show();

                    updateCover(url);
                }

                @Override
                public void onFail(String msg) {
                    //获取图片失败
                    Toast.makeText(CreateLiveActivity.this, "选择失败：" + msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
        mPicChooserHelper.showPicChooserDialog();
    }

    private void updateCover(String url) {
        coverUrl = url;
        ImgUtils.load(url, mCoverImg);
        mCoverTipTxt.setVisibility(View.GONE);
    }

    private void requestCreateRoom() {
        CreateRoomResquest.CreateRoomParam param = new CreateRoomResquest.CreateRoomParam();
        TIMUserProfile selfProfile = BearApplication.getApplication().getSelfProfile();
        if (selfProfile==null){
            Toast.makeText(this, "空指针", Toast.LENGTH_SHORT).show();
        }
        param.userId = selfProfile.getIdentifier();
        param.userAvatar = selfProfile.getFaceUrl();
        String nickName = selfProfile.getNickName();

        param.userName =TextUtils.isEmpty(nickName)? selfProfile.getIdentifier() : nickName;
        param.liveTitle = mTitleEt.getText().toString();
        param.liveCover = coverUrl;

        //创建房间
        CreateRoomResquest resquest = new CreateRoomResquest();
        resquest.setOnResultListener(new BaseRequest.OnResultListener<RoomInfo>() {
            @Override
            public void onFail(int code, String msg) {
                LogUtil.e("请求失败。");
                Toast.makeText(CreateLiveActivity.this, "请求失败："+msg, Toast.LENGTH_SHORT).show();
            }

            @Override
           public void onSuccess(RoomInfo roomInfo) {
                LogUtil.e("请求成功");
                Toast.makeText(CreateLiveActivity.this, "请求成功", Toast.LENGTH_SHORT).show();
                Intent intent= new Intent(CreateLiveActivity.this,HostLiveActivity.class);
                intent.putExtra("roomId",roomInfo.roomId);
                startActivity(intent);
                finish();
            }
        });
        String requestUrl = resquest.getUrl(param);
        resquest.request(requestUrl);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mPicChooserHelper!=null){
            mPicChooserHelper.onActivityResult(requestCode,resultCode,data);
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
