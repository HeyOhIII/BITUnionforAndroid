package com.example.bitunion;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.bitunion.model.BUUser;
import com.example.bitunion.util.BUApi;

import org.json.JSONObject;


public class MyinfoActivity extends AppCompatActivity  {

    private ImageView mAvatar;
    private TextView mUsername;
    private TextView mGroup;
    private TextView mCredit;
    private TextView mThreadnum;
    private TextView mPostnum;
    private TextView mRegdate;
    private TextView mLastactive;
    private TextView mEmail;
    private TextView mSignt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);

        // Show the Up button in the action bar.
        setupActionBar();

        if (savedInstanceState != null && !savedInstanceState.isEmpty())
            return;

        mAvatar = (ImageView) findViewById(R.id.myinfo_avatar);
        mUsername = (TextView) findViewById(R.id.myinfo_username);
        mGroup = (TextView) findViewById(R.id.myinfo_group);
        mCredit = (TextView) findViewById(R.id.myinfo_credit);
        mThreadnum = (TextView) findViewById(R.id.myinfo_threadnum);
        mPostnum = (TextView) findViewById(R.id.myinfo_postnum);
        mRegdate = (TextView) findViewById(R.id.myinfo_regdate);
        mLastactive = (TextView) findViewById(R.id.myinfo_lastvisit);
        mEmail = (TextView) findViewById(R.id.myinfo_email);
        mSignt = (TextView) findViewById(R.id.myinfo_signature);


        if (BUApi.isUserLoggedin()) {
            setInfoContent(BUApi.getLoggedinUser());
            readUserInfo();
        } else {
            BUApi.tryLogin(new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (BUApi.getResult(response) == BUApi.Result.SUCCESS)
                        readUserInfo();
                    else
                        showToast("登录异常！");
                }
            }, BUApi.sErrorListener);
        }
    }

    private void setInfoContent(BUUser info) {
        if (info == null)
            return;
        mUsername.setText(info.getUsername());
        mGroup.setText("用户组：" + info.getStatus());
        mCredit.setText("积分：" + info.getCredit());
        mThreadnum.setText("主题数：" + info.getThreadnum());
        mPostnum.setText("发帖数：" + info.getPostnum());
        mRegdate.setText("注册日期：" + info.getRegdate());
        mLastactive.setText("上次登录：" + info.getLastvisit());
        mEmail.setText("E-mail：" + info.getEmail());
        mSignt.setText(Html.fromHtml("签名：<br>" + info.getSignature(), null, null));
        mSignt.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {
        getSupportActionBar().setTitle("我的联盟");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private void readUserInfo() {
        BUApi.getUserProfile(null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (BUApi.getResult(response) == BUApi.Result.SUCCESS && !response.isNull("memberinfo")) {
                    BUUser info = new BUUser(response.optJSONObject("memberinfo"));
                    setInfoContent(info);
                } else
                    showToast("Server Error: " + response.toString());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showToast(getString(R.string.network_unknown));
            }
        });
    }


    private void showToast(String text) {
        Toast.makeText(MyinfoActivity.this, text, Toast.LENGTH_SHORT).show();
    }

}
