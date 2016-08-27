package com.example.bitunion.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.bitunion.BUApp;
import com.example.bitunion.R;
import com.example.bitunion.ThreadActivity;
import com.example.bitunion.model.BUThread;
import com.example.bitunion.util.BUApi;
import com.example.bitunion.util.CommonIntents;
import com.example.bitunion.util.DataParser;
import com.example.bitunion.util.Updateable;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by huolangzc on 2016/8/8.
 */
public class ForumFragment extends Fragment implements Updateable, AbsListView.OnScrollListener{

    public static final String ARG_PAGE = "page";
    public static final String ARG_FID = "fid";

    private SwipeRefreshLayout mRefreshLayout;
    private ListView mListView;
    private ProgressBar mProgressBar;

    private ArrayList<BUThread> threadlist = new ArrayList<>(40);
    private ThreadsListAdapter mAdapter;

    private int mReqCount = 0;
    private int mPageNum, mFid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            threadlist = savedInstanceState.getParcelableArrayList("threadlist");
            mPageNum = savedInstanceState.getInt("page");
            mFid = savedInstanceState.getInt("fid");
        }else {
            mPageNum = getArguments().getInt(ARG_PAGE);
            mFid = getArguments().getInt(ARG_FID);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("threadlist", threadlist);
        outState.putInt("page", mPageNum);
        outState.putInt("fid", mFid);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_display_thread, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.lyt_refresh);
        mRefreshLayout.setColorSchemeResources(R.color.blue_dark);
        mRefreshLayout.setOnRefreshListener(this);
        mListView = (ListView) view.findViewById(R.id.forum_listview);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        if(threadlist == null || threadlist.isEmpty()){
            mProgressBar.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }else {
            mProgressBar.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }

        mAdapter = new ThreadsListAdapter(getActivity(),R.layout.single_thread_item);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(this);
        onRefresh();
    }

    @Override
    public void onRefresh() {
        if(isUpdating())
            return;
        mRefreshLayout.setRefreshing(true);
        mReqCount = 0;
        int from = mPageNum*40;
        int to = (mPageNum+1)*40;
        final ArrayList<BUThread> threads = new ArrayList<>(40);
        while(from < to){
            mReqCount++;
            BUApi.readThreads(mFid, from, from + 20, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    mReqCount--;
                    if (BUApi.getResult(response) != BUApi.Result.SUCCESS) {
                        Toast.makeText(BUApp.getInstance(), response.toString(), Toast.LENGTH_SHORT).show();
                    } else {
                        ArrayList<BUThread> tempList = DataParser.parseThreadlist(response);
                        if (tempList != null)
                            threads.addAll(tempList);
                    }
                    if (!isUpdating()) {
                        threadlist = threads;
                        notifyUpdated();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (!isAdded() && isDetached())
                        return;
                    mReqCount--;
                    notifyUpdated();
                    Toast.makeText(BUApp.getInstance(), R.string.network_unknown, Toast.LENGTH_SHORT).show();
                }
            });
            from += 20;
        }
    }

    @Override
    public boolean isUpdating() {
        return mReqCount != 0;
    }

    @Override
    public void notifyUpdated() {
        mRefreshLayout.setRefreshing(false);
        mListView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(mListView);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView view, int i, int i1, int i2) {
        boolean refreshable;
        if (view.getChildCount() > 1) {
            refreshable = view.getChildAt(0).getTop()-view.getTop() == 0;
        } else
            refreshable = true;
        mRefreshLayout.setEnabled(refreshable);
    }

    private class ThreadsListAdapter extends ArrayAdapter<BUThread> {

        public ThreadsListAdapter(Context context, int resource) {
            super(context, resource, threadlist);
        }

        @Override
        public int getCount() {
            return threadlist.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_thread_item, null);
            TextView subjView = (TextView) view
                    .findViewById(R.id.thread_subject);
            TextView authorView = (TextView) view
                    .findViewById(R.id.thread_author);
            TextView repliesView = (TextView) view.findViewById(R.id.thread_replies);
            TextView viewsView = (TextView) view.findViewById(R.id.thread_views);
            if ((position % 2) == 1)
                view.setBackgroundResource(R.drawable.ripple_text_bg_light);
            else
                view.setBackgroundResource(R.drawable.ripple_text_bg_dark);
            final BUThread threadItem = threadlist.get(position);
            subjView.setText(threadItem.getSubject());
            subjView.setTextSize(TypedValue.COMPLEX_UNIT_SP, BUApp.settings.titletextsize);
            authorView.setText(threadItem.getAuthor());
            authorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, BUApp.settings.titletextsize - 2);
            repliesView.setText(threadItem.getRepliesDisplay());
            repliesView.setTextSize(TypedValue.COMPLEX_UNIT_SP, BUApp.settings.titletextsize - 2);
            viewsView.setText(threadItem.getViewsDisplay());
            viewsView.setTextSize(TypedValue.COMPLEX_UNIT_SP, BUApp.settings.titletextsize - 2);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ThreadActivity.class);
                    intent.putExtra(CommonIntents.EXTRA_TID, threadItem.getTid());
                    intent.putExtra(CommonIntents.EXTRA_THREAD_NAME, threadItem.getSubject());
                    intent.putExtra(CommonIntents.EXTRA_REPLIES, threadItem.getRepliesDisplay()+1);
                    startActivity(intent);
                }
            });

            return view;
        }
    }


}
