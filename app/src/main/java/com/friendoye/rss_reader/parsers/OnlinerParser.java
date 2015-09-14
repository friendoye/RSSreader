package com.friendoye.rss_reader.parsers;

import android.graphics.Bitmap;

import com.friendoye.rss_reader.model.OnlinerFeedItem;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.nostra13.universalimageloader.core.ImageLoader;

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

    public OnlinerParser(String source) {
        super(source);
    }

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

        return item;
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
