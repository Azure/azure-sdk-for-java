// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.UrlBuilder;

import java.net.MalformedURLException;
import java.net.URL;

public class TestProxyUtils {
    private static final String proxyUrlScheme = "http";
    private static final String proxyUrlHost = "localhost";
    private static final int proxyUrlPort = 5000;
    private static final String proxyUrl = String.format("%s://%s:%d", proxyUrlScheme, proxyUrlHost, proxyUrlPort);

    public static String getProxyUrl() {
        return proxyUrl;
    }

    public static void changeHeaders(HttpRequest request, String xRecordingId, String mode) {
        UrlBuilder builder = UrlBuilder.parse(request.getUrl());
        builder.setScheme(proxyUrlScheme);
        builder.setHost(proxyUrlHost);
        builder.setPort(proxyUrlPort);

        UrlBuilder builder2 = UrlBuilder.parse(request.getUrl());
        builder2.setPath("");
        builder2.setQuery("");

        try {
            URL originalUrl = builder2.toUrl();

            HttpHeaders headers = request.getHeaders();

            headers.add("x-recording-upstream-base-uri", originalUrl.toString());
            headers.add("x-recording-mode", mode);
            headers.add("x-recording-id", xRecordingId);
            request.setUrl(builder.toUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


}

