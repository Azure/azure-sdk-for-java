// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy;

import com.generic.core.http.MockHttpResponse;
import com.generic.core.http.NoOpHttpClient;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.models.RetryOptions;
import com.generic.core.http.pipeline.HttpPipeline;
import com.generic.core.http.pipeline.HttpPipelineBuilder;
import com.generic.core.implementation.http.policy.ExponentialBackoffDelay;
import com.generic.core.implementation.http.policy.FixedDelay;
import com.generic.core.implementation.util.DateTimeRfc1123;
import com.generic.core.models.HeaderName;
import com.generic.core.models.Headers;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests {@link RetryPolicy}.
 */
public class RetryPolicyTests {

    @ParameterizedTest
    @ValueSource(ints = {408, 500, 502, 503})
    public void defaultRetryPolicyRetriesExpectedErrorCodes(int returnCode) {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(new NoOpHttpClient() {
                @Override
                public HttpResponse send(HttpRequest request) {
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

        try (HttpResponse response = sendRequest(pipeline)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 402, 403, 404, 409, 412, 501, 505})
    public void defaultRetryPolicyDoesntRetryOnErrorCodes(int returnCode) {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(new NoOpHttpClient() {

                @Override
                public HttpResponse send(HttpRequest request) {
                    int count = attemptCount.getAndIncrement();

                    if (count == 0) {
                        return new MockHttpResponse(request, returnCode);
                    } else {
                        return new MockHttpResponse(request, 200);
                    }
                }
            })
            .build();

        try (HttpResponse response = sendRequest(pipeline)) {
            assertEquals(returnCode, response.getStatusCode());
        }
    }

    @Test
    public void defaultRetryPolicyRetriesIOException() {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(new NoOpHttpClient() {
                @Override
                public HttpResponse send(HttpRequest request) {
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

        try (HttpResponse response = sendRequest(pipeline)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @ParameterizedTest
    @MethodSource("customRetryPolicyCanDetermineRetryStatusCodesSupplier")
    public void customRetryPolicyCanDetermineRetryStatusCodes(RetryPolicy.RetryStrategy retryStrategy, int[] statusCodes,
                                                              int expectedStatusCode) {
        AtomicInteger attempt = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(retryStrategy, 2, null))
            .httpClient(new NoOpHttpClient() {

                @Override
                public HttpResponse send(HttpRequest request) {
                    return new MockHttpResponse(request, statusCodes[attempt.getAndIncrement()]);
                }
            })
            .build();

        try (HttpResponse response = sendRequest(pipeline)) {
            assertEquals(expectedStatusCode, response.getStatusCode());
        }
    }

    @Test
    public void retryMax() {
        final int maxRetries = 5;
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                int count = -1;

                @Override
                public HttpResponse send(HttpRequest request) {
                    Assertions.assertTrue(count++ < maxRetries);
                    return new MockHttpResponse(request, 500);
                }
            })
            .policies(new RetryPolicy(new FixedDelay(Duration.ofMillis(1)), 5, null))
            .build();

        try (HttpResponse response = sendRequest(pipeline)) {
            assertEquals(500, response.getStatusCode());
        }
    }

    @Test
    public void fixedDelayRetry() {
        final int maxRetries = 5;
        final long delayMillis = 500;
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
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
                public HttpResponse send(HttpRequest request) {
                    beforeSendingRequest();
                    return new MockHttpResponse(request, 500);
                }
            })
            .policies(new RetryPolicy(new FixedDelay(Duration.ofMillis(delayMillis)), 5, null))
            .build();

        try (HttpResponse response = sendRequest(pipeline)) {
            assertEquals(500, response.getStatusCode());
        }
    }

    @Test
    public void exponentialDelayRetry() {
        final int maxRetries = 5;
        final long baseDelayMillis = 100;
        final long maxDelayMillis = 1000;
        ExponentialBackoffDelay exponentialBackoff = new ExponentialBackoffDelay(Duration.ofMillis(baseDelayMillis), Duration.ofMillis(maxDelayMillis));
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                int count = -1;
                long previousAttemptMadeAt = -1;

                private void beforeSendingRequest() {
                    if (count > 0) {
                        long requestMadeAt = System.currentTimeMillis();
                        long expectedToBeMadeAt =
                            previousAttemptMadeAt + ((1L << (count - 1)) * (long) (baseDelayMillis * 0.95));
                        assertTrue(requestMadeAt >= expectedToBeMadeAt);
                    }

                    assertTrue(count++ < maxRetries);

                    previousAttemptMadeAt = System.currentTimeMillis();
                }

                @Override
                public HttpResponse send(HttpRequest request) {
                    beforeSendingRequest();

                    return new MockHttpResponse(request, 503);
                }
            })
            .policies(new RetryPolicy(exponentialBackoff, 5, null))
            .build();

        try (HttpResponse response = sendRequest(pipeline)) {
            assertEquals(503, response.getStatusCode());
        }
    }

    @Test
    public void retryConsumesBody() {
        AtomicInteger closeCalls = new AtomicInteger();
        HttpResponse closeTrackingHttpResponse = new MockHttpResponse(null, 503, new Headers()) {
            @Override
            public void close() {
                closeCalls.incrementAndGet();
                super.close();
            }
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(new RetryOptions(2, Duration.ofMillis(1))))
            .httpClient(new NoOpHttpClient() {

                @Override
                public HttpResponse send(HttpRequest request) {
                    return closeTrackingHttpResponse;
                }
            })
            .build();

        sendRequest(pipeline);
        assertEquals(2, closeCalls.get());
    }

    @Test
    public void propagatingExceptionHasOtherErrorsAsSuppressedExceptions() {
        AtomicInteger count = new AtomicInteger();
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(new RetryOptions(2, Duration.ofMillis(1))))
            .httpClient(new NoOpHttpClient() {
                @Override
                public HttpResponse send(HttpRequest request) {
                    throw new UncheckedIOException(new IOException("Attempt " + count.incrementAndGet()));
                }
            })
            .build();
        try {
            sendRequest(pipeline);
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
    public void getWellKnownRetryDelay(Headers responseHeaders, RetryPolicy.RetryStrategy retryStrategy, Duration expected) {
        assertEquals(expected, RetryPolicy.getWellKnownRetryDelay(responseHeaders, 1, retryStrategy,
            OffsetDateTime::now));
    }

    @Test
    public void retryAfterDateTime() {
        OffsetDateTime now = OffsetDateTime.now().withNano(0);
        Headers headers = new Headers().set(HeaderName.RETRY_AFTER,
            new DateTimeRfc1123(now.plusSeconds(30)).toString());
        Duration actual = RetryPolicy.getWellKnownRetryDelay(headers, 1, null, () -> now);

        assertEquals(Duration.ofSeconds(30), actual);
    }

    @Test
    public void retryOptionsCanConfigureHttpResponseRetryLogic() {
        // Fixed delay retry options which only retries on 429 responses
        RetryOptions retryOptions = new RetryOptions(1, Duration.ofMillis(1))
            .setShouldRetryCondition(retryInfo -> retryInfo.getResponse() != null
                && retryInfo.getResponse().getStatusCode() == 429);

        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(retryOptions))
            .httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    return new MockHttpResponse(request, 503);
                } else {
                    return new MockHttpResponse(request, 200);
                }
            })
            .build();

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"));
        assertEquals(503, response.getStatusCode());

        assertEquals(1, attemptCount.get());
    }

    @Test
    public void retryOptionsCanConfigureThrowableRetryLogic() {
        // Fixed delay retry options which only retries IOException-based exceptions.
        RetryOptions retryOptions = new RetryOptions(1, Duration.ofMillis(1))
            .setShouldRetryCondition(retryInfo -> retryInfo.getThrowable() instanceof TimeoutException);

        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(retryOptions))
            .httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    throw new UncheckedIOException(new IOException());
                } else {
                    return new MockHttpResponse(request, 200);
                }
            })
            .build();

        try {
            pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"));
            assertEquals(1, attemptCount.get());
            fail("Expected exception to be thrown");
        } catch (Exception e) {
            // Expected
            assertTrue(e instanceof UncheckedIOException);
        }
    }

    @Test
    public void retryOptionsCanConfigureRetryHeaders() {
        RetryOptions retryOptions = new RetryOptions(1, Duration.ofMillis(1))
            .setRetryHeaders(headerName -> headerName.equals(HeaderName.RETRY_AFTER) ? Duration.ofMillis(100) : null);
        Headers headers = new Headers().set(HeaderName.RETRY_AFTER, "10");

        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(retryOptions))
            .httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    return new MockHttpResponse(request, 503, headers);
                } else {
                    return new MockHttpResponse(request, 200);
                }
            })
            .build();

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"));
        assertEquals(200, response.getStatusCode());
        assertEquals(2, attemptCount.get());
    }

    static Stream<Arguments> customRetryPolicyCanDetermineRetryStatusCodesSupplier() {
        RetryPolicy.RetryStrategy onlyRetries429And503 = createStatusCodeRetryStrategy(429, 503);
        RetryPolicy.RetryStrategy onlyRetries409And412 = createStatusCodeRetryStrategy(409, 412);

        return Stream.of(
            Arguments.of(onlyRetries429And503, new int[] {429, 503, 404}, 404),
            Arguments.of(onlyRetries429And503, new int[] {429, 404}, 404),
            Arguments.of(onlyRetries429And503, new int[] {503, 404}, 404),
            Arguments.of(onlyRetries429And503, new int[] {429, 503, 503}, 503),
            Arguments.of(onlyRetries429And503, new int[] {429, 503, 429}, 429),

            Arguments.of(onlyRetries409And412, new int[] {409, 412, 404}, 404),
            Arguments.of(onlyRetries409And412, new int[] {409, 404}, 404),
            Arguments.of(onlyRetries409And412, new int[] {412, 404}, 404),
            Arguments.of(onlyRetries409And412, new int[] {409, 412, 409}, 409),
            Arguments.of(onlyRetries409And412, new int[] {409, 412, 412}, 412)
        );
    }

    static HttpResponse sendRequest(HttpPipeline pipeline) {
        return pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"));
    }

    static RetryPolicy.RetryStrategy createStatusCodeRetryStrategy(int... retriableErrorCodes) {
        return new RetryPolicy.RetryStrategy() {

            @Override
            public Duration calculateRetryDelay(int retryAttempts) {
                return Duration.ofMillis(1);
            }

            @Override
            public boolean shouldRetryCondition(RequestRetryCondition requestRetryCondition) {
                return Arrays.stream(retriableErrorCodes)
                    .anyMatch(retriableErrorCode -> requestRetryCondition.getResponse().getStatusCode() == retriableErrorCode);
            }
        };
    }

    private static final HeaderName X_MS_RETRY_AFTER_MS = HeaderName.fromString("x-ms-retry-after-ms");
    private static final HeaderName RETRY_AFTER_MS = HeaderName.fromString("retry-after-ms");

    static Stream<Arguments> getWellKnownRetryDelaySupplier() {
        RetryPolicy.RetryStrategy retryStrategy = retryAttempts -> Duration.ofSeconds(1);

        return Stream.of(
            // No well-known headers, fallback to the default.
            Arguments.of(new Headers(), retryStrategy, Duration.ofSeconds(1)),

            // x-ms-retry-after-ms should be respected as milliseconds.
            Arguments.of(new Headers().set(X_MS_RETRY_AFTER_MS, "10"), retryStrategy, Duration.ofMillis(10)),

            // x-ms-retry-after-ms wasn't a valid number, fallback to the default.
            Arguments.of(new Headers().set(X_MS_RETRY_AFTER_MS, "-10"), retryStrategy, Duration.ofSeconds(1)),
            Arguments.of(new Headers().set(X_MS_RETRY_AFTER_MS, "ten"), retryStrategy, Duration.ofSeconds(1)),

            // retry-after-ms should be respected as milliseconds.
            Arguments.of(new Headers().set(RETRY_AFTER_MS, "64"), retryStrategy, Duration.ofMillis(64)),

            // retry-after-ms wasn't a valid number, fallback to the default.
            Arguments.of(new Headers().set(RETRY_AFTER_MS, "-10"), retryStrategy, Duration.ofSeconds(1)),
            Arguments.of(new Headers().set(RETRY_AFTER_MS, "ten"), retryStrategy, Duration.ofSeconds(1)),

            // Retry-After should be respected as seconds.
            Arguments.of(new Headers().set(HeaderName.RETRY_AFTER, "10"), retryStrategy,
                Duration.ofSeconds(10)),

            // Retry-After wasn't a valid number, fallback to the default.
            Arguments.of(new Headers().set(HeaderName.RETRY_AFTER, "-10"), retryStrategy,
                Duration.ofSeconds(1)),
            Arguments.of(new Headers().set(HeaderName.RETRY_AFTER, "ten"), retryStrategy,
                Duration.ofSeconds(1)),

            // Retry-After was before the current time, fallback to the default.
            Arguments.of(new Headers().set(HeaderName.RETRY_AFTER, OffsetDateTime.now().minusMinutes(1)
                .atZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.RFC_1123_DATE_TIME)), retryStrategy, Duration.ofSeconds(1))
        );
    }
}
