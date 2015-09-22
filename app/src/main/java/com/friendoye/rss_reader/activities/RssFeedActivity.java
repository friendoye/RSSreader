package com.friendoye.rss_reader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.friendoye.rss_reader.Application;
import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.database.DatabaseHelper;
import com.friendoye.rss_reader.database.DatabaseManager;
import com.friendoye.rss_reader.dialogs.SourcesListDialogFragment;
import com.friendoye.rss_reader.fragments.RssFeedFragment;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.utils.Config;
import com.friendoye.rss_reader.utils.DataKeeper;
import com.friendoye.rss_reader.utils.DownloadManager;
import com.friendoye.rss_reader.utils.LoadingState;
import com.friendoye.rss_reader.utils.NetworkHelper;
import com.friendoye.rss_reader.utils.Packer;

/**
 * This activity holds RssFeedFragment.
 */
public class RssFeedActivity extends AppCompatActivity
        implements RssFeedFragment.OnDataUsageListener,
        SourcesListDialogFragment.OnSourcesChangedListener,
        DownloadManager.OnDownloadCompletedListener {
    private static final String STATE_KEY = "state key";

    private RssFeedFragment mFeedFragment;

    private DownloadManager mDownloadManager;
    private LoadingState mState;

    private DatabaseHelper mDatabaseHelper;
    private String[] mSources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rss_feed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (mFeedFragment == null) {
            mFeedFragment = (RssFeedFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.feed_fragment);
        }

        updateSources();
        mDatabaseHelper = DatabaseManager.getHelper(this, DatabaseHelper.class);

        mDownloadManager = Application.get(this).getDownloadManager();

        if (savedInstanceState == null) {
            setState(LoadingState.NONE);
        } else {
            mState = LoadingState.valueOf(savedInstanceState.getString(STATE_KEY));
        }

        mDownloadManager.subscribe(this);
        if (mState == LoadingState.LOADING) {
            setState(mDownloadManager.getState());
        }

        mFeedFragment.setFeedItems(mDatabaseHelper.getAllFeedItems(mSources));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mState == LoadingState.LOADING) {
            setState(mDownloadManager.getState());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_rss_feed, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pick_sources:
                DialogFragment newFragment = new SourcesListDialogFragment();
                newFragment.show(getSupportFragmentManager(), "sourcePicker");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDownloadComplete(LoadingState state) {
        setState(state);
    }

    protected void setState(LoadingState state) {
        mState = state;
        switch (state) {
            case LOADING:
                mFeedFragment.setRefreshing(true);
                break;
            case SUCCESS:
                mFeedFragment.setRefreshing(false);
                mFeedFragment.setFeedItems(mDatabaseHelper.getAllFeedItems(mSources));
                setState(LoadingState.NONE);
                break;
            case FAILURE:
                mFeedFragment.setRefreshing(false);
                Toast.makeText(this, R.string.fail_to_refresh_text,
                        Toast.LENGTH_LONG).show();
                setState(LoadingState.NONE);
                break;
            case NONE:
                break;
        }
    }

    @Override
    public void onSourcesChanged() {
        updateSources();
        mFeedFragment.setFeedItems(mDatabaseHelper.getAllFeedItems(mSources));
    }

    private void updateSources() {
        String savedPack = DataKeeper.restoreString(this, Config.SOURCES_STRING_KEY);
        mSources = Packer.unpackAsStringArray(savedPack);
    }

    @Override
    public void onItemSelected(RssFeedItem item) {
        if (!isFinishing()) {
            Intent startIntent = new Intent(this, DetailsActivity.class);
            startIntent.putExtra(DetailsActivity.LINK_KEY, item.link);
            startIntent.putExtra(DetailsActivity.CLASS_NAME_KEY,
                    item.getClass().getName());
            startActivity(startIntent);
        }
    }

    @Override
    public void onRefresh() {
        if (NetworkHelper.isConnected(this)) {
            mDownloadManager.refreshData(mSources);
            setState(mDownloadManager.getState());
        } else {
            mFeedFragment.setRefreshing(false);
            Toast.makeText(this, R.string.no_internet_connection_text,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_KEY, mState.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDownloadManager.unsubscribe(this);
        mDownloadManager = null;

        DatabaseManager.releaseHelper();
        mDatabaseHelper = null;
    }
}
