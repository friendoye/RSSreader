package com.friendoye.rss_reader

object FeatureFlags {
    val USE_COMPOSE_DIALOGS
        get() = BuildConfig.EXPERIMENTAL_USE_COMPOSE_DIALOGS
}