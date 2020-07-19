package com.friendoye.rss_reader.ui

import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.layout.fillMaxSize
import com.friendoye.rss_reader.DependenciesProvider
import com.friendoye.rss_reader.ui.details.DetailsScreen
import com.friendoye.rss_reader.ui.rssfeed.RssFeedScreen
import com.friendoye.rss_reader.ui.welcome.WelcomeScreen
import com.github.zsoltk.compose.router.BackStack
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