package com.friendoye.rss_reader.model.onliner;

import com.friendoye.rss_reader.model.AbstractRssSourceFactory;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.model.RssParser;

/**
 * Factory implementation for "Tut.by"
 */
public class OnlinerFactory extends AbstractRssSourceFactory {
    public static final String SOURCE = "http://www.onliner.by/feed";

    public RssFeedItem getFeedItem() {
        return new OnlinerFeedItem();
    }

    public RssParser getRssParser() {
        return new OnlinerParser();
    }
}
