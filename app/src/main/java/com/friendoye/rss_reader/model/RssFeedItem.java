package com.friendoye.rss_reader.model;

import android.graphics.Bitmap;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Model for RSS item.
 */
@DatabaseTable(tableName = "feeds")
abstract public class RssFeedItem implements Comparable<RssFeedItem> {
    public static final String PUB_DATE_KEY = "pub_date";
    public static final String LINK_KEY = "link";

    @DatabaseField(generatedId = true)
    public int id;
    @DatabaseField(canBeNull = false)
    public String title;
    @DatabaseField(columnName = LINK_KEY, canBeNull = false)
    public String link;
    @DatabaseField(columnName = PUB_DATE_KEY, canBeNull = false)
    public Date publicationDate;
    @DatabaseField
    public String imageUrl;
    @DatabaseField
    public String source;

    public Bitmap largeImage;
    public String description;

    public RssFeedItem() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RssFeedItem) {
            RssFeedItem item = (RssFeedItem) obj;
            return link.equals(item.link)
                    || (imageUrl != null && imageUrl.equals(item.imageUrl))
                    || title.contains(item.title)
                    || item.title.contains(title);
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(RssFeedItem item) {
        if (item == null) {
            return -1;
        } else {
            return item.publicationDate.compareTo(publicationDate);
        }
    }
}
