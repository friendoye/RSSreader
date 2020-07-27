package com.friendoye.rss_reader

import com.friendoye.rss_reader.data.SharedPreferencesRssSourcesStore
import com.friendoye.rss_reader.database.DatabaseHelper
import com.friendoye.rss_reader.database.DatabaseManager
import com.friendoye.rss_reader.domain.DownloadManager
import com.friendoye.rss_reader.utils.RealToastShower

class AndroidIntegrationDependencies(
    private val app: Application
) : IntegrationDependencies {
    private val rssSourcesStore = SharedPreferencesRssSourcesStore(app)
    private val rssFeedItemsStore = DatabaseManager.getHelper(app, DatabaseHelper::class.java)

    override fun getDownloadManager(): DownloadManager = app.downloadManager
    override fun getSourcesStore() = rssSourcesStore
    override fun getRssFeedItemsStore() = rssFeedItemsStore
    override fun getToastShower() = RealToastShower(app)
}