package com.friendoye.rss_reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

import com.friendoye.rss_reader.domain.AsyncTaskDownloadManager;
import com.friendoye.rss_reader.utils.Config;
import com.friendoye.rss_reader.domain.DownloadManager;
import com.friendoye.rss_reader.utils.Packer;

/**
 * Application class of our app.
 */
public class Application extends android.app.Application {
    private static AsyncTaskDownloadManager mDownloadManager;
    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initDefaultSources();
        mDownloadManager = new AsyncTaskDownloadManager(this);
    }

    private void initDefaultSources() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        String pack = preferences.getString(Config.SOURCES_STRING_KEY,
                null);
        if (pack == null) {
            String[] originalSources = getResources()
                    .getStringArray(R.array.rss_sources_array);
            pack = Packer.packCollection(originalSources);
            preferences.edit()
                    .putString(Config.SOURCES_STRING_KEY, pack)
                    .commit();
        }
    }

    public AsyncTaskDownloadManager getDownloadManager() {
        return mDownloadManager;
    }

    public static Application get(Context context) {
        return (Application) context.getApplicationContext();
    }

    public static Application getInstance() {
        return instance;
    }
}
