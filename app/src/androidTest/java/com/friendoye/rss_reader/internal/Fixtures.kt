package com.friendoye.rss_reader.internal

val TEST_SOURCES = listOf("Test 1", "Test 2")

val TEST_1_RSS_FEED = listOf(
    TestFeedItem(id = 10, title = "Test 10", link = "Link 10", source = "Test 1"),
    TestFeedItem(id = 11, title = "Test 11", link = "Link 11", source = "Test 1"),
    TestFeedItem(id = 12, title = "Test 12", link = "Link 12", source = "Test 1"),
    TestFeedItem(id = 13, title = "Test 13", link = "Link 13", source = "Test 1"),
    TestFeedItem(id = 14, title = "Test 14", link = "Link 14", source = "Test 1")
)

val TEST_2_RSS_FEED = listOf(
    TestFeedItem(id = 20, title = "Test 20", link = "Link 20", source = "Test 2"),
    TestFeedItem(id = 21, title = "Test 21", link = "Link 21", source = "Test 2"),
    TestFeedItem(id = 22, title = "Test 22", link = "Link 22", source = "Test 2"),
    TestFeedItem(id = 23, title = "Test 23", link = "Link 23", source = "Test 2"),
    TestFeedItem(id = 24, title = "Test 24", link = "Link 24", source = "Test 2")
)

val TEST_RSS_FEED = TEST_1_RSS_FEED + TEST_2_RSS_FEED

const val FAKE_FEED_ITEM_DESCRIPTION = "Test Description"