package com.example.bitunion.model;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by huolangzc on 2016/8/9.
 */
public class BUThread extends BUContent{

    private int tid;
    private String author;
    private String authorid;
    private String subject;
    private int dateline;
    private String lastpost;
    private String lastposter;
    private int views;
    private int replies;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(tid);
        parcel.writeString(author);
        parcel.writeString(authorid);
        parcel.writeString(subject);
        parcel.writeInt(dateline);
        parcel.writeString(lastpost);
        parcel.writeString(lastposter);
        parcel.writeInt(views);
        parcel.writeInt(replies);

    }

    public static final Creator<BUThread> CREATOR = new Creator<BUThread>() {
        @Override
        public BUThread createFromParcel(Parcel parcel) {
            return new BUThread(parcel);
        }

        @Override
        public BUThread[] newArray(int i) {
            return new BUThread[i];
        }
    };

    public BUThread(Parcel in){
        tid = in.readInt();
        author = in.readString();
        authorid = in.readString();
        subject = in.readString();
        dateline = in.readInt();
        lastpost = in.readString();
        lastposter = in.readString();
        views = in.readInt();
        replies = in.readInt();
    }

    public BUThread(JSONObject object) throws JSONException {
        try {
            tid = object.getInt("tid");
            author = URLDecoder.decode(object.getString("author"), "utf-8");
            authorid = object.getString("authorid");
            subject = URLDecoder.decode(object.getString("subject"), "utf-8");
            dateline = object.getInt("dateline");
            lastpost = object.getString("lastpost");
            lastposter = object.getString("lastposter");
            views = object.getInt("views");
            replies = object.getInt("replies");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        subject = subject.replaceAll("<[^>]+>", "");
       // subject = Utils.replaceHtmlChar(subject);
    }

    public int getTid() {
        return tid;
    }

    public String getAuthor() {
        return author;
    }

    public String getAuthorid() {
        return authorid;
    }

    public String getSubject() {
        return subject;
    }

    public String getDateline() {
        Date date = new Date(dateline * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    public String getViewsDisplay() {
        int n = views;
        if (n > 9999){
            return Integer.toString(n / 10000) + "." + Integer.toString(n % 10000 / 1000) + "万";
        } else
            return Integer.toString(views);
    }

    public String getRepliesDisplay() {
        int n = replies;
        if (n > 9999){
            return Integer.toString(n / 10000) + "." + Integer.toString(n % 10000 / 1000) + "万";
        } else
            return Integer.toString(replies);
    }

    public int getReplies() {
       return replies;
    }
}
