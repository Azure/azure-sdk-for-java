// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.test.HttpClientTestsWireMockServer;
import com.azure.core.test.http.HttpClientTests;
import com.azure.core.test.http.LocalTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class VertxAsyncHttpClientAsyncHttpClientTests extends HttpClientTests {
    private static LocalTestServer server;

    @BeforeAll
    public static void beforeAll() {
        server = HttpClientTestsWireMockServer.getHttpClientTestsServer();
        server.start();
    }

    @AfterAll
    public static void afterAll() {
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
        return new VertxAsyncHttpClientBuilder().build();
    }
}
