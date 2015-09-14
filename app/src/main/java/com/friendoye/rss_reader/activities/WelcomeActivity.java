package com.friendoye.rss_reader.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.loaders.RssFeedLoader;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.utils.LoadingState;

/**
 * Launcher splashscreen activity, that shows up until RSS feed won't be retrieved
 * or any error will occur.
 */
public class WelcomeActivity extends SherlockFragmentActivity
        implements LoaderManager.LoaderCallbacks<Boolean> {
    private static final String STATE_KEY = "state key";

    private TextView mWaitView;
    private ProgressBar mProgressBar;
    private TextView mTitleView;
    private TextView mMessageView;

    private LoadingState mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        mWaitView = (TextView) findViewById(R.id.waitTextView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTitleView = (TextView) findViewById(R.id.errorTitleView);
        mMessageView = (TextView) findViewById(R.id.errorMessageView);

        if (savedInstanceState != null) {
            String stateString = savedInstanceState.getString(STATE_KEY);
            mState = LoadingState.valueOf(stateString);
        } else {
            setState(LoadingState.LOADING);
        }

        if (mState == LoadingState.LOADING) {
            getSupportLoaderManager().initLoader(R.id.rss_feed_loader,
                    null, this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outState == null) {
            outState = new Bundle();
        }
        outState.putString(STATE_KEY, mState.toString());
        super.onSaveInstanceState(outState);
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

    private boolean activeNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void onClick(View view) {
        if (mState != LoadingState.FAILURE) {
            return;
        }
        switch (view.getId()) {
            case R.id.container_layout:
                setState(LoadingState.LOADING);
                getSupportLoaderManager().restartLoader(R.id.rss_feed_loader,
                        null, this);
                break;
        }
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case R.id.rss_feed_loader:
                // TODO: Inject dependency with preferences
                final String[] sources = getResources()
                        .getStringArray(R.array.rss_sources_array);
                return new RssFeedLoader(this, sources);
            default:
                throw new RuntimeException("There's no loader with given id.");
        }
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean success) {
        if (success) {
            setState(LoadingState.SUCCESS);
        } else {
            setState(LoadingState.FAILURE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {
        // Do nothing.
    }
}
