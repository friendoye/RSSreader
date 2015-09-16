package com.friendoye.rss_reader.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
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
    private OnItemSelectedListener mCallback;

    private RssItemAdapter mAdapter;

    public interface OnItemSelectedListener {
        void onItemSelected(RssFeedItem item);
    }

    public RssFeedFragment() {
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        try {
            mCallback = (OnItemSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must " +
                    "implement OnItemSelectedListener!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rss_feed, container, false);
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
                new PauseOnScrollListener(ImageLoader.getInstance(), false, true);
        getListView().setOnScrollListener(listener);
    }

    @Override
    public void onListItemClick(ListView parent, View view,
                                int position, long id) {
        RssFeedItem item = (RssFeedItem)
                parent.getItemAtPosition(position);
        mCallback.onItemSelected(item);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
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
