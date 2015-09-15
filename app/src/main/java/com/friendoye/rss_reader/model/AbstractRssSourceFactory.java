package com.friendoye.rss_reader.model;

import com.friendoye.rss_reader.model.onliner.OnlinerFactory;
import com.friendoye.rss_reader.model.tutby.TutByFactory;

/**
 * Abstract factory for creating necessary classes for convenient RSS source.
 */
abstract public class AbstractRssSourceFactory {

    abstract public RssFeedItem getFeedItem();

    abstract public RssParser getRssParser();

    public static AbstractRssSourceFactory getInstance(String source) {
        switch (source) {
            case OnlinerFactory.SOURCE:
                return new OnlinerFactory();
            case TutByFactory.SOURCE:
                return new TutByFactory();
            default:
                /* Falls through */
        }
        return null;
    }

}
