package com.friendoye.rss_reader.utils.compose

import androidx.compose.Stable
import androidx.ui.core.Modifier
import androidx.ui.core.semantics.semantics
import androidx.ui.semantics.testTag

@Stable
fun Modifier.testTag(tag: Any) = semantics(
    properties = {
        testTag = tag.toString()
    }
)