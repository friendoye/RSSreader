package com.friendoye.rss_reader;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Application class of our app.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initUIL();
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
}
