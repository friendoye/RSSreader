package com.friendoye.rss_reader.ui

import com.friendoye.rss_reader.model.RssFeedItem

sealed class Screen {
    object Welcome : Screen()
    object RssFeed : Screen()
    data class RssItemDetails(
        val item: RssFeedItem
    ) : Screen()
}