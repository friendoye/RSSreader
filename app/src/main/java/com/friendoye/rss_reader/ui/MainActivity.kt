package com.friendoye.rss_reader.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.animation.AnimationBuilder
import androidx.animation.TweenBuilder
import androidx.compose.*
import androidx.fragment.app.DialogFragment
import androidx.transition.Transition
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.core.testTag
import androidx.ui.foundation.drawBorder
import androidx.ui.graphics.Color
import androidx.ui.layout.fillMaxSize
import androidx.ui.unit.dp
import com.friendoye.rss_reader.ui.dialogs.sourceslist.SourcesListDialogFragment
import com.friendoye.rss_reader.utils.elements
import com.github.zsoltk.compose.backpress.AmbientBackPressHandler
import com.github.zsoltk.compose.backpress.BackPressHandler
import com.github.zsoltk.compose.router.BackStack
import com.github.zsoltk.compose.router.Router
import com.zachklipp.compose.backstack.Backstack
import com.zachklipp.compose.backstack.BackstackTransition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.reflect.KProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.javaGetter


@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(), LegacyDialogNavigation {
    private val backPressHandler = BackPressHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RssReaderApp(
                backPressHandler = backPressHandler,
                legacyNavigation = this
            )
        }
    }

    override fun onBackPressed() {
        if (!backPressHandler.handle()) {
            super.onBackPressed()
        }
    }

    override fun openPickSourcesDialog() {
        val newFragment: DialogFragment = SourcesListDialogFragment()
        newFragment.show(supportFragmentManager, "sourcePicker")
    }
}

interface LegacyDialogNavigation {
    fun openPickSourcesDialog()
}

@Composable
fun RssReaderApp(
    backPressHandler: BackPressHandler,
    legacyNavigation: LegacyDialogNavigation
) {
    Providers(
        AmbientBackPressHandler provides backPressHandler
    ) {
        RssReaderAppTheme {
            Router<Screen>(Screen.Welcome) { backstack ->
                var lastBackstackList by state<List<Screen>> { emptyList() }
                val currentTransition = remember(backstack.elements) {
                    getTransitionBetweenScreensBackStacks(lastBackstackList, backstack.elements)
                }

                onCommit(backstack.elements) {
                    lastBackstackList = backstack.elements
                }
                Backstack(
                    backstack = backstack.elements,
                    transition = currentTransition,
                    animationBuilder = DefaultBackstackAnimation,
                    modifier = Modifier.fillMaxSize()
                ) { currentScreen ->
                    when (currentScreen) {
                        Screen.Welcome -> WelcomeScreenLayout(backstack)
                        Screen.RssFeed -> RssFeedScreenLayout(
                            backstack = backstack,
                            legacyOpenPickSourcesDialog = legacyNavigation::openPickSourcesDialog
                        )
                        is Screen.RssItemDetails -> RssItemDetailsScreenLayout(
                            backstack,
                            currentScreen
                        )
                    }
                }
            }
        }
    }
}