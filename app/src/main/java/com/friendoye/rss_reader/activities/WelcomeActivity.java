package com.friendoye.rss_reader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.friendoye.rss_reader.Application;
import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.database.DatabaseHelper;
import com.friendoye.rss_reader.database.DatabaseManager;
import com.friendoye.rss_reader.utils.DownloadManager;
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
        implements DownloadManager.OnDownloadCompletedListener {
    private TextView mWaitView;
    private ProgressBar mProgressBar;
    private TextView mTitleView;
    private TextView mMessageView;

    private DownloadManager mDownloadManager;
    private LoadingState mState;

    private DatabaseHelper mDatabaseHelper;
    private String[] mSources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        mWaitView = (TextView) findViewById(R.id.waitTextView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTitleView = (TextView) findViewById(R.id.errorTitleView);
        mMessageView = (TextView) findViewById(R.id.errorMessageView);

        mDatabaseHelper = DatabaseManager.getHelper(this, DatabaseHelper.class);
        mSources = getResources().getStringArray(R.array.rss_sources_array);

        mDownloadManager = Application.get(this).getDownloadManager();
        mDownloadManager.subscribe(this);
        if (savedInstanceState == null) {
            mDownloadManager.refreshData(mSources);
            setState(LoadingState.LOADING);
        } else {
            setState(mDownloadManager.getState());
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

    @Override
    public void onDownloadComplete(LoadingState state) {
        setState(state);
    }

    protected void setState(LoadingState state) {
        mState = state;
        switch (state) {
            case LOADING:
                showProgressBar();
                break;
            case SUCCESS:
                Intent startIntent = new Intent(this, RssFeedActivity.class);
                startActivity(startIntent);
                finish();
                break;
            case FAILURE:
                if (mDatabaseHelper.hasItems()) {
                    setState(LoadingState.SUCCESS);
                } else {
                    showError();
                }
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
                    mDownloadManager.refreshData(mSources);
                    setState(mDownloadManager.getState());
                }
                break;
        }
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
