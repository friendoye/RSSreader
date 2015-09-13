package com.friendoye.rss_reader.parsers;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Xml;

import com.friendoye.rss_reader.model.RssFeedItem;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
 * RssParser for Onliner.
 */
class OnlinerParser extends RssParser {
    public static final String PARSE_EXCEPTION_TAG = "ParseException";

    public OnlinerParser(String source) {
        super(source);
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

    /**
     * Return reversed list of RssFeedItem from XML.
     */
    protected List<RssFeedItem> reedItems(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        LinkedList<RssFeedItem> items = new LinkedList<>();

        parser.require(XmlPullParser.START_TAG, null, "rss");
        while (skipUntil(parser, "item")) {
            items.addFirst(reedItem(parser));
        }

        return items;
    }

    protected RssFeedItem reedItem(XmlPullParser parser)
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
        item.source = mSource;
        return item;
    }

    protected String readTitle(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "title");
        return title;
    }

    protected String readLink(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "link");
        String link = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "link");
        return link;
    }

    protected Date readDate(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "pubDate");
        String stringDate = readText(parser);
        Date date;
        try {
            final SimpleDateFormat formatter =
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss",
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

    protected String readImageUrl(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "thumbnail");
        String imageUrl = parser.getAttributeValue(null, "url");
        parser.next();
        parser.require(XmlPullParser.END_TAG, null, "thumbnail");
        return imageUrl;
    }

    protected String readText(XmlPullParser parser)
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

    public String retrieveDescription(Document doc)
            throws RuntimeException {
        StringBuilder buffer = new StringBuilder();
        Elements blocks =
                doc.select("div[class=\"b-posts-1-item__text\"]");
        try {
            Element textBlock = blocks.get(0);
            for (Element paragraph : textBlock.getElementsByTag("p")) {
                if (paragraph.hasText()) {
                    Elements childParagraphs = paragraph.children();
                    if (childParagraphs.size() == 1
                            && paragraph.ownText().equals("")) {
                        // Do nothing, ignore such tags
                    } else {
                        buffer.append(paragraph.text()).append("\n");
                    }
                }
            }
            String description = buffer.toString();
            int lastCharPos = description.lastIndexOf('.');
            return description.substring(0, lastCharPos + 1);
        } catch (NullPointerException e) {
            throw new RuntimeException("No tag was found. Info: " + e);
        }
    }

    public Bitmap retrieveLargeImage(Document doc)
            throws RuntimeException {
        Elements blocks =
                doc.select("figure[class=\"b-posts-1-item__image\"]");
        try {
            Elements imageBlock = blocks.get(0).getElementsByTag("img");
            String imageLink = imageBlock.get(0).attr("src");
            if (imageLink != null) {
                return ImageLoader.getInstance().loadImageSync(imageLink);
            } else {
                throw new RuntimeException("No link in tag!");
            }
        } catch (NullPointerException e) {
            throw new RuntimeException("No tag was found. Info: " + e);
        }
    }
}
