// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.test.RestProxyTestsWireMockServer;
import com.azure.core.test.implementation.RestProxyTests;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;

import java.net.InetSocketAddress;

@Disabled("Should only be run manually when a local proxy server (e.g. Fiddler) is running")
public class RestProxyWithHttpProxyJdkHttpClientTests extends RestProxyTests {
    private static WireMockServer server;

    @BeforeAll
    public static void getWireMockServer() {
        server = RestProxyTestsWireMockServer.getRestProxyTestsServer();
        server.start();
    }

    @AfterAll
    public static void shutJdkAsyncHttpClientBuilderdownWireMockServer() {
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
        InetSocketAddress address = new InetSocketAddress("localhost", 8888);
        return new JdkAsyncHttpClientBuilder().proxy(new ProxyOptions(ProxyOptions.Type.HTTP, address)).build();
    }
}
