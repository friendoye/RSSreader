package com.friendoye.rss_reader.utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.friendoye.rss_reader.R;

/**
 * ViewHolder pattern for RssFeedItem's View.
 */
public class RssFeedItemViewHolder {
    public ImageView imageView;
    public TextView titleView;
    public TextView dateView;

    public RssFeedItemViewHolder(View parentView) {
        imageView = (ImageView) parentView.findViewById(R.id.imageView);
        titleView = (TextView) parentView.findViewById(R.id.titleTextView);
        dateView = (TextView) parentView.findViewById(R.id.dateTextView);
    }
}
