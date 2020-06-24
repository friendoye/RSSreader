package com.friendoye.rss_reader.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.friendoye.rss_reader.database.DatabaseHelper;
import com.friendoye.rss_reader.database.DatabaseManager;
import com.friendoye.rss_reader.model.AbstractRssSourceFactory;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.model.RssParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DownloadManager {
    private LoadingState mState;
    private RefreshTask mTask;
    private Context mContext;

    private ArrayList<OnDownloadCompletedListener> observers;

    public interface OnDownloadCompletedListener {
        void onDownloadComplete(LoadingState state);
    }

    public DownloadManager(Context context) {
        mContext = context;
        mState = LoadingState.SUCCESS;
        observers = new ArrayList<>();
    }

    public synchronized LoadingState getState() {
        return mState;
    }

    public synchronized void refreshData(List<String> sources) {
        if (mState != LoadingState.LOADING) {
            mTask = new RefreshTask(mContext, sources);
            mState = LoadingState.LOADING;
            mTask.execute();
        }
    }

    private synchronized void onRefreshComplete(boolean success) {
        mTask = null;
        mState = success ? LoadingState.SUCCESS : LoadingState.FAILURE;
        notifyObservers();
    }

    private void notifyObservers() {
        for (OnDownloadCompletedListener observer: observers) {
            observer.onDownloadComplete(mState);
        }
    }

    public void subscribe(OnDownloadCompletedListener observer) {
        observers.add(observer);
    }

    public void unsubscribe(OnDownloadCompletedListener observer) {
        boolean flag = observers.remove(observer);
        Log.i("DownloadManager","Unsubbscribe: " + flag);
    }

    private class RefreshTask extends AsyncTask<Void, Void, Boolean> {
        public static final String IO_EXCEPTION_TAG = "IOException";
        public static final String NOT_IO_EXCEPTION_TAG = "Exception";

        private Context mContext;
        private List<String> mSources;

        public RefreshTask(Context context, List<String> sources) {
            this.mContext = context;
            this.mSources = sources;
        }

        /**
         * Uploads RSS from mSource and parses it.
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            List<RssFeedItem> items;

            try {
                DatabaseHelper databaseHelper = DatabaseManager
                        .getHelper(mContext, DatabaseHelper.class);

                for (String source: mSources) {
                    InputStream rssStream = getRssStream(source);
                    if (rssStream != null) {
                        RssParser parser = AbstractRssSourceFactory.getInstance(source).getRssParser();
                        items = parser.parseRssStream(rssStream);
                    } else {
                        return false;
                    }
                    if (items != null && items.size() != 0) {
                        databaseHelper.addFeedItems(items);
                    }
                }

                return true;
            } catch (IOException e) {
                Log.e(IO_EXCEPTION_TAG,
                        "loadInBackground(): problems at parsing. Info: " + e);
            } catch (XmlPullParserException e) {
                Log.e(NOT_IO_EXCEPTION_TAG,
                        "loadInBackground(): problems at parsing. Info: " + e);
            } finally {
                DatabaseManager.releaseHelper();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mContext = null;
            onRefreshComplete(success);
        }

        protected InputStream getRssStream(String source) {
            InputStream networkStream = null;
            try {
                HttpURLConnection conn = (HttpURLConnection)
                        new URL(source).openConnection();

                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                conn.connect();
                networkStream = conn.getInputStream();
            } catch (IOException e) {
                Log.e(IO_EXCEPTION_TAG,
                        "getRssStream(): cannot connect! Info: " + e);
            } catch (Exception e) {
                Log.e(NOT_IO_EXCEPTION_TAG,
                        "getRssStream(): cannot open connection! Info: " + e);
            }
            return networkStream;
        }
    }
}
