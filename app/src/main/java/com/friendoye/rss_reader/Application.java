package com.friendoye.rss_reader;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 *
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initUIL();
    }

    private void initUIL() {
        ImageLoaderConfiguration config =
                ImageLoaderConfiguration.createDefault(this);
        ImageLoader.getInstance().init(config);
    }
}
