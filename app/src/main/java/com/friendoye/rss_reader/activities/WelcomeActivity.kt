package com.friendoye.rss_reader.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.friendoye.rss_reader.Application
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.database.DatabaseHelper
import com.friendoye.rss_reader.database.DatabaseManager
import com.friendoye.rss_reader.utils.DownloadManager
import com.friendoye.rss_reader.utils.LoadingState
import com.friendoye.rss_reader.utils.NetworkHelper
import net.hockeyapp.android.CrashManager
import net.hockeyapp.android.UpdateManager

/**
 * Launcher activity, that shows up until:
 * 1) RSS feeds are retrieved (if network is connected);
 * 2) RSS feeds aren't retrieved, but database has them (if network is disconnected);
 * 3) Error has occurred.
 */
class WelcomeActivity : AppCompatActivity(),
    DownloadManager.OnDownloadCompletedListener {
    private var mWaitView: TextView? = null
    private var mProgressBar: ProgressBar? = null
    private var mTitleView: TextView? = null
    private var mMessageView: TextView? = null
    private var mState: LoadingState? = null
    private var mDatabaseHelper: DatabaseHelper? = null

    private lateinit var mDownloadManager: DownloadManager
    private var mSources: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        mWaitView = findViewById<View>(R.id.waitTextView) as TextView
        mProgressBar = findViewById<View>(R.id.progressBar) as ProgressBar
        mTitleView = findViewById<View>(R.id.errorTitleView) as TextView
        mMessageView = findViewById<View>(R.id.errorMessageView) as TextView

        mDownloadManager = Application.get(this).downloadManager
        mDatabaseHelper =
            DatabaseManager.getHelper(this, DatabaseHelper::class.java)
        mSources = resources.getStringArray(R.array.rss_sources_array).toList()
        mDownloadManager.subscribe(this)
        if (savedInstanceState == null) {
            mDownloadManager.refreshData(mSources)
            setState(LoadingState.LOADING)
        } else {
            setState(mDownloadManager.getState())
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

    override fun onDownloadComplete(state: LoadingState) {
        setState(state)
    }

    protected fun setState(state: LoadingState?) {
        mState = state
        when (state) {
            LoadingState.LOADING -> showProgressBar()
            LoadingState.SUCCESS -> {
                val startIntent = Intent(this, RssFeedActivity::class.java)
                startActivity(startIntent)
                finish()
            }
            LoadingState.FAILURE -> if (mDatabaseHelper!!.hasItems()) {
                setState(LoadingState.SUCCESS)
            } else {
                showError()
            }
        }
    }

    private fun showProgressBar() {
        if (mProgressBar != null) {
            mTitleView!!.visibility = View.GONE
            mMessageView!!.visibility = View.GONE
            mWaitView!!.visibility = View.VISIBLE
            mProgressBar!!.visibility = View.VISIBLE
        }
    }

    private fun showError() {
        if (mProgressBar != null) {
            mWaitView!!.visibility = View.GONE
            mProgressBar!!.visibility = View.GONE
            mTitleView!!.visibility = View.VISIBLE
            mMessageView!!.visibility = View.VISIBLE
        }
    }

    fun onClick(view: View) {
        if (mState != LoadingState.FAILURE) {
            return
        }
        when (view.id) {
            R.id.container_layout -> if (NetworkHelper.isConnected(this)) {
                mDownloadManager!!.refreshData(mSources)
                setState(mDownloadManager!!.state)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mDownloadManager.unsubscribe(this)
        DatabaseManager.releaseHelper()
        mDatabaseHelper = null
    }
}

