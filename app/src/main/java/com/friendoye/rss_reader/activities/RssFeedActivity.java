package com.friendoye.rss_reader.activities;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.database.DatabaseHelper;
import com.friendoye.rss_reader.database.DatabaseManager;
import com.friendoye.rss_reader.fragments.RssFeedFragment;
import com.friendoye.rss_reader.model.RssFeedItem;

/**
 * This activity holds RssFeedFragment.
 */
public class RssFeedActivity extends SherlockFragmentActivity
        implements  RssFeedFragment.OnItemSelectedListener {
    private DatabaseHelper mDatabaseHelper;
    private RssFeedFragment mFeedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rss_feed);

        mDatabaseHelper = DatabaseManager.getHelper(this, DatabaseHelper.class);

        if (mFeedFragment == null) {
            mFeedFragment = (RssFeedFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.feed_fragment);
        }

        mFeedFragment.setFeedItems(mDatabaseHelper.getAllFeedItems());
    }

    @Override
    public void onItemSelected(RssFeedItem item) {
        Intent startIntent = new Intent(this, DetailsActivity.class);
        startIntent.putExtra(DetailsActivity.ID_KEY, item.id);
        startActivity(startIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DatabaseManager.releaseHelper();
        mDatabaseHelper = null;
    }
}
