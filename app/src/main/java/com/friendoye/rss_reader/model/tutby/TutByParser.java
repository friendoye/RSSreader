package com.friendoye.rss_reader.model.tutby;

import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.model.RssParser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * RssParser for "Tut.by".
 */
public class TutByParser extends RssParser {

    protected RssFeedItem reedItem(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        RssFeedItem item = new TutByFeedItem();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            switch (tagName) {
                case "title":
                    item.title = readText(parser, "title");
                    break;
                case "link":
                    item.link = readText(parser, "link");
                    break;
                case "pubDate":
                    item.publicationDate = readDate(parser, "pubDate");
                    break;
                case "content":
                    if (item.imageUrl == null ||!item.imageUrl.endsWith(".jpg")) {
                        item.imageUrl = readImageUrl(parser, "content");
                    }
                    break;
                default:
                    skipCurrentTag(parser);
                    break;
            }
        }

        return item;
    }

    public String retrieveDescription(Document doc)
            throws RuntimeException {
        StringBuilder buffer = new StringBuilder();
        Elements blocks =
                doc.select("div[id=\"article_body\"]");
        try {
            Element textBlock = blocks.get(0);
            for (Element paragraph : textBlock.getElementsByTag("p")) {
                if (paragraph.hasText()) {
                    Elements childParagraphs = paragraph.children();
                    if (childParagraphs.size() == 1
                            && childParagraphs.get(0).tagName().equals("img")) {
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
}
