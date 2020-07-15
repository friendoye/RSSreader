package com.friendoye.rss_reader.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.layout.fillMaxSize
import androidx.ui.material.MaterialTheme
import com.friendoye.rss_reader.DependenciesProvider
import com.friendoye.rss_reader.FeatureFlags
import com.friendoye.rss_reader.LIGHT_COLOR_PALETTE
import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.ui.details.DetailsScreen
import com.friendoye.rss_reader.ui.details.DetailsWorkflow
import com.friendoye.rss_reader.ui.rssfeed.RssFeedActivity
import com.friendoye.rss_reader.ui.rssfeed.RssFeedScreen
import com.friendoye.rss_reader.ui.rssfeed.RssFeedScreenState
import com.friendoye.rss_reader.ui.rssfeed.RssFeedWorkflow
import com.friendoye.rss_reader.ui.welcome.WelcomeScreen
import com.friendoye.rss_reader.ui.welcome.WelcomeWorkflow
import com.friendoye.rss_reader.utils.LoadingState
import com.friendoye.rss_reader.utils.RssSourcesStore
import com.squareup.workflow.*
import com.squareup.workflow.WorkflowAction.Companion.noAction
import com.squareup.workflow.diagnostic.SimpleLoggingDiagnosticListener
import com.squareup.workflow.ui.ViewEnvironment
import com.squareup.workflow.ui.ViewRegistry
import com.squareup.workflow.ui.WorkflowRunner
import com.squareup.workflow.ui.compose.WorkflowContainer
import com.squareup.workflow.ui.setContentWorkflow
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private val viewRegistry = ViewRegistry(WelcomeScreen, DetailsScreen, RssFeedScreen)
    private val viewEnvironment = ViewEnvironment(viewRegistry)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(colors = LIGHT_COLOR_PALETTE) {
                WorkflowContainer(
                    workflow = DependenciesProvider.provideMainWorkflow(),
                    viewEnvironment = viewEnvironment,
                    modifier = Modifier.fillMaxSize(),
                    onOutput = { finish() }
                )
            }
        }
//        val workflowRunner = setContentWorkflow(
//            registry = viewRegistry,
//            configure = {
//                WorkflowRunner.Config(
//                    workflow = DependenciesProvider.provideMainWorkflow()
//                )
//            },
//            onResult = { finish() }
//        )
    }
}

@ExperimentalCoroutinesApi
class MainWorkflow constructor(
    private val welcomeWorkflow: WelcomeWorkflow,
    private val rssFeedWorkflow: RssFeedWorkflow,
    private val detailsWorkflowFactory: (RssFeedItem) -> DetailsWorkflow,

    private val rssSourcesStore: RssSourcesStore
) : StatefulWorkflow<Unit, MainWorkflow.Screen, Unit, Any>() {

    sealed class Screen {
        object Welcome : Screen()
        object RssFeed : Screen()
        data class RssItemDetails(
            val item: RssFeedItem
        ) : Screen()
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): Screen {
        return Screen.Welcome
    }

    override fun render(props: Unit, state: Screen, context: RenderContext<Screen, Unit>): Any {
        return when (state) {
            Screen.Welcome -> {
                context.renderChild(welcomeWorkflow, Unit) {
                    navigateTo(Screen.RssFeed)
                }
            }
            Screen.RssFeed -> {
                context.renderChild(
                    rssFeedWorkflow,
                    RssFeedWorkflow.Input(rssSourcesStore.getActiveSources())
                ) { output ->
                    when (output) {
                        is RssFeedWorkflow.Output.NavigateToDetails -> {
                            navigateTo(Screen.RssItemDetails(output.rssFeedItem))
                        }
                        else -> noAction()
                        //is RssFeedWorkflow.Output.NavigateToSourcesListDialog -> {
                            //if (FeatureFlags.USE_COMPOSE_DIALOGS) {
                            //    openPickSourcesComposeDialog()
                            //} else {
                            //    openPickSourcesDialog()
                            //}
                        //}
                    }
                }
            }
            is Screen.RssItemDetails -> {
                context.renderChild(detailsWorkflowFactory(state.item), Unit) {
                    navigateTo(Screen.RssFeed)
                }
            }
        }
    }

    override fun snapshotState(state: Screen): Snapshot = Snapshot.EMPTY

    private fun navigateTo(screen: Screen) = action("navigateTo$screen") {
        nextState = screen
    }
}