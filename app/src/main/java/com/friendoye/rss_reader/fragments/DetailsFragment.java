package com.friendoye.rss_reader.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.utils.Config;

/**
 * Fragment for displaying full info about news.
 */
public class DetailsFragment extends Fragment {
    private TextView mTitleView;
    private TextView mPubDateView;
    private ImageView mImageView;
    private TextView mDescriptionView;

    private RssFeedItem mData;

    public DetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details,
                container, false);

        mTitleView = (TextView) rootView.findViewById(R.id.titleView);
        mPubDateView = (TextView) rootView.findViewById(R.id.publicationView);
        mImageView = (ImageView) rootView.findViewById(R.id.imageView);
        mDescriptionView = (TextView) rootView
                .findViewById(R.id.descriptionView);

        updateViews();

        return rootView;
    }

    public void setData(RssFeedItem item) {
        mData = item;
        updateViews();
    }

    protected void updateViews() {
        if (mData != null && mDescriptionView != null) {
            mTitleView.setText(mData.title);
            mPubDateView.setText(Config.DATE_FORMATTER
                    .format(mData.publicationDate));
            mImageView.setImageBitmap(mData.largeImage);
            mDescriptionView.setText(mData.description);
        }
    }
}
