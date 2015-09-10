package com.friendoye.rss_reader.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.database.DatabaseHelper;
import com.friendoye.rss_reader.database.DatabaseManager;
import com.friendoye.rss_reader.fragments.DetailsFragment;
import com.friendoye.rss_reader.fragments.RssFeedItemFragment;
import com.friendoye.rss_reader.model.RssFeedItem;

/**
 * This activity holds DetailsFragment.
 */
public class DetailsActivity extends AppCompatActivity
        implements RssFeedItemFragment.OnDownloadCompletedListener {
    public static final String ID_KEY = "id key";
    private static final String DATA_FRAGMENT_TAG = "data tag";

    private DetailsFragment mViewFragment;

    private RssFeedItemFragment mDataFragment;
    private DatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mDataFragment = (RssFeedItemFragment) fragmentManager
                .findFragmentByTag(DATA_FRAGMENT_TAG);;

        if (mDataFragment == null) {
            mDataFragment = new RssFeedItemFragment();
            fragmentManager.beginTransaction()
                    .add(mDataFragment, DATA_FRAGMENT_TAG)
                    .commit();
        }

        if (mViewFragment == null) {
            mViewFragment = (DetailsFragment) fragmentManager
                    .findFragmentById(R.id.view_fragment);
        }

        mDatabaseHelper = DatabaseManager.getHelper(this, DatabaseHelper.class);

        if (savedInstanceState == null) {
            int id = getIntent().getIntExtra(ID_KEY, -1);
            if (id != -1) {
                RssFeedItem item = mDatabaseHelper.getFeedItem(id);
                mDataFragment.setItem(item);
                mDataFragment.downloadDescription();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
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
    public void onDownloadSuccess() {
        RssFeedItem item = mDataFragment.getItem();
        mViewFragment.updateViews(item);
    }

    @Override
    public void onDownloadFailure() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseManager.releaseHelper();
        mDatabaseHelper = null;
    }
}
