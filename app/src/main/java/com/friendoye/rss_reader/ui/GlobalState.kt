package com.friendoye.rss_reader.ui

import androidx.compose.getValue
import androidx.compose.mutableStateOf
import androidx.compose.setValue
import com.friendoye.rss_reader.DependenciesProvider

object GlobalState {
    var mSources by mutableStateOf(
        DependenciesProvider.getSourcesStore().getActiveSources()
    )
}
