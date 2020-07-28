package com.friendoye.rss_reader.ui

import androidx.animation.AnimationBuilder
import androidx.animation.TweenBuilder
import androidx.compose.Composable
import androidx.compose.Stable
import androidx.ui.core.ContextAmbient
import com.friendoye.rss_reader.model.RssFeedItem
import com.zachklipp.compose.backstack.BackstackTransition

sealed class Screen(val tag: String) {
    object Welcome : Screen("Welcome")
    object RssFeed : Screen("RssFeed")
    data class RssItemDetails(
        val item: RssFeedItem
    ) : Screen("RssItemDetails[itemId=${item.id}]")
}

@Stable
internal fun getTransitionBetweenScreensBackStacks(
    lastScreensBackStack: List<Screen>,
    currentScreensBackStack: List<Screen>
): BackstackTransition {
    val prevScreen = lastScreensBackStack.lastOrNull()
    val currentScreen = currentScreensBackStack.lastOrNull()
    return when {
        prevScreen == Screen.Welcome -> BackstackTransition.Crossfade
        else -> BackstackTransition.Slide
    }
}

@Composable
internal val DefaultBackstackAnimation: AnimationBuilder<Float>
    get() {
        val context = ContextAmbient.current
        return TweenBuilder<Float>().apply {
            duration = context.resources.getInteger(android.R.integer.config_mediumAnimTime)
        }
    }
