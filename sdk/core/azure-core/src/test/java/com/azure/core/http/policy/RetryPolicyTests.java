// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.clients.NoOpHttpClient;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.FluxUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link RetryPolicy}.
 */
public class RetryPolicyTests {
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

    private static Stream<Throwable> defaultRetryPolicyRetriesAllExceptionsSupplier() {
        return Stream.of(
            new Throwable(),
            new MalformedURLException(),
            new RuntimeException(),
            new IllegalStateException(),
            new TimeoutException()
        );
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

    private static Stream<Arguments> customRetryPolicyCanDetermineRetryStatusCodesSupplier() {
        RetryStrategy onlyRetries429And503 = createStatusCodeRetryStrategy(429, 503);
        RetryStrategy onlyRetries409And412 = createStatusCodeRetryStrategy(409, 412);

        return Stream.of(
            Arguments.of(onlyRetries429And503, new int[]{429, 503, 404}, 404),
            Arguments.of(onlyRetries429And503, new int[]{429, 404}, 404),
            Arguments.of(onlyRetries429And503, new int[]{503, 404}, 404),
            Arguments.of(onlyRetries429And503, new int[]{429, 503, 503}, 503),
            Arguments.of(onlyRetries429And503, new int[]{429, 503, 429}, 429),

            Arguments.of(onlyRetries409And412, new int[]{409, 412, 404}, 404),
            Arguments.of(onlyRetries409And412, new int[]{409, 404}, 404),
            Arguments.of(onlyRetries409And412, new int[]{412, 404}, 404),
            Arguments.of(onlyRetries409And412, new int[]{409, 412, 409}, 409),
            Arguments.of(onlyRetries409And412, new int[]{409, 412, 412}, 412)
        );
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

    private static Stream<Arguments> customRetryPolicyCanDetermineRetryExceptionsSupplier() {
        RetryStrategy onlyRetriesIOExceptions = createExceptionRetryStrategy(
            Collections.singletonList(IOException.class));
        RetryStrategy onlyRetriesTimeoutAndRuntimeExceptions = createExceptionRetryStrategy(
            Arrays.asList(TimeoutException.class, RuntimeException.class));

        return Stream.of(
            Arguments.of(onlyRetriesIOExceptions, new Throwable[]{new IOException(), new IOException(),
                new RuntimeException()}, RuntimeException.class),
            Arguments.of(onlyRetriesIOExceptions, new Throwable[]{new IOException(), new RuntimeException()},
                RuntimeException.class),
            Arguments.of(onlyRetriesIOExceptions, new Throwable[]{new IOException(), new TimeoutException()},
                TimeoutException.class),
            Arguments.of(onlyRetriesIOExceptions, new Throwable[]{new IOException(), new IOException(),
                new IOException()}, IOException.class),

            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions, new Throwable[]{new TimeoutException(),
                new RuntimeException(), new IOException()}, IOException.class),
            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions, new Throwable[]{new TimeoutException(),
                new IOException()}, IOException.class),
            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions, new Throwable[]{new RuntimeException(),
                new IOException()}, IOException.class),
            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions, new Throwable[]{new TimeoutException(),
                new RuntimeException(), new TimeoutException()}, TimeoutException.class),
            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions, new Throwable[]{new TimeoutException(),
                new RuntimeException(), new RuntimeException()}, RuntimeException.class)
        );
    }

    @Test
    public void retryMax() {
        final int maxRetries = 5;
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                int count = -1;

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Assertions.assertTrue(count++ < maxRetries);
                    return Mono.just(new MockHttpResponse(request, 500));
                }
            })
            .policies(new RetryPolicy(new FixedDelay(maxRetries, Duration.ofMillis(1))))
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")))
            .assertNext(response -> assertEquals(500, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void fixedDelayRetry() {
        final int maxRetries = 5;
        final long delayMillis = 500;
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                int count = -1;
                long previousAttemptMadeAt = -1;

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    if (count > 0) {
                        Assertions.assertTrue(System.currentTimeMillis() >= previousAttemptMadeAt + delayMillis);
                    }
                    Assertions.assertTrue(count++ < maxRetries);
                    previousAttemptMadeAt = System.currentTimeMillis();
                    return Mono.just(new MockHttpResponse(request, 500));
                }
            })
            .policies(new RetryPolicy(new FixedDelay(maxRetries, Duration.ofMillis(delayMillis))))
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")))
            .assertNext(response -> assertEquals(500, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void exponentialDelayRetry() {
        final int maxRetries = 5;
        final long baseDelayMillis = 100;
        final long maxDelayMillis = 1000;
        ExponentialBackoff exponentialBackoff = new ExponentialBackoff(maxRetries, Duration.ofMillis(baseDelayMillis),
            Duration.ofMillis(maxDelayMillis));
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                int count = -1;
                long previousAttemptMadeAt = -1;

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    if (count > 0) {
                        long requestMadeAt = System.currentTimeMillis();
                        long expectedToBeMadeAt =
                            previousAttemptMadeAt + ((1L << (count - 1)) * (long) (baseDelayMillis * 0.95));
                        Assertions.assertTrue(requestMadeAt >= expectedToBeMadeAt);
                    }
                    Assertions.assertTrue(count++ < maxRetries);
                    previousAttemptMadeAt = System.currentTimeMillis();
                    return Mono.just(new MockHttpResponse(request, 503));
                }
            })
            .policies(new RetryPolicy(exponentialBackoff))
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")))
            .assertNext(response -> assertEquals(503, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void retryConsumesBody() {
        final AtomicInteger bodyConsumptionCount = new AtomicInteger();
        Flux<ByteBuffer> errorBody = Flux.generate(sink -> {
            bodyConsumptionCount.incrementAndGet();
            sink.next(ByteBuffer.wrap("Should be consumed" .getBytes(StandardCharsets.UTF_8)));
            sink.complete();
        });

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(new FixedDelay(2, Duration.ofMillis(1))))
            .httpClient(request -> Mono.just(new HttpResponse(request) {
                @Override
                public int getStatusCode() {
                    return 503;
                }

                @Override
                public String getHeaderValue(String name) {
                    return getHeaders().getValue(name);
                }

                @Override
                public HttpHeaders getHeaders() {
                    return new HttpHeaders();
                }

                @Override
                public Flux<ByteBuffer> getBody() {
                    return errorBody;
                }

                @Override
                public Mono<byte[]> getBodyAsByteArray() {
                    return FluxUtil.collectBytesInByteBufferStream(getBody());
                }

                @Override
                public Mono<String> getBodyAsString() {
                    return getBodyAsString(StandardCharsets.UTF_8);
                }

                @Override
                public Mono<String> getBodyAsString(Charset charset) {
                    return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
                }
            }))
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "https://example.com")))
            .expectNextCount(1)
            .verifyComplete();

        assertEquals(2, bodyConsumptionCount.get());
    }

    @ParameterizedTest
    @MethodSource("getWellKnownRetryDelaySupplier")
    public void getWellKnownRetryDelay(HttpHeaders responseHeaders, RetryStrategy retryStrategy, Duration expected) {
        assertEquals(expected, RetryPolicy.getWellKnownRetryDelay(responseHeaders, 1, retryStrategy));
    }

    private static Stream<Arguments> getWellKnownRetryDelaySupplier() {
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
                .format(DateTimeFormatter.RFC_1123_DATE_TIME)), retryStrategy, Duration.ofSeconds(1))
        );
    }

    @Test
    public void retryAfterDateTime() {
        HttpHeaders headers = new HttpHeaders().set("Retry-After",
            new DateTimeRfc1123(OffsetDateTime.now().plusSeconds(30)).toString());
        Duration actual = RetryPolicy.getWellKnownRetryDelay(headers, 1, null);

        // Since DateTime based Retry-After uses OffsetDateTime.now internally make sure this result skew isn't larger
        // than an allowable bound.
        Duration skew = Duration.ofSeconds(30).minus(actual);
        assertTrue(skew.toSeconds() < 2, () -> "Expected retry skew of less than 2 seconds but was "
            + skew.toMillis() + "ms.");
    }

    private static RetryStrategy createStatusCodeRetryStrategy(int... retriableErrorCodes) {
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

    private static RetryStrategy createExceptionRetryStrategy(List<Class<? extends Throwable>> retriableExceptions) {
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
}
