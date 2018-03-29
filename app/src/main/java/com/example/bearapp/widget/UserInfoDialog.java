package com.example.bearapp.widget;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bearapp.R;
import com.example.bearapp.editprofile.CustomProfile;
import com.example.bearapp.utils.ImgUtils;
import com.tencent.TIMUserProfile;

import java.util.Map;

/**
 * Created by zhengyg on 2018/3/19.
 */

public class UserInfoDialog extends TransParentDialog {

    private TIMUserProfile userInfo;

    private ImageView user_close;
    private ImageView user_avatar;
    private TextView user_name;
    private ImageView user_gender;
    private TextView user_level;
    private TextView user_id;
    private TextView user_renzhen;
    private TextView user_sign;
    private TextView user_songchu;
    private TextView user_bopiao;



    public UserInfoDialog(Activity activity,TIMUserProfile userInfo) {
        super(activity);
        this.userInfo = userInfo;

        View view  = LayoutInflater.from(activity).inflate(R.layout.dialog_userinfo,null);
        setContentView(view);
        findAllViews(view);
        bindDataToViews();

        setWidthAndHeight(activity.getWindow().getDecorView().getWidth()*8/100, WindowManager.LayoutParams.WRAP_CONTENT);

    }

    private void bindDataToViews() {
        String avatarUrl = userInfo.getFaceUrl();
        if (TextUtils.isEmpty(avatarUrl)){
            ImgUtils.loadRound(R.drawable.default_avatar,user_avatar);
        }else {
            ImgUtils.loadRound(avatarUrl,user_avatar);
        }

        String nickName = userInfo.getNickName();
        if (TextUtils.isDigitsOnly(nickName)){
            nickName = "用户";
        }
        user_name.setText(nickName);
        long genderValue = userInfo.getGender().getValue();
        user_gender.setImageResource(genderValue ==1 ? R.drawable.ic_male:R.drawable.ic_female);

        user_id.setText("ID:"+ userInfo.getIdentifier());
        String sign = userInfo.getSelfSignature();
        user_sign.setText(TextUtils.isEmpty(sign)? "ta 好像忘记签名了...":sign);

        Map<String ,byte[]> customInfo = userInfo.getCustomInfo();

        String rezhen = getValue(customInfo, CustomProfile.CUSTOM_RENZHENG,"未知");
        user_renzhen.setText(rezhen);

        int sendNum = Integer.valueOf(getValue(customInfo,CustomProfile.CUSTOM_SEND,"0"));
        user_songchu.setText("送出："+sendNum);
        int getNum = Integer.valueOf(getValue(customInfo,CustomProfile.CUSTOM_GET,"0"));
        user_bopiao.setText("播票："+ getNum);
        String level = getValue(customInfo,CustomProfile.CUSTOM_LEVEL,"0");

    }

    private String getValue(Map<String, byte[]> customInfo, String key, String defaultValue) {
        if (customInfo != null) {
            byte[] valueBytes = customInfo.get(key);
            if (valueBytes != null){
                return new String(valueBytes);
            }
        }
        return defaultValue;
    }

    private void findAllViews(View view) {
        user_close = (ImageView) view.findViewById(R.id.user_close);
        user_avatar = (ImageView) view.findViewById(R.id.user_avatar);
        user_name = (TextView) view.findViewById(R.id.user_name);
        user_gender = (ImageView) view.findViewById(R.id.user_gender);
        user_level = (TextView) view.findViewById(R.id.user_level);
        user_id = (TextView) view.findViewById(R.id.user_id);
        user_renzhen = (TextView) view.findViewById(R.id.user_renzhen);
        user_sign = (TextView) view.findViewById(R.id.user_sign);
        user_songchu = (TextView) view.findViewById(R.id.user_songchu);
        user_bopiao = (TextView) view.findViewById(R.id.user_bopiao);

        user_close.setOnClickListener(clickListener);
        user_avatar.setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            if (v == user_close){
                hideDialog();
            }
        }
    };

    private void hideDialog() {
        hide();
    }
}
