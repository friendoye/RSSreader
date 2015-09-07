package com.friendoye.rss_reader.activities;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.loaders.RssFeedLoader;

/**
 * Launcher splashscreen activity, that shows up until RSS feed won't be retrieved
 * or any error will occur.
 */
public class WelcomeActivity extends FragmentActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        getSupportLoaderManager().initLoader(R.id.rss_feed_loader, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mProgressBar.setVisibility(View.VISIBLE);
        // TODO: Source might be custom
        return new RssFeedLoader(this, "http://www.onliner.by/feed");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Toast.makeText(this, "RSS feed downloading completed.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
