package com.example.bitunion.fragment;

import android.app.Fragment;
import android.widget.AbsListView;

import com.example.bitunion.util.Updateable;

/**
 * Created by huolangzc on 2016/8/8.
 */
public class ForumFragment extends Fragment implements Updateable, AbsListView.OnScrollListener{






    @Override
    public void onRefresh() {

    }

    @Override
    public boolean isUpdating() {
        return false;
    }

    @Override
    public void notifyUpdated() {

    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {

    }
}
