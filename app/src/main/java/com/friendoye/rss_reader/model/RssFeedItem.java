package com.friendoye.rss_reader.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Model for RSS item.
 */
@DatabaseTable(tableName = "feeds")
public class RssFeedItem {
    @DatabaseField(generatedId = true)
    public int id;
    @DatabaseField(index = true)
    public String title;
    @DatabaseField
    public String link;
    @DatabaseField
    public Date publicationDate;
    @DatabaseField
    public String imageUrl;

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
