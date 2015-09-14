package com.friendoye.rss_reader.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.parsers.RssParser;

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

    private RssFeedItem mData;

    public interface OnDownloadCompletedListener {
        void onDownloadSuccess();
        void onDownloadFailure();
    }

    public RssFeedItemFragment() {
    }

    @Override
    public void onAttach(Activity context) {
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
            RssParser parser = RssParser.getInstance(mData.source);
            mTask = new RetrieveDescriptionTask(mData.link, parser);
            mTask.execute();
        }
    }

    protected void onDownloadComplete(String description,
                                      Bitmap largeImage) {
        if (description != null && largeImage != null) {
            if (mData != null) {
                mData.description = description;
                mData.largeImage = largeImage;
            }
            mCallback.onDownloadSuccess();
        } else {
            mCallback.onDownloadFailure();
        }
    }

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
                Document doc = Jsoup.connect(mLink).get();
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