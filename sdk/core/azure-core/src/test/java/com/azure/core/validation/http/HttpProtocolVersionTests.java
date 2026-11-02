// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.validation.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpProtocolVersion;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Shared tests of HTTP-version negotiation, fallback, and request and response bodies.
 */
@Execution(ExecutionMode.SAME_THREAD)
public abstract class HttpProtocolVersionTests {
    static final HttpHeaderName PROTOCOL_HEADER = HttpHeaderName.fromString("x-test-http-protocol");
    private static final byte[] RESPONSE_BODY = "protocol test response".getBytes(StandardCharsets.UTF_8);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static LocalTestServer http1Server;
    private static LocalTestServer http2Server;

    @BeforeAll
    public static void startProtocolServers() {
        http1Server = createServer(HttpProtocolVersion.HTTP_1_1);
        http2Server = createServer(HttpProtocolVersion.HTTP_2);
        http1Server.start();
        http2Server.start();
    }

    @AfterAll
    public static void stopProtocolServers() {
        if (http1Server != null) {
            http1Server.stop();
        }
        if (http2Server != null) {
            http2Server.stop();
        }
    }

    private static LocalTestServer createServer(HttpProtocolVersion version) {
        return new LocalTestServer((request, response, requestBody) -> {
            response.setHeader(PROTOCOL_HEADER.getCaseSensitiveName(), request.getProtocol());
            response.setStatus("/error".equals(request.getServletPath()) ? 503 : 200);
            byte[] body = requestBody.length == 0 ? RESPONSE_BODY : requestBody;
            int split = body.length / 2;
            response.getOutputStream().write(body, 0, split);
            response.flushBuffer();
            response.getOutputStream().write(body, split, body.length - split);
        }, 32, version);
    }

    @ParameterizedTest
    @MethodSource("negotiationArguments")
    public void negotiatesProtocol(HttpProtocolVersion version, boolean applySetting, boolean serverSupportsHttp2,
        boolean async) throws Exception {
        HttpClient client = createHttpClient(version, applySetting);
        LocalTestServer server = serverSupportsHttp2 ? http2Server : http1Server;
        HttpProtocolVersion maximum = version == null ? getDefaultMaximumHttpVersion() : version;
        String expected = serverSupportsHttp2 && maximum == HttpProtocolVersion.HTTP_2 ? "HTTP/2.0" : "HTTP/1.1";
        try (HttpResponse response = send(client, new HttpRequest(HttpMethod.GET, server.getHttpsUri()), async)) {
            assertEquals(200, response.getStatusCode());
            assertEquals(expected, response.getHeaderValue(PROTOCOL_HEADER));
            assertArrayEquals(RESPONSE_BODY, readBody(response));
        } finally {
            closeHttpClient(client);
        }
    }

    protected static Stream<Arguments> negotiationArguments() {
        List<Arguments> arguments = new ArrayList<>();
        for (boolean serverSupportsHttp2 : new boolean[] { false, true }) {
            for (boolean async : new boolean[] { false, true }) {
                arguments.add(Arguments.of(null, false, serverSupportsHttp2, async));
                arguments.add(Arguments.of(null, true, serverSupportsHttp2, async));
                for (HttpProtocolVersion version : HttpProtocolVersion.values()) {
                    arguments.add(Arguments.of(version, true, serverSupportsHttp2, async));
                }
            }
        }
        return arguments.stream();
    }

    @ParameterizedTest
    @MethodSource("versionArguments")
    public void transfersStreamedBodies(HttpProtocolVersion version, boolean async) throws Exception {
        HttpClient client = createHttpClient(version, true);
        int split = RESPONSE_BODY.length / 2;
        HttpRequest request = new HttpRequest(HttpMethod.PUT, http2Server.getHttpsUri())
            .setBody(Flux.just(ByteBuffer.wrap(RESPONSE_BODY, 0, split),
                ByteBuffer.wrap(RESPONSE_BODY, split, RESPONSE_BODY.length - split)));
        try (HttpResponse response = send(client, request, async)) {
            assertEquals(200, response.getStatusCode());
            assertEquals(version == HttpProtocolVersion.HTTP_2 ? "HTTP/2.0" : "HTTP/1.1",
                response.getHeaderValue(PROTOCOL_HEADER));
            assertArrayEquals(RESPONSE_BODY, readBody(response));
        } finally {
            closeHttpClient(client);
        }
    }

    @ParameterizedTest
    @MethodSource("versionArguments")
    public void returnsErrorResponses(HttpProtocolVersion version, boolean async) throws Exception {
        HttpClient client = createHttpClient(version, true);
        HttpRequest request = new HttpRequest(HttpMethod.GET, http2Server.getHttpsUri() + "/error");
        try (HttpResponse response = send(client, request, async)) {
            assertEquals(503, response.getStatusCode());
            assertEquals(version == HttpProtocolVersion.HTTP_2 ? "HTTP/2.0" : "HTTP/1.1",
                response.getHeaderValue(PROTOCOL_HEADER));
            assertArrayEquals(RESPONSE_BODY, readBody(response));
        } finally {
            closeHttpClient(client);
        }
    }

    @ParameterizedTest
    @MethodSource("versionArguments")
    public void supportsPlainHttp(HttpProtocolVersion version, boolean async) throws Exception {
        HttpClient client = createHttpClient(version, true);
        try (HttpResponse response = send(client, new HttpRequest(HttpMethod.GET, http2Server.getHttpUri()), async)) {
            assertEquals(200, response.getStatusCode());
            assertEquals("HTTP/1.1", response.getHeaderValue(PROTOCOL_HEADER));
            assertArrayEquals(RESPONSE_BODY, readBody(response));
        } finally {
            closeHttpClient(client);
        }
    }

    protected static Stream<Arguments> versionArguments() {
        return Stream.of(Arguments.of(HttpProtocolVersion.HTTP_1_1, false),
            Arguments.of(HttpProtocolVersion.HTTP_1_1, true), Arguments.of(HttpProtocolVersion.HTTP_2, false),
            Arguments.of(HttpProtocolVersion.HTTP_2, true));
    }

    private static HttpResponse send(HttpClient client, HttpRequest request, boolean async) {
        HttpResponse response = async ? client.send(request).block(TIMEOUT) : client.sendSync(request, Context.NONE);
        assertNotNull(response);
        return response;
    }

    private static byte[] readBody(HttpResponse response) {
        ByteArrayOutputStream body = response.getBody().collect(ByteArrayOutputStream::new, (output, buffer) -> {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            output.write(bytes, 0, bytes.length);
        }).block(TIMEOUT);
        assertNotNull(body);
        return body.toByteArray();
    }

    protected static TrustManagerFactory getTrustManagerFactory() throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream stream
            = Objects.requireNonNull(HttpProtocolVersionTests.class.getResourceAsStream("/http-protocol-keystore.jks"),
                "Missing protocol test keystore")) {
            keyStore.load(stream, "password".toCharArray());
        }
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(keyStore);
        return factory;
    }

    protected static X509TrustManager getTrustManager() throws GeneralSecurityException, IOException {
        for (javax.net.ssl.TrustManager manager : getTrustManagerFactory().getTrustManagers()) {
            if (manager instanceof X509TrustManager) {
                return (X509TrustManager) manager;
            }
        }
        throw new IllegalStateException("The test keystore did not provide an X509 trust manager.");
    }

    protected static SSLContext getSslContext() throws GeneralSecurityException, IOException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, getTrustManagerFactory().getTrustManagers(), null);
        return context;
    }

    protected abstract HttpClient createHttpClient(HttpProtocolVersion version, boolean applySetting) throws Exception;

    protected abstract HttpProtocolVersion getDefaultMaximumHttpVersion();

    protected abstract void closeHttpClient(HttpClient client) throws Exception;
}
