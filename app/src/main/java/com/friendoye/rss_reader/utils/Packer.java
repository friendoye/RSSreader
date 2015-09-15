package com.friendoye.rss_reader.utils;

import java.util.Collection;

/**
 * Helper class for packing several Strings as one String.
 * Usage:
 *  1) Storing Strings with SharedPreferences.
 */
public class Packer {

    public static String packCollection(Collection<String> strings) {
        StringBuilder buffer = new StringBuilder();
        for (String string: strings) {
            buffer.append(string).append(';');
        }
        int lastCharPos = buffer.length() - 1;
        return buffer.deleteCharAt(lastCharPos).toString();
    }

    public static String[] unpackAsStringArray(String string) {
        return string.split(";");
    }
}
