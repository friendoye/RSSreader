package com.friendoye.rss_reader.utils;

/**
 * Helper enum, for easy management of downloading from network.
 */
public enum LoadingState {
    NONE,
    LOADING,
    FAILURE,
    SUCCESS;
}
