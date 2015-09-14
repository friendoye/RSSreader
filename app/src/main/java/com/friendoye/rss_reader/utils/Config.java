package com.friendoye.rss_reader.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Helper class for providing setup across the app.
 */
public class Config {
    public static final SimpleDateFormat DATE_FORMATTER =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", new Locale("ru"));
    public static final String SOURCE_INDEX_SET_KEY = "source list key";
}
