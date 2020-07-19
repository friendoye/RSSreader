package com.friendoye.rss_reader.workers

import android.graphics.Bitmap
import android.util.Log
import com.friendoye.rss_reader.model.AbstractRssSourceFactory
import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.workers.FetchFullRssItemInfoWorker.Result
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import com.squareup.workflow.Worker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException

// TODO: Find out why you can't pass null to Worker<out Any?>
class FetchFullRssItemInfoWorker(
    private val mData: RssFeedItem
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
        val mLink = mData.link
        val parser = AbstractRssSourceFactory.getInstance(mData.source).rssParser

        val results = try {
            withContext(Dispatchers.IO) {
                val doc = Jsoup.connect(mLink).timeout(5000).get()
                Result(
                    description = parser.retrieveDescription(doc) ?: return@withContext null,
                    largeImage = parser.retrieveLargeImage(doc) ?: return@withContext null
                )
            }
        } catch (e: IOException) {
            Log.i(TAG, "Connection problems. Info: $e")
            null
        }

        emit(results.toOptional())
    }
}