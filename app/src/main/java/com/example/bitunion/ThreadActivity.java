package com.example.bitunion;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.example.bitunion.fragment.PostFragment;
import com.example.bitunion.util.CommonIntents;

/**
 * Created by huolangzc on 2016/8/7.
 */
public class ThreadActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private ThreadPagerAdapter mThreadAdapter;
    private int threadId;
    private String threadName;

    private int totalPage;
    private int lastpage, replies = 1;
    private int currentpage = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);

        Intent intent = getIntent();
        threadId = intent.getIntExtra(CommonIntents.EXTRA_TID, 0);
        threadName = intent.getStringExtra(CommonIntents.EXTRA_THREAD_NAME);
        replies = intent.getIntExtra(CommonIntents.EXTRA_REPLIES, 1);

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            threadId = savedInstanceState.getInt("tid");
            threadName = savedInstanceState.getString("subject");
            replies = savedInstanceState.getInt("replies");
        }

        calculateTotalPage();

        getSupportActionBar().setTitle(String.format("%s %d", threadName, currentpage+1));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        mThreadAdapter = new ThreadPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mThreadAdapter);
        mViewPager.addOnPageChangeListener(new MyOnPageChangeListener());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tid", threadId);
        outState.putString("subject", threadName);
        outState.putInt("replies", replies);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.thread, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh:
                mThreadAdapter.notifyRefresh(currentpage);
                return true;
            case R.id.action_reply:
                Intent intent = new Intent(this, NewthreadActivity.class);
                intent.putExtra(CommonIntents.EXTRA_ACTION, NewthreadActivity.ACTION_NEW_POST);
                intent.putExtra(CommonIntents.EXTRA_TID, threadId);
                startActivity(intent);
                return true;
        }
        return false;
    }

    private void calculateTotalPage() {
        if (replies % 40 == 0)
            lastpage = replies / 40 - 1;
        else
            lastpage = replies / 40;
        while (totalPage <= lastpage)
            totalPage++;
        if (mThreadAdapter != null)
            mThreadAdapter.notifyDataSetChanged();
    }

    public class ThreadPagerAdapter extends FragmentStatePagerAdapter {

        private SparseArray<PostFragment> registeredFragments = new SparseArray<PostFragment>();

        public ThreadPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            PostFragment frag = registeredFragments.get(position);
            if (frag == null) {
                frag = new PostFragment();
                Bundle args = new Bundle();
                args.putInt(PostFragment.ARG_TID, threadId);
                args.putInt(PostFragment.ARG_PAGE, position);
                frag.setArguments(args);
                registeredFragments.put(position, frag);
            } else if (!frag.isUpdating()){
                frag.onRefresh();
            }
            return frag;
        }

        @Override
        public int getCount() {
            return totalPage;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Integer.toString(position + 1);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PostFragment fragment = (PostFragment) super.instantiateItem(
                    container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public void notifyRefresh(int page) {
            PostFragment frag = registeredFragments.get(page);
            if (frag != null && !frag.isUpdating())
                frag.onRefresh();
        }
    }

    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onPageScrolled(int pos, float per, int arg2) {
        }

        @Override
        public void onPageSelected(int pos) {
            currentpage = pos;
            getSupportActionBar().setTitle(String.format("%s %d", threadName, currentpage+1));
        }
    }

}
