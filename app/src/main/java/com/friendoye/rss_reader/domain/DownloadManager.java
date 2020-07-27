package com.friendoye.rss_reader.domain;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.friendoye.rss_reader.database.DatabaseHelper;
import com.friendoye.rss_reader.database.DatabaseManager;
import com.friendoye.rss_reader.model.AbstractRssSourceFactory;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.model.RssParser;
import com.friendoye.rss_reader.utils.LoadingState;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public interface DownloadManager {

    interface OnDownloadStateChangedListener {
        void onDownloadStateChanged(LoadingState state);
    }

    @NonNull
    LoadingState getState();

    void refreshData(@NonNull List<String> sources);

    void subscribe(@NonNull OnDownloadStateChangedListener observer);
    void unsubscribe(@NonNull OnDownloadStateChangedListener observer);
}