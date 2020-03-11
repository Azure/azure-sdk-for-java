// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.test.RestProxyTestsWireMockServer;
import com.azure.core.test.implementation.RestProxyTests;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class RestProxyWithOkHttpTests extends RestProxyTests {
    private static WireMockServer server;

    @BeforeAll
    public static void getWireMockServer() {
        server = RestProxyTestsWireMockServer.getRestProxyTestsServer();

        if (!server.isRunning()) {
            server.start();
        }
    }

    @AfterAll
    public static void shutdownWireMockServer() {
        if (server.isRunning()) {
            server.shutdown();
        }
    }

    @Override
    protected HttpClient createHttpClient() {
        return new OkHttpAsyncHttpClientBuilder().build();
    }
}
