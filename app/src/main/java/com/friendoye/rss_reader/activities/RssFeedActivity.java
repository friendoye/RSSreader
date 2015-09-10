package com.friendoye.rss_reader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.database.DatabaseHelper;
import com.friendoye.rss_reader.database.DatabaseManager;
import com.friendoye.rss_reader.fragments.RssFeedFragment;
import com.friendoye.rss_reader.model.RssFeedItem;

/**
 * This activity holds RssFeedFragment.
 */
public class RssFeedActivity extends AppCompatActivity
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rss_feed, menu);
        return true;
    }

    @Override
    public void onItemSelected(RssFeedItem item) {
        Intent startIntent = new Intent(this, DetailsActivity.class);
        startIntent.putExtra(DetailsActivity.ID_KEY, item.id);
        startActivity(startIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DatabaseManager.releaseHelper();
        mDatabaseHelper = null;
    }
}
