package com.friendoye.rss_reader.ui.details

import com.friendoye.rss_reader.utils.LoadingState
import java.util.*

data class DetailsScreenState(
    val loadingState: LoadingState,
    val title: String?,
    val publicationDate: Date?,
    val posterUrl: String?,
    val description: String?,
    val onUpNavigation: () -> Unit,
    val onRetry: () -> Unit,
    val previewMode: Boolean = false
)