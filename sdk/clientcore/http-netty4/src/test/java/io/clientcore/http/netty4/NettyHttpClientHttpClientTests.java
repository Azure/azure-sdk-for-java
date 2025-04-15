// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.shared.HttpClientTests;
import io.clientcore.core.shared.HttpClientTestsServer;
import io.clientcore.core.shared.LocalTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

/**
 * Reactor Netty {@link HttpClientTests}.
 */
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class NettyHttpClientHttpClientTests extends HttpClientTests {
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
    @Deprecated
    protected int getPort() {
        return server.getHttpPort();
    }

    @Override
    protected String getServerUri(boolean secure) {
        return secure ? server.getHttpsUri() : server.getHttpUri();
    }

    @Override
    protected HttpClient getHttpClient() {
        return new NettyHttpClientBuilder().build();
    }

    @Disabled("Need to implement server sent event support in NettyHttpClient")
    @Test
    public void canReceiveServerSentEvents() {
    }

    @Disabled("Need to implement server sent event support in NettyHttpClient")
    @Test
    public void canRecognizeServerSentEvent() {
    }

    @Disabled("Need to implement server sent event support in NettyHttpClient")
    @Test
    public void onErrorServerSentEvents() {
    }

    @Disabled("Need to implement server sent event support in NettyHttpClient")
    @Test
    public void onRetryWithLastEventIdReceiveServerSentEvents() {
    }

    @Disabled("Need to implement server sent event support in NettyHttpClient")
    @Test
    public void throwsExceptionForNoListener() {
    }
}
