package com.friendoye.rss_reader.ui.rssfeed

import com.friendoye.rss_reader.data.RssFeedItemsStore
import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.ui.rssfeed.RssFeedWorkflow.Input
import com.friendoye.rss_reader.ui.rssfeed.RssFeedWorkflow.Output
import com.friendoye.rss_reader.ui.rssfeed.RssFeedWorkflow.Output.NavigateToDetails
import com.friendoye.rss_reader.ui.rssfeed.RssFeedWorkflow.Output.NavigateToSourcesListDialog
import com.friendoye.rss_reader.domain.DownloadManager
import com.friendoye.rss_reader.utils.LoadingState
import com.friendoye.rss_reader.data.RssSourcesStore
import com.friendoye.rss_reader.ui.ToastShower
import com.friendoye.rss_reader.ui.shared.workers.RefreshFeedWorker
import com.squareup.workflow.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class RssFeedWorkflow(
    private val downloadManager: DownloadManager,
    private val sourcesStore: RssSourcesStore,
    private var feedItemsStore: RssFeedItemsStore,
    private val toastShower: ToastShower
) : StatefulWorkflow<Input, RssFeedWorkflow.InternalState, Output, RssFeedScreenState>() {

    data class Input(
        val sources: List<String>
    )

    data class InternalState(
        val loadingState: LoadingState,
        val rssFeedItems: List<RssFeedItem>
    )

    sealed class Output {
        data class NavigateToDetails(
            val rssFeedItem: RssFeedItem
        ) : Output()

        object NavigateToSourcesListDialog : Output()
    }

    override fun initialState(props: Input, snapshot: Snapshot?): InternalState {
        return InternalState(
            loadingState = LoadingState.SUCCESS,
            rssFeedItems = feedItemsStore.getAllFeedItems(props.sources) ?: emptyList()
        )
    }

    override fun onPropsChanged(old: Input, new: Input, state: InternalState): InternalState {
        return state.copy(
            rssFeedItems = feedItemsStore.getAllFeedItems(new.sources) ?: emptyList()
        )
    }

    override fun render(
        props: Input,
        state: InternalState,
        context: RenderContext<InternalState, Output>
    ): RssFeedScreenState {
        when (state.loadingState) {
            LoadingState.NONE,
            LoadingState.LOADING -> {
                val sources = props.sources
                context.runningWorker(refreshWorker(sources)) {
                    updateLoadingState(it, sources)
                }
            }
            LoadingState.FAILURE -> {
                // TODO: Run side effect with Toast
            }
            LoadingState.SUCCESS -> {
                // Do nothing
            }
        }

        return RssFeedScreenState(
            loadingState = state.loadingState,
            isSwipeToRefreshInProgress = state.rssFeedItems.isNotEmpty()
                    && state.loadingState == LoadingState.LOADING,
            rssFeedItems = state.rssFeedItems,
            onRefresh = { context.actionSink.send(refresh()) },
            onPickRssSources = { context.actionSink.send(openRssSources()) },
            onRssFeedItemClick = { context.actionSink.send(openRssItemDetails(it)) }
        )
    }

    override fun snapshotState(state: InternalState): Snapshot = Snapshot.EMPTY

    private fun refreshWorker(sources: List<String>): Worker<LoadingState> {
        return RefreshFeedWorker(
            downloadManager,
            sources
        )
    }

    private fun refresh() = action("refresh") {
        nextState = nextState.copy(
            loadingState = LoadingState.LOADING
        )
    }

    private fun openRssSources() = action("openRssSources") {
        setOutput(NavigateToSourcesListDialog)
    }

    private fun openRssItemDetails(feedItem: RssFeedItem) = action("openRssItemDetails") {
        setOutput(NavigateToDetails(feedItem))
    }

    private fun updateLoadingState(loadingState: LoadingState, sources: List<String>) = action("updateLoadingState") {
        nextState = nextState.copy(
            loadingState = if (loadingState == LoadingState.FAILURE) {
                LoadingState.NONE
            } else {
                loadingState
            },
            rssFeedItems = if (loadingState == LoadingState.SUCCESS) {
                feedItemsStore.getAllFeedItems(sources) ?: emptyList()
            } else {
                nextState.rssFeedItems
            }
        )
    }

}