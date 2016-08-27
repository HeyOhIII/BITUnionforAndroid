package com.example.bitunion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.bitunion.util.BUApi;
import com.example.bitunion.util.Constants;
import com.example.bitunion.util.ToastUtil;

import org.json.JSONObject;

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

//        getSupportActionBar().setTitle("登录");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        readConfig();
        mUsernameView = (EditText)findViewById(R.id.username);
        mPasswordView = (EditText)findViewById(R.id.password);

        netGroup = (RadioGroup)findViewById(R.id.radioGroup);
        netGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.radio_in){
                    BUApp.settings.netType = Constants.BITNET;
                }else if(i == R.id.radio_out){
                    BUApp.settings.netType = Constants.OUTNET;
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
            BUApi.tryLogin(mUsername, mPassword, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (BUApi.getResult(response) == BUApi.Result.SUCCESS) {
                        ToastUtil.showToast(R.string.login_success);
                        saveConfig();
                        setResult(RESULT_OK, null);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
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

    public void saveConfig() {
        BUApp.settings.readPreference(this);
        BUApi.saveUser(this);
    }

    public void readConfig() {
        BUApp.settings.readPreference(this);
        BUApi.init(this);
    }
}
