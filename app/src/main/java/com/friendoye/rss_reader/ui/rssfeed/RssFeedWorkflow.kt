package com.friendoye.rss_reader.ui.rssfeed

import com.friendoye.rss_reader.FeatureFlags
import com.friendoye.rss_reader.database.DatabaseHelper
import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.ui.rssfeed.RssFeedWorkflow.Output
import com.friendoye.rss_reader.ui.rssfeed.RssFeedWorkflow.Output.NavigateToDetails
import com.friendoye.rss_reader.ui.rssfeed.RssFeedWorkflow.Output.NavigateToSourcesListDialog
import com.friendoye.rss_reader.utils.DownloadManager
import com.friendoye.rss_reader.utils.LoadingState
import com.friendoye.rss_reader.utils.RssSourcesStore
import com.friendoye.rss_reader.utils.ToastShower
import com.friendoye.rss_reader.workers.RefreshFeedWorker
import com.squareup.workflow.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class RssFeedWorkflow(
    private val downloadManager: DownloadManager,
    private val sourcesStore: RssSourcesStore,
    private val databaseHelper: DatabaseHelper,
    private val toastShower: ToastShower
) : StatefulWorkflow<Unit, RssFeedWorkflow.InternalState, Output, RssFeedScreenState>() {

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

    override fun initialState(props: Unit, snapshot: Snapshot?): InternalState {
        return InternalState(
            loadingState = LoadingState.SUCCESS,
            rssFeedItems = databaseHelper.getAllFeedItems(sourcesStore.getSources()) ?: emptyList()
        )
    }

    override fun render(
        props: Unit,
        state: InternalState,
        context: RenderContext<InternalState, Output>
    ): RssFeedScreenState {
        when (state.loadingState) {
            LoadingState.NONE,
            LoadingState.LOADING -> {
                context.runningWorker(refreshWorker()) {
                    updateLoadingState(it)
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

    private fun refreshWorker(): Worker<LoadingState> {
        return RefreshFeedWorker(
            downloadManager,
            sourcesStore.getSources()
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

    private fun updateLoadingState(loadingState: LoadingState) = action("updateLoadingState") {
        nextState = nextState.copy(
            loadingState = if (loadingState == LoadingState.FAILURE) {
                LoadingState.NONE
            } else {
                loadingState
            },
            rssFeedItems = if (loadingState == LoadingState.SUCCESS) {
                databaseHelper.getAllFeedItems(sourcesStore.getSources()) ?: emptyList()
            } else {
                nextState.rssFeedItems
            }
        )
    }

}