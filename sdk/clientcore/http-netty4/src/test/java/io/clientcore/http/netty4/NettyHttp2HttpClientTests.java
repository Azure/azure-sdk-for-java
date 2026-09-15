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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class NettyHttp2HttpClientTests extends HttpClientTests {
    private static LocalTestServer server;

    private static final HttpClient HTTP_CLIENT_INSTANCE;

    static {
        HTTP_CLIENT_INSTANCE = new NettyHttpClientBuilder().connectionPoolSize(0)
            .sslContextModifier(
                builder -> builder.trustManager(new InsecureTrustManager()).secureRandom(new SecureRandom()))
            .maximumHttpVersion(HttpProtocolVersion.HTTP_2)
            .build();
    }

    @BeforeAll
    public static void startTestServer() {
        server = HttpClientTestsServer.getHttpClientTestsServer(HttpProtocolVersion.HTTP_2, true);

        server.start();
    }

    @AfterAll
    public static void stopTestServer() {
        if (HTTP_CLIENT_INSTANCE instanceof NettyHttpClient) {
            ((NettyHttpClient) HTTP_CLIENT_INSTANCE).close();
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
    @Deprecated
    protected int getPort() {
        return server.getPort();
    }

    @Override
    protected String getServerUri(boolean secure) {
        return server.getHttpsUri();
    }

    @Override
    protected HttpClient getHttpClient() {
        return HTTP_CLIENT_INSTANCE;
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

    @ParameterizedTest
    @ValueSource(strings = { "gzip", "deflate" })
    public void decompressesResponse(String contentEncoding) throws IOException {
        byte[] expected = "Compressed HTTP/2 response".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (OutputStream compressor = "gzip".equals(contentEncoding)
            ? new GZIPOutputStream(compressed)
            : new DeflaterOutputStream(compressed)) {
            compressor.write(expected);
        }
        byte[] compressedBytes = compressed.toByteArray();
        LocalTestServer compressedServer
            = new LocalTestServer(HttpProtocolVersion.HTTP_2, true, (request, response, requestBody) -> {
                response.setHeader("Content-Encoding", contentEncoding);
                response.setContentLength(compressedBytes.length);
                response.getOutputStream().write(compressedBytes);
            });
        compressedServer.start();
        try (Response<BinaryData> response = getHttpClient()
            .send(new HttpRequest().setMethod(HttpMethod.GET).setUri(compressedServer.getHttpsUri()))) {
            assertEquals(200, response.getStatusCode());
            assertArrayEquals(expected, response.getValue().toBytes());
        } finally {
            compressedServer.stop();
        }
    }
}
