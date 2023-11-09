// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client.httpurlconnection;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.client.HttpClientTests;
import com.generic.core.http.client.HttpClientTestsServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class HttpUrlConnectionClientHttpClientTests extends HttpClientTests {

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
        return new HttpUrlConnectionClientProvider().createInstance();
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
