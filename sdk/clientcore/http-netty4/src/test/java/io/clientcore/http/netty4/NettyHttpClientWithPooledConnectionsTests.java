// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.shared.HttpClientTests;
import io.clientcore.core.shared.HttpClientTestsServer;
import io.clientcore.core.shared.LocalTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

/**
 * Tests for {@link NettyHttpClient} with a connection pool over plain HTTP.
 */
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class NettyHttpClientWithPooledConnectionsTests extends HttpClientTests {
    private static LocalTestServer server;
    private static HttpClient client;

    @BeforeAll
    public static void startTestServer() {
        server = HttpClientTestsServer.getHttpClientTestsServer(HttpProtocolVersion.HTTP_1_1, false);
        server.start();

        client = new NettyHttpClientBuilder().build();
    }

    @AfterAll
    public static void stopTestServer() {
        if (client instanceof NettyHttpClient) {
            ((NettyHttpClient) client).close();
        }
        if (server != null) {
            server.stop();
        }
    }

    @Override
    protected int getPort() {
        return server.getPort();
    }

    @Override
    protected String getServerUri(boolean secure) {
        return server.getUri();
    }

    @Override
    protected HttpClient getHttpClient() {
        return client;
    }
}
