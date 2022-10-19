// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.test.HttpClientTestsWireMockServer;
import com.azure.core.test.http.HttpClientTests;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class VertxAsyncHttpClientAsyncHttpClientTests extends HttpClientTests {
    private static WireMockServer server;

    @BeforeAll
    public static void beforeAll() {
        server = HttpClientTestsWireMockServer.getHttpClientTestsServer();
        server.start();
    }

    @AfterAll
    public static void afterAll() throws Exception {
        if (server != null) {
            server.shutdown();
        }
    }

    @Override
    protected int getWireMockPort() {
        return server.port();
    }

    @Override
    protected HttpClient createHttpClient() {
        return new VertxAsyncHttpClientBuilder().build();
    }
}
