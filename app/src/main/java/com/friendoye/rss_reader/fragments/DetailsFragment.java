package com.friendoye.rss_reader.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.model.RssFeedItem;

/**
 * Fragment for displaying full info about news.
 */
public class DetailsFragment extends Fragment {
    private ImageView mImageView;
    private TextView mDescriptionView;

    public DetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details,
                container, false);

        mImageView = (ImageView) rootView.findViewById(R.id.imageView);
        mDescriptionView = (TextView) rootView
                .findViewById(R.id.descriptionView);

        return rootView;
    }

    public void updateViews(RssFeedItem item) {
        mDescriptionView.setText(item.description);
    }
}
