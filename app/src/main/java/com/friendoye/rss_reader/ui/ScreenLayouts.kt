package com.friendoye.rss_reader.ui

import androidx.compose.*
import androidx.ui.core.Modifier
import androidx.ui.layout.fillMaxSize
import com.friendoye.rss_reader.DependenciesProvider
import com.friendoye.rss_reader.FeatureFlags
import com.friendoye.rss_reader.domain.updateSources
import com.friendoye.rss_reader.ui.details.DetailsScreen
import com.friendoye.rss_reader.ui.dialogs.sourceslist.SourcesListDialogScreenLayout
import com.friendoye.rss_reader.ui.rssfeed.RssFeedScreen
import com.friendoye.rss_reader.ui.rssfeed.RssFeedWorkflow
import com.friendoye.rss_reader.ui.welcome.WelcomeScreen
import com.github.zsoltk.compose.router.BackStack
import com.squareup.workflow.diagnostic.SimpleLoggingDiagnosticListener
import com.squareup.workflow.ui.ViewEnvironment
import com.squareup.workflow.ui.ViewRegistry
import com.squareup.workflow.ui.compose.WorkflowContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal val viewRegistry = ViewRegistry(WelcomeScreen, DetailsScreen, RssFeedScreen)
internal val viewEnvironment = ViewEnvironment(viewRegistry)

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun WelcomeScreenLayout(backstack: BackStack<Screen>) {
    WorkflowContainer(
        workflow = DependenciesProvider.provideWelcomeWorkflow(),
        viewEnvironment = viewEnvironment,
        modifier = Modifier.fillMaxSize(),
        onOutput = { backstack.replace(Screen.RssFeed) }
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun RssItemDetailsScreenLayout(backstack: BackStack<Screen>, info: Screen.RssItemDetails) {
    WorkflowContainer(
        workflow = DependenciesProvider.provideDetailsWorkflow(
            info.item
        ),
        viewEnvironment = viewEnvironment,
        modifier = Modifier.fillMaxSize(),
        onOutput = { backstack.pop() }
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun RssFeedScreenLayout(
    backstack: BackStack<Screen>,
    legacyOpenPickSourcesDialog: () -> Unit
) {
    var isDialogVisible by state { false }
    val legacyOpenPickSourcesDialogCallback = remember { legacyOpenPickSourcesDialog }

    onActive {
        updateSources()
    }

    WorkflowContainer(
        workflow = DependenciesProvider.provideRssFeedWorkflow(),
        props = RssFeedWorkflow.Input(GlobalState.mSources),
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
                        isDialogVisible = true
                    } else {
                        legacyOpenPickSourcesDialogCallback()
                    }
                }
            }
        }
    )
    if (FeatureFlags.USE_COMPOSE_DIALOGS && isDialogVisible) {
        SourcesListDialogScreenLayout(
            onClose = { isDialogVisible = false },
            onOptionsUpdated = ::updateSources
        )
    }
}