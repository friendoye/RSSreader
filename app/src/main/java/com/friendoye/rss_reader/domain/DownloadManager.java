package com.friendoye.rss_reader.domain;

import androidx.annotation.NonNull;

import com.friendoye.rss_reader.data.database.DatabaseHelper;
import com.friendoye.rss_reader.data.database.DatabaseManager;
import com.friendoye.rss_reader.utils.LoadingState;

import java.util.List;

public interface DownloadManager {

    interface OnDownloadStateChangedListener {
        void onDownloadStateChanged(LoadingState state);
    }

    @NonNull
    LoadingState getState();

    void refreshData(@NonNull List<String> sources);

    void subscribe(@NonNull OnDownloadStateChangedListener observer);
    void unsubscribe(@NonNull OnDownloadStateChangedListener observer);
}