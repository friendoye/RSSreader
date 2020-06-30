package com.friendoye.rss_reader.domain

import android.content.Context
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.utils.Config
import com.friendoye.rss_reader.utils.DataKeeper
import com.friendoye.rss_reader.utils.Packer
import java.util.*

fun getActiveSources(context: Context): Map<String, Boolean> {
    val sources = context.resources.getStringArray(R.array.rss_sources_array)
    val savedPack = DataKeeper.restoreString(context, Config.SOURCES_STRING_KEY)
    val activeSources: MutableSet<String> = TreeSet()
    if (savedPack != null) {
        activeSources.addAll(
            Packer.unpackAsStringArray(savedPack).toList()
        )
    }
    return sources.asSequence()
        .map { it to activeSources.contains(it) }
        .toMap()
}