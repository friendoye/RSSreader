package com.friendoye.rss_reader.ui.welcome

import com.friendoye.rss_reader.utils.LoadingState

data class WelcomeScreenState(
    val retry: () -> Unit,
    val loadingState: LoadingState
)