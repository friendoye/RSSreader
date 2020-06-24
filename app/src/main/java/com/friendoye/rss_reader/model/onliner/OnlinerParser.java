package com.friendoye.rss_reader.model.onliner;

import androidx.annotation.Nullable;

import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.model.RssParser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * RssParser for "Onliner".
 */
public class OnlinerParser extends RssParser {

    @Nullable
    protected RssFeedItem reedItem(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        RssFeedItem item = new OnlinerFeedItem();
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
                case "thumbnail":
                    item.imageUrl = readImageUrl(parser, "thumbnail");
                    break;
                default:
                    skipCurrentTag(parser);
                    break;
            }
        }

        if (item.link != null && item.link.contains("onliner.by")) {
            return item;
        } else {
            return null;
        }
    }

    public String retrieveDescription(Document doc)
            throws RuntimeException {
        String description;
        try {
            // News page
            final String[] newsPatterns = {
                    "div[class=\"news-text\"]"
            };
            description = retrieveText(doc, newsPatterns);
            if (description != null) {
                return description;
            }
            // Opinion page
            String[] opinionPatterns = {
                    "div[class=\"b-opinions-body__lead\"]",
                    "div[class=\"b-opinions-body__content\"]"
            };
            description = retrieveText(doc, opinionPatterns);
        } catch (NullPointerException e) {
            throw new RuntimeException("No tag was found. Info: " + e);
        }
        return description;
    }

    private String retrieveText(Document doc, String[] blockPatterns)
            throws NullPointerException {
        StringBuilder buffer = new StringBuilder();

        for (String pattern: blockPatterns) {
            Elements blocks = doc.select(pattern);
            if (blocks.size() == 0) {
                return null;
            }

            int bufferSize;
            for (Element textBlock : blocks) {
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
                bufferSize = buffer.length();
                buffer.substring(0, bufferSize);
            }
        }

        return buffer.toString();
    }
}
