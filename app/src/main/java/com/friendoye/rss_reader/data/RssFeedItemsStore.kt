package com.friendoye.rss_reader.data

import android.util.Log
import com.friendoye.rss_reader.database.DatabaseHelper
import com.friendoye.rss_reader.model.AbstractRssSourceFactory
import com.friendoye.rss_reader.model.RssFeedItem
import com.j256.ormlite.dao.RuntimeExceptionDao
import com.j256.ormlite.stmt.DeleteBuilder
import java.sql.SQLException
import java.util.*

interface RssFeedItemsStore {
    // TODO: Make more reasonable name/impl
    fun addFeedItems(items: List<RssFeedItem>)
    fun hasItems(): Boolean
    fun getAllFeedItems(sources: List<String>): List<RssFeedItem>?
    fun getFeedItem(link: String, itemClass: Class<*>): RssFeedItem
    fun getAllFeedItems(): List<RssFeedItem>
}