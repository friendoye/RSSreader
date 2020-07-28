package com.friendoye.rss_reader

import com.friendoye.rss_reader.di.AndroidIntegrationDependencies
import com.friendoye.rss_reader.di.IntegrationDependencies
import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.ui.details.DetailsWorkflow
import com.friendoye.rss_reader.ui.rssfeed.RssFeedWorkflow
import com.friendoye.rss_reader.ui.welcome.WelcomeWorkflow
import kotlinx.coroutines.ExperimentalCoroutinesApi

object DependenciesProvider : IntegrationDependencies {
    var integrationDepsDelegate : IntegrationDependencies = AndroidIntegrationDependencies(
        app = Application.getInstance()
    )

    override fun getDownloadManager() = integrationDepsDelegate.getDownloadManager()
    override fun getSourcesStore() = integrationDepsDelegate.getSourcesStore()
    override fun getRssFeedItemsStore() = integrationDepsDelegate.getRssFeedItemsStore()
    override fun getToastShower() = integrationDepsDelegate.getToastShower()
    override fun getRssFeedItemDetailsFetcher() = integrationDepsDelegate.getRssFeedItemDetailsFetcher()

    @ExperimentalCoroutinesApi
    fun provideWelcomeWorkflow() = WelcomeWorkflow(
        getDownloadManager(),
        getSourcesStore(),
        getRssFeedItemsStore()
    )

    @ExperimentalCoroutinesApi
    fun provideRssFeedWorkflow() = RssFeedWorkflow(
        getDownloadManager(),
        getSourcesStore(),
        getRssFeedItemsStore(),
        getToastShower()
    )

    fun provideDetailsWorkflow(item: RssFeedItem) = DetailsWorkflow(
        item,
        getRssFeedItemDetailsFetcher()
    )
}