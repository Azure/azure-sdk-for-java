// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import reactor.util.annotation.Nullable;

public class UrlParser {

    private UrlParser() {
    }

    /**
     * Returns the "target" (host:port) portion of the url.
     *
     * <p>Returns {@code null} if the target cannot be extracted from url for any reason.
     */
    @Nullable
    public static String getTargetFromUrl(String url) {

        int schemeEndIndex = url.indexOf(':');
        if (schemeEndIndex == -1) {
            // not a valid url
            return null;
        }

        int len = url.length();
        if (schemeEndIndex + 2 < len
            && url.charAt(schemeEndIndex + 1) == '/'
            && url.charAt(schemeEndIndex + 2) == '/') {
            // has authority component
            // look for
            //   '/' - start of path
            //   '?' or end of string - empty path
            int index;
            for (index = schemeEndIndex + 3; index < len; index++) {
                char c = url.charAt(index);
                if (c == '/' || c == '?' || c == '#') {
                    break;
                }
            }
            String target = url.substring(schemeEndIndex + 3, index);
            return target.isEmpty() ? null : target;
        } else {
            // has no authority
            return null;
        }
    }

    /**
     * Returns the path portion of the url.
     *
     * <p>Returns {@code null} if the path cannot be extracted from url for any reason.
     */
    @Nullable
    public static String getPathFromUrl(String url) {

        int schemeEndIndex = url.indexOf(':');
        if (schemeEndIndex == -1) {
            // not a valid url
            return null;
        }

        int len = url.length();
        if (schemeEndIndex + 2 < len
            && url.charAt(schemeEndIndex + 1) == '/'
            && url.charAt(schemeEndIndex + 2) == '/') {
            // has authority component
            // look for
            //   '/' - start of path
            //   '?' or end of string - empty path
            int pathStartIndex = -1;
            for (int i = schemeEndIndex + 3; i < len; i++) {
                char c = url.charAt(i);
                if (c == '/') {
                    pathStartIndex = i;
                    break;
                } else if (c == '?' || c == '#') {
                    // empty path
                    return "";
                }
            }
            if (pathStartIndex == -1) {
                // end of the url was reached while scanning for the beginning of the path
                // which means the path is empty
                return "";
            }
            int pathEndIndex = getPathEndIndex(url, pathStartIndex + 1);
            return url.substring(pathStartIndex, pathEndIndex);
        } else {
            // has no authority, path starts right away
            int pathStartIndex = schemeEndIndex + 1;
            int pathEndIndex = getPathEndIndex(url, pathStartIndex);
            return url.substring(pathStartIndex, pathEndIndex);
        }
    }

    // returns the ending index of the path component (exclusive)
    private static int getPathEndIndex(String url, int startIndex) {
        int len = url.length();
        for (int i = startIndex; i < len; i++) {
            char c = url.charAt(i);
            if (c == '?' || c == '#') {
                return i;
            }
        }
        return len;
    }
}
