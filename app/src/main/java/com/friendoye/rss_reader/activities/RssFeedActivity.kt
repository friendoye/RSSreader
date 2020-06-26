package com.friendoye.rss_reader.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.Composable
import androidx.fragment.app.DialogFragment
import androidx.ui.core.Alignment
import androidx.ui.core.ContentScale
import androidx.ui.core.Modifier
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
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.compose.SwipeToRefreshLayout
import com.friendoye.rss_reader.database.DatabaseHelper
import com.friendoye.rss_reader.database.DatabaseManager
import com.friendoye.rss_reader.dialogs.SourcesListDialogFragment
import com.friendoye.rss_reader.dialogs.SourcesListDialogFragment.OnSourcesChangedListener
import com.friendoye.rss_reader.fragments.RssFeedFragment
import com.friendoye.rss_reader.fragments.RssFeedFragment.OnDataUsageListener
import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.model.onliner.OnlinerFeedItem
import com.friendoye.rss_reader.model.tutby.TutByFeedItem
import com.friendoye.rss_reader.utils.*
import java.util.*

/**
 * This activity holds RssFeedFragment.
 */
class RssFeedActivity : AppCompatActivity(), OnDataUsageListener,
    OnSourcesChangedListener,
    DownloadManager.OnDownloadStateChangedListener {
    private var mFeedFragment: RssFeedFragment? = null
    private var mState: LoadingState? = null
    private var mDatabaseHelper: DatabaseHelper? = null

    private lateinit var mDownloadManager: DownloadManager
    private var mSources: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rss_feed)
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (mFeedFragment == null) {
            mFeedFragment = supportFragmentManager
                .findFragmentById(R.id.feed_fragment) as RssFeedFragment?
        }
        updateSources()
        mDatabaseHelper = DatabaseManager.getHelper(this, DatabaseHelper::class.java)
        if (savedInstanceState == null) {
            setState(LoadingState.NONE)
        } else {
            mState = LoadingState.valueOf(
                savedInstanceState.getString(STATE_KEY)!!
            )
        }
        mDownloadManager = Application.get(this).downloadManager
        mDownloadManager.subscribe(this)
        if (mState == LoadingState.LOADING) {
            setState(mDownloadManager.getState())
        }
        mFeedFragment!!.setFeedItems(
            mDatabaseHelper?.getAllFeedItems(mSources) ?: emptyList()
        )
    }

    override fun onStart() {
        super.onStart()
        if (mState == LoadingState.LOADING) {
            setState(mDownloadManager!!.state)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_rss_feed, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_pick_sources -> {
                val newFragment: DialogFragment = SourcesListDialogFragment()
                newFragment.show(supportFragmentManager, "sourcePicker")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDownloadStateChanged(state: LoadingState) {
        setState(state)
    }

    protected fun setState(state: LoadingState?) {
        mState = state
        when (state) {
            LoadingState.LOADING -> mFeedFragment!!.setRefreshing(true)
            LoadingState.SUCCESS -> {
                mFeedFragment!!.setRefreshing(false)
                mFeedFragment!!.setFeedItems(mDatabaseHelper!!.getAllFeedItems(mSources) ?: emptyList())
                setState(LoadingState.NONE)
            }
            LoadingState.FAILURE -> {
                mFeedFragment!!.setRefreshing(false)
                Toast.makeText(
                    this, R.string.fail_to_refresh_text,
                    Toast.LENGTH_LONG
                ).show()
                setState(LoadingState.NONE)
            }
            LoadingState.NONE -> {
            }
        }
    }

    override fun onSourcesChanged() {
        updateSources()
        mFeedFragment!!.setFeedItems(mDatabaseHelper?.getAllFeedItems(mSources) ?: emptyList())
    }

    private fun updateSources() {
        val savedPack =
            DataKeeper.restoreString(this, Config.SOURCES_STRING_KEY)
        mSources = Packer.unpackAsStringArray(savedPack).toList()
    }

    override fun onItemSelected(item: RssFeedItem) {
        if (!isFinishing) {
            val startIntent = Intent(this, DetailsActivity::class.java)
            startIntent.putExtra(DetailsActivity.LINK_KEY, item.link)
            startIntent.putExtra(
                DetailsActivity.CLASS_NAME_KEY,
                item.javaClass.name
            )
            startActivity(startIntent)
        }
    }

    override fun onRefresh() {
        if (NetworkHelper.isConnected(this)) {
            mDownloadManager!!.refreshData(mSources)
            setState(mDownloadManager!!.state)
        } else {
            mFeedFragment!!.setRefreshing(false)
            Toast.makeText(
                this, R.string.no_internet_connection_text,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_KEY, mState.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mDownloadManager.unsubscribe(this)
        DatabaseManager.releaseHelper()
        mDatabaseHelper = null
    }

    companion object {
        private const val STATE_KEY = "state key"
    }
}

private val ORANGE_500 = Color(0xFFFF9800)
private val ORANGE_700 = Color(0xFFF57C00)
private val AMBER_A400 = Color(0xFFFFC400)

private val LIGHT_COLOR_PALETTE = lightColorPalette(
    primary = ORANGE_500,
    primaryVariant = ORANGE_700,
    secondary = AMBER_A400
)

// TODO: Add dark color theme
//private val DarkThemeColors = darkColorPalette(
//    primary = Red300,
//    primaryVariant = Red700,
//    onPrimary = Color.Black,
//    secondary = Red300,
//    onSecondary = Color.White,
//    error = Red200
//)

data class RssFeedScreenState(
    val loadingState: LoadingState,
    val isSwipeToRefreshInProgress: Boolean,
    val rssFeedItems: List<RssFeedItem>,
    val onRefresh: () -> Unit,
    val onPickRssSources: () -> Unit,
    val onRssFeedItemClick: (RssFeedItem) -> Unit
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
            RssFeedListItem(item, onClick = { state.onRssFeedItemClick(item) })
        }
    }
}

@Composable
fun RssFeedListItem(item: RssFeedItem, onClick: () -> Unit) {
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

            // TODO: Replace image placeholder
            Image(
                asset = imageResource(id = R.drawable.image_post_placeholder),
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

val sampleFeed: List<RssFeedItem> = listOf(
    OnlinerFeedItem().apply {
        title = "Long Long\nLong Long\nLong Long Long Long Long Long Long Long Long Long title"
        description = "Long description"
        publicationDate = Date()
    },
    TutByFeedItem().apply {
        title = "Sample 1"
        description = "Sample 1 Long description"
        publicationDate = Date()
    },
    OnlinerFeedItem().apply {
        title = "Sample 2"
        description = "Sample 2 Long description"
        publicationDate = Date()
    },
    TutByFeedItem().apply {
        title = "Sample 3"
        description = "Sample 3 Long description"
        publicationDate = Date()
    },
    OnlinerFeedItem().apply {
        title = "Sample 4"
        description = "Sample 4 Long description"
        publicationDate = Date()
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
        onRssFeedItemClick = {}
    ))
}

@Preview(widthDp = 420, heightDp = 108)
@Composable
fun RssFeedListItem() {
    RssFeedListItem(sampleFeed.first(), {})
}