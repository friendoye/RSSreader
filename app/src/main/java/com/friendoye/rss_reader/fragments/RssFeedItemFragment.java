package com.friendoye.rss_reader.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.friendoye.rss_reader.model.RssFeedItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Retained fragment, that will keep RssFeedItem while orientation changes.
 */
public class RssFeedItemFragment extends Fragment {
    public static final String IO_EXCEPTION_TAG = "IOException";

    private OnDownloadCompletedListener mCallback;

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

    public void setItem(RssFeedItem data) {
        mData = data;
    }

    public void downloadDescription() {
        if (mData != null) {
            RetrieveDescriptionTask task =
                    new RetrieveDescriptionTask(mData.link);
            task.execute();
        }
    }

    protected void onDownloadComplete(String description) {
        if (description != null) {
            if (mData != null) {
                mData.description = description;
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

    private class RetrieveDescriptionTask extends AsyncTask<Void, Void, String> {
        private String mLink;

        public RetrieveDescriptionTask(String link) {
            mLink = link;
        }

        /**
         * TODO: Should work not only with "Onliner".
         */
        @Override
        protected String doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect(mLink).get();
                Elements blocks =
                        doc.select("div[class=\"b-posts-1-item__text\"]");

                String description = null;
                if (blocks.size() != 0) {
                    Element textBlock = blocks.get(0);
                    StringBuilder buffer = new StringBuilder();
                    for (Element paragraph : textBlock.getElementsByTag("p")) {
                        if (paragraph.hasText()) {
                            Elements childParagraphs = paragraph.children();
                            if (childParagraphs.size() == 1 &&!childParagraphs.hasText()
                                    && childParagraphs.get(0).tagName().equals("a")) {
                                // Do nothing, ignore such tags
                            } else {
                                buffer.append(paragraph.text()).append("\n");
                            }
                        }
                    }
                    description = buffer.toString();
                }

                return description;
            } catch (IOException e) {
                Log.i(IO_EXCEPTION_TAG,
                        "doInBackground(): connection problems. Info: " + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String string) {
            onDownloadComplete(string);
        }
    }

}