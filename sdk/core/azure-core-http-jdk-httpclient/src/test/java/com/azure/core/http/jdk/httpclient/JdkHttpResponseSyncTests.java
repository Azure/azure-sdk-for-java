// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.jdk.httpclient.implementation.JdkHttpResponseSync;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.net.ssl.SSLSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdkHttpResponseSyncTests {
    @ParameterizedTest
    @CsvSource({ "false, 0", "true, 0", "false, 17", "true, 17", "false, 16385", "true, 16385" })
    public void partialWritesPreserveEntireResponse(boolean streaming, int size) throws IOException {
        byte[] expected = new byte[size];
        for (int i = 0; i < size; i++) {
            expected[i] = (byte) i;
        }
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        WritableByteChannel destination = new WritableByteChannel() {
            @Override
            public int write(ByteBuffer src) {
                int count = Math.min(7, src.remaining());
                for (int i = 0; i < count; i++) {
                    output.write(src.get());
                }
                return count;
            }

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public void close() {
                throw new AssertionError("The caller owns the destination channel");
            }
        };
        try (JdkHttpResponseSync response = streaming
            ? new JdkHttpResponseSync(request, new StreamResponse(expected), Duration.ZERO)
            : new JdkHttpResponseSync(request, 200, new HttpHeaders(), expected)) {
            response.writeBodyTo(destination);
            assertArrayEquals(expected, output.toByteArray());
            assertTrue(destination.isOpen());
        }
    }

    private static final class StreamResponse implements java.net.http.HttpResponse<InputStream> {
        private final InputStream body;

        private StreamResponse(byte[] bytes) {
            body = new ByteArrayInputStream(bytes);
        }

        @Override
        public int statusCode() {
            return 200;
        }

        @Override
        public java.net.http.HttpRequest request() {
            return java.net.http.HttpRequest.newBuilder(uri()).build();
        }

        @Override
        public Optional<java.net.http.HttpResponse<InputStream>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public java.net.http.HttpHeaders headers() {
            return java.net.http.HttpHeaders.of(Collections.emptyMap(), (name, value) -> true);
        }

        @Override
        public InputStream body() {
            return body;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return URI.create("http://localhost");
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }
}
