// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class Strings {
    public static final String Emtpy = "";
    private final static String[][] ENCODE_URI_COMPONENT_REPLACEMENTS = {
        { "\\+", "%20" },
        { "%21", "!" },
        { "%27", "'" },
        { "%28", "(" },
        { "%29", ")" },
        { "%7E", "~" }
    };

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
        for (String[] entry : ENCODE_URI_COMPONENT_REPLACEMENTS) {
            result = result.replaceAll(entry[0], entry[1]);
        }

        return result;
    }
}
