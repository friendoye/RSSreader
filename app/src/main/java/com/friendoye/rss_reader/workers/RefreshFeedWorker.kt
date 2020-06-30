package com.friendoye.rss_reader.workers

import com.friendoye.rss_reader.utils.DownloadManager
import com.friendoye.rss_reader.utils.LoadingState
import com.squareup.workflow.Worker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

@ExperimentalCoroutinesApi
class RefreshFeedWorker(
    private val downloadManager: DownloadManager,
    private val sources: List<String>
) : Worker<LoadingState> {

    override fun doesSameWorkAs(otherWorker: Worker<*>): Boolean {
        return super.doesSameWorkAs(otherWorker)
                && (otherWorker as? RefreshFeedWorker)?.sources == sources
    }

    override fun run(): Flow<LoadingState> = callbackFlow {
        val downloadListener = DownloadManager.OnDownloadStateChangedListener {
            // TODO: Add error handling
            sendBlocking(it)
        }
        downloadManager.refreshData(sources)
        downloadManager.subscribe(downloadListener)

        awaitClose {
            downloadManager.unsubscribe(downloadListener)
        }
    }
}