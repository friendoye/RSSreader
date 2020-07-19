package com.friendoye.rss_reader.ui.rssfeed

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.*
import androidx.fragment.app.DialogFragment
import androidx.ui.core.*
import androidx.ui.layout.*
import androidx.ui.material.*
import com.friendoye.rss_reader.*
import com.friendoye.rss_reader.ui.dialogs.sourceslist.SourcesListDialogFragment
import com.friendoye.rss_reader.ui.dialogs.sourceslist.SourcesListDialogFragment.OnSourcesChangedListener
import com.friendoye.rss_reader.domain.getActiveSources
import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.ui.details.DetailsActivity
import com.friendoye.rss_reader.ui.dialogs.sourceslist.SourcesListDialogScreen
import com.friendoye.rss_reader.ui.dialogs.sourceslist.SourcesListDialogScreenState
import com.friendoye.rss_reader.ui.rssfeed.RssFeedWorkflow.Output
import com.squareup.workflow.diagnostic.SimpleLoggingDiagnosticListener
import com.squareup.workflow.ui.ViewEnvironment
import com.squareup.workflow.ui.ViewRegistry
import com.squareup.workflow.ui.compose.WorkflowContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * This activity holds RssFeedFragment.
 */
@ExperimentalCoroutinesApi
class RssFeedActivity : AppCompatActivity(),
    OnSourcesChangedListener {

    private var mSources by mutableStateOf(
        DependenciesProvider.getSourcesStore().getActiveSources()
    )

    private var mSourcesDialogState by mutableStateOf(
        SourcesListDialogScreenState(
            isShowing = false,
            options = emptyMap(),
            onOptionsUpdated = this::onSourcesChanged
        )
    )
    private val sourcesStore = DependenciesProvider.getSourcesStore()

    private val viewRegistry = ViewRegistry(RssFeedScreen)
    private val viewEnvironment = ViewEnvironment(viewRegistry)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateSources()

        setContent {
            MaterialTheme(colors = LIGHT_COLOR_PALETTE) {
                WorkflowContainer(
                    workflow = DependenciesProvider.provideRssFeedWorkflow(),
                    props = RssFeedWorkflow.Input(mSources),
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
            }
        }
    }

    override fun onSourcesChanged() {
        updateSources()
    }

    private fun onSourcesChanged(sourceSelection: Map<String, Boolean>) {
        sourcesStore.setActiveSources(
            sourceSelection.filterValues { isSelected -> isSelected }
                .keys.toList()
        )
        onSourcesChanged()
    }

    private fun openPickSourcesComposeDialog() {
        mSourcesDialogState = mSourcesDialogState.copy(isShowing = true)
    }

    private fun openPickSourcesDialog() {
        val newFragment: DialogFragment =
            SourcesListDialogFragment()
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

    private fun updateSources() {
        mSources = sourcesStore.getActiveSources()
        mSourcesDialogState = mSourcesDialogState.copy(
            options = getActiveSources(this)
        )
    }

// TODO: Make it as side-effects
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
//                Toast.makeText(
//                    this, R.string.fail_to_refresh_text,
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//    }
}