package com.friendoye.rss_reader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.database.DatabaseHelper;
import com.friendoye.rss_reader.database.DatabaseManager;
import com.friendoye.rss_reader.loaders.RssFeedLoader;
import com.friendoye.rss_reader.utils.LoadingState;
import com.friendoye.rss_reader.utils.NetworkHelper;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

/**
 * Launcher activity, that shows up until:
 * 1) RSS feeds are retrieved (if network is connected);
 * 2) RSS feeds aren't retrieved, but database has them (if network is disconnected);
 * 3) Error has occurred.
 */
public class WelcomeActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Boolean> {
    private static final String STATE_KEY = "state key";

    private TextView mWaitView;
    private ProgressBar mProgressBar;
    private TextView mTitleView;
    private TextView mMessageView;

    private LoadingState mState;

    private DatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        mWaitView = (TextView) findViewById(R.id.waitTextView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTitleView = (TextView) findViewById(R.id.errorTitleView);
        mMessageView = (TextView) findViewById(R.id.errorMessageView);

        mDatabaseHelper = DatabaseManager.getHelper(this, DatabaseHelper.class);

        if (savedInstanceState != null) {
            String stateString = savedInstanceState.getString(STATE_KEY);
            mState = LoadingState.valueOf(stateString);
        } else {
            setState(LoadingState.LOADING);
        }

        setState(mState);
        if (mState == LoadingState.LOADING) {
            getSupportLoaderManager().initLoader(R.id.rss_feed_loader,
                    null, this);
        }
    }

    @Override
     public void onResume() {
        super.onResume();
//        checkForCrashes();
//        checkForUpdates();
    }

    private void checkForCrashes() {
        CrashManager.register(this, "84c5a3551a6c0bf92bb6f99c72e2ab9c");
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this, "84c5a3551a6c0bf92bb6f99c72e2ab9c");
    }

    protected void setState(LoadingState state) {
        mState = state;
        switch (state) {
            case LOADING:
                showProgressBar();
                break;
            case SUCCESS:
                Intent startIntent = new Intent(this, RssFeedActivity.class);
                startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(startIntent);
                break;
            case FAILURE:
                showError();
                break;
        }
    }

    private void showProgressBar() {
        if (mProgressBar != null) {
            mTitleView.setVisibility(View.GONE);
            mMessageView.setVisibility(View.GONE);
            mWaitView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void showError() {
        if (mProgressBar != null) {
            mWaitView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            mTitleView.setVisibility(View.VISIBLE);
            mMessageView.setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View view) {
        if (mState != LoadingState.FAILURE) {
            return;
        }
        switch (view.getId()) {
            case R.id.container_layout:
                if (NetworkHelper.isConnected(this)) {
                    setState(LoadingState.LOADING);
                    getSupportLoaderManager().restartLoader(R.id.rss_feed_loader,
                            null, this);
                }
                break;
        }
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case R.id.rss_feed_loader:
                // Update with all sources
                final String[] sources = getResources()
                        .getStringArray(R.array.rss_sources_array);
                return new RssFeedLoader(this, sources);
            default:
                throw new RuntimeException("There's no loader with given id.");
        }
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean success) {
        if (success || mDatabaseHelper.hasItems()) {
            setState(LoadingState.SUCCESS);
        } else {
            setState(LoadingState.FAILURE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {
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
