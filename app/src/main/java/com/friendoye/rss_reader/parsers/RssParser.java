package com.friendoye.rss_reader.parsers;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Xml;

import com.friendoye.rss_reader.model.RssFeedItem;

import org.jsoup.nodes.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Base class for all parsers of RSS feed and detailed link.
 */
abstract public class RssParser {
    public static final String PARSE_EXCEPTION_TAG = "ParseException";

    protected String mSource;

    public RssParser(String source) {
        this.mSource = source;
    }

    public List<RssFeedItem> parseRssStream(InputStream input)
            throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(input, null);
            parser.next();
            return reedItems(parser);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    abstract protected RssFeedItem reedItem(XmlPullParser parser)
            throws XmlPullParserException, IOException;

    abstract public String retrieveDescription(Document doc)
            throws RuntimeException;

    abstract public Bitmap retrieveLargeImage(Document doc)
            throws RuntimeException;

    public static RssParser getInstance(String source) {
        switch (source) {
            case "http://www.onliner.by/feed":
                return new OnlinerParser("http://www.onliner.by/feed");
            case "http://news.tut.by/rss/all.rss":
                return new TutByParser("http://news.tut.by/rss/all.rss");
            default:
                /* Falls through */
        }
        return null;
    }

    /**
     * Return reversed list of RssFeedItem from XML.
     */
    protected List<RssFeedItem> reedItems(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        LinkedList<RssFeedItem> items = new LinkedList<>();

        parser.require(XmlPullParser.START_TAG, null, "rss");
        while (skipUntil(parser, "item")) {
            parser.require(XmlPullParser.START_TAG, null, "item");
            RssFeedItem item = reedItem(parser);
            item.source = mSource;
            items.addFirst(item);
        }

        return items;
    }

    protected String readText(XmlPullParser parser, String tagName)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, tagName);
        String title = retrieveText(parser);
        parser.require(XmlPullParser.END_TAG, null, tagName);
        return title;
    }

    protected Date readDate(XmlPullParser parser, String tagName)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, tagName);
        String stringDate = retrieveText(parser);
        Date date = null;;
        try {
            final SimpleDateFormat formatter =
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss",
                            Locale.ENGLISH);
            date = formatter.parse(stringDate);
        } catch (ParseException e) {
            Log.i(PARSE_EXCEPTION_TAG,
                    "readDate(): invalid parser. Info: " + e);
        }
        parser.require(XmlPullParser.END_TAG, null, tagName);
        return date;
    }

    protected String readImageUrl(XmlPullParser parser, String tagName)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, tagName);
        String imageUrl = parser.getAttributeValue(null, "url");
        parser.next();
        parser.require(XmlPullParser.END_TAG, null, tagName);
        return imageUrl;
    }

    protected String retrieveText(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    protected void skipCurrentTag(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    protected boolean skipUntil(XmlPullParser parser, String targetName)
            throws XmlPullParserException, IOException {
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            if (tagName.equals(targetName)) {
                return true;
            }
        }
        return false;
    }

}
