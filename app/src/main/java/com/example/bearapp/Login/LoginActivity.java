package com.example.bearapp.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bearapp.BearApplication;
import com.example.bearapp.R;
import com.example.bearapp.Register.RegisterActivity;
import com.example.bearapp.main.MainActivity;
import com.tencent.TIMFriendshipManager;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveLoginManager;

public class LoginActivity extends AppCompatActivity {
    private EditText mAccountEdt;
    private EditText mPasswordEdt;
    private Button mLoginBtn;
    private Button mRegisterBtn;
    private CheckBox rememberPass;
    private static final String TAG = "LoginActivity";
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;
    private String account;
    private String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findAllViews();
        init();
        setListeners();
    }

    private void init() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRemember = pref.getBoolean("remember_password",false);
        if (isRemember){
            //将账号和密码设置出来
            Toast.makeText(this, "记录了上次密码", Toast.LENGTH_SHORT).show();
             account = pref.getString("account","");
            password = pref.getString("password","");
            mAccountEdt.setText(account);
            mPasswordEdt.setText(password);
            rememberPass.setChecked(true);
        }
    }

    private void setListeners() {
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register() {
        //注册新用户，跳转到注册页面
        Intent intent = new Intent();
        intent.setClass(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    private void login() {

        final String accountStr =mAccountEdt.getText().toString();
        final String passwordStr = mPasswordEdt.getText().toString();

        if (TextUtils.isEmpty(accountStr) || TextUtils.isEmpty(passwordStr)){
            Toast.makeText(this, "用户账号或密码为空！", Toast.LENGTH_SHORT).show();
            return;
        }

        //调用腾讯IM登录
        ILiveLoginManager.getInstance().tlsLogin(accountStr, passwordStr, new ILiveCallBack<String>() {
            @Override
            public void onSuccess(String data) {
                //登陆成功。
                Toast.makeText(LoginActivity.this, "tls登录成功", Toast.LENGTH_SHORT).show();
                mEditor = pref.edit();
                if (rememberPass.isChecked()){
                    mEditor.putBoolean("remember_password",true);
                    mEditor.putString("account",accountStr);
                    mEditor.putString("password",passwordStr);
                }else{
                    mEditor.clear();
                }
                mEditor.apply();
                loginLive(accountStr, data);

//                Intent intent= new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(intent);
//
//                getSelfInfo();
//                finish();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                //登录失败
                Toast.makeText(LoginActivity.this, "tls登录失败：" + errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginLive(String accountStr, String data) {
        Log.e(TAG, "loginLive: " );

        ILiveLoginManager.getInstance().iLiveLogin(accountStr, data, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                //最终登录成功
                Toast.makeText(LoginActivity.this, "iLive登录成功！", Toast.LENGTH_SHORT).show();
                Intent intent= new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

                getSelfInfo();
                finish();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Toast.makeText(LoginActivity.this, "iLive登录失败"+errCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getSelfInfo() {
        TIMFriendshipManager.getInstance().getSelfProfile(new TIMValueCallBack<TIMUserProfile>() {
            @Override
            public void onError(int i, String s) {
                Toast.makeText(LoginActivity.this, "获取信息失败"+i, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(TIMUserProfile timUserProfile) {

                //获取信息成功
                Toast.makeText(LoginActivity.this, "获取信息成功", Toast.LENGTH_SHORT).show();
                BearApplication.getApplication().setSelfProfile(timUserProfile);
            }
        });
    }

    private void findAllViews() {
        mAccountEdt = (EditText) findViewById(R.id.account);
        mPasswordEdt = (EditText) findViewById(R.id.password);
        mLoginBtn  = (Button) findViewById(R.id.login);
        mRegisterBtn = (Button) findViewById(R.id.register);
        rememberPass = (CheckBox) findViewById(R.id.remember_pass);



    }
}
