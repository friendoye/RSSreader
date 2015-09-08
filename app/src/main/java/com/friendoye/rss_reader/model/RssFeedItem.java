package com.friendoye.rss_reader.model;

import com.j256.ormlite.field.DatabaseField;

/**
 * Model for RSS item.
 */
public class RssFeedItem {
    @DatabaseField(generatedId = true)
    public int id;
    @DatabaseField(index = true)
    public String title;
    @DatabaseField
    public String link;
    @DatabaseField
    public String publicationDate;
    @DatabaseField
    public String imageUrl;

    public RssFeedItem() {
    }

    public RssFeedItem(String title, String link, String publicationDate, String imageUrl) {
        this.title = title;
        this.link = link;
        this.publicationDate = publicationDate;
        this.imageUrl = imageUrl;
    }
}
