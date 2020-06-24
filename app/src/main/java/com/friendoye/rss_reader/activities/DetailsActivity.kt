package com.friendoye.rss_reader.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.fragment.app.Fragment
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.database.DatabaseHelper
import com.friendoye.rss_reader.database.DatabaseManager
import com.friendoye.rss_reader.fragments.DetailsFragment
import com.friendoye.rss_reader.fragments.ProgressFragment
import com.friendoye.rss_reader.fragments.ProgressFragment.OnRetryListener
import com.friendoye.rss_reader.fragments.RssFeedItemFragment
import com.friendoye.rss_reader.utils.LoadingState

/**
 * This activity holds DetailsFragment.
 */
class DetailsActivity : AppCompatActivity(),
    RssFeedItemFragment.OnDownloadCompletedListener, OnRetryListener {
    private var mViewFragment: DetailsFragment? = null
    private var mProgressFragment: ProgressFragment? = null
    private var mDataFragment: RssFeedItemFragment? = null
    private var mDatabaseHelper: DatabaseHelper? = null
    private var mState: LoadingState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        initFragments()
        mDatabaseHelper =
            DatabaseManager.getHelper(this, DatabaseHelper::class.java)
        if (savedInstanceState == null) {
            /////////////////////////////
            val dataFragment = supportFragmentManager
                .findFragmentByTag(DATA_FRAGMENT_TAG)
            if (dataFragment != null) {
                supportFragmentManager.beginTransaction()
                    .remove(dataFragment)
                    .commit()
            }
            /////////////////////////////
            val intent = intent
            val link = intent.getStringExtra(LINK_KEY)
            val configClass: Class<*>
            configClass = try {
                Class.forName(intent.getStringExtra(CLASS_NAME_KEY))
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            if (link != null) {
                val item = mDatabaseHelper!!.getFeedItem(link, configClass)
                mDataFragment!!.item = item
                mState = LoadingState.NONE
            } else {
                throw RuntimeException("There's no news to show!")
            }
        } else {
            val stateString =
                savedInstanceState.getString(STATE_KEY)
            mState = LoadingState.valueOf(stateString!!)
        }
        setState(mState)
        mViewFragment!!.setData(mDataFragment!!.item)
    }

    private fun initFragments() {
        val fragmentManager = supportFragmentManager
        mDataFragment = fragmentManager
            .findFragmentByTag(DATA_FRAGMENT_TAG) as RssFeedItemFragment?
        if (mDataFragment == null) {
            mDataFragment = RssFeedItemFragment()
            fragmentManager.beginTransaction()
                .add(mDataFragment!!, DATA_FRAGMENT_TAG)
                .commit()
        }
        mProgressFragment = fragmentManager
            .findFragmentByTag(PROGRESS_FRAGMENT_TAG) as ProgressFragment?
        if (mProgressFragment == null) {
            mProgressFragment = ProgressFragment()
        }
        mViewFragment = fragmentManager
            .findFragmentByTag(VIEW_FRAGMENT_TAG) as DetailsFragment?
        if (mViewFragment == null) {
            mViewFragment = DetailsFragment()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDownloadSuccess() {
        setState(LoadingState.SUCCESS)
    }

    override fun onDownloadFailure() {
        setState(LoadingState.FAILURE)
    }

    override fun onRetry() {
        setState(LoadingState.LOADING)
    }

    protected fun setState(state: LoadingState?) {
        mState = state
        when (state) {
            LoadingState.NONE -> {
                mDataFragment!!.downloadFullInfo()
                setState(LoadingState.LOADING)
            }
            LoadingState.LOADING -> setForegroundFragment(
                mProgressFragment,
                PROGRESS_FRAGMENT_TAG
            )
            LoadingState.SUCCESS -> {
                setForegroundFragment(mViewFragment, VIEW_FRAGMENT_TAG)
                val item = mDataFragment!!.item
                mViewFragment!!.setData(item)
            }
            LoadingState.FAILURE -> mProgressFragment!!.setState(LoadingState.FAILURE)
        }
    }

    protected fun setForegroundFragment(
        fragment: Fragment?,
        tag: String?
    ) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_view, fragment!!, tag)
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var outState: Bundle? = outState
        if (outState == null) {
            outState = Bundle()
        }
        outState.putString(STATE_KEY, mState.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        DatabaseManager.releaseHelper()
        mDatabaseHelper = null
    }

    companion object {
        const val LINK_KEY = "link key"
        const val CLASS_NAME_KEY = "class name key"
        private const val STATE_KEY = "state key"
        private const val VIEW_FRAGMENT_TAG = "view tag"
        private const val DATA_FRAGMENT_TAG = "data tag"
        private const val PROGRESS_FRAGMENT_TAG = "progress tag"
    }
}