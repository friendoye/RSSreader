package com.friendoye.rss_reader.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.MenuItem;

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
public class DetailsActivity extends AppCompatActivity
        implements RssFeedItemFragment.OnDownloadCompletedListener,
        ProgressFragment.OnRetryListener {
    public static final String LINK_KEY = "link key";
    public static final String CLASS_NAME_KEY = "class name key";
    private static final String STATE_KEY = "state key";
    private static final String VIEW_FRAGMENT_TAG = "view tag";
    private static final String DATA_FRAGMENT_TAG = "data tag";
    private static final String PROGRESS_FRAGMENT_TAG = "progress tag";

    private DetailsFragment mViewFragment;
    private ProgressFragment mProgressFragment;

    private RssFeedItemFragment mDataFragment;
    private DatabaseHelper mDatabaseHelper;

    private LoadingState mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initFragments();

        mDatabaseHelper = DatabaseManager.getHelper(this, DatabaseHelper.class);

        if (savedInstanceState == null) {
            /////////////////////////////
            Fragment dataFragment = getSupportFragmentManager()
                    .findFragmentByTag(DATA_FRAGMENT_TAG);
            if (dataFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(dataFragment)
                        .commit();
            }
            /////////////////////////////
            Intent intent = getIntent();
            String link = intent.getStringExtra(LINK_KEY);
            Class configClass;
            try {
                configClass = Class.forName(intent.getStringExtra(CLASS_NAME_KEY));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (link != null) {
                RssFeedItem item = mDatabaseHelper.getFeedItem(link, configClass);
                mDataFragment.setItem(item);
                mState = LoadingState.NONE;
            } else {
                throw new RuntimeException("There's no news to show!");
            }
        } else {
            String stateString = savedInstanceState.getString(STATE_KEY);
            mState = LoadingState.valueOf(stateString);
        }

        setState(mState);
        mViewFragment.setData(mDataFragment.getItem());
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

    @Override
    public void onRetry() {
        setState(LoadingState.LOADING);
    }

    protected void setState(LoadingState state) {
        mState = state;
        switch (state) {
            case NONE:
                mDataFragment.downloadFullInfo();
                setState(LoadingState.LOADING);
                break;
            case LOADING:
                setForegroundFragment(mProgressFragment, PROGRESS_FRAGMENT_TAG);
                break;
            case SUCCESS:
                setForegroundFragment(mViewFragment, VIEW_FRAGMENT_TAG);
                RssFeedItem item = mDataFragment.getItem();
                mViewFragment.setData(item);
                break;
            case FAILURE:
                mProgressFragment.setState(LoadingState.FAILURE);
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
        if (outState == null) {
            outState = new Bundle();
        }
        outState.putString(STATE_KEY, mState.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DatabaseManager.releaseHelper();
        mDatabaseHelper = null;
    }
}
