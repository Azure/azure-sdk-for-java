// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client.httpurlconnection;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.client.HttpClientTestsServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class HttpUrlConnectionClientHttpClientTests {

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
    protected HttpClient createHttpClient() {
        return new HttpUrlConnectionClientBuilder().build();
    }

    protected int getPort() {
        return server.getHttpPort();
    }

    protected String getServerUri(boolean secure) {
        return secure ? server.getHttpsUri() : server.getHttpUri();
    }
}
