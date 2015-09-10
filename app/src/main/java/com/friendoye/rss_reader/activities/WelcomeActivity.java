package com.friendoye.rss_reader.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.loaders.RssFeedLoader;

/**
 * Launcher splashscreen activity, that shows up until RSS feed won't be retrieved
 * or any error will occur.
 */
public class WelcomeActivity extends SherlockFragmentActivity
        implements LoaderManager.LoaderCallbacks<Boolean> {
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        if(activeNetworkConnection()) {
            getSupportLoaderManager().initLoader(R.id.rss_feed_loader,
                    null, this);
        } else {
            // TODO: Print error and suggest to try again
        }
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        mProgressBar.setVisibility(View.VISIBLE);
        switch (id) {
            case R.id.rss_feed_loader:
                // TODO: Source might be custom
                return new RssFeedLoader(this, "http://www.onliner.by/feed");
            default:
                throw new RuntimeException("There's no loader with given id.");
        }
    }

    private boolean activeNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean success) {
        if (success) {
            Intent startIntent = new Intent(this, RssFeedActivity.class);
            startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(startIntent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {
        // Do nothing.
    }
}
