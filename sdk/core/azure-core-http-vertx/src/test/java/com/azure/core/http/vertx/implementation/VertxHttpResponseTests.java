// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import org.junit.jupiter.api.Test;
import reactor.core.Exceptions;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VertxHttpResponseTests {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    @Test
    public void fetchesOnlyRequestedBuffers() {
        ResponseSource source = new ResponseSource();
        VertxHttpResponse response = source.createResponse();
        StepVerifier.create(response.getBody(), 0)
            .then(() -> assertEquals(0, source.requested.get()))
            .thenRequest(1)
            .then(() -> {
                assertEquals(1, source.requested.get());
                source.emit(new byte[] { 1, 2 });
            })
            .assertNext(buffer -> assertArrayEquals(new byte[] { 1, 2 }, buffer.array()))
            .then(() -> assertEquals(0, source.requested.get()))
            .thenRequest(2)
            .then(() -> {
                assertEquals(2, source.requested.get());
                source.emit(new byte[] { 3 });
                source.emit(new byte[] { 4 });
                source.end.get().handle(null);
            })
            .expectNextCount(2)
            .expectComplete()
            .verify(TIMEOUT);
        response.close();
        assertEquals(0, source.resets.get());
        verify(source.response, never()).resume();
    }

    @Test
    public void cancellationResetsOnlyOnceWithoutReading() {
        ResponseSource source = new ResponseSource();
        VertxHttpResponse response = source.createResponse();
        StepVerifier.create(response.getBody(), 0).thenCancel().verify(TIMEOUT);
        response.close();
        assertEquals(0, source.requested.get());
        assertEquals(1, source.resets.get());
        verify(source.response, never()).resume();
    }

    @Test
    public void failureResetsResponseAndPreservesError() {
        ResponseSource source = new ResponseSource();
        IOException expected = new IOException("Response failed");
        StepVerifier.create(source.createResponse().getBody(), 0)
            .then(() -> source.error.get().handle(expected))
            .expectErrorMatches(error -> error == expected)
            .verify(TIMEOUT);
        assertEquals(1, source.resets.get());
    }

    @Test
    public void unexpectedBufferWithoutDemandFailsInsteadOfBuffering() {
        ResponseSource source = new ResponseSource();
        StepVerifier.create(source.createResponse().getBody(), 0)
            .then(() -> source.data.get().handle(Buffer.buffer(new byte[] { 1 })))
            .expectErrorMatches(Exceptions::isOverflow)
            .verify(TIMEOUT);
        assertEquals(1, source.resets.get());
    }

    @Test
    public void closingUnconsumedResponseAbortsExchange() {
        ResponseSource source = new ResponseSource();
        VertxHttpResponse response = source.createResponse();
        response.close();
        response.close();
        StepVerifier.create(response.getBody()).expectComplete().verify(TIMEOUT);
        assertEquals(0, source.requested.get());
        assertEquals(1, source.resets.get());
    }

    private static final class ResponseSource {
        private final AtomicReference<Handler<Buffer>> data = new AtomicReference<>();
        private final AtomicReference<Handler<Void>> end = new AtomicReference<>();
        private final AtomicReference<Handler<Throwable>> error = new AtomicReference<>();
        private final AtomicLong requested = new AtomicLong();
        private final AtomicInteger resets = new AtomicInteger();
        private final HttpClientResponse response = mock(HttpClientResponse.class, RETURNS_SELF);

        private ResponseSource() {
            HttpClientRequest request = mock(HttpClientRequest.class, invocation -> {
                if ("reset".equals(invocation.getMethod().getName())) {
                    resets.incrementAndGet();
                    return invocation.getMethod().getReturnType() == boolean.class ? true : Future.succeededFuture();
                }
                return null;
            });
            when(response.headers()).thenReturn(MultiMap.caseInsensitiveMultiMap());
            when(response.request()).thenReturn(request);
            doAnswer(invocation -> {
                data.set(invocation.getArgument(0));
                return response;
            }).when(response).handler(any());
            doAnswer(invocation -> {
                end.set(invocation.getArgument(0));
                return response;
            }).when(response).endHandler(any());
            doAnswer(invocation -> {
                error.set(invocation.getArgument(0));
                return response;
            }).when(response).exceptionHandler(any());
            doAnswer(invocation -> {
                requested.addAndGet(invocation.getArgument(0));
                return response;
            }).when(response).fetch(anyLong());
        }

        private VertxHttpResponse createResponse() {
            return new VertxHttpResponse(new HttpRequest(HttpMethod.GET, "http://localhost"), response);
        }

        private void emit(byte[] bytes) {
            long previous = requested.getAndDecrement();
            if (previous <= 0) {
                throw new AssertionError("No transport demand");
            }
            data.get().handle(Buffer.buffer(bytes));
        }
    }
}
