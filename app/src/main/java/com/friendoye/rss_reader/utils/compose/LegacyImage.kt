package com.friendoye.rss_reader.utils.compose

import android.widget.ImageView
import androidx.compose.*
import androidx.ui.core.Modifier
import androidx.ui.layout.size
import androidx.ui.unit.dp
import androidx.ui.viewinterop.AndroidView
import coil.api.loadAny
import com.friendoye.rss_reader.R

private val emptyUpdater: (Any) -> Unit = {}

/**
 * Using compatibility mode (@see AndroidView), LegacyImage allows you
 * to load and reload images from network.
 *
 * @implNote: By default ScaleType.CENTER_CROP is used.
 */
@Composable
fun LegacyImage(
    data: Any,
    modifier: Modifier
) {
    var imageUpdater by state { emptyUpdater }

    onCommit(data) {
        imageUpdater(data)

        onDispose {
            imageUpdater = emptyUpdater
        }
    }

    AndroidView(
        resId = R.layout.view_legacy_image_view,
        modifier = modifier
    ) { view ->
        val imageView = view as ImageView
        imageUpdater = { imageSource ->
            imageView.loadAny(imageSource)
        }
    }
}