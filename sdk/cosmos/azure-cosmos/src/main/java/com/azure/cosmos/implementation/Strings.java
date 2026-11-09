// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class Strings {
    public static final String Emtpy = "";

    private final static String UTF8_CHARSET = StandardCharsets.UTF_8.name();

    public static boolean isNullOrWhiteSpace(String str) {
        return StringUtils.isEmpty(str) || StringUtils.isWhitespace(str);
    }
    public static boolean isNullOrEmpty(String str) {
        return StringUtils.isEmpty(str);
    }

    public static String toString(boolean value) {
        return Boolean.toString(value);
    }

    public static String toString(int value) {
        return Integer.toString(value);
    }

    public static boolean areEqual(String str1, String str2) {
        return StringUtils.equals(str1, str2);
    }

    public static boolean areEqualIgnoreCase(String str1, String str2) {
        return StringUtils.equalsIgnoreCase(str1, str2);
    }

    public static boolean containsIgnoreCase(String str1, String str2) {
        return StringUtils.containsIgnoreCase(str1, str2);
    }

    public static String fromCamelCaseToUpperCase(String str) {
        if (str == null) {
            return null;
        }

        StringBuilder result = new StringBuilder(str);

        int i = 1;
        while (i < result.length()) {
            if (Character.isUpperCase(result.charAt(i))) {
                result.insert(i, '_');
                i += 2;
            } else {
                result.replace(i, i + 1, Character.toString(Character.toUpperCase(result.charAt(i))));
                i ++;
            }
        }

        return result.toString();
    }

    public static String encodeURIComponent(String text) {
        String result;
        try {
            result = URLEncoder.encode(text, UTF8_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        // after URLEncoding - the following transformations need to be applied
        // to get to encodeUriComponent consistent behavior
        //  "+" -> "%20"
        //  "%21" -> "!"
        //  "%27" -> "'"
        //  "%28" -> "("
        //  "%29" -> ")"
        //  "%7E" -> "~"

        final int len = result.length();
        final StringBuilder buf = new StringBuilder(
            result.length() + 4); // leaving enough buffer for two '+' replacements
                                          // without having to allocate new buffer
        for (int i = 0; i < len; i++) {
            char currentChar = result.charAt(i);

            if (currentChar == '+') {
                buf.append("%20");
            } else if (currentChar == '%' && i < len - 2) {
                char nextChar = result.charAt(i + 1);
                char secondToNextChar = result.charAt(i + 2);
                if (nextChar == '7' && secondToNextChar == 'E') {
                    i += 2;
                    buf.append('~');
                } else if (nextChar == '2') {
                    switch (secondToNextChar) {
                        case '1':
                            buf.append('!');
                            i += 2;
                            break;
                        case '7':
                            buf.append('\'');
                            i += 2;
                            break;
                        case '8':
                            buf.append('(');
                            i += 2;
                            break;
                        case '9':
                            buf.append(')');
                            i += 2;
                            break;
                        default:
                            buf.append(currentChar);
                    }
                } else {
                    buf.append(currentChar);
                }
            } else {
                buf.append(currentChar);
            }
        }

        return buf.toString();
    }
}
