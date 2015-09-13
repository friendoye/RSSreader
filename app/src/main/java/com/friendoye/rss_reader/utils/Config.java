package com.friendoye.rss_reader.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Helper class for providing setup across the app.
 */
public class Config {
    public static final SimpleDateFormat dateFormatter =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", new Locale("ru"));
}
