// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.test.RestProxyTestsServer;
import com.azure.core.test.http.LocalTestServer;
import com.azure.core.test.implementation.RestProxyTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;

import java.net.InetSocketAddress;

@Disabled("Should only be run manually when a local proxy server (e.g. Fiddler) is running")
public class RestProxyWithHttpProxyJdkHttpClientTests extends RestProxyTests {
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
        InetSocketAddress address = new InetSocketAddress("localhost", 8888);
        return new JdkHttpClientBuilder().proxy(new ProxyOptions(ProxyOptions.Type.HTTP, address)).build();
    }
}
