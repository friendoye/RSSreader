package com.friendoye.rss_reader;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.friendoye.rss_reader.utils.Config;
import com.friendoye.rss_reader.utils.Packer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.Arrays;

/**
 * Application class of our app.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initUIL();
        initDefaultSources();
    }

    private void initUIL() {
        DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .resetViewBeforeLoading(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(displayOptions)
                .build();
        ImageLoader.getInstance().init(config);
    }

    private void initDefaultSources() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        String pack = preferences.getString(Config.SOURCES_STRING_KEY,
                                            null);
        if (pack == null) {
            String[] originalSources = getResources()
                    .getStringArray(R.array.rss_sources_array);
            pack = Packer.packCollection(Arrays.asList(originalSources));
            preferences.edit()
                    .putString(Config.SOURCES_STRING_KEY, pack)
                    .commit();
        }
    }
}
