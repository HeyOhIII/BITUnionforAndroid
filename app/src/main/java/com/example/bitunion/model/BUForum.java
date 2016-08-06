package com.example.bitunion.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by huolangzc on 2016/8/2.
 */
public class BUForum extends BUContent{

    private final String name;
    private final int fid;
    private final int type;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(fid);
        parcel.writeInt(type);
    }

    public static final Parcelable.Creator<BUForum> CREATOR = new Parcelable.Creator<BUForum>(){

        @Override
        public BUForum createFromParcel(Parcel parcel) {
            return new BUForum(parcel);
        }

        @Override
        public BUForum[] newArray(int i) {
            return new BUForum[i];
        }
    };

    public BUForum(Parcel in){
        name = in.readString();
        fid = in.readInt();
        type = in.readInt();
    }

    public BUForum(String name, int fid, int type){
        this.name = name;
        this.fid = fid;
        this.type = type;
    }

    public String getName(){
        return name;
    }

    public int getFid(){
        return fid;
    }

    public int getType(){
        return type;
    }
}
