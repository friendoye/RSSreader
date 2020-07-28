package com.friendoye.rss_reader.ui

import androidx.compose.Composable
import androidx.compose.onActive
import com.friendoye.rss_reader.domain.updateSources

@Composable
fun refreshSourcesEffect() {
    onActive {
        updateSources()
    }
}