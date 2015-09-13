package com.friendoye.rss_reader.parsers;

import android.graphics.Bitmap;

import com.friendoye.rss_reader.model.RssFeedItem;

import org.jsoup.nodes.Document;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Base class for all parsers of RSS feed and detailed link.
 */
abstract public class RssParser {
    protected String mSource;

    public RssParser(String source) {
        this.mSource = source;
    }

    abstract public List<RssFeedItem> parseRssStream(InputStream input)
            throws XmlPullParserException, IOException;

    abstract public String retrieveDescription(Document doc)
            throws RuntimeException;

    abstract public Bitmap retrieveLargeImage(Document doc)
            throws RuntimeException;

    public static RssParser getInstance(String source) {
        switch (source) {
            case "http://www.onliner.by/feed":
                return new OnlinerParser("http://www.onliner.by/feed");
            default:
                /* Falls through */
        }
        return null;
    }

}
