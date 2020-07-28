package com.friendoye.rss_reader.domain

import android.graphics.Bitmap
import com.friendoye.rss_reader.model.RssFeedItem

interface RssFeedItemDetailsFetcher {
    data class Result(
        val description: String,
        val largeImage: Bitmap
    )

    suspend fun getDetails(feedItem: RssFeedItem): Result?
}