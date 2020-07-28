package com.friendoye.rss_reader.domain

import com.friendoye.rss_reader.domain.RssFeedItemDetailsFetcher.Result
import com.friendoye.rss_reader.model.AbstractRssSourceFactory
import com.friendoye.rss_reader.model.RssFeedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException

class RealRssFeedItemDetailsFetcher : RssFeedItemDetailsFetcher {

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(IOException::class)
    override suspend fun getDetails(feedItem: RssFeedItem): Result? {
        val link = feedItem.link
        val parser = AbstractRssSourceFactory
            .getInstance(feedItem.source).rssParser

        return withContext(Dispatchers.IO) {
            val doc = Jsoup.connect(link).timeout(5000).get()
            Result(
                description = parser.retrieveDescription(doc) ?: return@withContext null,
                largeImage = parser.retrieveLargeImage(doc) ?: return@withContext null
            )
        }
    }
}