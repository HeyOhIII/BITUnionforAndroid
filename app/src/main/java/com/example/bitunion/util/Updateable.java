package com.example.bitunion.util;

import android.support.v4.widget.SwipeRefreshLayout;

/**
 * Created by huolangzc on 2016/8/9.
 */
public interface Updateable extends SwipeRefreshLayout.OnRefreshListener {
    @Override
    void onRefresh();
    boolean isUpdating();
    void notifyUpdated();
}
