package com.example.bitunion;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

/**
 * Created by huolangzc on 2016/7/18.
 */
public class LoginActivity extends AppCompatActivity {

    private String mUsername;
    private String mPassword;

    private EditText mUsernameView;
    private EditText mPasswordView;
    private RadioGroup netGroup;
    private Button signIn;
    private ProgressDialog progressDialog = null;
    android.app.ActionBar actionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //actionBar = getActionBar();
        // actionBar.hide();

        mUsernameView = (EditText)findViewById(R.id.username);
        mPasswordView = (EditText)findViewById(R.id.password);

        netGroup = (RadioGroup)findViewById(R.id.radioGroup);
        netGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.radio_in){
                    //选择校内网
                }else if(i == R.id.radio_out){
                    //选择外网
                }
            }
        });

        progressDialog = new ProgressDialog(this,  R.style.ProgressDialog);
        signIn = (Button)findViewById(R.id.sign_in_button);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    public void attemptLogin(){

        mUsernameView.setError(null);
        mPasswordView.setError(null);

        mUsername = mUsernameView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancle = false;
        View focusView = null;

        if(TextUtils.isEmpty(mUsername)){
            mUsernameView.setError("内容不能为空");
            focusView = mUsernameView;
            cancle = true;
        }

        if(TextUtils.isEmpty(mPassword)){
            mPasswordView.setError("内容不能为空");
            focusView = mPasswordView;
            cancle = true;
        }

        if(cancle){
            focusView.requestFocus();
        }else{
            showProgress(true);

            //请求登录


        }

    }

    private void showProgress(final boolean show){
        if(progressDialog == null){
            return;
        }
        if(show && !progressDialog.isShowing()){
            progressDialog.show();
        }else if(!show && progressDialog.isShowing()){
            progressDialog.dismiss();
        }

    }
}
