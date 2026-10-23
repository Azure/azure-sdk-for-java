// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.HostAndPort;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that overriding Host changes the HTTP authority without changing the connection destination.
 */
public class VertxHttpClientHostHeaderTests {
    private static final Duration TEST_TIMEOUT = Duration.ofSeconds(30);

    @ParameterizedTest
    @MethodSource("requestModes")
    @Timeout(60)
    public void hostOverridePreservesRouting(HttpVersion version, boolean synchronous) throws Exception {
        Vertx vertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(2));
        try {
            HttpServer server = vertx.createHttpServer().requestHandler(request -> {
                HostAndPort authority = request.version() == HttpVersion.HTTP_2 ? request.authority() : null;
                request.response()
                    .putHeader("x-request-version", request.version().name())
                    .putHeader("x-request-authority", authority == null ? "" : authority.toString())
                    .putHeader("x-request-host", String.join(",", request.headers().getAll("Host")))
                    .putHeader("x-request-host-count", Integer.toString(request.headers().getAll("Host").size()))
                    .putHeader("x-request-uri", request.uri())
                    .end();
            })
                .listen(0, "localhost")
                .toCompletionStage()
                .toCompletableFuture()
                .get(TEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            HttpClient client = new VertxHttpClientBuilder().vertx(vertx)
                .configuration(Configuration.NONE)
                .httpClientOptions(new HttpClientOptions().setProtocolVersion(version).setHttp2ClearTextUpgrade(false))
                .build();
            String serverUri = "http://localhost:" + server.actualPort();
            String requestUri = "/container/blob?layout=true";

            for (String host : new String[] {
                "account.blob.core.windows.net",
                "account.blob.core.windows.net:8443",
                "[2001:db8::1]",
                "[2001:db8::1]:8443",
                null }) {
                HttpRequest request = new HttpRequest(HttpMethod.GET, serverUri + requestUri);
                if (host != null) {
                    request.setHeader(HttpHeaderName.fromString("hOsT"), host);
                }
                String expectedAuthority = host == null ? "localhost:" + server.actualPort() : host;

                try (HttpResponse response = send(client, request, synchronous)) {
                    assertEquals(200, response.getStatusCode());
                    assertEquals(version.name(),
                        response.getHeaderValue(HttpHeaderName.fromString("x-request-version")));
                    assertEquals(requestUri, response.getHeaderValue(HttpHeaderName.fromString("x-request-uri")));
                    if (version == HttpVersion.HTTP_2) {
                        assertEquals(expectedAuthority,
                            response.getHeaderValue(HttpHeaderName.fromString("x-request-authority")));
                        assertEquals("0", response.getHeaderValue(HttpHeaderName.fromString("x-request-host-count")));
                    } else {
                        assertEquals(expectedAuthority,
                            response.getHeaderValue(HttpHeaderName.fromString("x-request-host")));
                        assertEquals("1", response.getHeaderValue(HttpHeaderName.fromString("x-request-host-count")));
                    }
                }
                assertEquals(host, request.getHeaders().getValue(HttpHeaderName.HOST));
            }

            if (version == HttpVersion.HTTP_2) {
                for (List<String> hosts : Arrays.asList(
                    Collections.singletonList("account.blob.core.windows.net:invalid"),
                    Collections.singletonList("account.blob.core.windows.net:65536"),
                    Collections.singletonList("account.blob.core.windows.net/path"),
                    Collections.singletonList("user@account.blob.core.windows.net"), Collections.singletonList(""),
                    Arrays.asList("account.blob.core.windows.net", "other.blob.core.windows.net"))) {
                    HttpRequest invalid = new HttpRequest(HttpMethod.GET, serverUri + requestUri);
                    invalid.getHeaders().set(HttpHeaderName.HOST, hosts);
                    IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                        () -> send(client, invalid, synchronous).close());
                    assertTrue(error.getMessage().contains("Host"), error.getMessage());
                }

                try (HttpResponse response
                    = send(client, new HttpRequest(HttpMethod.GET, serverUri + requestUri), synchronous)) {
                    assertEquals(200, response.getStatusCode());
                    assertEquals("localhost:" + server.actualPort(),
                        response.getHeaderValue(HttpHeaderName.fromString("x-request-authority")));
                }
            }
        } finally {
            vertx.close().toCompletionStage().toCompletableFuture().get(TEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private static Stream<Arguments> requestModes() {
        return Stream.of(Arguments.of(HttpVersion.HTTP_1_1, false), Arguments.of(HttpVersion.HTTP_1_1, true),
            Arguments.of(HttpVersion.HTTP_2, false), Arguments.of(HttpVersion.HTTP_2, true));
    }

    private static HttpResponse send(HttpClient client, HttpRequest request, boolean synchronous) {
        HttpResponse response
            = synchronous ? client.sendSync(request, Context.NONE) : client.send(request).block(TEST_TIMEOUT);
        assertNotNull(response);
        // getBodyAsByteArray() skips zero-length responses, so consume the stream directly before closing it.
        response.getBody().then().block(TEST_TIMEOUT);
        return response;
    }
}
