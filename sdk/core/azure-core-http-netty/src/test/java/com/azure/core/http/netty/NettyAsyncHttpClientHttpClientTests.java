// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.validation.http.HttpClientTests;
import com.azure.core.validation.http.HttpClientTestsServer;
import com.azure.core.validation.http.LocalTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Reactor Netty {@link HttpClientTests}.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class NettyAsyncHttpClientHttpClientTests extends HttpClientTests {
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
    protected HttpClient createHttpClient() {
        return new NettyAsyncHttpClientBuilder().build();
    }

    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    @Test
    public void canSendBinaryDataDebugging() throws IOException {
        // Use 4MB to trigger what would fail in the current http-netty design.
        byte[] expectedResponseBody = new byte[4 * 1024 * 1024];
        ThreadLocalRandom.current().nextBytes(expectedResponseBody);

        HttpRequest request = new HttpRequest(HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE))
            .setBody(BinaryData.fromBytes(expectedResponseBody));

        try (HttpResponse response = createHttpClient().sendSync(request, Context.NONE)) {
            assertArrayEquals(expectedResponseBody, response.getBodyAsBinaryData().toBytes());
        }
    }

    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    @Test
    public void canSendTinyBinaryDataDebugging() throws IOException {
        byte[] expectedResponseBody = new byte[512];
        ThreadLocalRandom.current().nextBytes(expectedResponseBody);

        HttpRequest request = new HttpRequest(HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE))
            .setBody(BinaryData.fromBytes(expectedResponseBody));

        try (HttpResponse response = createHttpClient().sendSync(request, Context.NONE)) {
            assertArrayEquals(expectedResponseBody, response.getBodyAsBinaryData().toBytes());
        }
    }
}
