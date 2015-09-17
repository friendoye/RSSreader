package com.friendoye.rss_reader.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
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
import java.util.List;

/**
 * Loader for retrieving RSS feed from custom source.
 */
public class RssFeedLoader extends AsyncTaskLoader<Boolean> {
    public static final String IO_EXCEPTION_TAG = "IOException";
    public static final String NOT_IO_EXCEPTION_TAG = "Exception";

    private final String[] mSources;

    public RssFeedLoader(Context context, String[] sources) {
        super(context);
        mSources = sources;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * Uploads RSS from mSource and parses it.
     * Return null, if problems occurred or parser didn't find any item.
     */
    @Override
    public Boolean loadInBackground() {
        List<RssFeedItem> items;
        try {
            DatabaseHelper databaseHelper = DatabaseManager
                    .getHelper(this.getContext(), DatabaseHelper.class);
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
            DatabaseManager.releaseHelper();
            return true;
        } catch (IOException e) {
            Log.e(IO_EXCEPTION_TAG,
                    "loadInBackground(): problems at parsing. Info: " + e);
        } catch (XmlPullParserException e) {
            Log.e(NOT_IO_EXCEPTION_TAG,
                    "loadInBackground(): problems at parsing. Info: " + e);
        }

        return false;
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
