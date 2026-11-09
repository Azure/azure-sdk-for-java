// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;
import com.azure.core.util.SharedExecutorService;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link RequestRetryPolicy}.
 */
@Isolated
public class RequestRetryPolicyTest {
    static RequestRetryOptions retryTestOptions
        = new RequestRetryOptions(RetryPolicyType.EXPONENTIAL, 4, 2, 100L, 1000L, "SecondaryHost");
    private static final RequestRetryOptions FAST_RETRY_OPTIONS
        = new RequestRetryOptions(RetryPolicyType.FIXED, 4, (Integer) null, 1L, 5L, null);

    @SyncAsyncTest
    public void requestRetryPolicySuccessMaxRetries() {
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy()).httpClient(new NoOpHttpClient() {
            int count = -1;
            HttpResponse response;

            private void beforeSendingRequest(HttpRequest request) {
                if (count++ < 3) {
                    response = new MockHttpResponse(request, 503);
                } else if (count++ == 4) {
                    response = new MockHttpResponse(request, 200);
                } else {
                    // Too many requests have been made.
                    response = new MockHttpResponse(request, 400);
                }
            }

            @Override
            public HttpResponse sendSync(HttpRequest request, Context context) {
                beforeSendingRequest(request);
                return response;
            }

            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                beforeSendingRequest(request);
                return Mono.just(response);
            }
        }).policies(new RequestRetryPolicy(retryTestOptions)).build();

        HttpResponse response
            = SyncAsyncExtension.execute(() -> sendRequestSync(pipeline), () -> sendRequest(pipeline));
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void requestRetryPolicyRetriesTimeoutSync() {
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy()).httpClient(new NoOpHttpClient() {
            int count = -1;
            long previousAttemptMadeAt = -1;
            HttpResponse response;

            private void beforeSendingRequest(HttpRequest request) {
                if (count > 0) {
                    Assertions.assertTrue(System.currentTimeMillis()
                        >= previousAttemptMadeAt + retryTestOptions.getTryTimeoutDuration().toMillis());
                }
                Assertions.assertTrue(count++ < retryTestOptions.getMaxTries());
                previousAttemptMadeAt = System.currentTimeMillis();
                if (count < 3) {
                    response = new MockHttpResponse(request, 503);
                } else if (count == 3) {
                    response = new MockHttpResponse(request, 200);
                } else {
                    // Too many requests have been made.
                    response = new MockHttpResponse(request, 400);
                }
            }

            @Override
            public HttpResponse sendSync(HttpRequest request, Context context) {
                return send(request).block();
            }

            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                beforeSendingRequest(request);
                return count < 3 ? Mono.just(response).delaySubscription(Duration.ofSeconds(5)) : Mono.just(response);
            }
        }).policies(new RequestRetryPolicy(retryTestOptions)).build();

        HttpResponse response = sendRequestSync(pipeline);

        assertEquals(200, response.getStatusCode());
    }

    @SyncAsyncTest
    public void retryConsumesBody() {
        AtomicInteger closeCalls = new AtomicInteger();
        HttpResponse closeTrackingHttpResponse = new MockHttpResponse(null, 503, new HttpHeaders()) {
            @Override
            public void close() {
                closeCalls.incrementAndGet();
                super.close();
            }
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RequestRetryPolicy(retryTestOptions))
            .httpClient(new NoOpHttpClient() {

                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
                    return closeTrackingHttpResponse;
                }

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    return Mono.just(closeTrackingHttpResponse);
                }
            })
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline), () -> sendRequest(pipeline));

        assertEquals(3, closeCalls.get());
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2 })
    public void tryTimeoutStopsBlockingSyncRequest(int maxTries) {
        AtomicInteger attempts = new AtomicInteger();
        RequestRetryOptions options = new RequestRetryOptions(RetryPolicyType.FIXED, maxTries, 1, 1L, 1L, null);
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RequestRetryPolicy(options)).httpClient(new NoOpHttpClient() {
                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
                    attempts.incrementAndGet();
                    return Mono.<HttpResponse>never().block(Duration.ofSeconds(10));
                }
            }).build();

        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            RuntimeException error = Assertions.assertThrows(RuntimeException.class, () -> sendRequestSync(pipeline));
            Assertions.assertInstanceOf(TimeoutException.class, Exceptions.unwrap(error));
            assertEquals(maxTries, attempts.get());
        });
    }

    @Test
    public void tryTimeoutRetriesBlockingSyncRequestSuccessfully() {
        AtomicInteger attempts = new AtomicInteger();
        RequestRetryOptions options = new RequestRetryOptions(RetryPolicyType.FIXED, 2, 1, 1L, 1L, null);
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RequestRetryPolicy(options)).httpClient(new NoOpHttpClient() {
                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
                    if (attempts.incrementAndGet() == 1) {
                        return Mono.<HttpResponse>never().block(Duration.ofSeconds(10));
                    }
                    return new MockHttpResponse(request, 200);
                }
            }).build();

        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            assertEquals(200, sendRequestSync(pipeline).getStatusCode());
            assertEquals(2, attempts.get());
        });
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2 })
    public void tryTimeoutStopsAsyncRequest(int maxTries) {
        AtomicInteger attempts = new AtomicInteger();
        RequestRetryOptions options = new RequestRetryOptions(RetryPolicyType.FIXED, maxTries, 1, 1L, 1L, null);
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RequestRetryPolicy(options)).httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    attempts.incrementAndGet();
                    return Mono.never();
                }
            }).build();

        StepVerifier.create(sendRequest(pipeline)).expectError(TimeoutException.class).verify(Duration.ofSeconds(5));
        assertEquals(maxTries, attempts.get());
    }

    @Test
    public void timedSyncRetriesKeepFileBackedBody(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("request.bin");
        byte[] payload = "request payload".getBytes(StandardCharsets.UTF_8);
        Files.write(file, payload);
        BinaryData body = BinaryData.fromFile(file);
        AtomicInteger attempts = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RequestRetryPolicy(new RequestRetryOptions(RetryPolicyType.FIXED, 2, 2, 1L, 1L, null)))
            .httpClient(new NoOpHttpClient() {
                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
                    Assertions.assertSame(body, request.getBodyAsBinaryData());
                    return new MockHttpResponse(request, attempts.incrementAndGet() == 1 ? 503 : 200);
                }
            })
            .build();
        try (HttpResponse response
            = pipeline.sendSync(new HttpRequest(HttpMethod.PUT, "http://localhost/").setBody(body), Context.NONE)) {
            assertEquals(200, response.getStatusCode());
            assertEquals(2, attempts.get());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void timedSyncAttemptDoesNotOccupySharedExecutor(boolean operationTimeout) {
        SharedExecutorService shared = SharedExecutorService.getInstance();
        ScheduledExecutorService previous = shared.getExecutorService();
        ScheduledExecutorService singleWorker = Executors.newSingleThreadScheduledExecutor();
        shared.setExecutorService(singleWorker);
        try {
            HttpPipeline pipeline = new HttpPipelineBuilder()
                .policies(new RequestRetryPolicy(new RequestRetryOptions(RetryPolicyType.FIXED, 1, 2, 1L, 1L, null)))
                .httpClient(new NoOpHttpClient() {
                    @Override
                    public HttpResponse sendSync(HttpRequest request, Context context) {
                        // Model a transport that dispatches to Core's shared executor, as the JDK client does.
                        if (!operationTimeout) {
                            return StorageImplUtils.sendRequest(() -> new MockHttpResponse(request, 200),
                                Duration.ofSeconds(3), IllegalStateException.class);
                        }
                        return new MockHttpResponse(request, 200);
                    }
                })
                .build();
            try (HttpResponse response = StorageImplUtils.sendRequest(() -> sendRequestSync(pipeline),
                operationTimeout ? Duration.ofSeconds(3) : null, IllegalStateException.class)) {
                assertEquals(200, response.getStatusCode());
            }
        } finally {
            singleWorker.shutdownNow();
            if (previous == null || previous.isShutdown()) {
                shared.reset();
            } else {
                shared.setExecutorService(previous);
            }
        }
    }

    @Test
    public void timedOutSyncAttemptHasIndependentBodyReader() {
        byte[] payload = "request payload".getBytes(StandardCharsets.UTF_8);
        AtomicInteger attempts = new AtomicInteger();
        AtomicReference<byte[]> firstPayload = new AtomicReference<>();
        CountDownLatch releaseFirstReader = new CountDownLatch(1);
        CountDownLatch firstReaderFinished = new CountDownLatch(1);
        RequestRetryOptions options = new RequestRetryOptions(RetryPolicyType.FIXED, 2, 2, 1L, 1L, null);
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RequestRetryPolicy(options)).httpClient(new NoOpHttpClient() {
                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
                    int attempt = attempts.incrementAndGet();
                    try (InputStream reader = request.getBodyAsBinaryData().toStream()) {
                        if (attempt == 1) {
                            byte[] actual = new byte[payload.length];
                            actual[0] = (byte) reader.read();
                            awaitIgnoringInterrupts(releaseFirstReader);
                            int offset = 1;
                            int read;
                            while (offset < actual.length
                                && (read = reader.read(actual, offset, actual.length - offset)) != -1) {
                                offset += read;
                            }
                            firstPayload.set(java.util.Arrays.copyOf(actual, offset));
                            firstReaderFinished.countDown();
                        } else {
                            Assertions.assertArrayEquals(payload, BinaryData.fromStream(reader).toBytes());
                            releaseFirstReader.countDown();
                            awaitIgnoringInterrupts(firstReaderFinished);
                        }
                        return new MockHttpResponse(request, 200);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }).build();
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(payload), (long) payload.length);
        Assertions.assertTrue(body.isReplayable(), "The source must exercise the mark/reset replayable stream path");
        HttpRequest request = new HttpRequest(HttpMethod.PUT, "http://localhost/").setBody(body);
        try {
            Assertions.assertTimeoutPreemptively(Duration.ofSeconds(8), () -> {
                try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
                    assertEquals(200, response.getStatusCode());
                    assertEquals(2, attempts.get());
                    Assertions.assertArrayEquals(payload, firstPayload.get());
                }
            });
        } finally {
            releaseFirstReader.countDown();
        }
    }

    @Test
    public void timedOutSyncAttemptDoesNotModifyRetryAndClosesLateResponse() {
        AtomicInteger attempts = new AtomicInteger();
        AtomicInteger closeCalls = new AtomicInteger();
        CountDownLatch releaseFirstAttempt = new CountDownLatch(1);
        CountDownLatch lateResponseClosed = new CountDownLatch(1);
        HttpPipelinePolicy blockingPolicy = new HttpPipelinePolicy() {
            @Override
            public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                return next.process();
            }

            @Override
            public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
                int attempt = attempts.incrementAndGet();
                if (attempt == 1) {
                    // Model a policy that resumes after cancellation, such as a non-interruptible token-cache lock.
                    awaitIgnoringInterrupts(releaseFirstAttempt);
                    context.getHttpRequest().setHeader(HttpHeaderName.AUTHORIZATION, "first");
                    context.setData("attempt", "first");
                } else {
                    context.getHttpRequest().setHeader(HttpHeaderName.AUTHORIZATION, "second");
                    context.setData("attempt", "second");
                    releaseFirstAttempt.countDown();
                    awaitIgnoringInterrupts(lateResponseClosed);
                    assertEquals("second",
                        context.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
                    assertEquals("second", context.getData("attempt").orElse(null));
                }
                return next.processSync();
            }
        };
        RequestRetryOptions options = new RequestRetryOptions(RetryPolicyType.FIXED, 2, 2, 1L, 1L, null);
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RequestRetryPolicy(options), blockingPolicy)
            .httpClient(new NoOpHttpClient() {
                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
                    if ("first".equals(context.getData("attempt").orElse(null))) {
                        return new MockHttpResponse(request, 200) {
                            @Override
                            public void close() {
                                closeCalls.incrementAndGet();
                                super.close();
                                lateResponseClosed.countDown();
                            }
                        };
                    }
                    return new MockHttpResponse(request, 200);
                }
            })
            .build();

        try {
            Assertions.assertTimeoutPreemptively(Duration.ofSeconds(8), () -> {
                HttpResponse response = sendRequestSync(pipeline);
                assertEquals("second", response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
                assertEquals(2, attempts.get());
                assertEquals(1, closeCalls.get());
                response.close();
            });
        } finally {
            releaseFirstAttempt.countDown();
        }
    }

    private static void awaitIgnoringInterrupts(CountDownLatch latch) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    Assertions.assertTrue(latch.await(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS),
                        "Timed out waiting for the other attempt");
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    public void retryDrainsBodyBeforeClosingAsync() {
        AtomicInteger requestCount = new AtomicInteger();
        AtomicBoolean firstResponseClosed = new AtomicBoolean();

        final HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RequestRetryPolicy(FAST_RETRY_OPTIONS))
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    if (requestCount.incrementAndGet() == 1) {
                        return Mono.just(new MockHttpResponse(request, 500) {
                            @Override
                            public Flux<ByteBuffer> getBody() {
                                return Flux.defer(() -> firstResponseClosed.get()
                                    ? Flux.never()
                                    : Flux.just(ByteBuffer.wrap(new byte[0])));
                            }

                            @Override
                            public void close() {
                                firstResponseClosed.set(true);
                                super.close();
                            }
                        });
                    }

                    return Mono.just(new MockHttpResponse(request, 200));
                }
            })
            .build();

        StepVerifier.create(sendRequest(pipeline))
            .expectNextMatches(response -> response.getStatusCode() == 200)
            .expectComplete()
            .verify(Duration.ofSeconds(1));
        assertEquals(2, requestCount.get());
        Assertions.assertTrue(firstResponseClosed.get(),
            "Retryable response should be closed after draining the body.");
    }

    @Test
    public void retryContinuesWhenDrainingBodyFailsAsync() {
        AtomicInteger requestCount = new AtomicInteger();
        AtomicBoolean firstResponseClosed = new AtomicBoolean();

        final HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RequestRetryPolicy(FAST_RETRY_OPTIONS))
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    if (requestCount.incrementAndGet() == 1) {
                        return Mono.just(new MockHttpResponse(request, 500) {
                            @Override
                            public Flux<ByteBuffer> getBody() {
                                return Flux.error(new IllegalStateException("Body was discarded after close."));
                            }

                            @Override
                            public void close() {
                                firstResponseClosed.set(true);
                                super.close();
                            }
                        });
                    }

                    return Mono.just(new MockHttpResponse(request, 200));
                }
            })
            .build();

        StepVerifier.create(sendRequest(pipeline))
            .expectNextMatches(response -> response.getStatusCode() == 200)
            .expectComplete()
            .verify(Duration.ofSeconds(1));
        assertEquals(2, requestCount.get());
        Assertions.assertTrue(firstResponseClosed.get(), "Retryable response should be closed after a drain error.");
    }

    @Test
    public void retryAfter500StillRetriesWhenDrainingClosedBodyErrors() {
        AtomicInteger requestCount = new AtomicInteger();
        AtomicBoolean firstResponseClosed = new AtomicBoolean();

        final HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RequestRetryPolicy(FAST_RETRY_OPTIONS))
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    if (requestCount.incrementAndGet() == 1) {
                        return Mono.just(new MockHttpResponse(request, 500) {
                            @Override
                            public Flux<ByteBuffer> getBody() {
                                return Flux.defer(() -> firstResponseClosed.get()
                                    ? Flux.error(new IllegalStateException("Body was discarded after close."))
                                    : Flux.just(ByteBuffer.wrap(new byte[0])));
                            }

                            @Override
                            public void close() {
                                firstResponseClosed.set(true);
                                super.close();
                            }
                        });
                    }

                    return Mono.just(new MockHttpResponse(request, 200));
                }
            })
            .build();

        StepVerifier.create(sendRequest(pipeline))
            .expectNextMatches(response -> response.getStatusCode() == 200)
            .expectComplete()
            .verify(Duration.ofSeconds(1));
        assertEquals(2, requestCount.get());
        Assertions.assertTrue(firstResponseClosed.get(),
            "Retryable response should be closed after draining the body.");
    }

    @SyncAsyncTest
    public void fixedDelayRetry() {
        final int maxRetries = 5;
        final long delayMillis = 500;
        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            int count = -1;
            long previousAttemptMadeAt = -1;

            private void beforeSendingRequest() {
                if (count > 0) {
                    Assertions.assertTrue(System.currentTimeMillis() >= previousAttemptMadeAt + delayMillis);
                }
                Assertions.assertTrue(count++ < maxRetries);
                previousAttemptMadeAt = System.currentTimeMillis();
            }

            @Override
            public HttpResponse sendSync(HttpRequest request, Context context) {
                beforeSendingRequest();
                return new MockHttpResponse(request, 500);
            }

            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                beforeSendingRequest();
                return Mono.just(new MockHttpResponse(request, 500));
            }
        })
            .policies(new RequestRetryPolicy(new RequestRetryOptions(RetryPolicyType.FIXED, maxRetries, null,
                Duration.ofMillis(delayMillis), Duration.ofMillis(delayMillis), null)))
            .build();

        HttpResponse response
            = SyncAsyncExtension.execute(() -> sendRequestSync(pipeline), () -> sendRequest(pipeline));
        assertEquals(500, response.getStatusCode());
    }

    @Test
    public void exponentialDelayRetrySync() {
        final int maxRetries = 5;
        final long retryDelayMillis = 100;
        final long maxDelayMillis = 1000;
        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            int count = -1;
            long previousAttemptMadeAt = -1;

            private void beforeSendingRequest() {
                if (count > 0) {
                    long requestMadeAt = System.currentTimeMillis();
                    long expectedToBeMadeAt
                        = previousAttemptMadeAt + (((powOfTwo(count - 1) - 1L)) * (retryDelayMillis));
                    Assertions.assertTrue(requestMadeAt >= expectedToBeMadeAt);
                }
                Assertions.assertTrue(count++ < maxRetries);
                previousAttemptMadeAt = System.currentTimeMillis();
            }

            @Override
            public HttpResponse sendSync(HttpRequest request, Context context) {
                beforeSendingRequest();
                return new MockHttpResponse(request, 503);
            }

            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                beforeSendingRequest();
                return Mono.just(new MockHttpResponse(request, 503));
            }
        })
            .policies(new RequestRetryPolicy(new RequestRetryOptions(RetryPolicyType.EXPONENTIAL, 6, 2,
                retryDelayMillis, maxDelayMillis, "SecondaryHost")))
            .build();

        HttpResponse response = sendRequestSync(pipeline);

        assertEquals(503, response.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("retryPolicyRetriesExceptionsSupplier")
    public void retryPolicyRetriesExceptions(Throwable throwable, boolean shouldBeRetried) {
        assertEquals(shouldBeRetried, RequestRetryPolicy.shouldErrorBeRetried(throwable, 0, 1).canBeRetried);
    }

    @ParameterizedTest
    @MethodSource("retryPolicyRetriesStatusCodeSupplier")
    public void retryPolicyRetriesStatusCode(int statusCode, boolean isPrimary, boolean shouldBeRetried) {
        assertEquals(shouldBeRetried, RequestRetryPolicy.shouldStatusCodeBeRetried(statusCode, isPrimary, null));
    }

    @ParameterizedTest
    @MethodSource("retryPolicyRetriesStatusCodeSupplier")
    public void retryPolicyRetriesResponse(int statusCode, boolean isPrimary, boolean shouldBeRetried) {
        MockHttpResponse response = new MockHttpResponse(null, 404,
            new HttpHeaders().set(HttpHeaderName.fromString("x-ms-copy-source-error-code"), "" + statusCode));

        assertEquals(shouldBeRetried, RequestRetryPolicy.shouldStatusCodeBeRetried(statusCode, isPrimary, response));

    }

    @ParameterizedTest
    @MethodSource("retryPolicyRetriesErrorCodeSupplier")
    public void retryPolicyRetriesHeaderBasedResponse(String errorCodeHeader, String statusCodeHeader,
        boolean isPrimary, boolean shouldBeRetried) {

        HttpHeaders headers = new HttpHeaders();

        if (errorCodeHeader != null) {
            headers.set(HttpHeaderName.fromString("x-ms-copy-source-error-code"), errorCodeHeader);
        }

        if (statusCodeHeader != null) {
            headers.set(HttpHeaderName.fromString("x-ms-copy-source-status-code"), statusCodeHeader);
        }

        MockHttpResponse response = new MockHttpResponse(null, 200, headers);

        assertEquals(shouldBeRetried, RequestRetryPolicy.shouldStatusCodeBeRetried(0, isPrimary, response));
    }

    private static Mono<HttpResponse> sendRequest(HttpPipeline pipeline) {
        return pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"));
    }

    private static HttpResponse sendRequestSync(HttpPipeline pipeline) {
        return pipeline.sendSync(new HttpRequest(HttpMethod.GET, "http://localhost/"), Context.NONE);
    }

    private long powOfTwo(int exponent) {
        long result = 1;
        for (int i = 0; i < exponent; i++) {
            result *= 2L;
        }

        return result;
    }

    static Stream<Arguments> retryPolicyRetriesExceptionsSupplier() {
        return Stream.of(Arguments.of(new IOException(), true), Arguments.of(new TimeoutException(), true),
            Arguments.of(new RuntimeException(), false), Arguments.of(Exceptions.propagate(new IOException()), true),
            Arguments.of(Exceptions.propagate(new TimeoutException()), true),
            Arguments.of(Exceptions.propagate(new RuntimeException()), false),
            Arguments.of(new Exception(new IOException()), true),
            Arguments.of(new Exception(new TimeoutException()), true),
            Arguments.of(new Exception(new RuntimeException()), false),
            Arguments.of(Exceptions.propagate(new Exception(new IOException())), true),
            Arguments.of(Exceptions.propagate(new Exception(new TimeoutException())), true),
            Arguments.of(Exceptions.propagate(new Exception(new RuntimeException())), false));
    }

    static Stream<Arguments> retryPolicyRetriesStatusCodeSupplier() {
        return Stream.of(Arguments.of(429, true, true), Arguments.of(429, false, true), Arguments.of(500, true, true),
            Arguments.of(500, false, true), Arguments.of(503, true, true), Arguments.of(503, false, true),
            Arguments.of(404, true, false), Arguments.of(404, false, true), Arguments.of(400, true, false));
    }

    static Stream<Arguments> retryPolicyRetriesErrorCodeSupplier() {
        return Stream.of(Arguments.of("InternalError", null, true, true),
            Arguments.of("OperationTimedOut", null, true, true), Arguments.of("ServerBusy", null, true, true),
            Arguments.of(null, "404", false, true), Arguments.of(null, "404", true, false),
            Arguments.of("SomeOtherError", null, true, false), Arguments.of(null, null, true, false));
    }
}
