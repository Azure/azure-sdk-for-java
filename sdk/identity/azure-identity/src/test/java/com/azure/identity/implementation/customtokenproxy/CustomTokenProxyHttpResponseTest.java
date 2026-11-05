// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.customtokenproxy;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Execution(ExecutionMode.SAME_THREAD)
@Isolated("Temporarily replaces the shared Reactor scheduler factory")
public class CustomTokenProxyHttpResponseTest {
    @ParameterizedTest
    @ValueSource(strings = { "binary", "stream", "channel" })
    public void synchronousBodyReadCompletesWithFullBoundedElasticPool(String accessor) throws IOException {
        HttpsURLConnection connection = mockConnection(200);
        AtomicReference<Thread> callingThread = new AtomicReference<>();
        when(connection.getInputStream()).thenAnswer(invocation -> {
            assertSame(callingThread.get(), Thread.currentThread(), "Synchronous I/O must stay on the calling thread");
            return new ByteArrayInputStream("response".getBytes(StandardCharsets.UTF_8));
        });
        CustomTokenProxyHttpResponse response = new CustomTokenProxyHttpResponse(
            new HttpRequest(HttpMethod.GET, "https://proxy.example.com/token"), connection);
        Schedulers.Snapshot snapshot = Schedulers.setFactoryWithSnapshot(new Schedulers.Factory() {
            @Override
            public Scheduler newBoundedElastic(int threadCap, int queuedTaskCap, ThreadFactory threadFactory,
                int ttlSeconds) {
                return Schedulers.Factory.super.newBoundedElastic(1, queuedTaskCap, threadFactory, ttlSeconds);
            }
        });
        try {
            String body = Mono.fromCallable(() -> {
                callingThread.set(Thread.currentThread());
                if ("binary".equals(accessor)) {
                    return response.getBodyAsBinaryData().toString();
                }
                if ("stream".equals(accessor)) {
                    try (InputStream stream = response.getBodyAsInputStreamSync()) {
                        byte[] bytes = new byte[8];
                        assertEquals(bytes.length, stream.read(bytes));
                        return new String(bytes, StandardCharsets.UTF_8);
                    }
                }
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                response.writeBodyTo(Channels.newChannel(output));
                return output.toString(StandardCharsets.UTF_8.name());
            }).subscribeOn(Schedulers.boundedElastic()).block(Duration.ofSeconds(5));

            assertEquals("response", body);
            assertEquals("response", response.getBodyAsBinaryData().toString());
            assertEquals("response", response.getBodyAsString().block(Duration.ofSeconds(5)));
            verify(connection, times(1)).getInputStream();
        } finally {
            Schedulers.resetFrom(snapshot);
            response.close();
        }
    }

    @Test
    public void asyncBodyReadOffloadsBlockingIoAndCachesBody() throws IOException {
        HttpsURLConnection connection = mockConnection(200);
        when(connection.getInputStream()).thenAnswer(invocation -> {
            assertFalse(Schedulers.isInNonBlockingThread(), "Response I/O must not run on an event-loop thread");
            return new ByteArrayInputStream("response".getBytes(StandardCharsets.UTF_8));
        });
        CustomTokenProxyHttpResponse response = new CustomTokenProxyHttpResponse(
            new HttpRequest(HttpMethod.GET, "https://proxy.example.com/token"), connection);

        StepVerifier.create(response.getBodyAsString().subscribeOn(Schedulers.parallel()))
            .expectNext("response")
            .expectComplete()
            .verify(Duration.ofSeconds(10));
        StepVerifier.create(response.getBodyAsString()).expectNext("response").verifyComplete();
        verify(connection, times(1)).getInputStream();
        response.close();
        verify(connection).disconnect();
    }

    @Test
    public void ioFailureWithoutErrorBodyIsNotAnEmptySuccess() throws IOException {
        HttpsURLConnection connection = mockConnection(200);
        IOException failure = new IOException("Response read failed");
        when(connection.getInputStream()).thenThrow(failure);
        CustomTokenProxyHttpResponse response = new CustomTokenProxyHttpResponse(
            new HttpRequest(HttpMethod.GET, "https://proxy.example.com/token"), connection);

        StepVerifier.create(response.getBodyAsByteArray()).expectErrorMatches(error -> error == failure).verify();
        assertSame(failure, assertThrows(UncheckedIOException.class, response::getBodyAsBinaryData).getCause());
        assertSame(failure, assertThrows(UncheckedIOException.class, response::getBodyAsInputStreamSync).getCause());
        assertSame(failure, assertThrows(IOException.class,
            () -> response.writeBodyTo(Channels.newChannel(new ByteArrayOutputStream()))));
    }

    @Test
    public void errorResponseBodyIsPreserved() throws IOException {
        HttpsURLConnection connection = mockConnection(400);
        when(connection.getErrorStream())
            .thenReturn(new ByteArrayInputStream("{\"error\":\"invalid_grant\"}".getBytes(StandardCharsets.UTF_8)));
        CustomTokenProxyHttpResponse response = new CustomTokenProxyHttpResponse(
            new HttpRequest(HttpMethod.POST, "https://proxy.example.com/token"), connection);

        StepVerifier.create(response.getBodyAsString()).expectNext("{\"error\":\"invalid_grant\"}").verifyComplete();
        assertEquals("{\"error\":\"invalid_grant\"}", response.getBodyAsBinaryData().toString());
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 401, 403, 500 })
    public void errorStatusWithoutBodyIsPreserved(int status) throws IOException {
        HttpsURLConnection connection = mockConnection(status);
        CustomTokenProxyHttpResponse response = new CustomTokenProxyHttpResponse(
            new HttpRequest(HttpMethod.POST, "https://proxy.example.com/token"), connection);

        assertEquals(status, response.getStatusCode());
        StepVerifier.create(response.getBodyAsString()).expectNext("").verifyComplete();
        assertEquals("", response.getBodyAsBinaryData().toString());
    }

    private HttpsURLConnection mockConnection(int status) throws IOException {
        HttpsURLConnection connection = mock(HttpsURLConnection.class);
        when(connection.getResponseCode()).thenReturn(status);
        when(connection.getHeaderFields()).thenReturn(Collections.emptyMap());
        return connection;
    }
}
