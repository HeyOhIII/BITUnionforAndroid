package com.example.bitunion.util;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.bitunion.BUApp;

/**
 * Created by huolangzc on 2016/8/23.
 */
public class ToastUtil {
    private static Toast toast;

    public static void showToast(@NonNull String message) {
        if (BUApp.getInstance() != null) {
            if (toast == null)
                toast = Toast.makeText(BUApp.getInstance(), "", Toast.LENGTH_SHORT);
            toast.setText(message);
            toast.show();
        }
        Log.i("Toast", message);
    }

    public static void showToast(int res) {
        if (BUApp.getInstance() != null) {
            if (toast == null)
                toast = Toast.makeText(BUApp.getInstance(), "", Toast.LENGTH_SHORT);
            toast.setText(res);
            toast.show();
        }
       // Log.i("Toast", BUApp.getInstance().getString(res));
    }
}
