package com.friendoye.rss_reader

import com.friendoye.rss_reader.data.RssFeedItemsStore
import com.friendoye.rss_reader.domain.DownloadManager
import com.friendoye.rss_reader.data.RssSourcesStore
import com.friendoye.rss_reader.ui.ToastShower

interface IntegrationDependencies {
    fun getDownloadManager(): DownloadManager
    fun getSourcesStore(): RssSourcesStore
    fun getRssFeedItemsStore(): RssFeedItemsStore
    fun getToastShower(): ToastShower
}