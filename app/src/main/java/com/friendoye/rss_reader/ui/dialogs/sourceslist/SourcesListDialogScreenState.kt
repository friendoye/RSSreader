package com.friendoye.rss_reader.ui.dialogs.sourceslist

data class SourcesListDialogScreenState(
    val isShowing: Boolean,
    val options: Map<String, Boolean>,
    val onOptionsUpdated: (Map<String, Boolean>) -> Unit
)
