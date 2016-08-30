package com.example.bitunion;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bitunion.model.BUForum;
import com.example.bitunion.util.BUApi;
import com.example.bitunion.util.CommonIntents;
import com.example.bitunion.util.ToastUtil;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import java.util.ArrayList;


/**
 * Created by huolangzc on 2016/7/25.
 */
public class ForumActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;

    private ArrayList<BUForum> forumList = new ArrayList<BUForum>();
    private ArrayList<ArrayList<BUForum>> fArrayList = new ArrayList<ArrayList<BUForum>>();
    private String[] groupList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupList = getResources().getStringArray(R.array.forum_group);
        String[] forumNames = getResources().getStringArray(R.array.forums);
        int[] forumFids = getResources().getIntArray(R.array.fids);
        int[] forumTypes = getResources().getIntArray(R.array.types);
        for(int i =0; i < forumNames.length; i++){
            forumList.add(new BUForum(forumNames[i], forumFids[i], forumTypes[i]));
        }
        for(int i = 0; i < groupList.length; i++){
            ArrayList<BUForum> forums = new ArrayList<>();
            for(BUForum forum : forumList)
                if(i == forum.getType())
                    forums.add(forum);
            fArrayList.add(forums);
            }

        mRecyclerView = (RecyclerView) findViewById(R.id.listview);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(null);
        ForumListAdapter itemAdapter = new ForumListAdapter();
        mAdapter = itemAdapter;
        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(itemAdapter);

        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);
    }

    @Override
    protected void onDestroy() {
        if (mRecyclerViewExpandableItemManager != null) {
            mRecyclerViewExpandableItemManager.release();
            mRecyclerViewExpandableItemManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mAdapter = null;
        mLayoutManager = null;
        super.onDestroy();
    }


    private class ForumListAdapter extends AbstractExpandableItemAdapter<GroupViewHolder, ChildViewHolder> {

        private ForumListAdapter() {
            setHasStableIds(true);
        }

        @Override
        public int getGroupCount() {
            return groupList.length;
        }

        @Override
        public int getChildCount(int groupPosition) {
            return fArrayList.get(groupPosition).size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public int getGroupItemViewType(int groupPosition) {
            return 0;
        }

        @Override
        public int getChildItemViewType(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_forum_group, parent, false);
            return new GroupViewHolder(view);
        }

        @Override
        public ChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_forum_title, parent, false);
            return new ChildViewHolder(view);
        }

        @Override
        public void onBindGroupViewHolder(GroupViewHolder holder, int groupPosition, int viewType) {
            holder.groupName.setText(groupList[groupPosition]);
            holder.itemView.setClickable(true);
        }

        @Override
        public void onBindChildViewHolder(ChildViewHolder holder, int groupPosition, int childPosition, int viewType) {
            if (fArrayList.get(groupPosition).get(childPosition).getName().contains("--"))
                holder.childTitle.setTextSize(16);
            else
                holder.childTitle.setTextSize(20);
            holder.childTitle.setText(fArrayList.get(groupPosition).get(childPosition).getName());
            final BUForum forum = fArrayList.get(groupPosition).get(childPosition);
           holder.childTitle.setOnClickListener(new View.OnClickListener() {

                @Override
                @SuppressWarnings("NewApi")
                public void onClick(View v) {
                    if (BUApi.isUserLoggedin()) {
                        if (forum.getFid() == -1) {
                            Intent intent = new Intent(ForumActivity.this, RecentListActivity.class);
                            startActivity(intent);
                        } else if (forum.getFid() == -2) {
                            ToastUtil.showToast("功能暂时无法使用");
                        } else {
                            Intent intent = new Intent(ForumActivity.this, DisplayActivity.class);
                            intent.putExtra(CommonIntents.EXTRA_FID, forum.getFid());
                            intent.putExtra(CommonIntents.EXTRA_FORUM_NAME, forum.getName());
                            startActivity(intent);
                        }
                    } else
                        ToastUtil.showToast("请先登录");
                }
            });
        }

        @Override
        public boolean onCheckCanExpandOrCollapseGroup(GroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
            holder.indicator.setImageResource(expand ? R.mipmap.ic_expand_more_grey600_48dp:R.mipmap.ic_expand_less_grey600_48dp);
            return true;
        }
    }

    private static class GroupViewHolder extends AbstractExpandableItemViewHolder {
        ImageView indicator;
        TextView groupName;

        private GroupViewHolder(View itemView) {
            super(itemView);
            indicator = (ImageView) itemView.findViewById(R.id.imgVw_group_expand_indicator);
            groupName = (TextView) itemView.findViewById(R.id.txtVw_group_title);
        }
    }

    private static class ChildViewHolder extends AbstractExpandableItemViewHolder {
        TextView childTitle;

        private ChildViewHolder(View itemView) {
            super(itemView);
            childTitle = (TextView) itemView.findViewById(R.id.txtVw_forum_title);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
