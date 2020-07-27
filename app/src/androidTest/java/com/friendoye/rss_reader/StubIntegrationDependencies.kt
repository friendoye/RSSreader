package com.friendoye.rss_reader

import androidx.annotation.StringRes
import com.friendoye.rss_reader.data.RssFeedItemsStore
import com.friendoye.rss_reader.data.RssSourcesStore
import com.friendoye.rss_reader.domain.DownloadManager
import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.ui.ToastShower
import com.friendoye.rss_reader.utils.LoadingState
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.test.TestCoroutineScope
import java.util.concurrent.CopyOnWriteArraySet

@ExperimentalCoroutinesApi
class StubIntegrationDependencies(
    private val allSources: List<String>,
    private val downloadManagerSuccessFeedItems: List<RssFeedItem>
) : IntegrationDependencies {
    internal val stubSourcesStore = InMemorySourcesStore(allSources)
    internal val stubFeedItemsStore = InMemoryFeeItemsStore()
    internal val stubToastShower = StubToastShower()
    internal val stubDownloadManager = StubDownloadManager(stubFeedItemsStore).apply {
        successFeedItems = downloadManagerSuccessFeedItems
    }

    override fun getDownloadManager() = stubDownloadManager
    override fun getSourcesStore() = stubSourcesStore
    override fun getRssFeedItemsStore() = stubFeedItemsStore
    override fun getToastShower() = stubToastShower
}

@ExperimentalCoroutinesApi
class StubDownloadManager(
    private val rssFeedItemsStore: RssFeedItemsStore
) : DownloadManager {
    internal val stateFlow: MutableStateFlow<LoadingState> = MutableStateFlow(LoadingState.NONE)
    internal val resultState = LoadingState.SUCCESS
    internal val taskScope = TestCoroutineScope()
    internal val notificationScope = MainScope()

    internal var successFeedItems: List<RssFeedItem> = listOf()

    private val refreshChannel = Channel<List<String>>(1)
    private val observers = CopyOnWriteArraySet<DownloadManager.OnDownloadStateChangedListener>()

    init {
        taskScope.launch {
            refreshChannel.consumeEach { sources ->
                stateFlow.value = LoadingState.LOADING
                //delay(1000)
                if (resultState == LoadingState.SUCCESS) {
                    rssFeedItemsStore.addFeedItems(successFeedItems)
                }
                stateFlow.value = resultState
            }
        }

        notificationScope.launch {
            stateFlow.collect { state ->
                observers.forEach { observer ->
                    observer.onDownloadStateChanged(state)
                }
            }
        }
    }

    override fun getState(): LoadingState = stateFlow.value

    override fun refreshData(sources: List<String>) {
        refreshChannel.offer(sources)
    }

    override fun subscribe(observer: DownloadManager.OnDownloadStateChangedListener) {
        observers.add(observer)
    }

    override fun unsubscribe(observer: DownloadManager.OnDownloadStateChangedListener) {
        observers.remove(observer)
    }
}

class InMemorySourcesStore(
    internal val allSources: List<String>
) : RssSourcesStore {
    internal var activeSources = allSources.toList()
        private set

    override fun getActiveSources(): List<String> = activeSources

    override fun setActiveSources(sources: List<String>) {
        activeSources = sources
    }

    override fun getAllSources(): List<String> = allSources
}

class InMemoryFeeItemsStore : RssFeedItemsStore {
    internal var rssItems = listOf<RssFeedItem>()

    override fun addFeedItems(items: List<RssFeedItem>) {
        rssItems = items
    }

    override fun hasItems(): Boolean {
        return rssItems.isNotEmpty()
    }

    override fun getAllFeedItems(sources: List<String>): List<RssFeedItem>? {
        return rssItems.filter { sources.contains(it.source) }
    }

    override fun getFeedItem(link: String, itemClass: Class<*>): RssFeedItem {
        TODO("Not yet implemented")
    }

    override fun getAllFeedItems(): List<RssFeedItem> {
        return rssItems
    }
}

class StubToastShower : ToastShower {
    internal var currentToastMessage: Int? = null
        private set

    override fun showShort(@StringRes messageRes: Int) {
        currentToastMessage = messageRes
    }

    override fun showLong(@StringRes messageRes: Int) {
        currentToastMessage = messageRes
    }
}