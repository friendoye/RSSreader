package com.friendoye.rss_reader.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.database.DatabaseHelper;
import com.friendoye.rss_reader.database.DatabaseManager;
import com.friendoye.rss_reader.dialogs.SourcesListDialogFragment;
import com.friendoye.rss_reader.fragments.RssFeedFragment;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.utils.Config;
import com.friendoye.rss_reader.utils.Packer;

/**
 * This activity holds RssFeedFragment.
 */
public class RssFeedActivity extends AppCompatActivity
        implements RssFeedFragment.OnItemSelectedListener,
        SourcesListDialogFragment.OnSourcesChangedListener {
    private DatabaseHelper mDatabaseHelper;
    private RssFeedFragment mFeedFragment;
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

        mDatabaseHelper = DatabaseManager.getHelper(this, DatabaseHelper.class);

        updateSources();
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
    protected void onDestroy() {
        super.onDestroy();

        DatabaseManager.releaseHelper();
        mDatabaseHelper = null;
    }
}
