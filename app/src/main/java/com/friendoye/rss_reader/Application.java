package com.friendoye.rss_reader;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

import com.friendoye.rss_reader.utils.Config;
import com.friendoye.rss_reader.utils.Packer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

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
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(displayOptions)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCacheSize(50 * 1024 * 1024)
                .memoryCacheSize(15 * 1024 * 1024)
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
            pack = Packer.packCollection(originalSources);
            preferences.edit()
                    .putString(Config.SOURCES_STRING_KEY, pack)
                    .commit();
        }
    }
}
