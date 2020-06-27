package com.friendoye.rss_reader.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.*
import androidx.fragment.app.DialogFragment
import androidx.ui.core.*
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.foundation.shape.corner.CircleShape
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.res.imageResource
import androidx.ui.text.style.TextOverflow
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.friendoye.rss_reader.Application
import com.friendoye.rss_reader.LIGHT_COLOR_PALETTE
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.compose.SwipeToRefreshLayout
import com.friendoye.rss_reader.database.DatabaseHelper
import com.friendoye.rss_reader.database.DatabaseManager
import com.friendoye.rss_reader.dialogs.SourcesListDialogFragment
import com.friendoye.rss_reader.dialogs.SourcesListDialogFragment.OnSourcesChangedListener
import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.model.onliner.OnlinerFeedItem
import com.friendoye.rss_reader.model.tutby.TutByFeedItem
import com.friendoye.rss_reader.utils.*
import dev.chrisbanes.accompanist.coil.CoilImage
import java.util.*

/**
 * This activity holds RssFeedFragment.
 */
class RssFeedActivity : AppCompatActivity(),
    OnSourcesChangedListener,
    DownloadManager.OnDownloadStateChangedListener {
    private var mDatabaseHelper: DatabaseHelper? = null
    private lateinit var mDownloadManager: DownloadManager
    private var mSources: List<String> = emptyList()

    private var mState by mutableStateOf(
        RssFeedScreenState(
            loadingState = LoadingState.NONE,
            isSwipeToRefreshInProgress = false,
            rssFeedItems = emptyList(),
            onRefresh = this::onRefresh,
            onPickRssSources = this::openPickSourcesDialog,
            onRssFeedItemClick = this::openRssFeedItemDetailsActivity
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RssFeedLayout(mState)
        }

        updateSources()
        mDatabaseHelper = DatabaseManager.getHelper(this, DatabaseHelper::class.java)
        mDownloadManager = Application.get(this).downloadManager

        mDownloadManager.subscribe(this)
        mState = mState.copy(
            rssFeedItems = mDatabaseHelper!!.getAllFeedItems(mSources) ?: emptyList()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mDownloadManager.unsubscribe(this)
        DatabaseManager.releaseHelper()
        mDatabaseHelper = null
    }

    override fun onDownloadStateChanged(state: LoadingState) {
        setState(state)
    }

    override fun onSourcesChanged() {
        updateSources()
        mState = mState.copy(
            rssFeedItems = mDatabaseHelper?.getAllFeedItems(mSources) ?: emptyList()
        )
    }

    private fun openPickSourcesDialog() {
        val newFragment: DialogFragment = SourcesListDialogFragment()
        newFragment.show(supportFragmentManager, "sourcePicker")
    }

    private fun openRssFeedItemDetailsActivity(item: RssFeedItem) {
        val startIntent = Intent(this, DetailsActivity::class.java)
        startIntent.putExtra(DetailsActivity.LINK_KEY, item.link)
        startIntent.putExtra(
            DetailsActivity.CLASS_NAME_KEY,
            item.javaClass.name
        )
        startActivity(startIntent)
    }

    private fun onRefresh() {
        if (NetworkHelper.isConnected(this)) {
            mDownloadManager!!.refreshData(mSources)
            setState(mDownloadManager!!.state)
        } else {
            Toast.makeText(
                this, R.string.no_internet_connection_text,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    protected fun setState(state: LoadingState?) {
        when (state) {
            LoadingState.LOADING -> {
                mState = mState.copy(
                    isSwipeToRefreshInProgress = true //?
                )
            }
            LoadingState.SUCCESS -> {
                mState = mState.copy(
                    loadingState = LoadingState.NONE,
                    isSwipeToRefreshInProgress = false, //?
                    rssFeedItems = mDatabaseHelper!!.getAllFeedItems(mSources) ?: emptyList()
                )
            }
            LoadingState.FAILURE -> {
                mState = mState.copy(
                    loadingState = LoadingState.NONE,
                    isSwipeToRefreshInProgress = false //?
                )

                Toast.makeText(
                    this, R.string.fail_to_refresh_text,
                    Toast.LENGTH_LONG
                ).show()
            }
            LoadingState.NONE -> {
            }
        }
    }

    private fun updateSources() {
        val savedPack =
            DataKeeper.restoreString(this, Config.SOURCES_STRING_KEY)
        mSources = Packer.unpackAsStringArray(savedPack).toList()
    }
}

data class RssFeedScreenState(
    val loadingState: LoadingState,
    val isSwipeToRefreshInProgress: Boolean,
    val rssFeedItems: List<RssFeedItem>,
    val onRefresh: () -> Unit,
    val onPickRssSources: () -> Unit,
    val onRssFeedItemClick: (RssFeedItem) -> Unit,
    val previewMode: Boolean = false
)

@Composable
fun RssFeedLayout(state: RssFeedScreenState) {
    MaterialTheme(colors = LIGHT_COLOR_PALETTE) {
        Column(horizontalGravity = Alignment.CenterHorizontally) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        // TODO: use string resource
                        title = { Text("RSS Feed") },
                        elevation = 4.dp,
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
        color = Color.White,
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

            if (previewMode) {
                Image(
                    asset = imageResource(id = R.drawable.image_post_placeholder),
                    modifier = Modifier.size(width = 120.dp, height = 80.dp)
                        .constrainAs(posterImage) {
                            linkTo(top = parent.top, bottom = parent.bottom)
                            end.linkTo(parent.end)
                        },
                    contentScale = ContentScale.Crop
                )
            } else {
                // TODO: Fix problem with image reloading
                CoilImage(
                    data = item.imageUrl,
                    modifier = Modifier.size(width = 120.dp, height = 80.dp)
                        .constrainAs(posterImage) {
                            linkTo(top = parent.top, bottom = parent.bottom)
                            end.linkTo(parent.end)
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

val sampleFeed: List<RssFeedItem> = listOf(
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
    RssFeedLayout(RssFeedScreenState(
        loadingState = LoadingState.NONE,
        isSwipeToRefreshInProgress = false,
        rssFeedItems = sampleFeed,
        onRefresh = {},
        onPickRssSources = {},
        onRssFeedItemClick = {},
        previewMode = true
    ))
}

@Preview(widthDp = 420, heightDp = 108)
@Composable
fun RssFeedListItem() {
    RssFeedListItem(sampleFeed.first(), {}, previewMode = true)
}