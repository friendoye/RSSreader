package com.friendoye.rss_reader.fragments;

import android.app.Activity;
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

    public void setItem(RssFeedItem data) {
        mData = data;
    }

    public void downloadFullInfo() {
        if (mData != null) {
            RetrieveDescriptionTask task =
                    new RetrieveDescriptionTask(mData.link);
            task.execute();
        }
    }

    protected void onDownloadComplete(String description,
                                      String detailedImageLink) {
        if (description != null && detailedImageLink != null) {
            if (mData != null) {
                mData.description = description;
                mData.imageUrl = detailedImageLink;
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

    private class RetrieveDescriptionTask extends AsyncTask<Void, Void, String[]> {
        private String mLink;

        public RetrieveDescriptionTask(String link) {
            mLink = link;
        }

        /**
         * TODO: Should work not only with "Onliner".
         */
        @Override
        protected String[] doInBackground(Void... params) {
            String[] results = null;
            try {
                Document doc = Jsoup.connect(mLink).get();
                results = new String[2];
                results[0] = retrieveDescription(doc);
                results[1] = retrieveImageLink(doc);
            } catch (IOException e) {
                Log.i(IO_EXCEPTION_TAG,
                        "doInBackground(): connection problems. Info: " + e);
            }
            return results;
        }

        private String retrieveDescription(Document doc)
                throws RuntimeException {
            StringBuilder buffer = new StringBuilder();
            Elements blocks =
                    doc.select("div[class=\"b-posts-1-item__text\"]");
            try {
                Element textBlock = blocks.get(0);
                for (Element paragraph : textBlock.getElementsByTag("p")) {
                    if (paragraph.hasText()) {
                        Elements childParagraphs = paragraph.children();
                        if (childParagraphs.size() == 1
                                && paragraph.ownText().equals("")) {
                            // Do nothing, ignore such tags
                        } else {
                            buffer.append(paragraph.text()).append("\n");
                        }
                    }
                }
                return buffer.toString();
            } catch (NullPointerException e) {
                throw new RuntimeException("No tag was found. Info: " + e);
            }
        }

        private String retrieveImageLink(Document doc)
                throws RuntimeException {
            Elements blocks =
                    doc.select("figure[class=\"b-posts-1-item__image\"]");
            try {
                Elements imageBlock = blocks.get(0).getElementsByTag("img");
                String imageLink = imageBlock.get(0).attr("src");
                if (imageLink != null) {
                    return imageLink;
                } else {
                    throw new RuntimeException("No link in tag!");
                }
            } catch (NullPointerException e) {
                throw new RuntimeException("No tag was found. Info: " + e);
            }
        }

        @Override
        protected void onPostExecute(String[] results) {
            onDownloadComplete(results[0], results[1]);
        }
    }

}