package com.friendoye.rss_reader.data

import android.content.Context
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.utils.Config
import com.friendoye.rss_reader.utils.DataKeeper
import com.friendoye.rss_reader.utils.Packer

interface RssSourcesStore {
    fun getActiveSources(): List<String>
    fun setActiveSources(sources: List<String>)
    fun getAllSources(): List<String>
}