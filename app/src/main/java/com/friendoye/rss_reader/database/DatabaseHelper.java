package com.friendoye.rss_reader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Helper class for executing operation on database.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    public static final String SQL_EXCEPTION_TAG = "SQL Exception";

    private static final String DATABASE_NAME = "rss.db";
    private static final int DATABASE_VERSION = 1;

    private RuntimeExceptionDao<RssFeedItem, Integer> mRuntimeDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null,
                DATABASE_VERSION, R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase database,
                         ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource,
                    RssFeedItem.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database,
                          ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, RssFeedItem.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        super.close();
        mRuntimeDao = null;
    }

    public void addFeedItems(@NonNull List<RssFeedItem> items) {
        RuntimeExceptionDao<RssFeedItem, Integer> dao = getRuntimeDao();
        RssFeedItem lastItem = getFirstItem(dao);
        // If given list has database last item, then we should add
        // only items, that come after. Otherwise, add all items.
        boolean matchLast = false;
        for (RssFeedItem item : items) {
            if (item.equals(lastItem)) {
                matchLast = true;
            } else if (matchLast) {
                dao.create(item);
            }
        }
        if (!matchLast) {
            for (RssFeedItem item : items) {
                dao.create(item);
            }
        }
    }

    protected RuntimeExceptionDao<RssFeedItem, Integer> getRuntimeDao() {
        if (mRuntimeDao == null) {
            mRuntimeDao = getRuntimeExceptionDao(RssFeedItem.class);
        }
        return mRuntimeDao;
    }

    protected <T extends Object, ID extends Object>
            T getFirstItem(RuntimeExceptionDao<T, ID> dao) {
        try {
            PreparedQuery<T> constructedQuery = dao.queryBuilder()
                    .orderBy(RssFeedItem.PUB_DATE_KEY, false)
                    .prepare();
            return dao.queryForFirst(constructedQuery);
        } catch (SQLException e) {
            Log.i(SQL_EXCEPTION_TAG,
                    "getFirstItem(): failed to prepare query. Info: " + e);
        }
        return null;
    }

    public List<RssFeedItem> getAllFeedItems() {
        RuntimeExceptionDao<RssFeedItem, Integer> dao = getRuntimeDao();
        try {
            PreparedQuery<RssFeedItem> constructedQuery = dao.queryBuilder()
                    .orderBy(RssFeedItem.PUB_DATE_KEY, false)
                    .prepare();
            return dao.query(constructedQuery);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
