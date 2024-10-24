// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// Includes work from:
/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import reactor.util.annotation.Nullable;

public class UrlParser {

    @Nullable
    public static String getTarget(String url) {

        int schemeEndIndexExclusive = getSchemeEndIndexExclusive(url);
        if (schemeEndIndexExclusive == -1) {
            // invalid url
            return null;
        }

        int hostEndIndexExclusive = getHostEndIndexExclusive(url, schemeEndIndexExclusive);
        if (hostEndIndexExclusive == schemeEndIndexExclusive) {
            // no host (or port)
            return null;
        }

        if (hostEndIndexExclusive < url.length() && url.charAt(hostEndIndexExclusive) != ':') {
            // no port
            return url.substring(schemeEndIndexExclusive, hostEndIndexExclusive);
        }

        int portStartIndex = hostEndIndexExclusive + 1;

        int portEndIndexExclusive = getPortEndIndexExclusive(url, portStartIndex);
        if (portEndIndexExclusive == portStartIndex) {
            // no port
            return url.substring(schemeEndIndexExclusive, hostEndIndexExclusive);
        }

        String port = url.substring(portStartIndex, portEndIndexExclusive);

        if ((port.equals("80") && url.startsWith("http://")) || (port.equals("443") && url.startsWith("https://"))) {
            return url.substring(schemeEndIndexExclusive, hostEndIndexExclusive);
        }

        return url.substring(schemeEndIndexExclusive, portEndIndexExclusive);
    }

    @Nullable
    public static String getPath(String url) {

        int schemeEndIndexExclusive = getSchemeEndIndexExclusive(url);
        if (schemeEndIndexExclusive == -1) {
            // invalid url
            return null;
        }

        int hostEndIndexExclusive = getHostEndIndexExclusive(url, schemeEndIndexExclusive);
        int portEndIndexExclusive = getPortEndIndexExclusive(url, hostEndIndexExclusive);
        int pathEndIndexExclusive = getPathEndIndexExclusive(url, portEndIndexExclusive);

        return url.substring(portEndIndexExclusive, pathEndIndexExclusive);
    }

    @Nullable
    public static String getHost(String url) {

        int hostStartIndex = getSchemeEndIndexExclusive(url);
        if (hostStartIndex == -1) {
            // invalid url
            return null;
        }

        int hostEndIndexExclusive = getHostEndIndexExclusive(url, hostStartIndex);
        if (hostEndIndexExclusive == hostStartIndex) {
            // no host
            return null;
        }

        return url.substring(hostStartIndex, hostEndIndexExclusive);
    }

    @Nullable
    public static Integer getPort(String url) {

        int schemeEndIndexExclusive = getSchemeEndIndexExclusive(url);
        if (schemeEndIndexExclusive == -1) {
            // invalid url
            return null;
        }

        int hostEndIndexExclusive = getHostEndIndexExclusive(url, schemeEndIndexExclusive);
        if (hostEndIndexExclusive == schemeEndIndexExclusive) {
            // no host (or port)
            return null;
        }

        if (hostEndIndexExclusive < url.length() && url.charAt(hostEndIndexExclusive) != ':') {
            // no port
            return null;
        }

        int portStartIndex = hostEndIndexExclusive + 1;

        int portEndIndexExclusive = getPortEndIndexExclusive(url, portStartIndex);
        if (portEndIndexExclusive == portStartIndex) {
            // no port
            return null;
        }

        return safeParse(url.substring(portStartIndex, portEndIndexExclusive));
    }

    public static int getSchemeEndIndexExclusive(String url) {

        int schemeEndIndex = url.indexOf(':');
        if (schemeEndIndex == -1) {
            // invalid url
            return -1;
        }

        int len = url.length();
        if (len <= schemeEndIndex + 2
            || url.charAt(schemeEndIndex + 1) != '/'
            || url.charAt(schemeEndIndex + 2) != '/') {
            // has no authority component
            return -1;
        }

        return schemeEndIndex + 3;
    }

    public static int getHostEndIndexExclusive(String url, int startIndex) {
        // look for the end of the host:
        //   ':' ==> start of port, or
        //   '/', '?', '#' ==> start of path
        int index;
        int len = url.length();
        for (index = startIndex; index < len; index++) {
            char c = url.charAt(index);
            if (c == ':' || c == '/' || c == '?' || c == '#') {
                break;
            }
        }
        return index;
    }

    public static int getPortEndIndexExclusive(String url, int startIndex) {
        // look for the end of the port:
        //   '/', '?', '#' ==> start of path
        int index;
        int len = url.length();
        for (index = startIndex; index < len; index++) {
            char c = url.charAt(index);
            if (c == '/' || c == '?' || c == '#') {
                break;
            }
        }
        return index;
    }

    @Nullable
    private static Integer safeParse(String port) {
        try {
            return Integer.valueOf(port);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // returns the ending index of the path component (exclusive)
    private static int getPathEndIndexExclusive(String url, int startIndex) {
        // look for the end of the port:
        //   '/', '?', '#' ==> start of path
        int index;
        int len = url.length();
        for (index = startIndex; index < len; index++) {
            char c = url.charAt(index);
            if (c == '?' || c == '#') {
                break;
            }
        }
        return index;
    }

    private UrlParser() {
    }
}
