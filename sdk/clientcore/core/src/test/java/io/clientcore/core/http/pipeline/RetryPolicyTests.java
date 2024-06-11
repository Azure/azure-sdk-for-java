// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.NoOpHttpClient;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpRetryOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.util.DateTimeRfc1123;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests {@link HttpRetryPolicy}.
 */
public class RetryPolicyTests {

    @ParameterizedTest
    @ValueSource(ints = { 408, 500, 502, 503 })
    public void defaultRetryPolicyRetriesExpectedErrorCodes(int returnCode) throws IOException {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new HttpRetryPolicy())
            .httpClient(new NoOpHttpClient() {
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
            })
            .build();

        try (Response<?> response = sendRequest(pipeline)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 401, 402, 403, 404, 409, 412, 501, 505 })
    public void defaultRetryPolicyDoesntRetryOnErrorCodes(int returnCode) throws IOException {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new HttpRetryPolicy())
            .httpClient(new NoOpHttpClient() {

                @Override
                public Response<?> send(HttpRequest request) {
                    int count = attemptCount.getAndIncrement();

                    if (count == 0) {
                        return new MockHttpResponse(request, returnCode);
                    } else {
                        return new MockHttpResponse(request, 200);
                    }
                }
            })
            .build();

        try (Response<?> response = sendRequest(pipeline)) {
            assertEquals(returnCode, response.getStatusCode());
        }
    }

    @Test
    public void defaultRetryPolicyRetriesIOException() throws IOException {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new HttpRetryPolicy())
            .httpClient(new NoOpHttpClient() {
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
            })
            .build();

        try (Response<?> response = sendRequest(pipeline)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @ParameterizedTest
    @MethodSource("customRetryPolicyCanDetermineRetryStatusCodesSupplier")
    public void customRetryPolicyCanDetermineRetryStatusCodes(HttpRetryOptions retryOptions, int[] statusCodes,
        int expectedStatusCode) throws IOException {
        AtomicInteger attempt = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new HttpRetryPolicy(retryOptions))
            .httpClient(new NoOpHttpClient() {

                @Override
                public Response<?> send(HttpRequest request) {
                    return new MockHttpResponse(request, statusCodes[attempt.getAndIncrement()]);
                }
            })
            .build();

        try (Response<?> response = sendRequest(pipeline)) {
            assertEquals(expectedStatusCode, response.getStatusCode());
        }
    }

    @Test
    public void retryMax() throws IOException {
        final int maxRetries = 5;
        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            int count = -1;

            @Override
            public Response<?> send(HttpRequest request) {
                Assertions.assertTrue(count++ < maxRetries);
                return new MockHttpResponse(request, 500);
            }
        }).policies(new HttpRetryPolicy(new HttpRetryOptions(5, Duration.ofMillis(1)))).build();

        try (Response<?> response = sendRequest(pipeline)) {
            assertEquals(500, response.getStatusCode());
        }
    }

    @Test
    public void fixedDelayRetry() throws IOException {
        final int maxRetries = 5;
        final long delayMillis = 500;
        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            int count = -1;
            long previousAttemptMadeAt = -1;

            private void beforeSendingRequest() {
                if (count > 0) {
                    assertTrue(System.currentTimeMillis() >= previousAttemptMadeAt + delayMillis);
                }

                assertTrue(count++ < maxRetries);

                previousAttemptMadeAt = System.currentTimeMillis();
            }

            @Override
            public Response<?> send(HttpRequest request) {
                beforeSendingRequest();
                return new MockHttpResponse(request, 500);
            }
        }).policies(new HttpRetryPolicy(new HttpRetryOptions(5, Duration.ofMillis(delayMillis)))).build();

        try (Response<?> response = sendRequest(pipeline)) {
            assertEquals(500, response.getStatusCode());
        }
    }

    @Test
    public void exponentialDelayRetry() throws IOException {
        final int maxRetries = 5;
        final long baseDelayMillis = 100;
        final long maxDelayMillis = 1000;
        HttpRetryOptions exponentialBackoff = new HttpRetryOptions(5, Duration.ofMillis(baseDelayMillis),
            Duration.ofMillis(maxDelayMillis));
        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            int count = -1;
            long previousAttemptMadeAt = -1;

            private void beforeSendingRequest() {
                if (count > 0) {
                    long requestMadeAt = System.currentTimeMillis();
                    long expectedToBeMadeAt = previousAttemptMadeAt + ((1L << (count - 1)) * (long) (baseDelayMillis
                        * 0.95));
                    assertTrue(requestMadeAt >= expectedToBeMadeAt);
                }

                assertTrue(count++ < maxRetries);

                previousAttemptMadeAt = System.currentTimeMillis();
            }

            @Override
            public Response<?> send(HttpRequest request) {
                beforeSendingRequest();

                return new MockHttpResponse(request, 503);
            }
        }).policies(new HttpRetryPolicy(exponentialBackoff)).build();

        try (Response<?> response = sendRequest(pipeline)) {
            assertEquals(503, response.getStatusCode());
        }
    }

    @Test
    public void retryConsumesBody() throws IOException {
        AtomicInteger closeCalls = new AtomicInteger();
        Response<?> closeTrackingHttpResponse = new MockHttpResponse(null, 503, new HttpHeaders()) {
            @Override
            public void close() throws IOException {
                closeCalls.incrementAndGet();
                super.close();
            }
        };

        HttpClient httpClient = request -> closeTrackingHttpResponse;

        final HttpPipeline pipeline = new HttpPipelineBuilder().policies(
            new HttpRetryPolicy(new HttpRetryOptions(2, Duration.ofMillis(1)))).httpClient(httpClient).build();

        Response<?> ignored = sendRequest(pipeline);
        assertEquals(2, closeCalls.get());
        ignored.close();
    }

    @Test
    public void propagatingExceptionHasOtherErrorsAsSuppressedExceptions() {
        AtomicInteger count = new AtomicInteger();

        HttpClient httpClient = request -> {
            throw new IOException("Attempt " + count.incrementAndGet());
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().policies(
            new HttpRetryPolicy(new HttpRetryOptions(2, Duration.ofMillis(1)))).httpClient(httpClient).build();

        try {
            sendRequest(pipeline).close();
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
    public void retryWellKnownRetryHeaders(HttpHeaders responseHeaders) throws IOException {
        HttpRetryOptions retryOptions = new HttpRetryOptions(1, Duration.ofMillis(1));

        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new HttpRetryPolicy(retryOptions))
            .httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    return new MockHttpResponse(request, 503, responseHeaders);
                } else {
                    return new MockHttpResponse(request, 200);
                }
            })
            .build();

        try (Response<?> response = pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"))) {
            assertEquals(200, response.getStatusCode());
            assertEquals(2, attemptCount.get());
        }
    }

    @Test
    public void retryOptionsCanConfigureHttpResponseRetryLogic() throws IOException {
        // Fixed delay retry options which only retries on 429 responses
        HttpRetryOptions retryOptions = new HttpRetryOptions(1, Duration.ofMillis(1)).setShouldRetryCondition(
            retryInfo -> retryInfo.getResponse() != null && retryInfo.getResponse().getStatusCode() == 429);

        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new HttpRetryPolicy(retryOptions))
            .httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    return new MockHttpResponse(request, 503);
                } else {
                    return new MockHttpResponse(request, 200);
                }
            })
            .build();

        try (Response<?> response = pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"))) {
            assertEquals(503, response.getStatusCode());
            assertEquals(1, attemptCount.get());
        }
    }

    @Test
    public void retryOptionsCanConfigureThrowableRetryLogic() {
        // Fixed delay retry options which only retries IOException-based exceptions.
        HttpRetryOptions retryOptions = new HttpRetryOptions(1, Duration.ofMillis(1)).setShouldRetryCondition(
            retryInfo -> retryInfo.getException() instanceof TimeoutException);

        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new HttpRetryPolicy(retryOptions))
            .httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    throw new UncheckedIOException(new IOException());
                } else {
                    return new MockHttpResponse(request, 200);
                }
            })
            .build();

        assertThrows(UncheckedIOException.class,
            () -> pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")).close());
    }

    @Test
    public void retryOptionsCanConfigureRetryHeaders() throws IOException {
        HttpRetryOptions retryOptions = new HttpRetryOptions(1, Duration.ofMillis(1)).setDelayFromHeaders(headers -> {
            String retryAfter = headers.getValue(HttpHeaderName.RETRY_AFTER);
            return retryAfter == null ? null : Duration.ofSeconds(10);
        });
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.RETRY_AFTER, "10");

        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new HttpRetryPolicy(retryOptions))
            .httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    return new MockHttpResponse(request, 503, headers);
                } else {
                    return new MockHttpResponse(request, 200);
                }
            })
            .build();

        try (Response<?> response = pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"))) {
            assertEquals(200, response.getStatusCode());
            assertEquals(2, attemptCount.get());
        }
    }

    static Stream<Arguments> customRetryPolicyCanDetermineRetryStatusCodesSupplier() {
        HttpRetryOptions onlyRetries429And503 = createStatusCodeRetryStrategy(429, 503);
        HttpRetryOptions onlyRetries409And412 = createStatusCodeRetryStrategy(409, 412);

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

    static Response<?> sendRequest(HttpPipeline pipeline) {
        return pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"));
    }

    static HttpRetryOptions createStatusCodeRetryStrategy(int... retriableErrorCodes) {
        return new HttpRetryOptions(2, Duration.ofMillis(1)).setShouldRetryCondition(
            requestRetryCondition -> Arrays.stream(retriableErrorCodes)
                .anyMatch(
                    retriableErrorCode -> requestRetryCondition.getResponse().getStatusCode() == retriableErrorCode));
    }

    private static final HttpHeaderName X_MS_RETRY_AFTER_MS = HttpHeaderName.fromString("x-ms-retry-after-ms");
    private static final HttpHeaderName RETRY_AFTER_MS = HttpHeaderName.fromString("retry-after-ms");

    static Stream<Arguments> getWellKnownRetryDelaySupplier() {

        return Stream.of(
            // No well-known headers, fallback to the default.
            Arguments.of(new HttpHeaders()),

            // x-ms-retry-after-ms should be respected as milliseconds.
            Arguments.of(new HttpHeaders().set(X_MS_RETRY_AFTER_MS, "10")),

            // x-ms-retry-after-ms wasn't a valid number, fallback to the default.
            Arguments.of(new HttpHeaders().set(X_MS_RETRY_AFTER_MS, "-10")),
            Arguments.of(new HttpHeaders().set(X_MS_RETRY_AFTER_MS, "ten")),

            // retry-after-ms should be respected as milliseconds.
            Arguments.of(new HttpHeaders().set(RETRY_AFTER_MS, "64")),

            // retry-after-ms wasn't a valid number, fallback to the default.
            Arguments.of(new HttpHeaders().set(RETRY_AFTER_MS, "-10")),
            Arguments.of(new HttpHeaders().set(RETRY_AFTER_MS, "ten")),

            // Retry-After should be respected as seconds.
            Arguments.of(new HttpHeaders().set(HttpHeaderName.RETRY_AFTER, "10")),

            // Retry-After wasn't a valid number, fallback to the default.
            Arguments.of(new HttpHeaders().set(HttpHeaderName.RETRY_AFTER, "-10")),
            Arguments.of(new HttpHeaders().set(HttpHeaderName.RETRY_AFTER, "ten")),

            // Retry-After was before the current time, fallback to the default.
            Arguments.of(new HttpHeaders().set(HttpHeaderName.RETRY_AFTER, OffsetDateTime.now()
                .minusMinutes(1)
                .atZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.RFC_1123_DATE_TIME))),

            Arguments.of(new HttpHeaders().set(HttpHeaderName.RETRY_AFTER,
                new DateTimeRfc1123(OffsetDateTime.now().withNano(0).plusSeconds(30)).toString())));
    }
}
