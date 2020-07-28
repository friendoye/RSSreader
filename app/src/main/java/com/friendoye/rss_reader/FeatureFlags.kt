package com.friendoye.rss_reader

object FeatureFlags {
    /**
     * If true, dialogs will be built using Jetpack Compose.
     * Otherwise, legacy Dialog and DialogFragment API will be used.
     */
    val USE_COMPOSE_DIALOGS
        get() = BuildConfig.EXPERIMENTAL_USE_COMPOSE_DIALOGS

    /**
     * This flag controls interoperability of Jetpack Compose with Legacy Android View API.
     * If true, AndroidView and R.layout.* resources will be used in some places.
     * Otherwise, in that places will be used regular Jetpack Compose views.
     */
    val USE_LEGACY_IMAGE
        get() = true
}