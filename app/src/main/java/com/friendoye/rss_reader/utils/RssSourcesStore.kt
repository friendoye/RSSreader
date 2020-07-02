package com.friendoye.rss_reader.utils

import android.content.Context
import com.friendoye.rss_reader.R

class RssSourcesStore(
    private val context: Context
) {

    fun getSources(): List<String> {
        val savedPack = DataKeeper.restoreString(context, Config.SOURCES_STRING_KEY)
        return Packer.unpackAsStringArray(savedPack).toList()
    }

    fun getAllSources(): List<String> {
        return context.resources.getStringArray(R.array.rss_sources_array).toList()
    }
}