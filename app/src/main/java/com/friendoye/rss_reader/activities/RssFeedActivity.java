package com.friendoye.rss_reader.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.database.DatabaseHelper;
import com.friendoye.rss_reader.database.DatabaseManager;
import com.friendoye.rss_reader.dialogs.SourcesListDialogFragment;
import com.friendoye.rss_reader.fragments.RssFeedFragment;
import com.friendoye.rss_reader.loaders.RssFeedLoader;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.utils.Config;
import com.friendoye.rss_reader.utils.LoadingState;
import com.friendoye.rss_reader.utils.Packer;

/**
 * This activity holds RssFeedFragment.
 */
public class RssFeedActivity extends AppCompatActivity
        implements RssFeedFragment.OnDataUsageListener,
        SourcesListDialogFragment.OnSourcesChangedListener,
        LoaderManager.LoaderCallbacks<Boolean> {
    private static final String STATE_KEY = "state key";

    private DatabaseHelper mDatabaseHelper;
    private RssFeedFragment mFeedFragment;
    private String[] mSources;

    private LoadingState mState;

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

        if (savedInstanceState != null) {
            String stateString = savedInstanceState.getString(STATE_KEY);
            mState = LoadingState.valueOf(stateString);
        } else {
            mState = LoadingState.SUCCESS;
        }

        if (mState == LoadingState.LOADING) {
            getSupportLoaderManager().initLoader(R.id.rss_feed_loader,
                    null, this);
        }

        mFeedFragment.setFeedItems(mDatabaseHelper.getAllFeedItems(mSources));
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
                return(super.onOptionsItemSelected(item));
        }
    }

    protected void setState(LoadingState state) {
        mState = state;
        switch (state) {
            case LOADING:
                getSupportLoaderManager()
                        .restartLoader(R.id.rss_feed_loader, null, this);
                break;
            case SUCCESS:
                mFeedFragment.setFeedItems(mDatabaseHelper.getAllFeedItems(mSources));
                break;
            case FAILURE:
                Toast.makeText(this, R.string.fail_to_refresh_text,
                        Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onSourcesChanged() {
        updateSources();
        mFeedFragment.setFeedItems(mDatabaseHelper.getAllFeedItems(mSources));
    }

    private void updateSources() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        String savedPack = preferences.getString(Config.SOURCES_STRING_KEY,
                                                 null);
        mSources = Packer.unpackAsStringArray(savedPack);
    }

    @Override
    public void onItemSelected(RssFeedItem item) {
        Intent startIntent = new Intent(this, DetailsActivity.class);
        startIntent.putExtra(DetailsActivity.ID_KEY, item.id);
        startIntent.putExtra(DetailsActivity.CLASS_NAME_KEY,
                item.getClass().getName());
        startActivity(startIntent);
    }

    @Override
    public void onRefresh() {
        if (activeNetworkConnection()) {
            setState(LoadingState.LOADING);
        } else {
            mFeedFragment.setRefreshing(false);
            Toast.makeText(this, R.string.no_internet_connection_text,
                    Toast.LENGTH_LONG).show();
        }
    }

    protected boolean activeNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case R.id.rss_feed_loader:
                return new RssFeedLoader(this, mSources);
            default:
                throw new RuntimeException("There's no loader with given id.");
        }
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean updated) {
        mFeedFragment.setRefreshing(false);
        if (updated) {
            setState(LoadingState.SUCCESS);
        } else {
            setState(LoadingState.FAILURE);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        // Do nothing.
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outState == null) {
            outState = new Bundle();
        }
        outState.putString(STATE_KEY, mState.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DatabaseManager.releaseHelper();
        mDatabaseHelper = null;
    }
}
