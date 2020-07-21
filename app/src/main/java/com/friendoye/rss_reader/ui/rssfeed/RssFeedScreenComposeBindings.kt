package com.friendoye.rss_reader.ui.rssfeed

import android.widget.ImageView
import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.ContentScale
import androidx.ui.core.Modifier
import androidx.ui.foundation.*
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.foundation.shape.corner.CircleShape
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.res.imageResource
import androidx.ui.text.style.TextOverflow
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import androidx.ui.viewinterop.AndroidView
import coil.Coil
import coil.api.loadAny
import com.friendoye.rss_reader.FeatureFlags
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.ui.RssReaderAppTheme
import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.model.onliner.OnlinerFeedItem
import com.friendoye.rss_reader.model.tutby.TutByFeedItem
import com.friendoye.rss_reader.utils.Config
import com.friendoye.rss_reader.utils.LoadingState
import com.friendoye.rss_reader.utils.compose.LegacyImage
import com.friendoye.rss_reader.utils.compose.SwipeToRefreshLayout
import com.squareup.workflow.ui.compose.composedViewFactory
import com.squareup.workflow.ui.compose.tooling.preview
import dev.chrisbanes.accompanist.coil.CoilImage
import java.util.*


private val sampleFeed: List<RssFeedItem> = listOf(
    OnlinerFeedItem().apply {
        title = "Long Long\nLong Long\nLong Long Long Long Long Long Long Long Long Long title"
        description = "Long description"
        publicationDate = Date()
        imageUrl = "https://content.onliner.by/news/thumbnail/8fdb0f7c04833a79406e62b7877d5e11.jpeg"
    },
    TutByFeedItem().apply {
        title = "Sample 1"
        description = "Sample 1 Long description"
        publicationDate = Date()
        imageUrl = "https://content.onliner.by/news/thumbnail/8fdb0f7c04833a79406e62b7877d5e11.jpeg"
    },
    OnlinerFeedItem().apply {
        title = "Sample 2"
        description = "Sample 2 Long description"
        publicationDate = Date()
        imageUrl = "https://content.onliner.by/news/thumbnail/8fdb0f7c04833a79406e62b7877d5e11.jpeg"
    },
    TutByFeedItem().apply {
        title = "Sample 3"
        description = "Sample 3 Long description"
        publicationDate = Date()
        imageUrl = "https://content.onliner.by/news/thumbnail/8fdb0f7c04833a79406e62b7877d5e11.jpeg"
    },
    OnlinerFeedItem().apply {
        title = "Sample 4"
        description = "Sample 4 Long description"
        publicationDate = Date()
        imageUrl = "https://content.onliner.by/news/thumbnail/8fdb0f7c04833a79406e62b7877d5e11.jpeg"
    }
)

@Preview(widthDp = 300, heightDp = 500)
@Composable
fun RssFeedLayoutPreview() {
    RssReaderAppTheme(darkTheme = true) {
        RssFeedScreen.preview(
            RssFeedScreenState(
                loadingState = LoadingState.NONE,
                isSwipeToRefreshInProgress = false,
                rssFeedItems = sampleFeed,
                onRefresh = {},
                onPickRssSources = {},
                onRssFeedItemClick = {},
                previewMode = true
            )
        )
    }
}

@Preview(widthDp = 420, heightDp = 108)
@Composable
fun RssFeedListItem() {
    RssReaderAppTheme(darkTheme = false) {
        RssFeedListItem(
            sampleFeed.first(),
            {},
            previewMode = true
        )
    }
}

val RssFeedScreen = composedViewFactory<RssFeedScreenState> { state, _ ->
    Column(horizontalGravity = Alignment.CenterHorizontally) {
        Scaffold(
            topBar = {
                TopAppBar(
                    // TODO: use string resource
                    title = { Text("RSS Feed") },
                    elevation = 4.dp,
                    backgroundColor = MaterialTheme.colors.primary,
                    actions = {
                        IconButton(onClick = state.onPickRssSources) {
                            Icon(imageResource(R.drawable.ic_list_white_36dp))
                        }
                    }
                )
            }
        ) {
            RssFeedContentLayout(state)
        }
    }
}

@Composable
fun RssFeedContentLayout(state: RssFeedScreenState) {
    SwipeToRefreshLayout(
        refreshingState = state.isSwipeToRefreshInProgress,
        onRefresh = state.onRefresh,
        refreshIndicator = {
            Surface(elevation = 10.dp, shape = CircleShape) {
                CircularProgressIndicator(Modifier.preferredSize(50.dp).padding(4.dp))
            }
        }
    ) {
        LazyColumnItems(items = state.rssFeedItems) { item ->
            RssFeedListItem(
                item,
                onClick = { state.onRssFeedItemClick(item) },
                previewMode = state.previewMode
            )
        }
    }
}

@Composable
fun RssFeedListItem(item: RssFeedItem, onClick: () -> Unit, previewMode: Boolean = false) {
    Surface(
        modifier = Modifier.height(108.dp).fillMaxWidth()
            .clickable(onClick = onClick, indication = null)
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val (titleText, dateText, posterImage) = createRefs()

            Text(
                text = item.title,
                modifier = Modifier.constrainAs(titleText) {
                    top.linkTo(posterImage.top)
                    start.linkTo(parent.start)
                    end.linkTo(posterImage.start, margin = 20.dp)
                    //bottom.linkTo(dateText.top)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                },
                overflow = TextOverflow.Ellipsis,
                maxLines = 3,
                style = MaterialTheme.typography.body1
            )

            // TODO: Check whether it's optimal to format date in Composable function
            val formattedDateString = Config.DATE_FORMATTER.format(item.publicationDate)
            Text(
                text = formattedDateString,
                modifier = Modifier.constrainAs(dateText) {
                    //top.linkTo(titleText.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(posterImage.start, margin = 20.dp)
                    bottom.linkTo(posterImage.bottom)
                    width = Dimension.fillToConstraints
                },
                style = MaterialTheme.typography.caption
            )

            val imageViewModifier = Modifier.size(width = 120.dp, height = 80.dp)
                .constrainAs(posterImage) {
                    linkTo(top = parent.top, bottom = parent.bottom)
                    end.linkTo(parent.end)
                }
            when {
                previewMode -> {
                    Image(
                        asset = imageResource(id = R.drawable.image_post_placeholder),
                        modifier = imageViewModifier,
                        contentScale = ContentScale.Crop
                    )
                }
                FeatureFlags.USE_LEGACY_IMAGE -> {
                    LegacyImage(
                        data = item.imageUrl,
                        modifier = imageViewModifier
                    )
                }
                else -> {
                    CoilImage(
                        data = item.imageUrl,
                        modifier = imageViewModifier,
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}