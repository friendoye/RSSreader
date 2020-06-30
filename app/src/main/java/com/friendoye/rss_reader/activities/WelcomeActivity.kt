package com.friendoye.rss_reader.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.foundation.currentTextStyle
import androidx.ui.layout.*
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.Surface
import androidx.ui.res.colorResource
import androidx.ui.res.imageResource
import androidx.ui.res.stringResource
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontWeight
import androidx.ui.text.style.TextAlign
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.TextUnit
import androidx.ui.unit.dp
import com.friendoye.rss_reader.Application
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.database.DatabaseHelper
import com.friendoye.rss_reader.database.DatabaseManager
import com.friendoye.rss_reader.utils.DownloadManager
import com.friendoye.rss_reader.utils.LoadingState
import com.friendoye.rss_reader.utils.NetworkHelper
import net.hockeyapp.android.CrashManager
import net.hockeyapp.android.UpdateManager

/**
 * Launcher activity, that shows up until:
 * 1) RSS feeds are retrieved (if network is connected);
 * 2) RSS feeds aren't retrieved, but database has them (if network is disconnected);
 * 3) Error has occurred.
 */
class WelcomeActivity : AppCompatActivity(),
    DownloadManager.OnDownloadStateChangedListener {
    private var mDatabaseHelper: DatabaseHelper? = null
    private lateinit var mDownloadManager: DownloadManager
    private var mSources: List<String> = emptyList()

    private var uiState by mutableStateOf(
        WelcomeScreenState(
            retry = this::onRetry,
            loadingState = LoadingState.NONE
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WelcomeScreenLayout(uiState)
        }

        mDownloadManager = Application.get(this).downloadManager
        mDatabaseHelper = DatabaseManager.getHelper(this, DatabaseHelper::class.java)
        mSources = resources.getStringArray(R.array.rss_sources_array).toList()

        if (savedInstanceState == null) {
            mDownloadManager.refreshData(mSources)
        }
        mDownloadManager.subscribe(this)
    }

    public override fun onResume() {
        super.onResume()
//        checkForCrashes();
//        checkForUpdates();
    }

    override fun onDestroy() {
        super.onDestroy()
        mDownloadManager.unsubscribe(this)
        DatabaseManager.releaseHelper()
        mDatabaseHelper = null
    }

    override fun onDownloadStateChanged(state: LoadingState) {
        changeStateOnDownloadStateChanged(state)
        effectOnDownloadStateChanged(state)
    }

    private fun effectOnDownloadStateChanged(state: LoadingState) {
        when (state) {
            LoadingState.SUCCESS -> {
                val startIntent = Intent(this, RssFeedActivity::class.java)
                startActivity(startIntent)
                finish()
            }
            LoadingState.FAILURE -> if (mDatabaseHelper!!.hasItems()) {
                val startIntent = Intent(this, RssFeedActivity::class.java)
                startActivity(startIntent)
                finish()
            }
        }
    }

    private fun changeStateOnDownloadStateChanged(state: LoadingState) {
        val newState = if (state == LoadingState.FAILURE && mDatabaseHelper!!.hasItems()) {
            LoadingState.SUCCESS
        } else {
            state
        }
        uiState = WelcomeScreenState(
            retry = this@WelcomeActivity::onRetry,
            loadingState = newState
        )
    }

    private fun onRetry(state: LoadingState) {
        if (state != LoadingState.FAILURE) {
            return
        }
        mDownloadManager.refreshData(mSources)
    }

    private fun checkForCrashes() {
        CrashManager.register(this, "84c5a3551a6c0bf92bb6f99c72e2ab9c")
    }

    private fun checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this, "84c5a3551a6c0bf92bb6f99c72e2ab9c")
    }
}

data class WelcomeScreenState(
    val retry: (LoadingState) -> Unit,
    val loadingState: LoadingState
)

@Preview(widthDp = 300, heightDp = 600)
@Composable
fun WelcomeScreenLayout() {
    WelcomeScreenLayout(
        WelcomeScreenState(retry = {}, loadingState = LoadingState.FAILURE)
    )
}

@Composable
fun WelcomeScreenLayout(state: WelcomeScreenState) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = { state.retry(state.loadingState) }, indication = null),
        color = colorResource(id = R.color.orange_500)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalGravity = Alignment.CenterHorizontally
        ) {
            when (state.loadingState) {
                LoadingState.LOADING,
                LoadingState.SUCCESS,
                LoadingState.NONE -> {
                    Text(
                        text = stringResource(id = R.string.wait_text),
                        fontSize = TextUnit.Sp(46),
                        color = colorResource(id = R.color.amber_A400),
                        textAlign = TextAlign.Center,
                        style = currentTextStyle().copy(fontWeight = FontWeight.Bold)
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.gravity(Alignment.CenterHorizontally),
                        color = colorResource(id = R.color.amber_A400)
                    )
                }
                LoadingState.FAILURE -> {
                    Column {
                        Image(
                            modifier = Modifier.gravity(Alignment.CenterHorizontally),
                            asset = imageResource(id = R.drawable.ic_warning_grey_300_36dp)
                        )
                        Text(
                            text = stringResource(id = R.string.welcome_network_error_message_text),
                            color = colorResource(id = R.color.amber_A400),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}