package com.friendoye.rss_reader.data

import android.content.Context
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.utils.Config
import com.friendoye.rss_reader.utils.DataKeeper
import com.friendoye.rss_reader.utils.Packer

class SharedPreferencesRssSourcesStore(
    private val context: Context
) : RssSourcesStore {

    override fun getActiveSources(): List<String> {
        val savedPack = DataKeeper.restoreString(
            context,
            Config.SOURCES_STRING_KEY
        )
        return Packer.unpackAsStringArray(
            savedPack
        ).toList()
    }

    override fun setActiveSources(sources: List<String>) {
        val pack =
            Packer.packCollection(sources.toTypedArray())
        DataKeeper.saveString(
            context,
            Config.SOURCES_STRING_KEY,
            pack
        )
    }

    override fun getAllSources(): List<String> {
        return context.resources.getStringArray(R.array.rss_sources_array).toList()
    }
}