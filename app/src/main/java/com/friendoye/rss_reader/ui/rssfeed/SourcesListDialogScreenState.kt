package com.friendoye.rss_reader.ui.rssfeed

data class SourcesListDialogScreenState(
    val isShowing: Boolean,
    val options: Map<String, Boolean>,
    val onOptionsUpdated: (Map<String, Boolean>) -> Unit
)
