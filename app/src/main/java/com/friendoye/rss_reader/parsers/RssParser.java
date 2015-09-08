package com.friendoye.rss_reader.parsers;

import android.util.Xml;

import com.friendoye.rss_reader.model.RssFeedItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class for parsing XML RSS stream.
 */
public class RssParser {
    // TODO: Maintain compatibility not only with "http://www.onliner.by/feed"
    public static List<RssFeedItem> parse(InputStream input)
            throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(input, null);
            parser.next();
            return reedItems(parser);
        } finally {
            input.close();
        }
    }

    protected static List<RssFeedItem> reedItems(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        List<RssFeedItem> items = new LinkedList<>();

        parser.require(XmlPullParser.START_TAG, null, "rss");
        while (skipUntil(parser, "item")) {
            items.add(reedItem(parser));
        }

        return items;
    }

    protected static RssFeedItem reedItem(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "item");
        //RssFeedItem item = new RssFeedItem();
        String title = new String(), link = new String(), publicationDate = new String(), imageUrl = new String();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            if (tagName.equals("title")) {
                title = readTitle(parser);
            } else if (tagName.equals("link")) {
                link = readLink(parser);
            } else if (tagName.equals("pubDate")) {
                publicationDate = readDate(parser);
            } else if (tagName.equals("thumbnail")) {
                imageUrl = readImageUrl(parser);
            } else {
                skipCurrentTag(parser);
            }
        }

        return new RssFeedItem(title, link, publicationDate, imageUrl);
    }

    protected static String readTitle(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "title");
        return title;
    }

    protected static String readLink(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "link");
        String link = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "link");
        return link;
    }

    protected static String readDate(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "pubDate");
        String date = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "pubDate");
        return date;
    }

    protected static String readImageUrl(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "thumbnail");
        String imageUrl = parser.getAttributeValue(null, "url");
        parser.next();
        parser.require(XmlPullParser.END_TAG, null, "thumbnail");
        return imageUrl;
    }

    protected static String readText(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    protected static void skipCurrentTag(XmlPullParser parser)
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

    protected static boolean skipUntil(XmlPullParser parser, String targetName)
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
