// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.shared.HttpClientTests;
import io.clientcore.core.shared.HttpClientTestsServer;
import io.clientcore.core.shared.InsecureTrustManager;
import io.clientcore.core.shared.LocalTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Tests for {@link NettyHttpClient} with a connection pool using HTTP/2.
 */
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class NettyHttpClientWithPooledHttp2ConnectionsTests extends HttpClientTests {
    private static LocalTestServer server;
    private static HttpClient client;

    @BeforeAll
    public static void startTestServer() {
        server = HttpClientTestsServer.getHttpClientTestsServer(HttpProtocolVersion.HTTP_2, true);
        server.start();

        client = new NettyHttpClientBuilder().maximumHttpVersion(HttpProtocolVersion.HTTP_2)
            .sslContextModifier(
                builder -> builder.trustManager(new InsecureTrustManager()).secureRandom(new SecureRandom()))
            .build();
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
    protected boolean isHttp2() {
        return true;
    }

    @Override
    protected boolean isSecure() {
        return true;
    }

    @Override
    protected int getPort() {
        return server.getPort();
    }

    @Override
    protected String getServerUri(boolean secure) {
        return server.getHttpsUri();
    }

    @Override
    protected HttpClient getHttpClient() {
        return client;
    }

    @Test
    public void canSendBinaryDataDebug() {
        byte[] expectedBytes = new byte[1024 * 1024];
        ThreadLocalRandom.current().nextBytes(expectedBytes);
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.PUT)
            .setUri(getRequestUri("echo"))
            .setBody(BinaryData.fromBytes(expectedBytes));

        try (Response<BinaryData> response = getHttpClient().send(request)) {
            assertArrayEquals(expectedBytes, response.getValue().toBytes());
        }
    }
}
