// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.shared.HttpClientTests;
import io.clientcore.core.shared.HttpClientTestsServer;
import io.clientcore.core.shared.LocalTestServer;
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

    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    @Test
    public void canSendBinaryDataDebugging() throws IOException {
        byte[] expectedResponseBody = new byte[8192 * 16];
        ThreadLocalRandom.current().nextBytes(expectedResponseBody);

        HttpRequest request = new HttpRequest().setMethod(HttpMethod.PUT)
            .setUri(getRequestUri(ECHO_RESPONSE))
            .setBody(BinaryData.fromBytes(expectedResponseBody));

        try (Response<BinaryData> response = getHttpClient().send(request)) {
            assertArrayEquals(expectedResponseBody, response.getValue().toBytes());
        }
    }
}
