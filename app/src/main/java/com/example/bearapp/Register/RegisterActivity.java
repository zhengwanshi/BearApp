package com.example.bearapp.Register;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bearapp.BearApplication;
import com.example.bearapp.R;
import com.example.bearapp.editprofile.EditProfileActivity;
import com.tencent.TIMFriendshipManager;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveLoginManager;

/**
 * Created by zhengyg on 2018/3/11.
 */

public class RegisterActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText mAccountEdt;
    private EditText mPasswordEdt;
    private EditText mConfirmPasswordEdt;

    private Button mRegisterBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findAllView();
        setListeners();
        setTitleBar();
    }

    private void setTitleBar() {
        mToolbar.setTitle("注册新用户");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
    }

    private void setListeners() {
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register() {
        String accountStr = mAccountEdt.getText().toString();
        String passwordStr = mPasswordEdt.getText().toString();
        String confirmPswStr = mConfirmPasswordEdt.getText().toString();

        if (TextUtils.isEmpty(accountStr) ||TextUtils.isEmpty(passwordStr)||
                TextUtils.isEmpty(confirmPswStr)){
            Toast.makeText(this, "账号或密码不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!passwordStr.equals(confirmPswStr)){
            Toast.makeText(this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        ILiveLoginManager.getInstance().tlsRegister(accountStr, passwordStr, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                //注册成功

                Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
               login();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
            //注册失败
                Toast.makeText(RegisterActivity.this, "注册失败"+errCode, Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void login() {
        final String accountStr = mAccountEdt.getText().toString();
        String passwordStr = mPasswordEdt.getText().toString();

        //调用腾讯IM登录
        //调用腾讯IM登录
        ILiveLoginManager.getInstance().tlsLogin(accountStr, passwordStr, new ILiveCallBack<String>() {
            @Override
            public void onSuccess(String data) {
                //登陆成功。
                Toast.makeText(RegisterActivity.this, "tls登录成功", Toast.LENGTH_SHORT).show();
                loginLive(accountStr, data);

//                Intent intent = new Intent(RegisterActivity.this, EditProfileActivity.class);
//                startActivity(intent);
//
//                getSelfInfo();
//                finish();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                //登录失败
                Toast.makeText(RegisterActivity.this, "tls登录失败：" + errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginLive(String accountStr, String data) {
        ILiveLoginManager.getInstance().iLiveLogin(accountStr, data, new ILiveCallBack() {

            @Override
            public void onSuccess(Object data) {
                //最终登录成功
                Toast.makeText(RegisterActivity.this, "iLive登录成功" , Toast.LENGTH_SHORT).show();
                //跳转到修改用户信息界面。
                Intent intent = new Intent();
                intent.setClass(RegisterActivity.this,EditProfileActivity.class);
                startActivity(intent);

                getSelfInfo();
                finish();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                //登录失败
                Toast.makeText(RegisterActivity.this, "iLive登录失败：" + errCode, Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void getSelfInfo() {
        TIMFriendshipManager.getInstance().getSelfProfile(new TIMValueCallBack<TIMUserProfile>() {
            @Override
            public void onError(int i, String s) {
                Toast.makeText(RegisterActivity.this, "获取信息失败：" + i, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(TIMUserProfile timUserProfile) {
                //获取自己信息成功
                BearApplication.getApplication().setSelfProfile(timUserProfile);
                Toast.makeText(RegisterActivity.this, "获取信息成功" , Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void findAllView() {
        mToolbar = (Toolbar) findViewById(R.id.titlebar);
        mAccountEdt = (EditText) findViewById(R.id.account);
        mPasswordEdt = (EditText) findViewById(R.id.password);
        mConfirmPasswordEdt = (EditText) findViewById(R.id.confirm_password);
        mRegisterBtn = (Button) findViewById(R.id.register);
    }
}
