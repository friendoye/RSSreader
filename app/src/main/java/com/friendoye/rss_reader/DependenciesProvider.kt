package com.friendoye.rss_reader

import com.friendoye.rss_reader.utils.RssSourcesStore
import com.friendoye.rss_reader.database.DatabaseHelper
import com.friendoye.rss_reader.database.DatabaseManager
import com.friendoye.rss_reader.utils.DownloadManager
import com.friendoye.rss_reader.utils.ToastShower

object DependenciesProvider {
    private val app = Application.getInstance()

    fun getDownloadManager(): DownloadManager = app.downloadManager
    fun getSourcesStore(): RssSourcesStore =
        RssSourcesStore(app)
    fun getDatabaseHelper() = DatabaseManager.getHelper(app, DatabaseHelper::class.java)
    fun getToastShower() = ToastShower(app)
}