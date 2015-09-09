package com.friendoye.rss_reader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ProgressBar;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.loaders.RssFeedLoader;

/**
 * Launcher splashscreen activity, that shows up until RSS feed won't be retrieved
 * or any error will occur.
 */
public class WelcomeActivity extends FragmentActivity
        implements LoaderManager.LoaderCallbacks<Boolean> {
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        getSupportLoaderManager().initLoader(R.id.rss_feed_loader, null, this);
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        mProgressBar.setVisibility(View.VISIBLE);
        switch (id) {
            case R.id.rss_feed_loader:
                // TODO: Source might be custom
                return new RssFeedLoader(this, "http://www.onliner.by/feed");
            default:
                throw new RuntimeException("There's no loader with gived id.");
        }
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean success) {
        Intent startIntent = new Intent(this, RssFeedActivity.class);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startIntent);
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {
    }
}
