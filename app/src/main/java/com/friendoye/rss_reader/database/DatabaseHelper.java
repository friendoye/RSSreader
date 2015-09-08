package com.friendoye.rss_reader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.friendoye.rss_reader.model.RssFeedItem;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Helper class for executing operation on database.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "rss.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.dropTable(connectionSource, RssFeedItem.class, true);
            TableUtils.createTableIfNotExists(connectionSource, RssFeedItem.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        RuntimeExceptionDao<RssFeedItem, Integer> dao = getRuntimeExceptionDao(RssFeedItem.class);
        RssFeedItem item = new RssFeedItem("velocity", "velocity", "velocity", "velocity");
        dao.create(item);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, RssFeedItem.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addFeedItems(@NonNull List<RssFeedItem> items) {
        RuntimeExceptionDao<RssFeedItem, Integer> dao = getRuntimeExceptionDao(RssFeedItem.class);
        for (RssFeedItem item : items) {
            dao.create(item);
        }
    }

    public List<RssFeedItem> getAllFeedItems() {
        RuntimeExceptionDao<RssFeedItem, Integer> dao = getRuntimeExceptionDao(RssFeedItem.class);
        return dao.queryForAll();
    }
}
