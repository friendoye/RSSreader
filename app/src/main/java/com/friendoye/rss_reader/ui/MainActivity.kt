package com.friendoye.rss_reader.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.compose.*
import androidx.fragment.app.DialogFragment
import androidx.ui.core.setContent
import androidx.ui.material.MaterialTheme
import com.friendoye.rss_reader.LIGHT_COLOR_PALETTE
import com.friendoye.rss_reader.ui.dialogs.sourceslist.SourcesListDialogFragment
import com.github.zsoltk.compose.backpress.AmbientBackPressHandler
import com.github.zsoltk.compose.backpress.BackPressHandler
import com.github.zsoltk.compose.router.Router
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {
    private val backPressHandler = BackPressHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Providers(
                AmbientBackPressHandler provides backPressHandler
            ) {
                MaterialTheme(colors = LIGHT_COLOR_PALETTE) {
                    Router<Screen>(Screen.Welcome) { backstack ->
                        when (val currentScreen = backstack.last()) {
                            Screen.Welcome -> WelcomeScreenLayout(backstack)
                            Screen.RssFeed -> RssFeedScreenLayout(
                                backstack = backstack,
                                legacyOpenPickSourcesDialog = ::openPickSourcesDialog
                            )
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

    private fun openPickSourcesDialog() {
        val newFragment: DialogFragment = SourcesListDialogFragment()
        newFragment.show(supportFragmentManager, "sourcePicker")
    }
}
