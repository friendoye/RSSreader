package com.friendoye.rss_reader.database;

import com.friendoye.rss_reader.model.RssFeedItem;
import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

/**
 * Configuration class, that should be executed after changing
 * our app's models. Run this class using standard JRE.
 */
public class OrmDatabaseConfig extends OrmLiteConfigUtil {
    private static final Class<?>[] classes = new Class[]{
            RssFeedItem.class,
    };

    public static void main(String[] args) throws Exception {
        writeConfigFile("ormlite_config.txt", classes);
    }
}
