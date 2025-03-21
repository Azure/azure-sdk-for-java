// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpProtocolVersion;
import com.azure.core.validation.http.HttpClientTests;
import com.azure.core.validation.http.HttpClientTestsServer;
import com.azure.core.validation.http.LocalTestServer;
import io.vertx.core.http.HttpClientOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.EnumSet;

public class VertxHttpClientWithHttp2Tests extends HttpClientTests {
    private static LocalTestServer server;

    private static final HttpClient HTTP_CLIENT_INSTANCE;

    static {
        HTTP_CLIENT_INSTANCE = new VertxHttpClientBuilder()
            .httpClientOptions(new HttpClientOptions().setTrustAll(true).setVerifyHost(false))
            .setProtocolVersions(EnumSet.of(HttpProtocolVersion.HTTP_2, HttpProtocolVersion.HTTP_1_1))
            .build();
    }

    @BeforeAll
    public static void startTestServer() {
        server = HttpClientTestsServer.getHttpClientTestsServer(HttpProtocolVersion.HTTP_2, true);
        server.start();
    }

    @AfterAll
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Override
    protected boolean isSecure() {
        return true;
    }

    @Override
    protected boolean isHttp2() {
        return true;
    }

    @Override
    @Deprecated
    protected int getPort() {
        return server.getPort();
    }

    @Override
    protected String getServerUri(boolean secure) {
        return server.getHttpsUri();
    }

    @Override
    protected HttpClient createHttpClient() {
        return HTTP_CLIENT_INSTANCE;
    }
}
