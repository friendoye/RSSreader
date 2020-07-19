package com.friendoye.rss_reader.domain

import android.content.Context
import androidx.compose.Composable
import com.friendoye.rss_reader.Application
import com.friendoye.rss_reader.DependenciesProvider
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.ui.GlobalState
import com.friendoye.rss_reader.utils.Config
import com.friendoye.rss_reader.utils.DataKeeper
import com.friendoye.rss_reader.utils.Packer
import com.friendoye.rss_reader.utils.RssSourcesStore
import java.util.*

fun getActiveSources(context: Context = Application.getInstance()): Map<String, Boolean> {
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

fun updateSources() {
    val sourcesStore = DependenciesProvider.getSourcesStore()
    GlobalState.mSources = sourcesStore.getActiveSources()
}

fun updateSources(sourceSelection: Map<String, Boolean>) {
    val sourcesStore: RssSourcesStore = DependenciesProvider.getSourcesStore()
    sourcesStore.setActiveSources(
        sourceSelection.filterValues { isSelected -> isSelected }
            .keys.toList()
    )
    GlobalState.mSources = sourcesStore.getActiveSources()
}