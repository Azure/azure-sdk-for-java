// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.http.HttpClient;
import com.azure.core.test.http.HttpClientTests;
import com.azure.core.test.http.LocalTestServer;
import com.azure.core.test.utils.HttpURLConnectionHttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class HttpClientTestsTests extends HttpClientTests {
    private static LocalTestServer server;

    @BeforeAll
    public static void getServer() {
        server = HttpClientTestsWireMockServer.getHttpClientTestsServer();
        server.start();
    }

    @AfterAll
    public static void shutdownServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Override
    @Deprecated
    protected int getWireMockPort() {
        return server.getHttpPort();
    }

    @Override
    protected String getServerUri(boolean secure) {
        return secure ? server.getHttpsUri() : server.getHttpUri();
    }

    @Override
    protected HttpClient createHttpClient() {
        return new HttpURLConnectionHttpClient();
    }
}
