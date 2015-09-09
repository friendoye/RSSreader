package com.friendoye.rss_reader.parsers;

import android.app.DatePickerDialog;
import android.util.Log;
import android.util.Xml;

import com.friendoye.rss_reader.model.RssFeedItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Helper class for parsing XML RSS stream.
 */
public class RssParser {
    public static final String PARSE_EXCEPTION_TAG = "ParseException";

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
        RssFeedItem item = new RssFeedItem();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            switch (tagName) {
            case "title":
                item.title = readTitle(parser);
                break;
            case "link":
                item.link = readLink(parser);
                break;
            case "pubDate":
                item.publicationDate = readDate(parser);
                break;
            case "thumbnail":
                item.imageUrl = readImageUrl(parser);
                break;
            default:
                skipCurrentTag(parser);
                break;
            }
        }

        return item;
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

    protected static Date readDate(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "pubDate");
        String stringDate = readText(parser);
        Date date;
        try {
            SimpleDateFormat formatter =
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",
                                         Locale.ENGLISH);
            date = formatter.parse(stringDate);
        } catch (ParseException e) {
            Log.i(PARSE_EXCEPTION_TAG,
                    "readDate(): invalid parser. Info: " + e);
            date = null;
        }
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
