package com.friendoye.rss_reader.loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Loader for retrieving RSS feed from custom source.
 */
public class RssFeedLoader extends AsyncTaskLoader<Cursor> {
    private final String mSource;

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    public RssFeedLoader(Context context, String source) {
        super(context);
        mSource = source;
    }

    @Override
    public Cursor loadInBackground() {
        JsonFactory jsonFactory = new JsonFactory();
        InputStream rssStream = getRssStream();
        try {
            JsonParser parser = jsonFactory.createParser(rssStream);
        } catch(Exception e) {

        }

        return null;
    }

    protected InputStream getRssStream() {
        InputStream networkStream = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(mSource).openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            networkStream = conn.getInputStream();
        } catch (Exception e) {
            // TODO: Examine exeption
        }
        return networkStream;
    }

}
