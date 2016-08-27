package com.example.bitunion.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.CookieSyncManager;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.bitunion.BUApp;
import com.example.bitunion.R;
import com.example.bitunion.model.BUUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

import javax.xml.transform.Result;

/**
 * Created by huolangzc on 2016/8/22.
 */
public class BUApi {

    private static final String TAG = BUApi.class.getSimpleName();

    private static BUUser sLoggedinUser;

    private static String mUsername;
    private static String mPassword;
    private static String mSession;

    private static final String SCHEME = "http";
    private static String domain;
    private static String rooturl;
    private static String baseurl;

    private static RequestQueue mApiQueue;
    private static Queue<ApiRequest> mRetryQueue = new LinkedList<ApiRequest>();
    private static boolean isRetrying;

    public enum Result {
        SUCCESS,
        FAILURE,
        NETWRONG,
        UNKNOWN;
    }

    private static class ApiRequest{
        String endpoint;
        Map<String, String> params;
        Response.Listener<JSONObject> responseListener;
        Response.ErrorListener errorListener;
    }

    public static Response.ErrorListener sErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Log.d(TAG, "Volley default error", volleyError);
        }
    };

    public static boolean hasValidUser(){
        return mUsername != null && mPassword != null;
    }

    public static  boolean isUserLoggedin(){
        return mSession != null && !mSession.isEmpty();
    }

    public static  String getSessionCookie(){
        return "sid = "+ mSession;
    }

    public static void tryInsetCookie() {
        final android.webkit.CookieManager cookieMngr = android.webkit.CookieManager.getInstance();
        cookieMngr.setCookie(domain, BUApi.getSessionCookie());
        new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cookieMngr.flush();
                } else {
                    CookieSyncManager.getInstance().sync();
                }
            }

        }.run();
    }

    public static void tryLogin(final String username, final String password,
                                @NonNull final Response.Listener<JSONObject> responseListener,
                                @NonNull final Response.ErrorListener errorListener){
        if(username == null || password == null)
            return;
        String path = baseurl + "/bu_logging.php";
        Map<String, String> params = new HashMap<>();
        params.put("action","login");
        params.put("username", username);
        params.put("password", password);
        isRetrying = true;
        httpPost(path, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                isRetrying = false;
                switch (BUApi.getResult(response)) {
                    case FAILURE:
                        ToastUtil.showToast(R.string.login_fail);
                        break;
                    case SUCCESS:
                        mSession = response.optString("session");
                        tryInsetCookie();
                        VolleyImageLoaderFactory.flush();
                        Log.v(TAG, "sid = " + mSession);

                        mUsername = username;
                        mPassword = password;
                        updateUser();
                        break;
                    case UNKNOWN:
                        ToastUtil.showToast(R.string.network_unknown);
                        break;
                }
                responseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                isRetrying = false;
                errorListener.onErrorResponse(volleyError);
            }
        });
    }

    public static void tryLogin(@NonNull Response.Listener<JSONObject> responseListener,
                                @NonNull Response.ErrorListener errorListener) {
        tryLogin(mUsername, mPassword, responseListener, errorListener);
    }

    public static void logoutUser(@NonNull final Response.Listener<JSONObject> responseListener,
                                  @NonNull Response.ErrorListener errorListener){
        if(mUsername == null || mPassword == null)
            return;
        String path = baseurl + "/bu_logging.php";
        Map<String, String> params = new HashMap<>();
        params.put("action", "logout");
        params.put("password", mPassword);
        appendUserCookie(params);
        httpPost(path, params, 1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(getResult(response) == Result.SUCCESS){
                    mUsername = null;
                    mPassword = null;
                    mSession = null;
                    saveUser(BUApp.getInstance());
                }
                responseListener.onResponse(response);
            }
        }, errorListener);
    }

    public static void postNewPost(int tid, String message, @Nullable File attachment,
                                   Response.Listener<JSONObject> responseListener,
                                   Response.ErrorListener errorListener){
        if(tid <= 0 || message == null || message.isEmpty())
            return;
        String path = baseurl + "/bu_newpost.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "newreply");
        params.put("tid", Integer.toString(tid));
        params.put("message", message);
        params.put("attachment", attachment==null ? "0":"1");
        appendUserCookie(params);
        if (attachment == null)
            httpPost(path, params, responseListener, errorListener);
    }

    public static void postNewThread(int fid, String title, String message, @Nullable File attachment,
                                     Response.Listener<JSONObject> responseListener,
                                     Response.ErrorListener errorListener) {
        if (fid < 0 || title == null || title.isEmpty() || message == null || message.isEmpty())
            return;
        String path = baseurl + "/bu_newpost.php";
        Map<String, String> params = new HashMap<>();
        params.put("action", "newthread");
        params.put("fid", Integer.toString(fid));
        params.put("subject", title);
        params.put("message", message);
        params.put("attachment", attachment==null ? "0":"1");
        appendUserCookie(params);
        if (attachment == null)
            httpPost(path, params, responseListener, errorListener);
    }

    public static void getUserProfile(int uid, Response.Listener<JSONObject> responseListener,
                                      Response.ErrorListener errorListener) {
        if (uid <= 0)
            return;
        String path = baseurl + "/bu_profile.php";
        Map<String, String> params = new HashMap<>();
        params.put("action", "profile");
        params.put("uid", Integer.toString(uid));
        appendUserCookie(params);
        httpPost(path, params, 1, responseListener, errorListener);
    }

    public static void getUserProfile(String userName, Response.Listener<JSONObject> responseListener,
                                      Response.ErrorListener errorListener) {
        if (userName == null)
            userName = mUsername;
        String path = baseurl + "/bu_profile.php";
        Map<String, String> params = new HashMap<>();
        params.put("action", "profile");
        params.put("queryusername", userName);
        appendUserCookie(params);
        httpPost(path, params, 1, responseListener, errorListener);
    }

    public static void readHomeThreads(Response.Listener<JSONObject> responseListener,
                                       Response.ErrorListener errorListener) {
        String path = baseurl + "/bu_home.php";
        Map<String, String> params = new HashMap<>();
        appendUserCookie(params);
        httpPost(path, params, responseListener, errorListener);
    }

    public static void readThreads(int fid, int from, int to,
                                   Response.Listener<JSONObject> responseListener,
                                   Response.ErrorListener errorListener) {
        if (from < 0 || to < 0 || from > to)
            return;
        String path = baseurl + "/bu_thread.php";
        Map<String, String> params = new HashMap<>();
        params.put("action", "thread");
        params.put("fid", Integer.toString(fid));
        params.put("from", Integer.toString(from));
        params.put("to", Integer.toString(to));
        appendUserCookie(params);
        httpPost(path, params, 1, responseListener, errorListener);
    }

    public static void readPostList(int tid, int from, int to,
                                    Response.Listener<JSONObject> responseListener,
                                    Response.ErrorListener errorListener) {
        if (from < 0 || to < 0 || from > to)
            return;
        String path = baseurl + "/bu_post.php";
        Map<String, String> params = new HashMap<>();
        params.put("action", "post");
        params.put("tid", Integer.toString(tid));
        params.put("from", Integer.toString(from));
        params.put("to", Integer.toString(to));
        appendUserCookie(params);
        httpPost(path, params, 1, responseListener, errorListener);
    }

    public static void checkTotalPosts(int tid, Response.Listener<JSONObject> responseListener,
                                       Response.ErrorListener errorListener) {
        if (tid <= 0)
            return;
        String path = baseurl + "/bu_fid_tid.php";
        Map<String, String> params = new HashMap<>();
        params.put("tid", Integer.toString(tid));
        appendUserCookie(params);
        httpPost(path, params, responseListener, errorListener);
    }

    public static void readForumsList( Response.Listener<JSONObject> responseListener,
                                      Response.ErrorListener errorListener) {

        String path = baseurl + "/bu_forum.php";
        Map<String, String> params = new HashMap<>();
        params.put("action", "forum");
        appendUserCookie(params);
        httpPost(path, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(TAG, "ForumsList" + response.toString());
            }
        }, errorListener);
    }

//    private static void httpPostMultipart(final String path, final Map<String, String> params,
//                                          final Response.Listener<JSONObject> responseListener,
//                                          final Response.ErrorListener errorListener) {
//
//    }



    private static void httpPost(final String path, final Map<String, String> params,
                                 final int retryLimit,
                                 final Response.Listener<JSONObject> responseListener,
                                 final Response.ErrorListener errorListener) {
        if(retryLimit <= 0)
            httpPost(path, params, responseListener, errorListener);
        else
            httpPost(path, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    switch (getResult(response)){
                        case SUCCESS:
                            responseListener.onResponse(response);
                            break;
                        default:
                        case FAILURE:
                            ApiRequest request = new ApiRequest();
                            request.endpoint = path;
                            request.params = params;
                            request.responseListener = responseListener;
                            request.errorListener = errorListener;
                            mRetryQueue.add(request);
                            if(isRetrying)
                                break;
                            tryLogin(new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    if(getResult(response) == Result.SUCCESS){
                                        while (mRetryQueue.peek() != null){
                                            ApiRequest request = mRetryQueue.poll();
                                            appendUserCookie(request.params);
                                            httpPost(request.endpoint, request.params, request.responseListener, request.errorListener);
                                        }
                                    }

                                }
                            },errorListener);
                        break;
                    }
                }
            },errorListener);

    }


    private static void httpPost(final String path, final Map<String, String> params,
                                 final Response.Listener<JSONObject> responseListener,
                                 final Response.ErrorListener errorListener){
        JSONObject postReq = new JSONObject();
        try {
            for(Map.Entry<String, String> entry : params.entrySet()){
            if(entry.getValue() == null)
                continue;
                postReq.put(entry.getKey(), URLEncoder.encode(entry.getValue(),"UTF-8"));
            }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        Log.d(TAG, "BUILD " + path + " >> " + postReq.toString());
        mApiQueue.add(new JsonObjectRequest(Request.Method.POST, path, postReq, responseListener, errorListener){
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                Log.v(TAG, path + " >> " + new String(response.data));
                return super.parseNetworkResponse(response);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                try {
                    Log.e(TAG, path + " >> " + volleyError.networkResponse.statusCode, volleyError);
                    Log.e(TAG, path + " >> " + new String(volleyError.networkResponse.data));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return super.parseNetworkError(volleyError);
            }
        });
    }

    public static String getTimeStr(JSONObject jsonObject, String name,
                                    String format) {
        try {
            String t = URLDecoder.decode(jsonObject.getString(name));
            return formatTime(t, format);
        } catch (JSONException e) {
            return "";
        }

    }

    private static String formatTime(String t, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.CHINA);
        return dateFormat.format(new Date(Long.valueOf(t) * 1000L));
    }

    public static Result getResult(JSONObject response){
        if("fail".equals(response.optString("result")))
            return Result.FAILURE;
        if("success".equals(response.optString("result")))
            return  Result.SUCCESS;
        return Result.UNKNOWN ;
    }

    private static void appendUserCookie(Map<String, String> params){
        params.put("username", mUsername);
        params.put("session", mSession);
    }

    public static  void saveUser(Context context){
        SharedPreferences config = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = config.edit();
        editor.putString("username", mUsername);
        editor.putString("password", mPassword);
        editor.apply();
    }

    public static void init(Context context) {
        SharedPreferences config = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        mUsername = config.getString("username", null);
        mPassword = config.getString("password", null);
        setNetType(BUApp.settings.netType);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            CookieSyncManager.createInstance(context);
        mApiQueue = Volley.newRequestQueue(context);
        sLoggedinUser = null;
    }

    public static void setNetType(int net) {
        if (net == Constants.BITNET)
            domain = "www.bitunion.org";
        else if (net == Constants.OUTNET)
            domain = "out.bitunion.org";
        rooturl = SCHEME + "://" + domain;
        baseurl = rooturl + "/open_api";
    }

    private static void updateUser() {
        getUserProfile(null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (getResult(response) == Result.SUCCESS)
                    sLoggedinUser = new BUUser(response.optJSONObject("memberinfo"));
            }
        }, sErrorListener);
    }

    public static BUUser getLoggedinUser() {
        return sLoggedinUser;
    }

    public static void clearUser() {
        SharedPreferences config = BUApp.getInstance()
                .getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = config.edit();
        BUApp.settings.netType = Constants.OUTNET;
        editor.putInt("nettype", Constants.OUTNET);
        editor.putString("username", null);
        editor.putString("password", null);
        editor.apply();
    }

    public static String getImageAbsoluteUrl(String shortUrl) {
        String path;
        path = shortUrl;
        path = path.replaceAll("(http://)?(www|v6|kiss|out).bitunion.org", rooturl);
        path = path.replaceAll("^images/", rooturl + "/images/");
        path = path.replaceAll("^attachments/", rooturl + "/attachments/");
        return path;
    }
    public static String getRootUrl() {
        return rooturl;
    }
}
