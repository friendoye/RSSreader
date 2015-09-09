package com.friendoye.rss_reader.database;

import android.app.Application;
import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;

/**
 * Our wrapper for standard OpenHelperManager. Should be used
 * everywhere instead of OpenHelperManager.
 */
public class DatabaseManager extends OpenHelperManager {

    public static <T extends OrmLiteSqliteOpenHelper> T getHelper(Context context, Class<T> openHelperClass) {
        if (context instanceof Application) {
            return OpenHelperManager.getHelper(context, openHelperClass);
        } else {
            return OpenHelperManager.getHelper(context.getApplicationContext(), openHelperClass);
        }
    }
}
