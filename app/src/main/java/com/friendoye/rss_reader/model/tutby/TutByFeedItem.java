package com.friendoye.rss_reader.model.tutby;

import com.friendoye.rss_reader.model.RssFeedItem;
import com.j256.ormlite.table.DatabaseTable;

/**
 * TutBy RssFeedItem.
 */
@DatabaseTable(tableName = "tut_by_feeds")
public class TutByFeedItem extends RssFeedItem {

    public TutByFeedItem() {
        source = TutByFactory.SOURCE;
    }
};
