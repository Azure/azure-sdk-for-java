/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import reactor.util.annotation.Nullable;

public class UrlParser {

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

    private UrlParser() {
    }
}
