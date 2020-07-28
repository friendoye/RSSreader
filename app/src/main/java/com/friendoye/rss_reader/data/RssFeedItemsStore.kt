package com.friendoye.rss_reader.data

import com.friendoye.rss_reader.data.database.DatabaseHelper
import com.friendoye.rss_reader.model.RssFeedItem

interface RssFeedItemsStore {
    // TODO: Make more reasonable name/impl
    fun addFeedItems(items: List<RssFeedItem>)
    fun hasItems(): Boolean
    fun getAllFeedItems(sources: List<String>): List<RssFeedItem>?
    fun getFeedItem(link: String, itemClass: Class<*>): RssFeedItem
    fun getAllFeedItems(): List<RssFeedItem>
}