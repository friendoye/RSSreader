package com.friendoye.rss_reader.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.mutableStateOf
import androidx.compose.setValue
import androidx.core.app.NavUtils
import androidx.ui.core.Alignment
import androidx.ui.core.ContentScale
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.ArrowBack
import androidx.ui.res.colorResource
import androidx.ui.res.imageResource
import androidx.ui.res.stringResource
import androidx.ui.text.style.TextAlign
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.friendoye.rss_reader.LIGHT_COLOR_PALETTE
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.database.DatabaseHelper
import com.friendoye.rss_reader.database.DatabaseManager
import com.friendoye.rss_reader.fragments.RssFeedItemFragment
import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.utils.Config
import com.friendoye.rss_reader.utils.LoadingState
import dev.chrisbanes.accompanist.coil.CoilImage
import java.util.*

/**
 * This activity holds DetailsFragment.
 */
class DetailsActivity : AppCompatActivity(),
    RssFeedItemFragment.OnDownloadCompletedListener {
    private var mDataFragment: RssFeedItemFragment? = null
    private var mDatabaseHelper: DatabaseHelper? = null

    private var mState by mutableStateOf(
        DetailsScreenState(
            loadingState = LoadingState.NONE,
            title = "",
            publicationDate = Date(),
            posterUrl = null,
            description = "",
            onUpNavigation = this::navigateUp,
            onRetry = this::onRetry
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DetailsLayout(mState)
        }

        initFragments()
        mDatabaseHelper = DatabaseManager.getHelper(this, DatabaseHelper::class.java)

        var loadingState = LoadingState.NONE
        if (savedInstanceState == null) {
            mDataFragment!!.item = retrieveRssFeedItem()
            loadingState = LoadingState.NONE
        } else {
            val stateString = savedInstanceState.getString(STATE_KEY)
            loadingState = LoadingState.valueOf(stateString!!)
        }
        mDataFragment?.item?.let { item ->
            mState = mState.copy(
                title = item.title,
                publicationDate = item.publicationDate,
                posterUrl = item.imageUrl,
                description = item.description
            )
        }
        setState(loadingState)
    }

    private fun retrieveRssFeedItem(): RssFeedItem {
        val intent = intent
        val link = intent.getStringExtra(LINK_KEY)
        val configClass: Class<*>
        configClass = try {
            Class.forName(intent.getStringExtra(CLASS_NAME_KEY))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        if (link != null) {
            return mDatabaseHelper!!.getFeedItem(link, configClass)
        } else {
            throw RuntimeException("There's no news to show!")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var outState: Bundle? = outState
        if (outState == null) {
            outState = Bundle()
        }
        outState.putString(STATE_KEY, mState.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        DatabaseManager.releaseHelper()
        mDatabaseHelper = null
    }

    override fun onDownloadSuccess() {
        setState(LoadingState.SUCCESS)
    }

    override fun onDownloadFailure() {
        setState(LoadingState.FAILURE)
    }

    private fun initFragments() {
        val fragmentManager = supportFragmentManager
        mDataFragment = fragmentManager
            .findFragmentByTag(DATA_FRAGMENT_TAG) as RssFeedItemFragment?
        if (mDataFragment == null) {
            mDataFragment = RssFeedItemFragment()
            fragmentManager.beginTransaction()
                .add(mDataFragment!!, DATA_FRAGMENT_TAG)
                .commit()
        }
    }

    private fun navigateUp() {
        NavUtils.navigateUpFromSameTask(this)
    }

    private fun onRetry() {
        setState(LoadingState.LOADING)
        mDataFragment!!.downloadFullInfo()
    }

    private fun setState(state: LoadingState) {
        when (state) {
            LoadingState.NONE -> {
                mDataFragment!!.downloadFullInfo()
                mState = mState.copy(loadingState = LoadingState.LOADING)
            }
            LoadingState.LOADING,
            LoadingState.FAILURE -> {
                mState = mState.copy(loadingState = state)
            }
            LoadingState.SUCCESS -> {
                val item = mDataFragment!!.item
                mState = mState.copy(
                    loadingState = state,
                    title = item?.title,
                    publicationDate = item?.publicationDate,
                    posterUrl = item?.imageUrl,
                    description = item?.description
                )
            }
        }
    }

    companion object {
        const val LINK_KEY = "link key"
        const val CLASS_NAME_KEY = "class name key"
        private const val STATE_KEY = "state key"
        private const val DATA_FRAGMENT_TAG = "data tag"
    }
}

data class DetailsScreenState(
    val loadingState: LoadingState,
    val title: String?,
    val publicationDate: Date?,
    val posterUrl: String?,
    val description: String?,
    val onUpNavigation: () -> Unit,
    val onRetry: () -> Unit,
    val previewMode: Boolean = false
)

@Composable
fun DetailsLayout(state: DetailsScreenState) {
    MaterialTheme(colors = LIGHT_COLOR_PALETTE) {
        Column(horizontalGravity = Alignment.CenterHorizontally) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        // TODO: use string resource
                        title = { Text("News") },
                        elevation = 4.dp,
                        navigationIcon = {
                            IconButton(onClick = state.onUpNavigation) {
                                Icon(Icons.Filled.ArrowBack)
                            }
                        }
                    )
                }
            ) {
                LceLayout(
                    loadingState = state.loadingState,
                    onRetry = state.onRetry
                ) {
                    DetailsContentLayout(state)
                }
            }
        }
    }
}

@Composable
fun DetailsContentLayout(state: DetailsScreenState) {
    VerticalScroller(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.fillMaxWidth().height(16.dp))

        Text(
            text = state.title ?: "",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.h6
        )

        state.publicationDate?.let { date ->
            val formattedDateString = Config.DATE_FORMATTER.format(date)
            Text(
                text = formattedDateString,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.caption,
                textAlign = TextAlign.End
            )
        }

        if (state.previewMode) {
            Image(
                asset = imageResource(id = R.drawable.image_post_placeholder),
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        } else {
            state.posterUrl?.let { posterUrl ->
                CoilImage(
                    data = posterUrl,
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Text(
            text = state.description ?: "",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.body1
        )

        Spacer(modifier = Modifier.fillMaxWidth().height(16.dp))
    }
}

@Composable
fun LceLayout(
    loadingState: LoadingState,
    onRetry: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalGravity = Alignment.CenterHorizontally
    ) {
        when (loadingState) {
            LoadingState.LOADING -> {
                CircularProgressIndicator(
                    modifier = Modifier.gravity(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    color = colorResource(id = R.color.amber_A400)
                )
            }
            LoadingState.FAILURE -> {
                Text(
                    modifier = Modifier.gravity(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp),
                    text = stringResource(id = R.string.network_error_message_text),
                    textAlign = TextAlign.Center
                )
                Button(
                    modifier = Modifier.gravity(Alignment.CenterHorizontally),
                    text = {
                        Text(stringResource(id = R.string.retry_text))
                    },
                    onClick = onRetry
                )
            }
            LoadingState.NONE -> {
                // Nothing
            }
            LoadingState.SUCCESS -> {
                content()
            }
        }
    }
}

@Preview(heightDp = 200, widthDp = 300, showBackground = true)
@Composable
fun LceLayoutLoadingPreview() {
    LceLayout(loadingState = LoadingState.LOADING)
}

@Preview(heightDp = 200, widthDp = 300, showBackground = true)
@Composable
fun LceLayoutErrorPreview() {
    LceLayout(loadingState = LoadingState.FAILURE)
}

@Preview
@Composable
fun DetailsLayoutPreview() {
    DetailsLayout(
        DetailsScreenState(
            loadingState = LoadingState.SUCCESS,
            title = stringResource(id = R.string.title_text),
            publicationDate = Date(),
            posterUrl = null,
            description = stringResource(id = R.string.description_text),
            onUpNavigation = {},
            onRetry = {},
            previewMode = true
        )
    )
}