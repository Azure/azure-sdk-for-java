// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.test.RestProxyTestsServer;
import com.azure.core.test.http.LocalTestServer;
import com.azure.core.test.implementation.RestProxyTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class VertxAsyncHttpClientRestProxyTests extends RestProxyTests {
    private static LocalTestServer server;

    @BeforeAll
    public static void beforeAll() {
        server = RestProxyTestsServer.getRestProxyTestsServer();
        server.start();
    }

    @AfterAll
    public static void afterAll() {
        if (server != null) {
            server.stop();
        }
    }

    @Override
    protected int getPort() {
        return server.getHttpPort();
    }

    @Override
    protected HttpClient createHttpClient() {
        return new VertxAsyncHttpClientBuilder().build();
    }
}
