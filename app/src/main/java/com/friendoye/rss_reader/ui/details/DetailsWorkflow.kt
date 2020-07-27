package com.friendoye.rss_reader.ui.details

import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.ui.details.DetailsWorkflow.InternalState
import com.friendoye.rss_reader.utils.LoadingState
import com.friendoye.rss_reader.workers.FetchFullRssItemInfoWorker
import com.gojuno.koptional.Optional
import com.nostra13.universalimageloader.core.ImageLoader
import com.squareup.workflow.RenderContext
import com.squareup.workflow.Snapshot
import com.squareup.workflow.StatefulWorkflow
import com.squareup.workflow.Worker
import com.squareup.workflow.*
import com.friendoye.rss_reader.workers.FetchFullRssItemInfoWorker.Result as FetchFullInfoResult

class DetailsWorkflow(
    private val rssFeedItem: RssFeedItem
) : StatefulWorkflow<Unit, InternalState, Unit, DetailsScreenState>() {

    companion object {
        private val DEFAULT_STATE = InternalState(
            loadingState = LoadingState.LOADING,
            rssFeedItem = null
        )
    }

    data class InternalState(
        val loadingState: LoadingState,
        val rssFeedItem: RssFeedItem?
    )

    override fun initialState(props: Unit, snapshot: Snapshot?): InternalState {
        return DEFAULT_STATE
    }

    override fun render(
        props: Unit,
        state: InternalState,
        context: RenderContext<InternalState, Unit>
    ): DetailsScreenState {
        when (state.loadingState) {
            LoadingState.NONE,
            LoadingState.LOADING -> {
                context.runningWorker(fetchFullRssItemInfoWorker()) {
                    setItemFullInfo(it)
                }
            }
            LoadingState.FAILURE,
            LoadingState.SUCCESS -> {
                // Do nothing
            }
        }

        return DetailsScreenState(
            loadingState = state.loadingState,
            title = rssFeedItem.title,
            publicationDate = rssFeedItem.publicationDate,
            posterUrl = rssFeedItem.imageUrl,
            description = rssFeedItem.description,
            onUpNavigation = { context.actionSink.send(navigateUp()) },
            onRetry = { context.actionSink.send(retry()) }
        )
    }

    override fun snapshotState(state: InternalState): Snapshot = Snapshot.EMPTY

    private fun fetchFullRssItemInfoWorker(): Worker<Optional<FetchFullInfoResult>> {
        return FetchFullRssItemInfoWorker(rssFeedItem)
    }

    private fun navigateUp() = action("navigateUp") {
        setOutput(Unit)
    }

    private fun retry() = action("retry") {
        nextState = nextState.copy(loadingState = LoadingState.LOADING)
    }

    private fun setItemFullInfo(wrapperdResult: Optional<FetchFullInfoResult>) = action("setItemFullInfo") {
        val result = wrapperdResult.toNullable()
        if (result == null) {
            nextState = InternalState(
                loadingState = LoadingState.FAILURE,
                rssFeedItem = rssFeedItem
            )
        } else {
            rssFeedItem.apply {
                description = result.description
                largeImage = if (result.largeImage != null) {
                    result.largeImage
                } else {
                    ImageLoader.getInstance().loadImageSync(rssFeedItem.imageUrl)
                }
            }
            nextState = InternalState(
                loadingState = LoadingState.SUCCESS,
                rssFeedItem = rssFeedItem
            )
        }
    }
}