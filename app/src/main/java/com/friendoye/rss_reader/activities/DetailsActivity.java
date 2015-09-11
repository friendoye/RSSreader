package com.friendoye.rss_reader.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.database.DatabaseHelper;
import com.friendoye.rss_reader.database.DatabaseManager;
import com.friendoye.rss_reader.fragments.DetailsFragment;
import com.friendoye.rss_reader.fragments.ProgressFragment;
import com.friendoye.rss_reader.fragments.RssFeedItemFragment;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.utils.LoadingState;

/**
 * This activity holds DetailsFragment.
 */
public class DetailsActivity extends SherlockFragmentActivity
        implements RssFeedItemFragment.OnDownloadCompletedListener {
    public static final String ID_KEY = "id key";
    private static final String STATE_KEY = "state key";
    private static final String VIEW_FRAGMENT_TAG = "view tag";
    private static final String DATA_FRAGMENT_TAG = "data tag";
    private static final String PROGRESS_FRAGMENT_TAG = "progress tag";

    private DetailsFragment mViewFragment;
    private ProgressFragment mProgressFragment;

    private RssFeedItemFragment mDataFragment;
    private DatabaseHelper mDatabaseHelper;

    private LoadingState state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        initFragments();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabaseHelper = DatabaseManager.getHelper(this, DatabaseHelper.class);

        if (savedInstanceState == null) {
            int id = getIntent().getIntExtra(ID_KEY, -1);
            if (id != -1) {
                RssFeedItem item = mDatabaseHelper.getFeedItem(id);
                mDataFragment.setItem(item);
                setState(LoadingState.LOADING);
            } else {
                throw new RuntimeException("There's no news to show!");
            }
        } else {
            String stateString = savedInstanceState.getString(STATE_KEY);
            state = LoadingState.valueOf(stateString);
        }
    }

    private void initFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        mDataFragment = (RssFeedItemFragment) fragmentManager
                .findFragmentByTag(DATA_FRAGMENT_TAG);
        if (mDataFragment == null) {
            mDataFragment = new RssFeedItemFragment();
            fragmentManager.beginTransaction()
                    .add(mDataFragment, DATA_FRAGMENT_TAG)
                    .commit();
        }

        mProgressFragment = (ProgressFragment) fragmentManager
                .findFragmentByTag(PROGRESS_FRAGMENT_TAG);
        if (mProgressFragment == null) {
            mProgressFragment = new ProgressFragment();
        }

        mViewFragment = (DetailsFragment) fragmentManager
                .findFragmentByTag(VIEW_FRAGMENT_TAG);
        if (mViewFragment == null) {
            mViewFragment = new DetailsFragment();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDownloadSuccess() {
        setState(LoadingState.SUCCESS);
    }

    @Override
    public void onDownloadFailure() {
        setState(LoadingState.FAILURE);
    }

    protected void setState(LoadingState state) {
        switch (state) {
            case LOADING:
                setForegroundFragment(mProgressFragment, PROGRESS_FRAGMENT_TAG);
                mDataFragment.downloadFullInfo();
                break;
            case SUCCESS:
                setForegroundFragment(mViewFragment, VIEW_FRAGMENT_TAG);
                RssFeedItem item = mDataFragment.getItem();
                mViewFragment.setData(item);
                break;
        }
    }

    protected void setForegroundFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_view, fragment, tag)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_KEY, state.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseManager.releaseHelper();
        mDatabaseHelper = null;
    }
}
