package com.example.bearapp.editprofile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.bearapp.R;
import com.example.bearapp.main.MainActivity;
import com.example.bearapp.utils.ImgUtils;
import com.example.bearapp.utils.LogUtil;
import com.example.bearapp.utils.PicChooserHelper;
import com.tencent.TIMCallBack;
import com.tencent.TIMFriendGenderType;
import com.tencent.TIMFriendshipManager;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;

import java.util.Map;

/**
 * Created by zhengyg on 2018/3/12.
 */

public class EditProfileFragment extends android.support.v4.app.Fragment {


    private Toolbar mTitlebar;
    private View mAvatarView;
    private ImageView mAvatarImg;
    private ProfileEdit mNickNameEdt;
    private ProfileEdit mGenderEdt;
    private ProfileEdit mSignEdt;
    private ProfileEdit mRenzhengEdt;
    private ProfileEdit mLocationEdt;
    private ProfileTextView mIdView;
    private ProfileTextView mLevelView;
    private ProfileTextView mGetNumsView;
    private ProfileTextView mSendNumsView;
    private Button mCompleteBtn;
    private PicChooserHelper mPicChooserHelper;
    private TIMUserProfile mUserProfile;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_edit_profile,container,false);
        findAllViews(mainView);
        setListeners();
        setTitleBar();
        setIconKey();
        getSelfInfo();
        return mainView;
    }

    private void setIconKey() {
        mNickNameEdt.set(R.drawable.ic_info_nickname,"昵称","");
        mGenderEdt.set(R.drawable.ic_info_gender, "性别", "");
        mSignEdt.set(R.drawable.ic_info_sign, "签名", "无");
        mRenzhengEdt.set(R.drawable.ic_info_renzhen, "认证", "未知");
        mLocationEdt.set(R.drawable.ic_info_location, "地区", "未知");
        mIdView.set(R.drawable.ic_info_id, "ID", "");
        mLevelView.set(R.drawable.ic_info_level, "等级", "0");
        mGetNumsView.set(R.drawable.ic_info_get, "获得票数", "0");
        mSendNumsView.set(R.drawable.ic_info_send, "送出票数", "0");
    }

    private void setTitleBar() {
        mTitlebar.setTitle("编辑个人信息");
        mTitlebar.setTitleTextColor(Color.WHITE);
        Activity activity = getActivity();
        if (activity instanceof AppCompatActivity){
            ((AppCompatActivity) activity).setSupportActionBar(mTitlebar);
        }
    }

    private void setListeners() {
        mAvatarView.setOnClickListener(clickListener);
        mNickNameEdt.setOnClickListener(clickListener);
        mGenderEdt.setOnClickListener(clickListener);
        mSignEdt.setOnClickListener(clickListener);
        mRenzhengEdt.setOnClickListener(clickListener);
        mLocationEdt.setOnClickListener(clickListener);
        mCompleteBtn.setOnClickListener(clickListener);

    }
    private View.OnClickListener clickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id){
                case R.id.avatar:
                    //修改头像
                    LogUtil.e("修改头像");
                    choosePic();
                    break;
                case R.id.nick_name:
                    //修改昵称
                    showEditNickNameDialog();
                    break;
                case R.id.gender:
                    //修改性别
                    showEditGenderDialog();
                    break;
                case R.id.sign:
                    //修改签名
                    showEditSignDialog();
                    break;
                case R.id.renzheng:
                    //修改认证
                    showEditRenzhengDialog();
                    break;
                case R.id.location:
                    //修改位置
                    showEditLocationDialog();
                    break;
                case R.id.complete:
                    //完成，跳转到主页面
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), MainActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    private void showEditLocationDialog() {
        EditStrProfileDialog dialog = new EditStrProfileDialog(getActivity());
        dialog.setOnOKListener(new EditStrProfileDialog.OnOKListener() {
            @Override
            public void onOk(String title, final String content) {
                TIMFriendshipManager.getInstance().setLocation(content, new TIMCallBack() {
                    @Override
                    public void onError(int i, String s) {
                        Toast.makeText(getActivity(), "更新地区失败：" + s, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess() {
                        //更新成功
                        getSelfInfo();
                    }
                });
            }
        });
        dialog.show("地区", R.drawable.ic_info_location, mLocationEdt.getValue());
    }

    private void showEditRenzhengDialog() {
        EditStrProfileDialog dialog = new EditStrProfileDialog(getActivity());
        dialog.setOnOKListener(new EditStrProfileDialog.OnOKListener() {
            @Override
            public void onOk(String title, final String content) {
                TIMFriendshipManager.getInstance().setCustomInfo(CustomProfile.CUSTOM_RENZHENG, content.getBytes(), new TIMCallBack() {
                    @Override
                    public void onError(int i, String s) {
                        Toast.makeText(getActivity(), "更新认证失败：" + s, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess() {
                        //更新成功
                        getSelfInfo();
                    }
                });
            }
        });
        dialog.show("认证", R.drawable.ic_info_renzhen, mRenzhengEdt.getValue());
    }

    private void showEditSignDialog() {
        EditStrProfileDialog dialog = new EditStrProfileDialog(getActivity());
        dialog.setOnOKListener(new EditStrProfileDialog.OnOKListener() {
            @Override
            public void onOk(String title, final String content) {
                TIMFriendshipManager.getInstance().setSelfSignature(content, new TIMCallBack() {
                    @Override
                    public void onError(int i, String s) {
                        Toast.makeText(getActivity(), "更新签名失败：" + s, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess() {
                        //更新成功
                        getSelfInfo();
                    }
                });
            }
        });
        dialog.show("签名", R.drawable.ic_info_sign, mSignEdt.getValue());
    }

    private void showEditGenderDialog() {
        EditGenderDialog dialog = new EditGenderDialog(getActivity());
        dialog.setOnChangeGenderListener(new EditGenderDialog.OnChangeGenderListener() {
            @Override
            public void onChangeGender(boolean isMale) {
                TIMFriendGenderType gender = isMale ? TIMFriendGenderType.Male : TIMFriendGenderType.Female;
                TIMFriendshipManager.getInstance().setGender(gender, new TIMCallBack() {

                    @Override
                    public void onError(int i, String s) {
                        Toast.makeText(getActivity(), "更新性别失败：" + i, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess() {
                        //更新成功
                        getSelfInfo();
                    }
                });
            }
        });
        dialog.show(mGenderEdt.getValue().equals("男"));
    }

    private void showEditNickNameDialog() {
        EditStrProfileDialog dialog = new EditStrProfileDialog(getActivity());
        dialog.setOnOKListener(new EditStrProfileDialog.OnOKListener() {
            @Override
            public void onOk(String title, String content) {
                TIMFriendshipManager.getInstance().setNickName(content, new TIMCallBack() {
                    @Override
                    public void onError(int i, String s) {
                        Toast.makeText(getActivity(), "更新昵称失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess() {
                        //更新成功
                        getSelfInfo();
                    }
                });
            }
        });
        dialog.show("昵称",R.drawable.ic_info_nickname,mNickNameEdt.getValue());
    }

    private void choosePic() {
        LogUtil.e("选择图片");
        if (mPicChooserHelper == null){
            LogUtil.e("");
            mPicChooserHelper = new PicChooserHelper(this,PicChooserHelper.PicType.Avatar);
            mPicChooserHelper.setOnChooseResultListener(new PicChooserHelper.OnChooseResultListener() {
                @Override
                public void onSuccess(String url) {
                    updateAvatar(url);
                }

                @Override
                public void onFail(String msg) {

                }
            });
        }
        mPicChooserHelper.showPicChooserDialog();
    }

    private void updateAvatar(String url) {
        TIMFriendshipManager.getInstance().setFaceUrl(url, new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                Toast.makeText(getActivity(), "头像更新失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                //头像更新成功
                getSelfInfo();

            }
        });
    }

    private void findAllViews(View mainView) {
        mTitlebar = (Toolbar) mainView.findViewById(R.id.title_bar);

        mAvatarView = mainView.findViewById(R.id.avatar);
        mAvatarImg = (ImageView) mainView.findViewById(R.id.avatar_img);

        mNickNameEdt = (ProfileEdit) mainView.findViewById(R.id.nick_name);
        mGenderEdt = (ProfileEdit) mainView.findViewById(R.id.gender);
        mSignEdt = (ProfileEdit) mainView.findViewById(R.id.sign);
        mRenzhengEdt = (ProfileEdit) mainView.findViewById(R.id.renzheng);
        mLocationEdt = (ProfileEdit) mainView.findViewById(R.id.location);

        mIdView = (ProfileTextView) mainView.findViewById(R.id.id);
        mLevelView = (ProfileTextView) mainView.findViewById(R.id.level);
        mGetNumsView = (ProfileTextView) mainView.findViewById(R.id.get_nums);
        mSendNumsView = (ProfileTextView) mainView.findViewById(R.id.send_nums);

        mCompleteBtn = (Button) mainView.findViewById(R.id.complete);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (mPicChooserHelper != null) {
            mPicChooserHelper.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void getSelfInfo() {
        TIMFriendshipManager.getInstance().getSelfProfile(new TIMValueCallBack<TIMUserProfile>() {
            @Override
            public void onError(int i, String s) {
                Toast.makeText(getActivity(), "获取信息失败："+i, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(TIMUserProfile timUserProfile) {
                //获取自己信息成功
                mUserProfile= timUserProfile;
                updateViews(timUserProfile);
            }
        });
    }

    private void updateViews(TIMUserProfile timUserProfile) {
        //更新界面
        String faceUrl = timUserProfile.getFaceUrl();
        if (TextUtils.isEmpty(faceUrl)) {
            ImgUtils.loadRound(R.drawable.default_avatar, mAvatarImg);
        } else {
            ImgUtils.loadRound(faceUrl, mAvatarImg);
        }
        mNickNameEdt.updateValue(timUserProfile.getNickName());
        long genderValue = timUserProfile.getGender().getValue();
        String genderStr = genderValue == 1 ? "男" : "女";
        mGenderEdt.updateValue(genderStr);
        mSignEdt.updateValue(timUserProfile.getSelfSignature());
        mLocationEdt.updateValue(timUserProfile.getLocation());
        mIdView.updateValue(timUserProfile.getIdentifier());

        Map<String, byte[]> customInfo = timUserProfile.getCustomInfo();
        mRenzhengEdt.updateValue(getValue(customInfo, CustomProfile.CUSTOM_RENZHENG, "未知"));
        mLevelView.updateValue(getValue(customInfo, CustomProfile.CUSTOM_LEVEL, "0"));
        mGetNumsView.updateValue(getValue(customInfo, CustomProfile.CUSTOM_GET, "0"));
        mSendNumsView.updateValue(getValue(customInfo, CustomProfile.CUSTOM_SEND, "0"));
    }

    private String getValue(Map<String, byte[]> customInfo, String key, String defaultValue) {
        if (customInfo != null){
            byte[] valueBytes = customInfo.get(key);
            if (valueBytes !=null){
                return new String(valueBytes);
            }
        }
        return defaultValue;
    }
}
