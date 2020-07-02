package com.friendoye.rss_reader.ui.rssfeed

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.*
import androidx.fragment.app.DialogFragment
import androidx.ui.core.*
import androidx.ui.foundation.*
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.foundation.shape.corner.CircleShape
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.res.imageResource
import androidx.ui.text.style.TextOverflow
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.friendoye.rss_reader.*
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.utils.compose.SwipeToRefreshLayout
import com.friendoye.rss_reader.database.DatabaseHelper
import com.friendoye.rss_reader.database.DatabaseManager
import com.friendoye.rss_reader.ui.dialogs.SourcesListDialogFragment
import com.friendoye.rss_reader.ui.dialogs.SourcesListDialogFragment.OnSourcesChangedListener
import com.friendoye.rss_reader.domain.getActiveSources
import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.model.onliner.OnlinerFeedItem
import com.friendoye.rss_reader.model.tutby.TutByFeedItem
import com.friendoye.rss_reader.ui.details.DetailsActivity
import com.friendoye.rss_reader.ui.rssfeed.RssFeedWorkflow.Output
import com.friendoye.rss_reader.ui.welcome.WelcomeScreen
import com.friendoye.rss_reader.ui.welcome.WelcomeWorkflow
import com.friendoye.rss_reader.utils.*
import com.squareup.workflow.diagnostic.SimpleLoggingDiagnosticListener
import com.squareup.workflow.ui.ViewEnvironment
import com.squareup.workflow.ui.ViewRegistry
import com.squareup.workflow.ui.compose.WorkflowContainer
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

/**
 * This activity holds RssFeedFragment.
 */
@ExperimentalCoroutinesApi
class RssFeedActivity : AppCompatActivity(),
    OnSourcesChangedListener {

    private var mSourcesDialogState by mutableStateOf(
        SourcesListDialogScreenState(
            isShowing = false,
            options = emptyMap(),
            onOptionsUpdated = this::onSourcesChanged
        )
    )

    private val viewRegistry = ViewRegistry(RssFeedScreen)
    private val viewEnvironment = ViewEnvironment(viewRegistry)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(colors = LIGHT_COLOR_PALETTE) {
                WorkflowContainer(
                    workflow = RssFeedWorkflow(
                        DependenciesProvider.getDownloadManager(),
                        DependenciesProvider.getSourcesStore(),
                        DependenciesProvider.getDatabaseHelper(),
                        DependenciesProvider.getToastShower()
                    ),
                    viewEnvironment = viewEnvironment,
                    modifier = Modifier.fillMaxSize(),
                    diagnosticListener = SimpleLoggingDiagnosticListener(),
                    onOutput = { output ->
                        when (output) {
                            is Output.NavigateToDetails -> {
                                openRssFeedItemDetailsActivity(output.rssFeedItem)
                            }
                            is Output.NavigateToSourcesListDialog -> {
                                if (FeatureFlags.USE_COMPOSE_DIALOGS) {
                                    openPickSourcesComposeDialog()
                                } else {
                                    openPickSourcesDialog()
                                }
                            }
                        }
                    }
                )
//                if (FeatureFlags.USE_COMPOSE_DIALOGS) {
//                    if (mSourcesDialogState.isShowing) {
//                        SourcesListDialogScreen(
//                            sourceOptions = mSourcesDialogState.options,
//                            onApplySourceOptionsRequest = { newItems ->
//                                mSourcesDialogState = mSourcesDialogState.copy(
//                                    isShowing = false,
//                                    options = newItems
//                                )
//                                mSourcesDialogState.onOptionsUpdated(newItems)
//                            },
//                            onCloseRequest = {
//                                mSourcesDialogState = mSourcesDialogState.copy(
//                                    isShowing = false
//                                )
//                            }
//                        )
//                    }
//                }
            }
        }
    }

    override fun onSourcesChanged() {
        updateSources()
    }

    private fun onSourcesChanged(sourceSelection: Map<String, Boolean>) {
        val pack = Packer.packCollection(
            sourceSelection.filterValues { isSelected -> isSelected }
                .keys.toTypedArray()
        )
        DataKeeper.saveString(this, Config.SOURCES_STRING_KEY, pack)
        onSourcesChanged()
    }

    private fun openPickSourcesComposeDialog() {
        mSourcesDialogState = mSourcesDialogState.copy(isShowing = true)
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

//    private fun onRefresh() {
//        if (NetworkHelper.isConnected(this)) {
//            mDownloadManager!!.refreshData(mSources)
//            setState(mDownloadManager!!.state)
//        } else {
//            Toast.makeText(
//                this, R.string.no_internet_connection_text,
//                Toast.LENGTH_LONG
//            ).show()
//        }
//    }

//    protected fun setState(state: LoadingState?) {
//        when (state) {
//            LoadingState.FAILURE -> {
//                mState = mState.copy(
//                    loadingState = LoadingState.NONE,
//                    isSwipeToRefreshInProgress = false //?
//                )
//
//                Toast.makeText(
//                    this, R.string.fail_to_refresh_text,
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//    }

    private fun updateSources() {
        mSourcesDialogState = mSourcesDialogState.copy(
            options = getActiveSources(this)
        )
    }
}