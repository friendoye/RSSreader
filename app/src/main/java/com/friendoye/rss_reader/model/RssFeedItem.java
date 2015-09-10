package com.friendoye.rss_reader.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Model for RSS item.
 */
@DatabaseTable(tableName = "feeds")
public class RssFeedItem {
    public static final String PUB_DATE_KEY = "pub_date";

    @DatabaseField(generatedId = true)
    public int id;
    @DatabaseField(canBeNull = false)
    public String title;
    @DatabaseField(canBeNull = false)
    public String link;
    @DatabaseField(columnName = PUB_DATE_KEY, canBeNull = false)
    public Date publicationDate;
    @DatabaseField
    public String imageUrl;

    public String description;

    public RssFeedItem() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RssFeedItem) {
            RssFeedItem item = (RssFeedItem) obj;
            return title.equals(item.title)
                    && link.equals(item.link)
                    && publicationDate.equals(item.publicationDate)
                    && imageUrl.equals(item.imageUrl);
        } else {
            return false;
        }
    }
}
