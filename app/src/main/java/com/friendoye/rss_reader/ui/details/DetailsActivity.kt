package com.friendoye.rss_reader.ui.details

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.layout.*
import com.friendoye.rss_reader.DependenciesProvider
import com.friendoye.rss_reader.database.DatabaseHelper
import com.friendoye.rss_reader.model.RssFeedItem
import com.squareup.workflow.diagnostic.SimpleLoggingDiagnosticListener
import com.squareup.workflow.ui.ViewEnvironment
import com.squareup.workflow.ui.ViewRegistry
import com.squareup.workflow.ui.compose.WorkflowContainer

/**
 * This activity holds DetailsFragment.
 */
class DetailsActivity : AppCompatActivity() {

    private var mDatabaseHelper: DatabaseHelper? = null

    private val viewRegistry = ViewRegistry(DetailsScreen)
    private val viewEnvironment = ViewEnvironment(viewRegistry)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mDatabaseHelper = DependenciesProvider.getDatabaseHelper()

        setContent {
            WorkflowContainer(
                workflow = DependenciesProvider.provideDetailsWorkflow(
                    retrieveRssFeedItem()
                ),
                viewEnvironment = viewEnvironment,
                modifier = Modifier.fillMaxSize(),
                diagnosticListener = SimpleLoggingDiagnosticListener(),
                onOutput = {
                    NavUtils.navigateUpFromSameTask(this)
                }
            )
        }
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

    companion object {
        const val LINK_KEY = "link key"
        const val CLASS_NAME_KEY = "class name key"
    }
}