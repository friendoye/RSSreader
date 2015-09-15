package com.friendoye.rss_reader.model.tutby;

import com.friendoye.rss_reader.model.AbstractRssSourceFactory;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.model.RssParser;

/**
 * Factory implementation for "Tut.by"
 */
public class TutByFactory extends AbstractRssSourceFactory {
    public static final String SOURCE = "http://news.tut.by/rss/all.rss";

    public RssFeedItem getFeedItem() {
        return new TutByFeedItem();
    }

    public RssParser getRssParser() {
        return new TutByParser();
    }
}
