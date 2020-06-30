package com.friendoye.rss_reader.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.friendoye.rss_reader.model.AbstractRssSourceFactory;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.model.RssParser;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Retained fragment, that will keep RssFeedItem while orientation changes.
 */
public class RssFeedItemFragment extends Fragment {
    public static final String IO_EXCEPTION_TAG = "IOException";

    private OnDownloadCompletedListener mCallback;
    private RetrieveDescriptionTask mTask;

    @Nullable
    private RssFeedItem mData;

    public interface OnDownloadCompletedListener {
        void onDownloadSuccess();
        void onDownloadFailure();
    }

    public RssFeedItemFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnDownloadCompletedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must " +
                    "implement OnDownloadCompletedListener!");
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    public void setItem(RssFeedItem data) {
        mData = data;
    }

    public void downloadFullInfo() {
        if (mData != null) {
            RssParser parser = AbstractRssSourceFactory.getInstance(mData.source).getRssParser();
            if (mTask == null || mTask.isCancelled() || mTask.getStatus() == AsyncTask.Status.FINISHED) {
                mTask = new RetrieveDescriptionTask(mData.link, parser);
                mTask.execute();
            }
        }
    }

    protected void onDownloadComplete(String description,
                                      Bitmap largeImage) {
        if (description != null) {
            if (mData != null) {
                mData.description = description;
                mData.largeImage = largeImage != null ? largeImage :
                        ImageLoader.getInstance().loadImageSync(mData.imageUrl);
            }
            if (mCallback != null) {
                mCallback.onDownloadSuccess();
            }
        } else {
            if (mCallback != null) {
                mCallback.onDownloadFailure();
            }
        }
    }

    @Nullable
    public RssFeedItem getItem() {
        return mData;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    private class RetrieveDescriptionTask extends AsyncTask<Void, Void, Object[]> {
        private String mLink;
        private RssParser mParser;

        public RetrieveDescriptionTask(String link, RssParser parser) {
            mLink = link;
            mParser = parser;
        }

        @Override
        protected Object[] doInBackground(Void... params) {
            Object[] results = null;
            try {
                Document doc = Jsoup.connect(mLink).timeout(5000).get();
                results = new Object[2];
                results[0] = mParser.retrieveDescription(doc);
                results[1] = mParser.retrieveLargeImage(doc);
            } catch (IOException e) {
                Log.i(IO_EXCEPTION_TAG,
                        "doInBackground(): connection problems. Info: " + e);
            }
            return results;
        }

        @Override
        protected void onPostExecute(Object[] results) {
            if (results != null) {
                onDownloadComplete((String) results[0],
                                   (Bitmap) results[1]);
            } else {
                onDownloadComplete(null, null);
            }
        }
    }

}