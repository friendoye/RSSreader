package com.friendoye.rss_reader.utils

import androidx.ui.test.SemanticsNodeInteraction
import com.friendoye.rss_reader.utils.ComposeMatcherIdlingResource

fun awaitForComposeFinder(
    action: (() -> Unit) = {},
    nodeChecker: () -> SemanticsNodeInteraction
) {
    val idling = ComposeMatcherIdlingResource(nodeChecker)
    try {
        idling.registerSelfIntoEspresso()
    } finally {
        idling.unregisterSelfFromEspresso()
    }
    action()
}