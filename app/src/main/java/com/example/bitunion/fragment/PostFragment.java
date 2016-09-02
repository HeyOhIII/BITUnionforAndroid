package com.example.bitunion.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.bitunion.BUApp;
import com.example.bitunion.R;
import com.example.bitunion.model.BUPost;
import com.example.bitunion.model.BUThread;
import com.example.bitunion.util.BUApi;
import com.example.bitunion.util.CommonIntents;
import com.example.bitunion.util.DataParser;
import com.example.bitunion.util.ToastUtil;
import com.example.bitunion.util.Updateable;
import com.example.bitunion.util.Utils;

import org.json.JSONObject;
import org.xml.sax.XMLReader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huolangzc on 2016/8/22.
 */
public class PostFragment extends Fragment implements Updateable, AbsListView.OnScrollListener{

    public static final String ARG_PAGE = "page";
    public static final String ARG_TID = "tid";

    private SwipeRefreshLayout mRefreshLayout;
    private ArrayList<BUPost> postlist = new ArrayList<BUPost>(40);;
    private ListView listView;
    private PostsAdapter mAdapter;

    private static String COLOR_BG_DARK;
    private static String COLOR_BG_LIGHT;

    private int mReqCount = 0;
    private int mPageNum, mTid;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mPageNum = savedInstanceState.getInt("page");
            mTid = savedInstanceState.getInt("tid");
        } else {
            mPageNum = getArguments().getInt(ARG_PAGE);
            mTid = getArguments().getInt(ARG_TID);
        }

        Resources res = getResources();
        COLOR_BG_DARK = Integer.toHexString(res.getColor(R.color.blue_light) & 0x00ffffff);
        COLOR_BG_LIGHT = Integer.toHexString(res.getColor(R.color.blue_text_bg_light) & 0x00ffffff);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("page", mPageNum);
        outState.putInt("tid", mTid);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.lyt_post_refresh);
        mRefreshLayout.setOnRefreshListener(this);
        listView = (ListView) getActivity().findViewById(R.id.list);
        mAdapter = new PostsAdapter(getActivity(),R.layout.post_item);
        listView.setAdapter(mAdapter);
        listView.setOnScrollListener(this);
        onRefresh();
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

    @Override
    public void onRefresh() {
        if(isUpdating())
            return;
        mRefreshLayout.setRefreshing(true);
        mReqCount = 0;
        int from = mPageNum*40;
        int to = (mPageNum+1)*40;
        final ArrayList<BUPost> posts = new ArrayList<>(40);
        while(from < to){
            mReqCount++;
            BUApi.readPostList(mTid, from, from + 20, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    mReqCount--;
                    if (BUApi.getResult(response) != BUApi.Result.SUCCESS) {
                        //Toast.makeText(BUApp.getInstance(), response.toString(), Toast.LENGTH_SHORT).show();
                        ToastUtil.showToast(response.toString());
                    } else {
                        ArrayList<BUPost> tempList = DataParser.parsePostlist(response);
                        if (tempList != null)
                            posts.addAll(tempList);
                    }
                    if (!isUpdating()) {
                        postlist = posts;
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
        listView.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(listView);
        mAdapter.notifyDataSetChanged();

    }

    private class PostsAdapter extends ArrayAdapter<BUPost>{

        public PostsAdapter(Context context, int resource) {
            super(context, resource, postlist);
        }

        @Override
        public int getCount() {
            return postlist.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, null);
            TextView authorText = (TextView) view.findViewById(R.id.authorText);
            TextView timeText = (TextView) view.findViewById(R.id.timeText);
            TextView messageView = (TextView) view.findViewById(R.id.messageView);
            ImageView attachmentView = (ImageView) view.findViewById(R.id.attachmentView);
            TextView quotesView = (TextView) view.findViewById(R.id.quotesView);

            if ((position % 2) == 1)
                view.setBackgroundResource(R.drawable.ripple_text_bg_light);
            else
                view.setBackgroundResource(R.drawable.ripple_text_bg_dark);

            final BUPost postItem = postlist.get(position);
            authorText.setText(postItem.getAuthor());
            timeText.setText(postItem.getDateline());
//            messageView.setText(postItem.getMessage());
//            quotesView.setText(postItem.toQuote());

            String htmlcode = createHtmlCode(position);
            messageView.setText(Html.fromHtml(htmlcode, new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String s) {
                    Drawable drawable = null;
                    URL url;
                    try {
                        url = new URL(s);
                        drawable = Drawable.createFromStream(url.openStream(), "");  //获取网路图片
                    } catch (Exception e) {
                        return null;
                    }
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
                            .getIntrinsicHeight());
                    return drawable;
                }
            }, new Html.TagHandler() {
                @Override
                public void handleTag(boolean b, String s, Editable editable, XMLReader xmlReader) {

                }
            }));

            return view;
        }
    }

    private String createHtmlCode(int position) {

        StringBuilder content = new StringBuilder("<!DOCTYPE ><html><body>");

        BUPost postItem = postlist.get(position);
        content.append(postItem.getHtmlLayout(position));
        content.append("</body></html>");
        return content.toString();
    }

}
