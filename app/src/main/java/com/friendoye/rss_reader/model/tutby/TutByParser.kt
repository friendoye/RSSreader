package com.friendoye.rss_reader.model.tutby

import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.model.RssParser
import org.jsoup.nodes.Document
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

/**
 * RssParser for "Tut.by".
 */
class TutByParser : RssParser() {

    @Throws(XmlPullParserException::class, IOException::class)
    override fun reedItem(parser: XmlPullParser): RssFeedItem? {
        val item: RssFeedItem = TutByFeedItem()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val tagName = parser.name
            when (tagName) {
                "title" -> item.title = readText(parser, "title")
                "link" -> item.link = readText(parser, "link")
                "pubDate" -> item.publicationDate = readDate(parser, "pubDate")
                "content" -> if (item.imageUrl == null || !item.imageUrl.endsWith(".jpg")) {
                    item.imageUrl = readImageUrl(parser, "content")
                }
                else -> skipCurrentTag(parser)
            }
        }
        return if (item.link != null && item.link.contains("news.tut.by")) {
            item
        } else {
            null
        }
    }

    @Throws(RuntimeException::class)
    override fun retrieveDescription(doc: Document): String? {
        val buffer = StringBuilder()
        val blocks = doc.select("div[id=\"article_body\"]")
        return try {
            val textBlock = blocks[0]
            for (paragraph in textBlock.getElementsByTag("p")) {
                if (paragraph.hasText()) {
                    val childParagraphs = paragraph.children()
                    if (childParagraphs.size == 1
                        && childParagraphs[0].tagName() == "img"
                    ) {
                        // Do nothing, ignore such tags
                    } else {
                        buffer.append(paragraph.text()).append("\n")
                    }
                }
            }
            val description = buffer.toString()
            val lastCharPos = description.lastIndexOf('.')
            description.substring(0, lastCharPos + 1)
        } catch (e: NullPointerException) {
            throw RuntimeException("No tag was found. Info: $e")
        }
    }
}