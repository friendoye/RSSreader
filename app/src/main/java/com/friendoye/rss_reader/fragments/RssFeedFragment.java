package com.friendoye.rss_reader.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.utils.Config;
import com.friendoye.rss_reader.utils.RssFeedItemViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying list with RSS feed items.
 */
public class RssFeedFragment extends ListFragment {
    public static final String IS_REFRESHING_KEY = "refreshing key";
    public static final String LIST_POSITION_KEY = "list position key";

    private SwipeRefreshLayout mSwipeLayout;

    private OnDataUsageListener mCallback;

    private RssItemAdapter mAdapter;

    public interface OnDataUsageListener {
        void onItemSelected(RssFeedItem item);
        void onRefresh();
    }

    public RssFeedFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnDataUsageListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must " +
                    "implement OnDataUsageListener!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rss_feed,
                container, false);

        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(
                R.id.swipe_layout);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mCallback != null) {
                    mCallback.onRefresh();
                }
            }
        });
        mSwipeLayout.setColorSchemeResources(R.color.amber_A400,
                R.color.orange_500);

        if (savedInstanceState != null) {
        }

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new RssItemAdapter();
        setListAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PauseOnScrollListener listener =
                new PauseOnScrollListener(ImageLoader.getInstance(), true, true);
        getListView().setOnScrollListener(listener);

        if (savedInstanceState != null) {
            boolean flag = savedInstanceState.getBoolean(IS_REFRESHING_KEY);
            if (flag) {
                mSwipeLayout.setRefreshing(true);
                mSwipeLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mSwipeLayout.isRefreshing()) {
                            setRefreshing(true);
                        }
                    }
                }, 500);
            }
            int savedPosition = savedInstanceState.getInt(LIST_POSITION_KEY);
            getListView().setSelection(savedPosition);
        }
    }

    @Override
    public void onListItemClick(ListView parent, View view,
                                int position, long id) {
        RssFeedItem item = (RssFeedItem)
                parent.getItemAtPosition(position);
        mCallback.onItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_REFRESHING_KEY, mSwipeLayout.isRefreshing());
        outState.putInt(LIST_POSITION_KEY, getListView().getFirstVisiblePosition());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
        mAdapter = null;
    }

    public void setRefreshing(boolean check) {
        if (mSwipeLayout != null) {
            mSwipeLayout.setRefreshing(check);
        }
    }

    public void setFeedItems(List<RssFeedItem> items) {
        mAdapter.setItems(items);
    }

    private class RssItemAdapter extends BaseAdapter {
        private List<RssFeedItem> mItems;

        public RssItemAdapter() {
            mItems = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).hashCode();
        }

        @Override
        public boolean isEnabled(int position)
        {
            return true;
        }

        public void setItems(@NonNull List<RssFeedItem> items) {
            mItems = items;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RssFeedItemViewHolder holder;
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.view_rss_feed_item, parent, false);
                holder = new RssFeedItemViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (RssFeedItemViewHolder) convertView.getTag();
            }

            RssFeedItem item = mItems.get(position);
            ImageLoader.getInstance().displayImage(item.imageUrl,
                                                   holder.imageView);
            holder.titleView.setText(item.title);
            holder.dateView.setText(Config.DATE_FORMATTER
                    .format(item.publicationDate));

            return convertView;
        }

    }
}
