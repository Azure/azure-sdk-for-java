// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.SyncAsyncExtension;
import com.azure.core.SyncAsyncTest;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.clients.NoOpHttpClient;
import com.azure.core.util.Context;
import com.azure.core.util.DateTimeRfc1123;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Tests {@link RetryPolicy}.
 */
public class RetryPolicyTests {

    @Mock
    HttpResponse httpCloseableResponse;

    private AutoCloseable mockCloseable;

    @BeforeEach
    public void setup() {
        mockCloseable = MockitoAnnotations.openMocks(this);
    }
    @AfterEach
    public void teardown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
        Mockito.framework().clearInlineMock(this);
        Mockito.reset(httpCloseableResponse);
    }

    @ParameterizedTest
    @ValueSource(ints = {408, 429, 500, 502, 503})
    public void defaultRetryPolicyRetriesExpectedErrorCodes(int returnCode) {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    return Mono.just(new MockHttpResponse(request, returnCode));
                } else if (count == 1) {
                    return Mono.just(new MockHttpResponse(request, 200));
                } else {
                    // Too many requests have been made.
                    return Mono.just(new MockHttpResponse(request, 400));
                }
            })
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(ints = {408, 429, 500, 502, 503})
    public void defaultRetryPolicySyncRetriesExpectedErrorCodes(int returnCode) {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    throw new IllegalStateException("Expected to call 'sendSync' API");
                }

                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
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

        HttpResponse response = sendRequestSync(pipeline);
        assertEquals(200, response.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 402, 403, 404, 409, 412, 501, 505})
    public void defaultRetryPolicyDoesntRetryOnErrorCodes(int returnCode) {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    return Mono.just(new MockHttpResponse(request, returnCode));
                } else {
                    return Mono.just(new MockHttpResponse(request, 200));
                }
            })
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")))
            .assertNext(response -> assertEquals(returnCode, response.getStatusCode()))
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 402, 403, 404, 409, 412, 501, 505})
    public void defaultRetryPolicySyncDoesntRetryOnErrorCodes(int returnCode) {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(new NoOpHttpClient() {

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    throw new IllegalStateException("Expected to call 'sendSync' API");
                }

                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
                    int count = attemptCount.getAndIncrement();
                    if (count == 0) {
                        return new MockHttpResponse(request, returnCode);
                    } else {
                        return new MockHttpResponse(request, 200);
                    }
                }
            })
            .build();

        HttpResponse response = sendRequestSync(pipeline);
        assertEquals(returnCode, response.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("defaultRetryPolicyRetriesAllExceptionsSupplier")
    public void defaultRetryPolicyRetriesAllExceptions(Throwable throwable) {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    return Mono.error(throwable);
                } else {
                    return Mono.just(new MockHttpResponse(request, 200));
                }
            })
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }
    @Test
    public void defaultRetryPolicySyncRetriesIOException() {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(new NoOpHttpClient() {

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    throw new IllegalStateException("Expected to call 'sendSync' API");
                }
                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
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

        HttpResponse response = sendRequestSync(pipeline);
        assertEquals(200, response.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("defaultRetryPolicyDoesNotRetryErrorsSupplier")
    public void defaultRetryPolicyDoesNotRetryErrors(Throwable throwable) {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    return Mono.error(throwable);
                } else {
                    return Mono.just(new MockHttpResponse(request, 200));
                }
            })
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")))
            .verifyError(throwable.getClass());
    }

    @ParameterizedTest
    @MethodSource("customRetryPolicyCanDetermineRetryStatusCodesSupplier")
    public void customRetryPolicyCanDetermineRetryStatusCodes(RetryStrategy retryStrategy, int[] statusCodes,
        int expectedStatusCode) {
        AtomicInteger attempt = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(retryStrategy))
            .httpClient(request -> Mono.just(new MockHttpResponse(request, statusCodes[attempt.getAndIncrement()])))
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
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(retryStrategy))
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    throw new IllegalStateException("Expected to call 'sendSync' API");
                }

                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
                    return new MockHttpResponse(request, statusCodes[attempt.getAndIncrement()]);
                }
            })
            .build();

        HttpResponse response = sendRequestSync(pipeline);
        assertEquals(expectedStatusCode, response.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("customRetryPolicyCanDetermineRetryExceptionsSupplier")
    public void customRetryPolicyCanDetermineRetryExceptions(RetryStrategy retryStrategy, Throwable[] exceptions,
        Class<? extends Throwable> expectedException) {
        AtomicInteger attempt = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(retryStrategy))
            .httpClient(request -> Mono.error(exceptions[attempt.getAndIncrement()]))
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")))
            .verifyError(expectedException);
    }
    @SyncAsyncTest
    public void retryMax() throws Exception {
        final int maxRetries = 5;
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                int count = -1;

                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
                    Assertions.assertTrue(count++ < maxRetries);
                    return new MockHttpResponse(request, 500);
                }

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Assertions.assertTrue(count++ < maxRetries);
                    return Mono.just(new MockHttpResponse(request, 500));
                }
            })
            .policies(new RetryPolicy(new FixedDelay(maxRetries, Duration.ofMillis(1))))
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline),
            () -> sendRequest(pipeline)
        );
        assertEquals(500, response.getStatusCode());
    }

    @SyncAsyncTest
    public void fixedDelayRetry() throws Exception {
        final int maxRetries = 5;
        final long delayMillis = 500;
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
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
            .policies(new RetryPolicy(new FixedDelay(maxRetries, Duration.ofMillis(delayMillis))))
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline),
            () -> sendRequest(pipeline)
        );
        assertEquals(500, response.getStatusCode());
    }

    @SyncAsyncTest
    public void exponentialDelayRetry() throws Exception {
        final int maxRetries = 5;
        final long baseDelayMillis = 100;
        final long maxDelayMillis = 1000;
        ExponentialBackoff exponentialBackoff = new ExponentialBackoff(maxRetries, Duration.ofMillis(baseDelayMillis),
            Duration.ofMillis(maxDelayMillis));
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                int count = -1;
                long previousAttemptMadeAt = -1;

                private void beforeSendingRequest() {
                    if (count > 0) {
                        long requestMadeAt = System.currentTimeMillis();
                        long expectedToBeMadeAt =
                            previousAttemptMadeAt + ((1L << (count - 1)) * (long) (baseDelayMillis * 0.95));
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
            .policies(new RetryPolicy(exponentialBackoff))
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline),
            () -> sendRequest(pipeline)
        );

        assertEquals(503, response.getStatusCode());
    }

    @SyncAsyncTest
    public void retryConsumesBody() throws Exception {

        when(httpCloseableResponse.getStatusCode()).thenReturn(503);
        when(httpCloseableResponse.getHeaders()).thenReturn(new HttpHeaders());

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(new FixedDelay(2, Duration.ofMillis(1))))
            .httpClient(new NoOpHttpClient() {

                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
                    return httpCloseableResponse;
                }

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    return Mono.just(httpCloseableResponse);
                }
            })
            .build();

        SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline),
            () -> sendRequest(pipeline)
        );

        Mockito.verify(httpCloseableResponse, times(2)).close();
    }

    @ParameterizedTest
    @MethodSource("getWellKnownRetryDelaySupplier")
    public void getWellKnownRetryDelay(HttpHeaders responseHeaders, RetryStrategy retryStrategy, Duration expected) {
        assertEquals(expected, RetryPolicy.getWellKnownRetryDelay(responseHeaders, 1, retryStrategy,
            OffsetDateTime::now));
    }

    @Test
    public void retryAfterDateTime() {
        OffsetDateTime now = OffsetDateTime.now().withNano(0);
        HttpHeaders headers = new HttpHeaders().set("Retry-After", new DateTimeRfc1123(now.plusSeconds(30)).toString());
        Duration actual = RetryPolicy.getWellKnownRetryDelay(headers, 1, null, () -> now);

        assertEquals(Duration.ofSeconds(30), actual);
    }

    static Stream<Arguments> customRetryPolicyCanDetermineRetryStatusCodesSupplier() {
        RetryStrategy onlyRetries429And503 = createStatusCodeRetryStrategy(429, 503);
        RetryStrategy onlyRetries409And412 = createStatusCodeRetryStrategy(409, 412);

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
        return pipeline.sendSync(new HttpRequest(HttpMethod.GET, "http://localhost/"), Context.NONE);
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

    static Stream<Arguments> getWellKnownRetryDelaySupplier() {
        RetryStrategy retryStrategy = mock(RetryStrategy.class);
        when(retryStrategy.calculateRetryDelay(anyInt())).thenReturn(Duration.ofSeconds(1));

        return Stream.of(
            // No well-known headers, fallback to the default.
            Arguments.of(new HttpHeaders(), retryStrategy, Duration.ofSeconds(1)),

            // x-ms-retry-after-ms should be respected as milliseconds.
            Arguments.of(new HttpHeaders().set("x-ms-retry-after-ms", "10"), retryStrategy, Duration.ofMillis(10)),

            // x-ms-retry-after-ms wasn't a valid number, fallback to the default.
            Arguments.of(new HttpHeaders().set("x-ms-retry-after-ms", "-10"), retryStrategy, Duration.ofSeconds(1)),
            Arguments.of(new HttpHeaders().set("x-ms-retry-after-ms", "ten"), retryStrategy, Duration.ofSeconds(1)),

            // retry-after-ms should be respected as milliseconds.
            Arguments.of(new HttpHeaders().set("retry-after-ms", "64"), retryStrategy, Duration.ofMillis(64)),

            // retry-after-ms wasn't a valid number, fallback to the default.
            Arguments.of(new HttpHeaders().set("retry-after-ms", "-10"), retryStrategy, Duration.ofSeconds(1)),
            Arguments.of(new HttpHeaders().set("retry-after-ms", "ten"), retryStrategy, Duration.ofSeconds(1)),

            // Retry-After should be respected as seconds.
            Arguments.of(new HttpHeaders().set("Retry-After", "10"), retryStrategy, Duration.ofSeconds(10)),

            // Retry-After wasn't a valid number, fallback to the default.
            Arguments.of(new HttpHeaders().set("Retry-After", "-10"), retryStrategy, Duration.ofSeconds(1)),
            Arguments.of(new HttpHeaders().set("Retry-After", "ten"), retryStrategy, Duration.ofSeconds(1)),

            // Retry-After was before the current time, fallback to the default.
            Arguments.of(new HttpHeaders().set("Retry-After", OffsetDateTime.now().minusMinutes(1)
                .atZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.RFC_1123_DATE_TIME)), retryStrategy, Duration.ofSeconds(1))
        );
    }

    static Stream<Throwable> defaultRetryPolicyRetriesAllExceptionsSupplier() {
        return Stream.of(
            new MalformedURLException(),
            new RuntimeException(),
            new IllegalStateException(),
            new TimeoutException()
        );
    }

    static Stream<Throwable> defaultRetryPolicyDoesNotRetryErrorsSupplier() {
        // Don't use specific types of Error as it leads to the JVM issues with JUnit, such as ThreadDeath killing the
        // JUnit test runner thread.
        return Stream.of(
            new Throwable(),
            new Error()
        );
    }

    static Stream<Arguments> customRetryPolicyCanDetermineRetryExceptionsSupplier() {
        RetryStrategy onlyRetriesIOExceptions = createExceptionRetryStrategy(
            Collections.singletonList(IOException.class));
        RetryStrategy onlyRetriesTimeoutAndRuntimeExceptions = createExceptionRetryStrategy(
            Arrays.asList(TimeoutException.class, RuntimeException.class));

        return Stream.of(
            Arguments.of(onlyRetriesIOExceptions, new Throwable[] {new IOException(), new IOException(),
                new RuntimeException()}, RuntimeException.class),
            Arguments.of(onlyRetriesIOExceptions, new Throwable[] {new IOException(), new RuntimeException()},
                RuntimeException.class),
            Arguments.of(onlyRetriesIOExceptions, new Throwable[] {new IOException(), new TimeoutException()},
                TimeoutException.class),
            Arguments.of(onlyRetriesIOExceptions, new Throwable[] {new IOException(), new IOException(),
                new IOException()}, IOException.class),

            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions, new Throwable[] {new TimeoutException(),
                new RuntimeException(), new IOException()}, IOException.class),
            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions, new Throwable[] {new TimeoutException(),
                new IOException()}, IOException.class),
            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions, new Throwable[] {new RuntimeException(),
                new IOException()}, IOException.class),
            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions, new Throwable[] {new TimeoutException(),
                new RuntimeException(), new TimeoutException()}, TimeoutException.class),
            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions, new Throwable[] {new TimeoutException(),
                new RuntimeException(), new RuntimeException()}, RuntimeException.class)
        );
    }
}
