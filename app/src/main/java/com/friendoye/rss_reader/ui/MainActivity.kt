package com.friendoye.rss_reader.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.compose.*
import androidx.core.app.NavUtils
import androidx.fragment.app.DialogFragment
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.layout.Column
import androidx.ui.layout.fillMaxSize
import androidx.ui.material.IconButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Scaffold
import androidx.ui.material.TopAppBar
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.ArrowBack
import androidx.ui.res.stringResource
import androidx.ui.unit.dp
import com.friendoye.rss_reader.DependenciesProvider
import com.friendoye.rss_reader.FeatureFlags
import com.friendoye.rss_reader.LIGHT_COLOR_PALETTE
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.domain.getActiveSources
import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.ui.details.*
import com.friendoye.rss_reader.ui.dialogs.sourceslist.SourcesListDialogFragment
import com.friendoye.rss_reader.ui.dialogs.sourceslist.SourcesListDialogScreen
import com.friendoye.rss_reader.ui.dialogs.sourceslist.SourcesListDialogScreenState
import com.friendoye.rss_reader.ui.rssfeed.RssFeedActivity
import com.friendoye.rss_reader.ui.rssfeed.RssFeedScreen
import com.friendoye.rss_reader.ui.rssfeed.RssFeedWorkflow
import com.friendoye.rss_reader.ui.welcome.WelcomeScreen
import com.friendoye.rss_reader.ui.welcome.WelcomeWorkflow
import com.friendoye.rss_reader.utils.LoadingState
import com.friendoye.rss_reader.utils.RssSourcesStore
import com.friendoye.rss_reader.utils.workflow.NamedBinding
import com.github.zsoltk.compose.backpress.AmbientBackPressHandler
import com.github.zsoltk.compose.backpress.BackPressHandler
import com.github.zsoltk.compose.router.BackStack
import com.github.zsoltk.compose.router.Router
import com.squareup.workflow.*
import com.squareup.workflow.WorkflowAction.Companion.noAction
import com.squareup.workflow.diagnostic.SimpleLoggingDiagnosticListener
import com.squareup.workflow.ui.*
import com.squareup.workflow.ui.compose.WorkflowContainer
import com.squareup.workflow.ui.compose.composedViewFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(),
    SourcesListDialogFragment.OnSourcesChangedListener {
    private val backPressHandler = BackPressHandler()

    private val sourcesStore = DependenciesProvider.getSourcesStore()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Providers(
                AmbientBackPressHandler provides backPressHandler
            ) {
                MaterialTheme(colors = LIGHT_COLOR_PALETTE) {
                    Router<Screen>(Screen.Welcome) { backstack ->
                        val currentScreen = backstack.last()
                        when (currentScreen) {
                            Screen.Welcome -> WelcomeScreenLayout(backstack)
                            Screen.RssFeed -> {
                                onActive {
                                    updateSources()
                                }
                                WorkflowContainer(
                                    workflow = DependenciesProvider.provideRssFeedWorkflow(),
                                    props = RssFeedWorkflow.Input(mSources),
                                    viewEnvironment = viewEnvironment,
                                    modifier = Modifier.fillMaxSize(),
                                    diagnosticListener = SimpleLoggingDiagnosticListener(),
                                    onOutput = { output ->
                                        when (output) {
                                            is RssFeedWorkflow.Output.NavigateToDetails -> {
                                                val nextScreen = Screen.RssItemDetails(output.rssFeedItem)
                                                backstack.push(nextScreen)
                                            }
                                            is RssFeedWorkflow.Output.NavigateToSourcesListDialog -> {
                                                if (FeatureFlags.USE_COMPOSE_DIALOGS) {
                                                    openPickSourcesComposeDialog()
                                                } else {
                                                    openPickSourcesDialog()
                                                }
                                            }
                                        }
                                    }
                                )
                                if (FeatureFlags.USE_COMPOSE_DIALOGS) {
                                    if (mSourcesDialogState.isShowing) {
                                        SourcesListDialogScreen(
                                            sourceOptions = mSourcesDialogState.options,
                                            onApplySourceOptionsRequest = { newItems ->
                                                mSourcesDialogState = mSourcesDialogState.copy(
                                                    isShowing = false,
                                                    options = newItems
                                                )
                                                mSourcesDialogState.onOptionsUpdated(newItems)
                                            },
                                            onCloseRequest = {
                                                mSourcesDialogState = mSourcesDialogState.copy(
                                                    isShowing = false
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                            is Screen.RssItemDetails -> RssItemDetailsScreenLayout(backstack, currentScreen)
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (!backPressHandler.handle()) {
            super.onBackPressed()
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

    private fun updateSources() {
        mSources = sourcesStore.getActiveSources()
        mSourcesDialogState = mSourcesDialogState.copy(
            options = getActiveSources(this)
        )
    }

    private fun openPickSourcesComposeDialog() {
        mSourcesDialogState = mSourcesDialogState.copy(isShowing = true)
    }

    private fun openPickSourcesDialog() {
        val newFragment: DialogFragment = SourcesListDialogFragment()
        newFragment.show(supportFragmentManager, "sourcePicker")
    }
}
