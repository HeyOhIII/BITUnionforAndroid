package com.example.bitunion;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.example.bitunion.util.BUApi;
import com.example.bitunion.util.CommonIntents;
import com.example.bitunion.util.ToastUtil;

import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.File;

/**
 * Created by huolangzc on 2016/8/23.
 */
public class NewthreadActivity extends AppCompatActivity implements TextWatcher{

    public static final String ACTION_NEW_POST = "NewthreadActivity.ACTION_NEW_POST";
    public static final String ACTION_NEW_THREAD = "NewthreadActivity.ACTION_NEW_THREAD ";

    private EditText mSubET;
    private EditText mMsgET;
    private MenuItem mSendIc;

    private String title;
    private String action;
    private int fid,tid;
    private File mAttachFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newthread);

        mSubET = (EditText) findViewById(R.id.newthread_subject);
        mMsgET = (EditText) findViewById(R.id.newthread_message);

        Intent intent = getIntent();
        action = intent.getStringExtra(CommonIntents.EXTRA_ACTION);
        if(action.equals(ACTION_NEW_THREAD)){
            fid = intent.getIntExtra(CommonIntents.EXTRA_FID, 0);
            title = intent.getStringExtra(CommonIntents.EXTRA_FORUM_NAME) + " - 发新话题";
        }else if(action.equals(ACTION_NEW_POST)){
            mSubET.setEnabled(false);
            tid = intent.getIntExtra(CommonIntents.EXTRA_TID, 0);
            String m = intent.getStringExtra(CommonIntents.EXTRA_MESSAGE);
            mMsgET.setText(m);
            mMsgET.setSelection(mMsgET.getText().toString().length());
            title = "tid=" + tid + " - 高级回复";
        }

        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSubET.addTextChangedListener(this);
        mMsgET.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (mSendIc == null)
            return;
        if (ACTION_NEW_POST.equals(action)) {
            mSendIc.setEnabled(!mMsgET.getText().toString().trim().isEmpty());
        } else if (ACTION_NEW_THREAD.equals(action)) {
            mSendIc.setEnabled(!mSubET.getText().toString().isEmpty() && !mMsgET.getText().toString().trim().isEmpty());
        }
    }

    private void sendMessage(String subject, String message) {
        if (message == null || message.length() < 5) {
            mMsgET.setError("内容长度不能小于5");
            return;
        }
//        if (BUApp.settings.showSignature)
//            message += getString(R.string.buapp_client_postfix).replace("$device_name", Devices.getDeviceName());
        mSendIc.setEnabled(false);
        ToastUtil.showToast(R.string.message_sending);
        BUApi.postNewThread(fid, subject, message, mAttachFile, mResponseListener, mErrorListener);
    }

    private void sendMessage(String message) {
//        if (BUApp.settings.showSignature)
//            message += getString(R.string.buapp_client_postfix).replace("$device_name", Devices.getDeviceName());
        mSendIc.setEnabled(false);
        ToastUtil.showToast(R.string.message_sending);
        BUApi.postNewPost(tid, message, mAttachFile, mResponseListener, mErrorListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.newthread, menu);
        mSendIc = menu.findItem(R.id.action_send);
        mSendIc.setTitle(R.string.menu_action_post);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_send:
                if (ACTION_NEW_THREAD.equals(action))
                    sendMessage(mSubET.getText().toString(), mMsgET.getText().toString());
                else
                    sendMessage(mMsgET.getText().toString());
        }
        return true;
    }

    private Response.Listener<JSONObject> mResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if (BUApi.getResult(response) == BUApi.Result.SUCCESS) {
                ToastUtil.showToast(R.string.message_sent_success);
                finish();
            } else {
                ToastUtil.showToast(R.string.message_sent_fail);
            }
        }
    };

    private Response.ErrorListener mErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            ToastUtil.showToast(R.string.network_unknown);
        }
    };

}
