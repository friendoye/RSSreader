package com.friendoye.rss_reader.utils

import android.content.Context
import com.friendoye.rss_reader.R

class RssSourcesStore(
    private val context: Context
) {

    fun getSources(): List<String> {
        return context.resources.getStringArray(R.array.rss_sources_array).toList()
    }
}