package com.friendoye.rss_reader.utils;

import java.util.Arrays;

/**
 * Helper class for packing several Strings as one String.
 * Usage:
 *  1) Storing Strings with SharedPreferences.
 */
public class Packer {

    public static String packCollection(String[] strings) {
        boolean[] using = new boolean[strings.length];
        Arrays.fill(using, true);
        return packCollection(strings, using);
    }

    public static String packCollection(String[] strings,
                                        boolean[] using) {
        StringBuilder buffer = new StringBuilder();
        int i = 0;
        for (String string: strings) {
            if (using[i++]) {
                buffer.append(string).append(';');
            }
        }

        if (buffer.length() == 0) {
            return null;
        } else {
            int lastCharPos = buffer.length() - 1;
            return buffer.deleteCharAt(lastCharPos).toString();
        }
    }

    public static String[] unpackAsStringArray(String string) {
        if (string != null) {
            return string.split(";");
        } else {
            return null;
        }
    }
}
