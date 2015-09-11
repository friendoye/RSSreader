package com.friendoye.rss_reader.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Fragment for displaying full info about news.
 */
public class DetailsFragment extends Fragment {
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
        if (mData == null) {
            return;
        }
        if (mDescriptionView != null) {
            mDescriptionView.setText(mData.description);
        }
        if (mImageView != null) {
            Bitmap imageBitmap = ImageLoader.getInstance()
                    .loadImageSync(mData.imageUrl);
            mImageView.setImageBitmap(imageBitmap);
        }
    }
}
