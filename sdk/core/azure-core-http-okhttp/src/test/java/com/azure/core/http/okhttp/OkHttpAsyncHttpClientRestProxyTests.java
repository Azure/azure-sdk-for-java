// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.RestProxyTestsServer;
import com.azure.core.test.http.LocalTestServer;
import com.azure.core.test.implementation.RestProxyTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static com.azure.core.http.okhttp.TestUtils.createQuietDispatcher;

public class OkHttpAsyncHttpClientRestProxyTests extends RestProxyTests {
    private static LocalTestServer server;

    @BeforeAll
    public static void getWireMockServer() {
        server = RestProxyTestsServer.getRestProxyTestsServer();
        server.start();
    }

    @AfterAll
    public static void shutdownWireMockServer() {
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
        return new OkHttpAsyncHttpClientBuilder()
            .dispatcher(createQuietDispatcher(UnexpectedLengthException.class, "request body emitted"))
            .build();
    }
}
