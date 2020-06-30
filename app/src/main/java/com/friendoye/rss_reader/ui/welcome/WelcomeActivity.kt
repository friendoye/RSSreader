package com.friendoye.rss_reader.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.layout.*
import com.friendoye.rss_reader.DependenciesProvider
import com.friendoye.rss_reader.ui.RssFeedActivity
import com.squareup.workflow.diagnostic.SimpleLoggingDiagnosticListener
import com.squareup.workflow.ui.ViewEnvironment
import com.squareup.workflow.ui.ViewRegistry
import com.squareup.workflow.ui.compose.WorkflowContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.hockeyapp.android.CrashManager
import net.hockeyapp.android.UpdateManager

/**
 * Launcher activity, that shows up until:
 * 1) RSS feeds are retrieved (if network is connected);
 * 2) RSS feeds aren't retrieved, but database has them (if network is disconnected);
 * 3) Error has occurred.
 */
@ExperimentalCoroutinesApi
class WelcomeActivity : AppCompatActivity() {

    private val viewRegistry = ViewRegistry(WelcomeScreen)
    private val viewEnvironment = ViewEnvironment(viewRegistry)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WorkflowContainer(
                workflow = WelcomeWorkflow(
                    DependenciesProvider.getDownloadManager(),
                    DependenciesProvider.getSourcesStore(),
                    DependenciesProvider.getDatabaseHelper()
                ),
                viewEnvironment = viewEnvironment,
                modifier = Modifier.fillMaxSize(),
                diagnosticListener = SimpleLoggingDiagnosticListener(),
                onOutput = {
                    val startIntent = Intent(this, RssFeedActivity::class.java)
                    startActivity(startIntent)
                    finish()
                }
            )
        }
    }

    public override fun onResume() {
        super.onResume()
//        checkForCrashes();
//        checkForUpdates();
    }

    private fun checkForCrashes() {
        CrashManager.register(this, "84c5a3551a6c0bf92bb6f99c72e2ab9c")
    }

    private fun checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this, "84c5a3551a6c0bf92bb6f99c72e2ab9c")
    }
}