package com.friendoye.rss_reader

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.createComposeRule
import androidx.ui.test.doClick
import androidx.ui.test.findByText
import com.friendoye.rss_reader.ui.LegacyDialogNavigation
import com.friendoye.rss_reader.ui.RssReaderApp
import com.github.zsoltk.compose.backpress.BackPressHandler
import kotlinx.coroutines.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@MediumTest
@RunWith(AndroidJUnit4::class)
class RssReaderUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        DependenciesProvider.integrationDepsDelegate = StubIntegrationDependencies(
            allSources = TEST_SOURCES,
            downloadManagerSuccessFeedItems = TEST_1_RSS_FEED + TEST_2_RSS_FEED
        )
    }

    @Test
    fun appNavigationToDetailsScreen() {
        val backPressHandler = BackPressHandler()
        val legacyNavigation = object : LegacyDialogNavigation {
            override fun openPickSourcesDialog() {
                // No-op
            }
        }
        println("Here 1")
        composeTestRule.setContent {
            RssReaderApp(
                backPressHandler = backPressHandler, //?
                legacyNavigation = legacyNavigation
            )
        }
        println("Here 2")
        Thread.sleep(15000)
        println("Here 4")
        findByText("Test 11")
            .assertExists("No RSS feed item found.")
    }
}