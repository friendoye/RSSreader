package com.friendoye.rss_reader.utils

import android.content.Context
import com.friendoye.rss_reader.R

class RssSourcesStore(
    private val context: Context
) {

    fun getActiveSources(): List<String> {
        val savedPack = DataKeeper.restoreString(context, Config.SOURCES_STRING_KEY)
        return Packer.unpackAsStringArray(savedPack).toList()
    }

    fun setActiveSources(sources: List<String>) {
        val pack = Packer.packCollection(sources.toTypedArray())
        DataKeeper.saveString(context, Config.SOURCES_STRING_KEY, pack)
    }

    fun getAllSources(): List<String> {
        return context.resources.getStringArray(R.array.rss_sources_array).toList()
    }
}