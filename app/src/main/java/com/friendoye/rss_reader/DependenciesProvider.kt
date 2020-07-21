package com.friendoye.rss_reader

import com.friendoye.rss_reader.utils.RssSourcesStore
import com.friendoye.rss_reader.database.DatabaseHelper
import com.friendoye.rss_reader.database.DatabaseManager
import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.ui.details.DetailsWorkflow
import com.friendoye.rss_reader.ui.rssfeed.RssFeedWorkflow
import com.friendoye.rss_reader.ui.welcome.WelcomeWorkflow
import com.friendoye.rss_reader.utils.DownloadManager
import com.friendoye.rss_reader.utils.ToastShower
import kotlinx.coroutines.ExperimentalCoroutinesApi

object DependenciesProvider {
    private val app = Application.getInstance()

    fun getDownloadManager(): DownloadManager = app.downloadManager
    fun getSourcesStore(): RssSourcesStore =
        RssSourcesStore(app)
    fun getDatabaseHelper() = DatabaseManager.getHelper(app, DatabaseHelper::class.java)
    fun getToastShower() = ToastShower(app)

    @ExperimentalCoroutinesApi
    fun provideWelcomeWorkflow() = WelcomeWorkflow(
        getDownloadManager(),
        getSourcesStore(),
        getDatabaseHelper()
    )

    @ExperimentalCoroutinesApi
    private val RssFeedWorkflow by lazy {
        RssFeedWorkflow(
            getDownloadManager(),
            getSourcesStore(),
            getDatabaseHelper(),
            getToastShower()
        )
    }
    @ExperimentalCoroutinesApi
    fun provideRssFeedWorkflowSingleton() = RssFeedWorkflow
    fun provideDetailsWorkflow(item: RssFeedItem) = DetailsWorkflow(item)
}