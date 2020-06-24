package com.friendoye.rss_reader.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.friendoye.rss_reader.Application
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.database.DatabaseHelper
import com.friendoye.rss_reader.database.DatabaseManager
import com.friendoye.rss_reader.dialogs.SourcesListDialogFragment
import com.friendoye.rss_reader.dialogs.SourcesListDialogFragment.OnSourcesChangedListener
import com.friendoye.rss_reader.fragments.RssFeedFragment
import com.friendoye.rss_reader.fragments.RssFeedFragment.OnDataUsageListener
import com.friendoye.rss_reader.model.RssFeedItem
import com.friendoye.rss_reader.utils.*

/**
 * This activity holds RssFeedFragment.
 */
class RssFeedActivity : AppCompatActivity(), OnDataUsageListener,
    OnSourcesChangedListener,
    DownloadManager.OnDownloadCompletedListener {
    private var mFeedFragment: RssFeedFragment? = null
    private var mState: LoadingState? = null
    private var mDatabaseHelper: DatabaseHelper? = null

    private lateinit var mDownloadManager: DownloadManager
    private var mSources: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rss_feed)
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (mFeedFragment == null) {
            mFeedFragment = supportFragmentManager
                .findFragmentById(R.id.feed_fragment) as RssFeedFragment?
        }
        updateSources()
        mDatabaseHelper = DatabaseManager.getHelper(this, DatabaseHelper::class.java)
        if (savedInstanceState == null) {
            setState(LoadingState.NONE)
        } else {
            mState = LoadingState.valueOf(
                savedInstanceState.getString(STATE_KEY)!!
            )
        }
        mDownloadManager = Application.get(this).downloadManager
        mDownloadManager.subscribe(this)
        if (mState == LoadingState.LOADING) {
            setState(mDownloadManager.getState())
        }
        mFeedFragment!!.setFeedItems(
            mDatabaseHelper?.getAllFeedItems(mSources) ?: emptyList()
        )
    }

    override fun onStart() {
        super.onStart()
        if (mState == LoadingState.LOADING) {
            setState(mDownloadManager!!.state)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_rss_feed, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_pick_sources -> {
                val newFragment: DialogFragment = SourcesListDialogFragment()
                newFragment.show(supportFragmentManager, "sourcePicker")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDownloadComplete(state: LoadingState) {
        setState(state)
    }

    protected fun setState(state: LoadingState?) {
        mState = state
        when (state) {
            LoadingState.LOADING -> mFeedFragment!!.setRefreshing(true)
            LoadingState.SUCCESS -> {
                mFeedFragment!!.setRefreshing(false)
                mFeedFragment!!.setFeedItems(mDatabaseHelper!!.getAllFeedItems(mSources) ?: emptyList())
                setState(LoadingState.NONE)
            }
            LoadingState.FAILURE -> {
                mFeedFragment!!.setRefreshing(false)
                Toast.makeText(
                    this, R.string.fail_to_refresh_text,
                    Toast.LENGTH_LONG
                ).show()
                setState(LoadingState.NONE)
            }
            LoadingState.NONE -> {
            }
        }
    }

    override fun onSourcesChanged() {
        updateSources()
        mFeedFragment!!.setFeedItems(mDatabaseHelper?.getAllFeedItems(mSources) ?: emptyList())
    }

    private fun updateSources() {
        val savedPack =
            DataKeeper.restoreString(this, Config.SOURCES_STRING_KEY)
        mSources = Packer.unpackAsStringArray(savedPack).toList()
    }

    override fun onItemSelected(item: RssFeedItem) {
        if (!isFinishing) {
            val startIntent = Intent(this, DetailsActivity::class.java)
            startIntent.putExtra(DetailsActivity.LINK_KEY, item.link)
            startIntent.putExtra(
                DetailsActivity.CLASS_NAME_KEY,
                item.javaClass.name
            )
            startActivity(startIntent)
        }
    }

    override fun onRefresh() {
        if (NetworkHelper.isConnected(this)) {
            mDownloadManager!!.refreshData(mSources)
            setState(mDownloadManager!!.state)
        } else {
            mFeedFragment!!.setRefreshing(false)
            Toast.makeText(
                this, R.string.no_internet_connection_text,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_KEY, mState.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mDownloadManager.unsubscribe(this)
        DatabaseManager.releaseHelper()
        mDatabaseHelper = null
    }

    companion object {
        private const val STATE_KEY = "state key"
    }
}