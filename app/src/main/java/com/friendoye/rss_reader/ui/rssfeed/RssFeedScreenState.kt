package com.friendoye.rss_reader.ui.rssfeed

import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.utils.LoadingState

data class RssFeedScreenState(
    val loadingState: LoadingState,
    val isSwipeToRefreshInProgress: Boolean,
    val rssFeedItems: List<RssFeedItem>,
    val onRefresh: () -> Unit,
    val onPickRssSources: () -> Unit,
    val onRssFeedItemClick: (RssFeedItem) -> Unit,
    val previewMode: Boolean = false
)