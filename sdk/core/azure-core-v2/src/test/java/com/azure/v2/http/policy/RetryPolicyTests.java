// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.SyncAsyncExtension;
import com.azure.core.SyncAsyncTest;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.HttpPipeline;
import io.clientcore.core.http.HttpPipelineBuilder;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.clients.NoOpHttpClient;
import com.azure.core.v2.util.BinaryData;
import io.clientcore.core.util.Context;
import io.clientcore.core.implementation.util.DateTimeRfc1123;
import com.azure.core.v2.util.FluxUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;

import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests {@link HttpRetryPolicy}.
 */
public class RetryPolicyTests {

    @ParameterizedTest
    @ValueSource(ints = { 408, 429, 500, 502, 503 })
    public void defaultRetryPolicyRetriesExpectedErrorCodes(int returnCode) {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy()).httpClient(request -> {
            int count = attemptCount.getAndIncrement();
            if (count == 0) {
                return new MockHttpResponse(request, returnCode));
            } else if (count == 1) {
                return new MockHttpResponse(request, 200));
            } else {
                // Too many requests have been made.
                return new MockHttpResponse(request, 400));
            }
        }).build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(ints = { 408, 429, 500, 502, 503 })
    public void defaultRetryPolicySyncRetriesExpectedErrorCodes(int returnCode) {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy()).httpClient(new NoOpHttpClient() {
            @Override
            public Response<?>> send(HttpRequest request) {
                throw new IllegalStateException("Expected to call 'sendSync' API");
            }

            @Override
            public Response<?> send(HttpRequest request) {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    return new MockHttpResponse(request, returnCode);
                } else if (count == 1) {
                    return new MockHttpResponse(request, 200);
                } else {
                    // Too many requests have been made.
                    return new MockHttpResponse(request, 400);
                }
            }
        }).build();

        try (Response<?> response = sendRequestSync(pipeline)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 401, 402, 403, 404, 409, 412, 501, 505 })
    public void defaultRetryPolicyDoesntRetryOnErrorCodes(int returnCode) {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy()).httpClient(request -> {
            int count = attemptCount.getAndIncrement();
            if (count == 0) {
                return new MockHttpResponse(request, returnCode));
            } else {
                return new MockHttpResponse(request, 200));
            }
        }).build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")))
            .assertNext(response -> assertEquals(returnCode, response.getStatusCode()))
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 401, 402, 403, 404, 409, 412, 501, 505 })
    public void defaultRetryPolicySyncDoesntRetryOnErrorCodes(int returnCode) {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy()).httpClient(new NoOpHttpClient() {

            @Override
            public Response<?>> send(HttpRequest request) {
                throw new IllegalStateException("Expected to call 'sendSync' API");
            }

            @Override
            public Response<?> send(HttpRequest request) {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    return new MockHttpResponse(request, returnCode);
                } else {
                    return new MockHttpResponse(request, 200);
                }
            }
        }).build();

        try (Response<?> response = sendRequestSync(pipeline)) {
            assertEquals(returnCode, response.getStatusCode());
        }
    }

    @ParameterizedTest
    @MethodSource("defaultRetryPolicyRetriesAllExceptionsSupplier")
    public void defaultRetryPolicyRetriesAllExceptions(Throwable throwable) {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy()).httpClient(request -> {
            int count = attemptCount.getAndIncrement();
            if (count == 0) {
                return Mono.error(throwable);
            } else {
                return new MockHttpResponse(request, 200));
            }
        }).build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void defaultRetryPolicySyncRetriesIOException() {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy()).httpClient(new NoOpHttpClient() {

            @Override
            public Response<?>> send(HttpRequest request) {
                throw new IllegalStateException("Expected to call 'sendSync' API");
            }

            @Override
            public Response<?> send(HttpRequest request) {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    try {
                        throw new IOException();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    return new MockHttpResponse(request, 200);
                }
            }
        }).build();

        try (Response<?> response = sendRequestSync(pipeline)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @ParameterizedTest
    @MethodSource("defaultRetryPolicyDoesNotRetryErrorsSupplier")
    public void defaultRetryPolicyDoesNotRetryErrors(Throwable throwable) {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy()).httpClient(request -> {
            int count = attemptCount.getAndIncrement();
            if (count == 0) {
                return Mono.error(throwable);
            } else {
                return new MockHttpResponse(request, 200));
            }
        }).build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")))
            .verifyError(throwable.getClass());
    }

    @ParameterizedTest
    @MethodSource("customRetryPolicyCanDetermineRetryStatusCodesSupplier")
    public void customRetryPolicyCanDetermineRetryStatusCodes(RetryStrategy retryStrategy, int[] statusCodes,
        int expectedStatusCode) {
        AtomicInteger attempt = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy(retryStrategy))
            .httpClient(request -> new MockHttpResponse(request, statusCodes[attempt.getAndIncrement()])))
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("customRetryPolicyCanDetermineRetryStatusCodesSupplier")
    public void customRetryPolicySyncCanDetermineRetryStatusCodes(RetryStrategy retryStrategy, int[] statusCodes,
        int expectedStatusCode) {
        AtomicInteger attempt = new AtomicInteger();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RetryPolicy(retryStrategy)).httpClient(new NoOpHttpClient() {
                @Override
                public Response<?>> send(HttpRequest request) {
                    throw new IllegalStateException("Expected to call 'sendSync' API");
                }

                @Override
                public Response<?> send(HttpRequest request) {
                    return new MockHttpResponse(request, statusCodes[attempt.getAndIncrement()]);
                }
            }).build();

        try (Response<?> response = sendRequestSync(pipeline)) {
            assertEquals(expectedStatusCode, response.getStatusCode());
        }
    }

    @ParameterizedTest
    @MethodSource("customRetryPolicyCanDetermineRetryExceptionsSupplier")
    public void customRetryPolicyCanDetermineRetryExceptions(RetryStrategy retryStrategy, Throwable[] exceptions,
        Class<? extends Throwable> expectedException) {
        AtomicInteger attempt = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy(retryStrategy))
            .httpClient(request -> Mono.error(exceptions[attempt.getAndIncrement()]))
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")))
            .verifyError(expectedException);
    }

    @SyncAsyncTest
    public void retryMax() throws Exception {
        final int maxRetries = 5;
        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            int count = -1;

            @Override
            public Response<?> send(HttpRequest request) {
                Assertions.assertTrue(count++ < maxRetries);
                return new MockHttpResponse(request, 500);
            }

            @Override
            public Response<?>> send(HttpRequest request) {
                Assertions.assertTrue(count++ < maxRetries);
                return new MockHttpResponse(request, 500));
            }
        }).policies(new RetryPolicy(new FixedDelay(maxRetries, Duration.ofMillis(1)))).build();

        try (Response<?> response
            = SyncAsyncExtension.execute(() -> sendRequestSync(pipeline), () -> sendRequest(pipeline))) {
            assertEquals(500, response.getStatusCode());
        }
    }

    @SyncAsyncTest
    public void fixedDelayRetry() throws Exception {
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
            public Response<?> send(HttpRequest request) {
                beforeSendingRequest();
                return new MockHttpResponse(request, 500);
            }

            @Override
            public Response<?>> send(HttpRequest request) {
                beforeSendingRequest();
                return new MockHttpResponse(request, 500));
            }
        }).policies(new RetryPolicy(new FixedDelay(maxRetries, Duration.ofMillis(delayMillis)))).build();

        try (Response<?> response
            = SyncAsyncExtension.execute(() -> sendRequestSync(pipeline), () -> sendRequest(pipeline))) {
            assertEquals(500, response.getStatusCode());
        }
    }

    @SyncAsyncTest
    public void exponentialDelayRetry() throws Exception {
        final int maxRetries = 5;
        final long baseDelayMillis = 100;
        final long maxDelayMillis = 1000;
        ExponentialBackoff exponentialBackoff
            = new ExponentialBackoff(maxRetries, Duration.ofMillis(baseDelayMillis), Duration.ofMillis(maxDelayMillis));
        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            int count = -1;
            long previousAttemptMadeAt = -1;

            private void beforeSendingRequest() {
                if (count > 0) {
                    long requestMadeAt = System.currentTimeMillis();
                    long expectedToBeMadeAt
                        = previousAttemptMadeAt + ((1L << (count - 1)) * (long) (baseDelayMillis * 0.95));
                    Assertions.assertTrue(requestMadeAt >= expectedToBeMadeAt);
                }
                Assertions.assertTrue(count++ < maxRetries);
                previousAttemptMadeAt = System.currentTimeMillis();
            }

            @Override
            public Response<?> send(HttpRequest request) {
                beforeSendingRequest();
                return new MockHttpResponse(request, 503);
            }

            @Override
            public Response<?>> send(HttpRequest request) {
                beforeSendingRequest();
                return new MockHttpResponse(request, 503));
            }
        }).policies(new RetryPolicy(exponentialBackoff)).build();

        try (Response<?> response
            = SyncAsyncExtension.execute(() -> sendRequestSync(pipeline), () -> sendRequest(pipeline))) {
            assertEquals(503, response.getStatusCode());
        }
    }

    @SyncAsyncTest
    public void retryConsumesBody() throws Exception {
        AtomicInteger closeCalls = new AtomicInteger();
        HttpResponse closeTrackingHttpResponse = new MockHttpResponse(null, 503, new HttpHeaders()) {
            @Override
            public void close() {
                closeCalls.incrementAndGet();
                super.close();
            }
        };

        final HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RetryPolicy(new FixedDelay(2, Duration.ofMillis(1))))
                .httpClient(new NoOpHttpClient() {

                    @Override
                    public Response<?> send(HttpRequest request) {
                        return closeTrackingHttpResponse;
                    }

                    @Override
                    public Response<?>> send(HttpRequest request) {
                        return closeTrackingHttpResponse);
                    }
                })
                .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline), () -> sendRequest(pipeline));

        assertEquals(2, closeCalls.get());
    }

    @SyncAsyncTest
    public void propagatingExceptionHasOtherErrorsAsSuppressedExceptions() {
        AtomicInteger count = new AtomicInteger();
        final HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RetryPolicy(new FixedDelay(2, Duration.ofMillis(1))))
                .httpClient(new NoOpHttpClient() {

                    @Override
                    public Response<?> send(HttpRequest request) {
                        throw new UncheckedIOException(new IOException("Attempt " + count.incrementAndGet()));
                    }

                    @Override
                    public Response<?>> send(HttpRequest request) {
                        return Mono
                            .error(new UncheckedIOException(new IOException("Attempt " + count.incrementAndGet())));
                    }
                })
                .build();
        try {
            SyncAsyncExtension.execute(() -> sendRequestSync(pipeline), () -> sendRequest(pipeline));
            fail("Should throw");
        } catch (Exception e) {
            boolean hasAttempt1 = false;
            boolean hasAttempt2 = false;
            for (Throwable suppressed : e.getSuppressed()) {
                if (suppressed.getMessage().contains("Attempt 1")) {
                    hasAttempt1 = true;
                } else if (suppressed.getMessage().contains("Attempt 2")) {
                    hasAttempt2 = true;
                }
            }

            assertTrue(hasAttempt1, "Did not find suppressed with 'Attempt 1' in message.");
            assertTrue(hasAttempt2, "Did not find suppressed with 'Attempt 2' in message.");
        }
    }

    @ParameterizedTest
    @MethodSource("getWellKnownRetryDelaySupplier")
    public void getWellKnownRetryDelay(HttpHeaders responseHeaders, RetryStrategy retryStrategy, Duration expected) {
        assertEquals(expected,
            RetryPolicy.getWellKnownRetryDelay(responseHeaders, 1, retryStrategy, OffsetDateTime::now));
    }

    @Test
    public void retryAfterDateTime() {
        OffsetDateTime now = OffsetDateTime.now().withNano(0);
        HttpHeaders headers
            = new HttpHeaders().set(HttpHeaderName.RETRY_AFTER, new DateTimeRfc1123(now.plusSeconds(30)).toString());
        Duration actual = RetryPolicy.getWellKnownRetryDelay(headers, 1, null, () -> now);

        assertEquals(Duration.ofSeconds(30), actual);
    }

    static Stream<Arguments> customRetryPolicyCanDetermineRetryStatusCodesSupplier() {
        RetryStrategy onlyRetries429And503 = createStatusCodeRetryStrategy(429, 503);
        RetryStrategy onlyRetries409And412 = createStatusCodeRetryStrategy(409, 412);

        return Stream.of(Arguments.of(onlyRetries429And503, new int[] { 429, 503, 404 }, 404),
            Arguments.of(onlyRetries429And503, new int[] { 429, 404 }, 404),
            Arguments.of(onlyRetries429And503, new int[] { 503, 404 }, 404),
            Arguments.of(onlyRetries429And503, new int[] { 429, 503, 503 }, 503),
            Arguments.of(onlyRetries429And503, new int[] { 429, 503, 429 }, 429),

            Arguments.of(onlyRetries409And412, new int[] { 409, 412, 404 }, 404),
            Arguments.of(onlyRetries409And412, new int[] { 409, 404 }, 404),
            Arguments.of(onlyRetries409And412, new int[] { 412, 404 }, 404),
            Arguments.of(onlyRetries409And412, new int[] { 409, 412, 409 }, 409),
            Arguments.of(onlyRetries409And412, new int[] { 409, 412, 412 }, 412));
    }

    static RetryStrategy createExceptionRetryStrategy(List<Class<? extends Throwable>> retriableExceptions) {
        return new RetryStrategy() {
            @Override
            public int getMaxRetries() {
                return 2;
            }

            @Override
            public Duration calculateRetryDelay(int retryAttempts) {
                return Duration.ofMillis(1);
            }

            @Override
            public boolean shouldRetryException(Throwable throwable) {
                return retriableExceptions.stream()
                    .anyMatch(retriableException -> retriableException.isAssignableFrom(throwable.getClass()));
            }
        };
    }

    static HttpResponse sendRequest(HttpPipeline pipeline) {
        return pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")).block();
    }

    static HttpResponse sendRequestSync(HttpPipeline pipeline) {
        return pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"), Context.none());
    }

    static RetryStrategy createStatusCodeRetryStrategy(int... retriableErrorCodes) {
        return new RetryStrategy() {
            @Override
            public int getMaxRetries() {
                return 2;
            }

            @Override
            public Duration calculateRetryDelay(int retryAttempts) {
                return Duration.ofMillis(1);
            }

            @Override
            public boolean shouldRetry(HttpResponse httpResponse) {
                return Arrays.stream(retriableErrorCodes)
                    .anyMatch(retriableErrorCode -> httpResponse.getStatusCode() == retriableErrorCode);
            }
        };
    }

    private static final HttpHeaderName X_MS_RETRY_AFTER_MS = HttpHeaderName.fromString("x-ms-retry-after-ms");
    private static final HttpHeaderName RETRY_AFTER_MS = HttpHeaderName.fromString("retry-after-ms");

    static Stream<Arguments> getWellKnownRetryDelaySupplier() {
        RetryStrategy retryStrategy = new RetryStrategy() {
            @Override
            public int getMaxRetries() {
                return 0;
            }

            @Override
            public Duration calculateRetryDelay(int retryAttempts) {
                return Duration.ofSeconds(1);
            }
        };

        return Stream.of(
            // No well-known headers, fallback to the default.
            Arguments.of(new HttpHeaders(), retryStrategy, Duration.ofSeconds(1)),

            // x-ms-retry-after-ms should be respected as milliseconds.
            Arguments.of(new HttpHeaders().set(X_MS_RETRY_AFTER_MS, "10"), retryStrategy, Duration.ofMillis(10)),

            // x-ms-retry-after-ms wasn't a valid number, fallback to the default.
            Arguments.of(new HttpHeaders().set(X_MS_RETRY_AFTER_MS, "-10"), retryStrategy, Duration.ofSeconds(1)),
            Arguments.of(new HttpHeaders().set(X_MS_RETRY_AFTER_MS, "ten"), retryStrategy, Duration.ofSeconds(1)),

            // retry-after-ms should be respected as milliseconds.
            Arguments.of(new HttpHeaders().set(RETRY_AFTER_MS, "64"), retryStrategy, Duration.ofMillis(64)),

            // retry-after-ms wasn't a valid number, fallback to the default.
            Arguments.of(new HttpHeaders().set(RETRY_AFTER_MS, "-10"), retryStrategy, Duration.ofSeconds(1)),
            Arguments.of(new HttpHeaders().set(RETRY_AFTER_MS, "ten"), retryStrategy, Duration.ofSeconds(1)),

            // Retry-After should be respected as seconds.
            Arguments.of(new HttpHeaders().set(HttpHeaderName.RETRY_AFTER, "10"), retryStrategy,
                Duration.ofSeconds(10)),

            // Retry-After wasn't a valid number, fallback to the default.
            Arguments.of(new HttpHeaders().set(HttpHeaderName.RETRY_AFTER, "-10"), retryStrategy,
                Duration.ofSeconds(1)),
            Arguments.of(new HttpHeaders().set(HttpHeaderName.RETRY_AFTER, "ten"), retryStrategy,
                Duration.ofSeconds(1)),

            // Retry-After was before the current time, fallback to the default.
            Arguments.of(new HttpHeaders().set(HttpHeaderName.RETRY_AFTER,
                OffsetDateTime.now()
                    .minusMinutes(1)
                    .atZoneSameInstant(ZoneOffset.UTC)
                    .format(DateTimeFormatter.RFC_1123_DATE_TIME)),
                retryStrategy, Duration.ofSeconds(1)));
    }

    static Stream<Throwable> defaultRetryPolicyRetriesAllExceptionsSupplier() {
        return Stream.of(new MalformedURLException(), new RuntimeException(), new IllegalStateException(),
            new TimeoutException());
    }

    static Stream<Throwable> defaultRetryPolicyDoesNotRetryErrorsSupplier() {
        // Don't use specific types of Error as it leads to the JVM issues with JUnit, such as ThreadDeath killing the
        // JUnit test runner thread.
        return Stream.of(new Throwable(), new Error());
    }

    static Stream<Arguments> customRetryPolicyCanDetermineRetryExceptionsSupplier() {
        RetryStrategy onlyRetriesIOExceptions
            = createExceptionRetryStrategy(Collections.singletonList(IOException.class));
        RetryStrategy onlyRetriesTimeoutAndRuntimeExceptions
            = createExceptionRetryStrategy(Arrays.asList(TimeoutException.class, RuntimeException.class));

        return Stream.of(Arguments.of(onlyRetriesIOExceptions,
            new Throwable[] { new IOException(), new IOException(), new RuntimeException() }, RuntimeException.class),
            Arguments.of(onlyRetriesIOExceptions, new Throwable[] { new IOException(), new RuntimeException() },
                RuntimeException.class),
            Arguments.of(onlyRetriesIOExceptions, new Throwable[] { new IOException(), new TimeoutException() },
                TimeoutException.class),
            Arguments.of(onlyRetriesIOExceptions,
                new Throwable[] { new IOException(), new IOException(), new IOException() }, IOException.class),

            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions,
                new Throwable[] { new TimeoutException(), new RuntimeException(), new IOException() },
                IOException.class),
            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions,
                new Throwable[] { new TimeoutException(), new IOException() }, IOException.class),
            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions,
                new Throwable[] { new RuntimeException(), new IOException() }, IOException.class),
            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions,
                new Throwable[] { new TimeoutException(), new RuntimeException(), new TimeoutException() },
                TimeoutException.class),
            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions,
                new Throwable[] { new TimeoutException(), new RuntimeException(), new RuntimeException() },
                RuntimeException.class));
    }

    @Test
    public void nothingIsClonedIfThereIsNoRetryAsync() {
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(new byte[4096]));
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost/").setBody(body);

        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RetryPolicy(new FixedDelay(0, Duration.ofMillis(1))))
                .httpClient(r -> new MockHttpResponse(r, (r.getBodyAsBinaryData() != body) ? 400 : 200)))
                .build();

        StepVerifier.create(pipeline.send(request))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void nothingIsClonedIfThereIsNoRetrySync() {
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(new byte[4096]));
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost/").setBody(body);

        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RetryPolicy(new FixedDelay(0, Duration.ofMillis(1))))
                .httpClient(r -> new MockHttpResponse(r, (r.getBodyAsBinaryData() != body) ? 400 : 200)))
                .build();

        try (Response<?> response = pipeline.send(request)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void requestIsClonedIfThereIsRetryAsync() {
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(new byte[4096]));
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost/").setBody(body);

        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RetryPolicy(new FixedDelay(1, Duration.ofMillis(1))))
                .httpClient(r -> new MockHttpResponse(r, (r.getBodyAsBinaryData() != body) ? 200 : 400)))
                .build();

        StepVerifier.create(pipeline.send(request))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void requestIsClonedIfThereIsRetrySync() {
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(new byte[4096]));
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost/").setBody(body);

        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RetryPolicy(new FixedDelay(1, Duration.ofMillis(1))))
                .httpClient(r -> new MockHttpResponse(r, (r.getBodyAsBinaryData() != body) ? 200 : 400)))
                .build();

        try (Response<?> response = pipeline.send(request)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void requestBodyIsOnlyClonedOnceAsync() {
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(new byte[4096]));
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost/").setBody(body);
        Map<BinaryData, Boolean> set = new IdentityHashMap<>();

        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
                .httpClient(r -> {
                    set.put(r.getBodyAsBinaryData(), true);
                    return new MockHttpResponse(r, 503));
                })
                .build();

        StepVerifier.create(pipeline.send(request)).assertNext(response -> {
            assertEquals(503, response.getStatusCode());
            // The request body should only be buffered once.
            assertEquals(1, set.size());
        }).verifyComplete();
    }

    @Test
    public void requestBodyIsOnlyClonedOnceSync() {
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(new byte[4096]));
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost/").setBody(body);
        Map<BinaryData, Boolean> set = new IdentityHashMap<>();

        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
                .httpClient(r -> {
                    set.put(r.getBodyAsBinaryData(), true);
                    return new MockHttpResponse(r, 503));
                })
                .build();

        try (Response<?> response = pipeline.send(request)) {
            assertEquals(503, response.getStatusCode());
            // The request body should only be buffered once.
            assertEquals(1, set.size());
        }
    }

    @Test
    public void requestIsDeeplyClonedForRetriesAsync() {
        byte[] data = new byte[8192];
        ThreadLocalRandom.current().nextBytes(data);
        Flux<ByteBuffer> onlyOnce = Flux.just(ByteBuffer.wrap(data)).publish().autoConnect();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost/").setBody(onlyOnce);

        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
                .httpClient(r -> FluxUtil.collectBytesInByteBufferStream(r.getBody(), 8192).map(bytes -> {
                    Assertions.assertArrayEquals(data, bytes);
                    return new MockHttpResponse(r, 503);
                }))
                .build();

        StepVerifier.create(pipeline.send(request))
            .assertNext(response -> assertEquals(503, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void requestIsDeeplyClonedForRetriesSync() {
        byte[] data = new byte[8192];
        ThreadLocalRandom.current().nextBytes(data);
        Flux<ByteBuffer> onlyOnce = Flux.just(ByteBuffer.wrap(data)).publish().autoConnect();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost/").setBody(onlyOnce);

        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
                .httpClient(r -> FluxUtil.collectBytesInByteBufferStream(r.getBody(), 8192).map(bytes -> {
                    Assertions.assertArrayEquals(data, bytes);
                    return new MockHttpResponse(r, 503);
                }))
                .build();

        try (Response<?> response = pipeline.send(request)) {
            assertEquals(503, response.getStatusCode());
        }
    }

    @Test
    public void retryOptionsCanConfigureHttpResponseRetryLogic() {
        // Fixed delay retry options which only retries on 429 responses
        HttpRetryOptions retryOptions
            = new HttpRetryOptions(new FixedDelayOptions(1, Duration.ofMillis(1))).setShouldRetryCondition(
                retryInfo -> retryInfo.getResponse() != null && retryInfo.getResponse().getStatusCode() == 429);

        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RetryPolicy(retryOptions)).httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    return new MockHttpResponse(request, 503));
                } else {
                    return new MockHttpResponse(request, 200));
                }
            }).build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")))
            .assertNext(response -> assertEquals(503, response.getStatusCode()))
            .verifyComplete();

        assertEquals(1, attemptCount.get());
    }

    @Test
    public void retryOptionsCanConfigureThrowableRetryLogic() {
        // Fixed delay retry options which only retries IOException-based exceptions.
        HttpRetryOptions retryOptions = new HttpRetryOptions(new FixedDelayOptions(1, Duration.ofMillis(1)))
            .setShouldRetryCondition(retryInfo -> retryInfo.getThrowable() instanceof IOException);

        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RetryPolicy(retryOptions)).httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    return Mono.error(new TimeoutException());
                } else {
                    return new MockHttpResponse(request, 200));
                }
            }).build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")))
            .verifyError(TimeoutException.class);

        assertEquals(1, attemptCount.get());
    }
}
