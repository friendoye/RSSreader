package com.friendoye.rss_reader.ui.welcome

import com.friendoye.rss_reader.data.RssFeedItemsStore
import com.friendoye.rss_reader.ui.shared.workers.RefreshFeedWorker
import com.friendoye.rss_reader.data.RssSourcesStore
import com.friendoye.rss_reader.domain.DownloadManager
import com.friendoye.rss_reader.utils.LoadingState
import com.squareup.workflow.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class WelcomeWorkflow(
    private val downloadManager: DownloadManager,
    private val sourcesStore: RssSourcesStore,
    private var feedItemsStore: RssFeedItemsStore
) : StatefulWorkflow<Unit, LoadingState, Unit, WelcomeScreenState>() {

    companion object {
        private val DEFAULT_STATE = LoadingState.NONE
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): LoadingState {
        return DEFAULT_STATE
    }

    override fun render(
        props: Unit,
        state: LoadingState,
        context: RenderContext<LoadingState, Unit>
    ): WelcomeScreenState {
        when (state) {
            LoadingState.NONE,
            LoadingState.LOADING -> {
                context.runningWorker(refreshWorker()) {
                    updateLoadingState(it)
                }
            }
            LoadingState.FAILURE,
            LoadingState.SUCCESS -> {
                // Do nothing
            }
        }
        return WelcomeScreenState(
            retry = { context.actionSink.send(retry()) },
            loadingState = state
        )
    }

    override fun snapshotState(state: LoadingState): Snapshot = Snapshot.EMPTY

    private fun refreshWorker(): Worker<LoadingState> {
        return RefreshFeedWorker(
            downloadManager,
            sourcesStore.getAllSources()
        )
    }

    private fun retry() = action("retry") {
        nextState = LoadingState.LOADING
    }

    private fun updateLoadingState(loadingState: LoadingState) = action("updateLoadingState") {
        if (loadingState == LoadingState.FAILURE && feedItemsStore.hasItems()) {
            // If we failed to update RSS feed and we have some items in DB,
            // proceed to next screen to show cached feed.
            nextState = LoadingState.SUCCESS
            setOutput(Unit)
            return@action
        } else if (loadingState == LoadingState.SUCCESS) {
            // If managed to update RSS feed, so proceed to next screen.
            setOutput(Unit)
        }

        nextState = loadingState
    }

}