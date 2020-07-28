package com.friendoye.rss_reader.model

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import coil.ImageLoader.Companion.invoke
import android.util.Xml
import coil.ImageLoader
import coil.request.GetRequest
import com.friendoye.rss_reader.Application
import org.jsoup.nodes.Document
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.lang.IndexOutOfBoundsException
import java.lang.NullPointerException
import java.lang.RuntimeException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Base class for all parsers of RSS feed and detailed link.
 */
abstract class RssParser {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parseRssStream(input: InputStream?): List<RssFeedItem> {
        return try {
            val parser = Xml.newPullParser()
            parser.setInput(input, null)
            parser.next()
            reedItems(parser)
        } finally {
            input?.close()
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    protected abstract fun reedItem(parser: XmlPullParser): RssFeedItem?

    @Throws(RuntimeException::class)
    abstract fun retrieveDescription(doc: Document): String?

    @Throws(RuntimeException::class)
    suspend fun retrieveLargeImage(doc: Document): Bitmap? {
        val blocks = doc.select("meta[property^=\"og:image\"]")
        try {
            val imageLink = blocks[0].attr("content")
            if (imageLink != null) {
                // TODO: Replace with Coil
                val request: GetRequest = GetRequest.Builder(Application.getInstance())
                    .data(imageLink)
                    .allowHardware(false) // Disable hardware bitmaps.
                    .build()
                return (ImageLoader(Application.getInstance())
                    .execute(request)
                    .drawable as? BitmapDrawable)
                    ?.bitmap
            } else {
                Log.e(PARSE_EXCEPTION_TAG, "No link in tag!")
            }
        } catch (e: NullPointerException) {
            Log.e(PARSE_EXCEPTION_TAG, "No tag was found. Info: $e")
        } catch (e: IndexOutOfBoundsException) {
            Log.e(PARSE_EXCEPTION_TAG, "No tag was found. Info: $e")
        }
        return null
    }

    /**
     * ============== HELP METHODS ====================
     */

    /**
     * Return reversed list of RssFeedItem from XML.
     */
    @Throws(XmlPullParserException::class, IOException::class)
    protected fun reedItems(parser: XmlPullParser): List<RssFeedItem> {
        val items = LinkedList<RssFeedItem>()
        parser.require(XmlPullParser.START_TAG, null, "rss")
        while (skipUntil(parser, "item")) {
            parser.require(XmlPullParser.START_TAG, null, "item")
            val item = reedItem(parser)
            if (item != null) {
                items.addFirst(item) // This method should be implemented
            }
        }
        return items
    }

    @Throws(XmlPullParserException::class, IOException::class)
    protected fun readText(parser: XmlPullParser, tagName: String?): String {
        parser.require(XmlPullParser.START_TAG, null, tagName)
        val title = retrieveText(parser)
        parser.require(XmlPullParser.END_TAG, null, tagName)
        return title
    }

    @Throws(XmlPullParserException::class, IOException::class)
    protected fun readDate(parser: XmlPullParser, tagName: String?): Date? {
        parser.require(XmlPullParser.START_TAG, null, tagName)
        val stringDate = retrieveText(parser)
        var date: Date? = null
        try {
            val formatter = SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss",
                Locale.ENGLISH
            )
            date = formatter.parse(stringDate)
        } catch (e: ParseException) {
            Log.i(
                PARSE_EXCEPTION_TAG,
                "readDate(): invalid parser. Info: $e"
            )
        }
        parser.require(XmlPullParser.END_TAG, null, tagName)
        return date
    }

    @Throws(XmlPullParserException::class, IOException::class)
    protected fun readImageUrl(parser: XmlPullParser, tagName: String?): String {
        parser.require(XmlPullParser.START_TAG, null, tagName)
        val imageUrl = parser.getAttributeValue(null, "url")
        parser.next()
        parser.require(XmlPullParser.END_TAG, null, tagName)
        return imageUrl
    }

    @Throws(IOException::class, XmlPullParserException::class)
    protected fun retrieveText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    protected fun skipCurrentTag(parser: XmlPullParser) {
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    protected fun skipUntil(parser: XmlPullParser, targetName: String): Boolean {
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val tagName = parser.name
            if (tagName == targetName) {
                return true
            }
        }
        return false
    }

    companion object {
        const val PARSE_EXCEPTION_TAG = "ParseException"
    }
}