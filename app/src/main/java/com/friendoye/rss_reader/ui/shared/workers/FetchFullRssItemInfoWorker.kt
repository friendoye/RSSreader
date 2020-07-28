package com.friendoye.rss_reader.ui.shared.workers

import android.graphics.Bitmap
import android.util.Log
import com.friendoye.rss_reader.domain.RssFeedItemDetailsFetcher
import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.ui.shared.workers.FetchFullRssItemInfoWorker.Result
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import com.squareup.workflow.Worker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

// TODO: Find out why you can't pass null to Worker<out Any?>
class FetchFullRssItemInfoWorker(
    private val mData: RssFeedItem,
    private val detailsFetcher: RssFeedItemDetailsFetcher
) : Worker<Optional<Result>> {

    companion object {
        private val TAG = "FetchFullRssItemInfoWorker"
    }

    data class Result(
        val description: String,
        val largeImage: Bitmap
    )

    @Suppress("BlockingMethodInNonBlockingContext")
    override fun run(): Flow<Optional<Result>> = flow {
        val results = try {
            detailsFetcher.getDetails(mData)?.let { domainResult ->
                Result(
                    description = domainResult.description,
                    largeImage = domainResult.largeImage
                )
            }
        } catch (e: IOException) {
            Log.i(TAG, "Connection problems. Info: $e")
            null
        }

        emit(results.toOptional())
    }
}