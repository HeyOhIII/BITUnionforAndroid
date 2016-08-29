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

import com.example.bitunion.fragment.ForumFragment;
import com.example.bitunion.util.CommonIntents;

/**
 * Created by huolangzc on 2016/8/7.
 */
public class DisplayActivity extends AppCompatActivity{

    private ThreadPagerAdapter mPagerAdapter;
    private int mTotalPage;
    private ViewPager mViewPager;
    private int currentpage = 0;
    private int forumId;
    private String forumName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        Intent intent = getIntent();
        forumId = intent.getIntExtra(CommonIntents.EXTRA_FID, 27);
        forumName = intent.getStringExtra(CommonIntents.EXTRA_FORUM_NAME);

        if(savedInstanceState != null && !savedInstanceState.isEmpty()){
            forumId = savedInstanceState.getInt("fid");
            forumName = savedInstanceState.getString("name");
            forumName = forumName.replace("--", "");
        }

        getSupportActionBar().setTitle(String.format("%s %d", forumName, currentpage+1));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        mTotalPage = 3;
        int trigger =  getResources().getDimensionPixelSize(R.dimen.swipe_trigger_limit);
        mPagerAdapter = new ThreadPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new MyOnPageChangeListener());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("fid", forumId);
        outState.putString("name", forumName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_refresh:
                mPagerAdapter.notifyRefresh(currentpage);
                break;
            case R.id.action_newthread:
                Intent intent = new Intent(DisplayActivity.this, NewthreadActivity.class);
                intent.putExtra(CommonIntents.EXTRA_ACTION, NewthreadActivity.ACTION_NEW_THREAD);
                intent.putExtra(CommonIntents.EXTRA_FORUM_NAME, forumName);
                intent.putExtra(CommonIntents.EXTRA_FID, forumId);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ThreadPagerAdapter extends FragmentStatePagerAdapter {

        private SparseArray<ForumFragment> registeredFragments = new SparseArray<>();

        public ThreadPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ForumFragment fragment = (ForumFragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            if(position < mTotalPage){
                args.putInt(ForumFragment.ARG_PAGE, position);
                args.putInt(ForumFragment.ARG_FID, forumId);
            }
            ForumFragment frag = registeredFragments.get(position);
            if (frag == null) {
                frag = new ForumFragment();
                frag.setArguments(args);
                registeredFragments.put(position, frag);
            } else if (!frag.isUpdating())
                frag.onRefresh();
            return frag;
        }

        @Override
        public int getCount() {
            return mTotalPage;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Integer.toString(position + 1);
        }

        public void notifyRefresh(int page) {
            ForumFragment frag = registeredFragments.get(page);
            if (frag != null && !frag.isUpdating())
                frag.onRefresh();
        }
    }

    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            currentpage = position;
            getSupportActionBar().setTitle(String.format("%s %d", forumName, currentpage+1));
            boolean added = false;
            while (mTotalPage - position < 2) {
                mTotalPage++;
                added = true;
            }
            if (added) {
                mPagerAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}
