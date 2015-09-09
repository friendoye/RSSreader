package com.friendoye.rss_reader.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.database.DatabaseHelper;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.utils.RssFeedItemViewHolder;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying list with RSS feed items.
 */
public class RssFeedFragment extends ListFragment {

    public RssFeedFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rss_feed, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Set up better access to db
        Context appContext = getActivity().getApplicationContext();
        DatabaseHelper db_helper = OpenHelperManager
                .getHelper(appContext, DatabaseHelper.class);
        List<RssFeedItem> items = db_helper.getAllFeedItems();
        OpenHelperManager.releaseHelper();
        db_helper = null;

        RssItemAdapter adapter = new RssItemAdapter();
        adapter.setItems(items);
        setListAdapter(adapter);
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
            // TODO: Place code with UIL
            //holder.imageView.setImageBitmap();
            holder.titleView.setText(item.title);
            holder.dateView.setText(item.publicationDate.toString());

            return convertView;
        }

    }
}
