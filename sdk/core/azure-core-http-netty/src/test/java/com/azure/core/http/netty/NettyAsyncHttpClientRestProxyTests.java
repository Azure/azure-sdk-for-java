// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.test.RestProxyTestsServer;
import com.azure.core.test.http.LocalTestServer;
import com.azure.core.test.implementation.RestProxyTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class NettyAsyncHttpClientRestProxyTests extends RestProxyTests {
    private static LocalTestServer server;

    @BeforeAll
    public static void startTestServer() {
        server = RestProxyTestsServer.getRestProxyTestsServer();
        server.start();
    }

    @AfterAll
    public static void stopTestServer() {
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
        return new NettyAsyncHttpClientBuilder().build();
    }
}
