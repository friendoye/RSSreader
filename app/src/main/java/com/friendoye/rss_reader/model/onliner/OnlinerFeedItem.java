package com.friendoye.rss_reader.model.onliner;

import com.friendoye.rss_reader.model.RssFeedItem;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Onliner RssFeedItem.
 */
@DatabaseTable(tableName = "onliner_feeds")
public class OnlinerFeedItem extends RssFeedItem {

    public OnlinerFeedItem() {
        source = OnlinerFactory.SOURCE;
    }
};
