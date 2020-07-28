package com.friendoye.rss_reader.model.onliner

import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.model.RssParser
import org.jsoup.nodes.Document
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

/**
 * RssParser for "Onliner".
 */
class OnlinerParser : RssParser() {

    @Throws(XmlPullParserException::class, IOException::class)
    override fun reedItem(parser: XmlPullParser): RssFeedItem? {
        val item: RssFeedItem = OnlinerFeedItem()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val tagName = parser.name
            when (tagName) {
                "title" -> item.title = readText(parser, "title")
                "link" -> item.link = readText(parser, "link")
                "pubDate" -> item.publicationDate = readDate(parser, "pubDate")
                "thumbnail" -> item.imageUrl = readImageUrl(parser, "thumbnail")
                else -> skipCurrentTag(parser)
            }
        }
        return if (item.link != null && item.link.contains("onliner.by")) {
            item
        } else {
            null
        }
    }

    @Throws(RuntimeException::class)
    override fun retrieveDescription(doc: Document): String? {
        var description: String?
        try {
            // News page
            val newsPatterns = arrayOf(
                "div[class=\"news-text\"]"
            )
            description = retrieveText(doc, newsPatterns)
            if (description != null) {
                return description
            }
            // Opinion page
            val opinionPatterns = arrayOf(
                "div[class=\"b-opinions-body__lead\"]",
                "div[class=\"b-opinions-body__content\"]"
            )
            description = retrieveText(doc, opinionPatterns)
        } catch (e: NullPointerException) {
            throw RuntimeException("No tag was found. Info: $e")
        }
        return description
    }

    @Throws(NullPointerException::class)
    private fun retrieveText(doc: Document, blockPatterns: Array<String>): String? {
        val buffer = StringBuilder()
        for (pattern in blockPatterns) {
            val blocks = doc.select(pattern)
            if (blocks.size == 0) {
                return null
            }
            var bufferSize: Int
            for (textBlock in blocks) {
                for (paragraph in textBlock.getElementsByTag("p")) {
                    if (paragraph.hasText()) {
                        val childParagraphs = paragraph.children()
                        if (childParagraphs.size == 1
                            && paragraph.ownText() == ""
                        ) {
                            // Do nothing, ignore such tags
                        } else {
                            buffer.append(paragraph.text()).append("\n")
                        }
                    }
                }
                bufferSize = buffer.length
                buffer.substring(0, bufferSize)
            }
        }
        return buffer.toString()
    }
}