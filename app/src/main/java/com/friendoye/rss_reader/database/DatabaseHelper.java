package com.friendoye.rss_reader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.data.RssFeedItemsStore;
import com.friendoye.rss_reader.model.AbstractRssSourceFactory;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.model.onliner.OnlinerFeedItem;
import com.friendoye.rss_reader.model.tutby.TutByFeedItem;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class for executing operation on database.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper implements RssFeedItemsStore {
    public static final String SQL_EXCEPTION_TAG = "SQL Exception";
    public static final int MAX_TABLE_ITEMS_AMOUNT = 50;

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

    @Override
    public synchronized void addFeedItems(@NotNull List<? extends RssFeedItem> items) {
        RssFeedItem firstRetrItem = items.get(0);
        Class itemClass = firstRetrItem.getClass();
        RuntimeExceptionDao<RssFeedItem, Integer> dao = getRuntimeDao(itemClass);

        try {
            DeleteBuilder deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where()
                    .gt(RssFeedItem.PUB_DATE_KEY, firstRetrItem.publicationDate)
                    .or().eq("link", firstRetrItem.link)
                    .or().eq("imageUrl", firstRetrItem.imageUrl);
            deleteBuilder.delete();
        } catch (Exception e) {
            throw new RuntimeException("Something bad in DatabaseHelper");
        }

        for (RssFeedItem item : items) {
            dao.create(item);
        }

        // Leave in table only <MAX_TABLE_ITEMS_AMOUNT> items
        List<RssFeedItem> currentItems = getAllFeedItems(itemClass);
        if (currentItems.size() > MAX_TABLE_ITEMS_AMOUNT) {
            List redundantItems = currentItems.subList(MAX_TABLE_ITEMS_AMOUNT,
                    currentItems.size());
            dao.delete(redundantItems);
        }
    }

    public synchronized boolean hasItems() {
        for (Class configClass: CONFIG_CLASSES) {
            if (getRuntimeDao(configClass).queryForAll().size() > 0) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public synchronized List<RssFeedItem> getAllFeedItems(List<String> sources) {
        List<RssFeedItem> list = new LinkedList<>();
        List returnedList;
        for (String source: sources) {
            AbstractRssSourceFactory factory = AbstractRssSourceFactory
                    .getInstance(source);
            if (factory != null) {
                Class itemClass = factory.getFeedItem().getClass();
                returnedList = getAllFeedItems(itemClass);
                list.addAll(returnedList);
            }
        }
        Collections.sort(list);
        return list.size() == 0 ? null : list;
    }

    @NonNull
    public synchronized RssFeedItem getFeedItem(String link, Class itemClass) {
        RuntimeExceptionDao<RssFeedItem, Integer> dao = getRuntimeDao(itemClass);
        return dao.queryForEq(RssFeedItem.LINK_KEY, link).get(0);
    }

    public synchronized List<RssFeedItem> getAllFeedItems() {
        LinkedList<RssFeedItem> list = new LinkedList<>();
        List returnedList;
        for (Class itemClass: CONFIG_CLASSES){
            returnedList = getAllFeedItems(itemClass);
            if (returnedList != null) {
                list.addAll(returnedList);
            }
        }
        return list.size() == 0 ? null : list;
    }

    protected List<RssFeedItem> getAllFeedItems(Class<RssFeedItem> itemClass) {
    try {
        RuntimeExceptionDao<RssFeedItem, Integer> dao = getRuntimeDao(itemClass);
        PreparedQuery<RssFeedItem> constructedQuery = dao.queryBuilder()
                .orderBy(RssFeedItem.PUB_DATE_KEY, false)
                .prepare();
        return  dao.query(constructedQuery);
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
