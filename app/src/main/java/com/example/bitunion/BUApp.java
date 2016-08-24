package com.example.bitunion;

import android.app.Application;

import com.example.bitunion.util.BUApi;
import com.example.bitunion.util.Settings;

/**
 * Created by huolangzc on 2016/8/23.
 */
public class BUApp extends Application {
    private static BUApp instance;

       public static Settings settings = new Settings();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        settings.readPreference(this);
        BUApi.init(this);
    }

    public synchronized static BUApp getInstance() {
        return instance;
    }
}
