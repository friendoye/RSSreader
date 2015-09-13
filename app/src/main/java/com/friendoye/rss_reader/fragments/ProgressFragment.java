package com.friendoye.rss_reader.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.model.RssFeedItem;
import com.friendoye.rss_reader.utils.LoadingState;

/**
 * Fragment, that shows progress bar, while you're loading your data.
 */
public class ProgressFragment extends Fragment {
    private ProgressBar mProgressBar;
    private TextView mMessageView;
    private Button mRetryButton;

    private OnRetryListener mCallback;

    private LoadingState mState = LoadingState.LOADING;

    public interface OnRetryListener {
        void onRetry();
    }

    public ProgressFragment() {
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        try {
            mCallback = (OnRetryListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must " +
                    "implement OnRetryListener!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_progress,
                container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mMessageView = (TextView) rootView.findViewById(R.id.messageView);
        mRetryButton = (Button) rootView.findViewById(R.id.retryButton);
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar();
                mCallback.onRetry();
            }
        });

        updateViews();

        return rootView;
    }

    public void setState(LoadingState state) {
        mState = state;
        if (getView() != null) {
            updateViews();
        }
    }

    protected void updateViews() {
        switch (mState) {
            case FAILURE:
                showRetryMessage();
                break;
            default:
                showProgressBar();
                break;
        }
    }

    private void showProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            mMessageView.setVisibility(View.GONE);
            mRetryButton.setVisibility(View.GONE);
        }
    }

    private void showRetryMessage() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
            mMessageView.setVisibility(View.VISIBLE);
            mRetryButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }
}
