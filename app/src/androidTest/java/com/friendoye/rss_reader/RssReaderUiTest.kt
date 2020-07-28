package com.friendoye.rss_reader

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.ui.test.*
import com.friendoye.rss_reader.internal.StubIntegrationDependencies
import com.friendoye.rss_reader.ui.*
import com.friendoye.rss_reader.internal.FAKE_FEED_ITEM_DESCRIPTION
import com.friendoye.rss_reader.internal.TEST_RSS_FEED
import com.friendoye.rss_reader.internal.TEST_SOURCES
import com.friendoye.rss_reader.utils.awaitForComposeFinder
import com.github.zsoltk.compose.backpress.BackPressHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class RssReaderUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var backPressHandler: BackPressHandler
    private lateinit var legacyNavigation: LegacyDialogNavigation

    @Before
    fun setUp() {
        DependenciesProvider.integrationDepsDelegate = StubIntegrationDependencies(
            allSources = TEST_SOURCES,
            downloadManagerSuccessFeedItems = TEST_RSS_FEED,
            defaultRssFeedItemDescription = FAKE_FEED_ITEM_DESCRIPTION
        )

        backPressHandler = BackPressHandler()
        legacyNavigation = object : LegacyDialogNavigation {
            override fun openPickSourcesDialog() {
                // No-op
            }
        }

        composeTestRule.setContent {
            GlobalState.mSources = DependenciesProvider.getSourcesStore().getActiveSources()
            RssReaderApp(
                backPressHandler = backPressHandler,
                legacyNavigation = legacyNavigation
            )
        }
    }

    @Test(timeout = 5000L)
    fun appNavigationToRssFeedScreen() {
        awaitForComposeFinder {
            findByTag(Screen.RssFeed.tag).assertExists("Not found.")
        }
        findByText("Test 11")
            .assertExists("No RSS feed item found.")
    }

    @Test(timeout = 8000L)
    fun appNavigationToDetailsScreen() {
        awaitForComposeFinder {
            findByTag(Screen.RssFeed.tag).assertExists("Not found.")
        }

        findByText("Test 11")
            .assertExists("No RSS feed item found.")
            .doClick()

        val feedItem = TEST_RSS_FEED.first { it.title == "Test 11" }
        awaitForComposeFinder {
            findByTag(Screen.RssItemDetails(feedItem).tag).assertExists("Not found.")
        }

        findByText(FAKE_FEED_ITEM_DESCRIPTION)
            .assertExists("No description view for feed item with \"${feedItem.title}\" title found.")
    }
}