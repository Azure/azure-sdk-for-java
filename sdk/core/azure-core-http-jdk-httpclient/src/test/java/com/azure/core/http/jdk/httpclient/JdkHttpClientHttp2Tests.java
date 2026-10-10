// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpProtocolVersion;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.validation.http.HttpProtocolVersionTests;
import com.azure.core.validation.http.LocalTestServer;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests HTTP-version negotiation with the JDK HTTP client.
 */
@DisabledForJreRange(max = JRE.JAVA_11)
public class JdkHttpClientHttp2Tests extends HttpProtocolVersionTests {
    private static final HttpHeaderName PROTOCOL_HEADER = HttpHeaderName.fromString("x-test-http-protocol");

    @Override
    protected HttpClient createHttpClient(HttpProtocolVersion version, boolean applySetting) throws Exception {
        JdkHttpClientBuilder builder = new JdkHttpClientBuilder(java.net.http.HttpClient.newBuilder()
            .sslContext(getSslContext())
            .version(java.net.http.HttpClient.Version.HTTP_1_1)).configuration(Configuration.NONE);
        if (applySetting) {
            builder.maximumHttpVersion(version);
        }
        return builder.build();
    }

    @ParameterizedTest
    @MethodSource("internalVersionArguments")
    public void explicitMaximumOverridesInternalVersionWithoutChangingIt(HttpProtocolVersion internalVersion, boolean async)
        throws Exception {
        LocalTestServer server = new LocalTestServer((request, response, body) -> {
            response.setHeader(PROTOCOL_HEADER.getCaseSensitiveName(), request.getProtocol());
            response.getOutputStream().write(1);
        }, 32, HttpProtocolVersion.HTTP_2);
        HttpClient original = null;
        HttpClient overridden = null;
        HttpClient cleared = null;
        try {
            server.start();
            java.net.http.HttpClient.Version version = internalVersion == HttpProtocolVersion.HTTP_2
                ? java.net.http.HttpClient.Version.HTTP_2
                : java.net.http.HttpClient.Version.HTTP_1_1;
            JdkHttpClientBuilder builder = new JdkHttpClientBuilder(
                java.net.http.HttpClient.newBuilder().sslContext(getSslContext()).version(version))
                    .configuration(Configuration.NONE);
            original = builder.build();
            overridden = builder.maximumHttpVersion(
                internalVersion == HttpProtocolVersion.HTTP_2 ? HttpProtocolVersion.HTTP_1_1 : HttpProtocolVersion.HTTP_2)
                .build();
            cleared = builder.maximumHttpVersion(null).build();

            String internalProtocol = internalVersion == HttpProtocolVersion.HTTP_2 ? "HTTP/2.0" : "HTTP/1.1";
            String overriddenProtocol = internalVersion == HttpProtocolVersion.HTTP_2 ? "HTTP/1.1" : "HTTP/2.0";
            assertNegotiatedProtocol(original, server.getHttpsUri(), internalProtocol, async);
            assertNegotiatedProtocol(overridden, server.getHttpsUri(), overriddenProtocol, async);
            assertNegotiatedProtocol(cleared, server.getHttpsUri(), internalProtocol, async);
            assertNegotiatedProtocol(overridden, server.getHttpsUri(), overriddenProtocol, async);
            assertEquals(version, getInternalClient(original).version());
            assertEquals(version, getInternalClient(overridden).version());
            assertEquals(version, getInternalClient(cleared).version());
        } finally {
            if (original != null) {
                closeInternalClient(original);
            }
            if (overridden != null) {
                closeInternalClient(overridden);
            }
            if (cleared != null) {
                closeInternalClient(cleared);
            }
            server.stop();
        }
    }

    private static Stream<Arguments> internalVersionArguments() {
        return Stream.of(Arguments.of(HttpProtocolVersion.HTTP_1_1, false),
            Arguments.of(HttpProtocolVersion.HTTP_1_1, true), Arguments.of(HttpProtocolVersion.HTTP_2, false),
            Arguments.of(HttpProtocolVersion.HTTP_2, true));
    }

    private static void assertNegotiatedProtocol(HttpClient client, String uri, String expectedProtocol, boolean async)
        throws Exception {
        HttpRequest request = new HttpRequest(HttpMethod.GET, uri);
        try (HttpResponse response
            = async ? client.send(request).block(Duration.ofSeconds(30)) : client.sendSync(request, Context.NONE)) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertEquals(expectedProtocol, response.getHeaderValue(PROTOCOL_HEADER));
            byte[] body = response.getBodyAsByteArray().block(Duration.ofSeconds(30));
            assertNotNull(body);
            assertEquals(1, body.length);
        }
    }

    @Override
    protected HttpProtocolVersion getDefaultMaximumHttpVersion() {
        return HttpProtocolVersion.HTTP_1_1;
    }

    @Override
    protected void closeHttpClient(HttpClient client) throws Exception {
        closeInternalClient(client);
    }

    static void closeInternalClient(HttpClient client) throws Exception {
        java.net.http.HttpClient internalClient = getInternalClient(client);
        if (internalClient instanceof AutoCloseable) {
            ((AutoCloseable) internalClient).close();
        }
    }

    static java.net.http.HttpClient getInternalClient(HttpClient client) throws ReflectiveOperationException {
        Field field = JdkHttpClient.class.getDeclaredField("jdkHttpClient");
        field.setAccessible(true);
        return (java.net.http.HttpClient) field.get(client);
    }
}
