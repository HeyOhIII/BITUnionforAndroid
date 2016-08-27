package com.example.bitunion;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.bitunion.model.RecentThread;
import com.example.bitunion.util.BUApi;
import com.example.bitunion.util.CommonIntents;
import com.example.bitunion.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huolangzc on 2016/8/6.
 */
public class RecentListActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mRefreshLyt;
    private RecyclerView mRecyclerView;
    private RecentListAdapter mAdapter;

    private List<RecentThread> mThreadList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_list);

        if (savedInstanceState != null) {

        } else {
            mThreadList = new ArrayList<RecentThread>();
        }

        getSupportActionBar().setTitle(R.string.title_recent_list);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRefreshLyt = (SwipeRefreshLayout) findViewById(R.id.lyt_refresh_frame);
        mRefreshLyt.setOnRefreshListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.listview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RecentListAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnScrollListener(mScrollListener);

        showLoading(true);
        onRefresh();
    }

    private void showLoading(boolean loading){
        mProgressBar.setVisibility(loading ? View.VISIBLE:View.GONE);
        mRecyclerView.setVisibility(loading ? View.GONE:View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        mRefreshLyt.setRefreshing(true);
        BUApi.readHomeThreads(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (BUApi.getResult(response) != BUApi.Result.SUCCESS) {
                    ToastUtil.showToast(response.toString());
                } else {
                    JSONArray newlist = response.optJSONArray("newlist");
                    for (int i = 0; i < newlist.length(); i++)
                        try {
                            mThreadList.add(new RecentThread(newlist.getJSONObject(i)));
                        } catch (JSONException e) {
                            continue;
                        }
                }
                mAdapter.notifyDataSetChanged();
                showLoading(false);
                mRefreshLyt.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtil.showToast(R.string.network_unknown);
                showLoading(false);
                mRefreshLyt.setRefreshing(false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recent_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.action_refresh:
                onRefresh();
                break;
        }
        return true;
    }

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        private int totalY;
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            totalY += dy;
            if (totalY != 0)
                mRefreshLyt.setEnabled(false);
            else
                mRefreshLyt.setEnabled(true);
        }
    };


    private class RecentListAdapter extends RecyclerView.Adapter<VH>{
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_thread_item, parent, false);
            return new VH(row);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            final RecentThread item = mThreadList.get(position);
            if (position % 2 == 0)
                holder.itemView.setBackgroundResource(R.drawable.ripple_text_bg_dark);
            else
                holder.itemView.setBackgroundResource(R.drawable.ripple_text_bg_light);
            holder.title.setText(item.title);
            holder.forum.setText(item.forum);
            holder.author.setText(item.author);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ThreadActivity.class);
                    intent.putExtra(CommonIntents.EXTRA_TID, item.tid);
                    intent.putExtra(CommonIntents.EXTRA_THREAD_NAME, item.title);
                    v.getContext().startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return mThreadList.size();
        }
    }

    private static class VH extends RecyclerView.ViewHolder{

        TextView title;
        TextView forum;
        TextView author;

        public VH(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.thread_title);
            forum = (TextView) itemView.findViewById(R.id.forum_title);
            author = (TextView) itemView.findViewById(R.id.author_name);
        }
    }


}
