// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.test.HttpClientTestsServer;
import com.azure.core.test.http.HttpClientTests;
import com.azure.core.test.http.LocalTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
public class VertxHttpClientAsyncHttpClientTests extends HttpClientTests {
    private static LocalTestServer server;

    @BeforeAll
    public static void beforeAll() {
        server = HttpClientTestsServer.getHttpClientTestsServer();
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
    protected int getPort() {
        return server.getHttpPort();
    }

    @Override
    protected String getServerUri(boolean secure) {
        return secure ? server.getHttpsUri() : server.getHttpUri();
    }

    @Override
    protected HttpClient createHttpClient() {
        return new VertxHttpClientBuilder().build();
    }
}
