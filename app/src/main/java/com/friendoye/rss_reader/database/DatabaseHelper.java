package com.friendoye.rss_reader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.model.AbstractRssSourceFactory;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.model.onliner.OnlinerFeedItem;
import com.friendoye.rss_reader.model.tutby.TutByFeedItem;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class for executing operation on database.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    public static final String SQL_EXCEPTION_TAG = "SQL Exception";
    public static final long MAX_TABLE_ITEMS_AMOUNT = 50L;

    private static final String DATABASE_NAME = "rss.db";
    private static final int DATABASE_VERSION = 1;

    private static final Class[] CONFIG_CLASSES = {
            OnlinerFeedItem.class, TutByFeedItem.class
    };
    private RuntimeExceptionDao<RssFeedItem, Integer> mRuntimeDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION,
                R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase database,
                         ConnectionSource connectionSource) {
        try {
            for (Class configClass: CONFIG_CLASSES) {
                TableUtils.createTableIfNotExists(connectionSource, configClass);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database,
                          ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
        try {
            for (Class configClass: CONFIG_CLASSES) {
                TableUtils.dropTable(connectionSource, configClass, true);
            }
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addFeedItems(@NonNull List<RssFeedItem> items) {
        Class itemClass = items.get(0).getClass();
        RuntimeExceptionDao<RssFeedItem, Integer> dao = getRuntimeDao(itemClass);
        RssFeedItem lastItem = getFirstItem(dao);

        // If given list has database last item, then we should add
        // only items, that come after. Otherwise, add all items.
        boolean matchLast = false;
        for (RssFeedItem item : items) {
            if (matchLast) {
                dao.create(item);
                continue;
            }
            if (item.equals(lastItem)) {
                matchLast = true;
                dao.delete(lastItem);
                dao.create(item);
            }
        }
        if (!matchLast) {
            for (RssFeedItem item : items) {
                dao.create(item);
            }
        }

        // Leave in table only <MAX_TABLE_ITEMS_AMOUNT> items
        trunkItems(dao);
    }

    public boolean hasItems() {
        for (Class configClass: CONFIG_CLASSES) {
            if (getRuntimeDao(configClass).queryForAll().size() > 0) {
                return true;
            }
        }
        return false;
    }

    public RssFeedItem getFeedItem(int id, Class itemClass) {
        RuntimeExceptionDao<RssFeedItem, Integer> dao = getRuntimeDao(itemClass);
        return dao.queryForId(id);
    }

    public List<RssFeedItem> getAllFeedItems(String[] sources) {
        List<Class> classList = new LinkedList<>();
        for (String source: sources) {
            AbstractRssSourceFactory factory = AbstractRssSourceFactory
                    .getInstance(source);
            if (factory != null) {
                classList.add(factory.getFeedItem().getClass());
            }
        }
        return getAllFeedItems(classList);
    }

    public List<RssFeedItem> getAllFeedItems() {
        return getAllFeedItems(Arrays.asList(CONFIG_CLASSES));
    }

    protected <ID> void trunkItems(RuntimeExceptionDao<RssFeedItem, ID> dao) {
        try {
            PreparedQuery<RssFeedItem> constructedQuery = dao.queryBuilder()
                    .offset(MAX_TABLE_ITEMS_AMOUNT)
                    .orderBy(RssFeedItem.PUB_DATE_KEY, false)
                    .prepare();
            List<RssFeedItem> redundantItems =  dao.query(constructedQuery);
            dao.delete(redundantItems);
        } catch (Exception e) {
            Log.i(SQL_EXCEPTION_TAG,
                    "trunkItems(): failed to prepare query. Info: " + e);
        }
    }

    protected List<RssFeedItem> getAllFeedItems(Collection<Class> sourceClasses) {
    try {
        List<RssFeedItem> items = new ArrayList<>();
        for (Class configClass: sourceClasses) {
            RuntimeExceptionDao<RssFeedItem, Integer> dao = getRuntimeDao(configClass);
            PreparedQuery<RssFeedItem> constructedQuery = dao.queryBuilder()
                    .orderBy(RssFeedItem.PUB_DATE_KEY, false)
                    .prepare();
            items.addAll(dao.query(constructedQuery));
        }
        return items;
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
    }

    protected RuntimeExceptionDao<RssFeedItem, Integer> getRuntimeDao(Class itemClass) {
        if (mRuntimeDao == null || mRuntimeDao.getDataClass() != itemClass) {
            mRuntimeDao = getRuntimeExceptionDao(itemClass);
        }
        return mRuntimeDao;
    }

    protected <ID> RssFeedItem getFirstItem(RuntimeExceptionDao<RssFeedItem, ID> dao) {
        try {
            PreparedQuery<RssFeedItem> constructedQuery = dao.queryBuilder()
                    .orderBy(RssFeedItem.PUB_DATE_KEY, false)
                    .prepare();
            return dao.queryForFirst(constructedQuery);
        } catch (SQLException e) {
            Log.i(SQL_EXCEPTION_TAG,
                    "getFirstItem(): failed to prepare query. Info: " + e);
        }
        return null;
    }

    @Override
    public void close() {
        super.close();
        mRuntimeDao = null;
    }
}
