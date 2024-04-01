// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.implementation.http.client.DefaultHttpClientProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.generic.core.shared.HttpClientTests;
import com.generic.core.shared.HttpClientTestsServer;
import com.generic.core.shared.LocalTestServer;

public class DefaultHttpClientTests extends HttpClientTests {

    private static LocalTestServer server;
    @BeforeAll
    public static void startTestServer() {
        server = HttpClientTestsServer.getHttpClientTestsServer();
        server.start();
    }

    @AfterAll
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }
    }
    @Override
    protected HttpClient getHttpClient() {
        return new DefaultHttpClientProvider().getInstance();
    }

    @Override
    protected String getServerUri(boolean secure) {
        return secure ? server.getHttpsUri() : server.getHttpUri();
    }

    @Override
    protected int getPort() {
        return server.getHttpPort();
    }
}
