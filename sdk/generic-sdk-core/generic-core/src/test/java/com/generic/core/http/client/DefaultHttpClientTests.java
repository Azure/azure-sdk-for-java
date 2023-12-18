// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.implementation.test.HttpClientTests;
import com.generic.core.implementation.test.HttpClientTestsServer;
import com.generic.core.implementation.test.LocalTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

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
    protected HttpClient createHttpClient() {
        return new DefaultHttpClientProvider().createInstance();
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
