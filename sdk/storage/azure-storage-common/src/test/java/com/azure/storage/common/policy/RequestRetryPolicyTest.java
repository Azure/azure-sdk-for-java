// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link RequestRetryPolicy}.
 */
public class RequestRetryPolicyTest {
    static RequestRetryOptions retryTestOptions = new RequestRetryOptions(RetryPolicyType.EXPONENTIAL, 4, 2,
        100L, 1000L, "SecondaryHost");

    @SyncAsyncTest
    public void requestRetryPolicySuccessMaxRetries() {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(new NoOpHttpClient() {
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
            })
            .policies(new RequestRetryPolicy(retryTestOptions))
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline),
            () -> sendRequest(pipeline)
        );
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void requestRetryPolicyRetriesTimeoutSync() {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(new NoOpHttpClient() {
                int count = -1;
                long previousAttemptMadeAt = -1;
                HttpResponse response;
                private void beforeSendingRequest(HttpRequest request) {
                    if (count > 0) {
                        Assertions.assertTrue(System.currentTimeMillis() >= previousAttemptMadeAt + retryTestOptions.getTryTimeoutDuration().toMillis());
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
                    return count < 3
                        ? Mono.just(response).delaySubscription(Duration.ofSeconds(5)) : Mono.just(response);
                }
            })
            .policies(new RequestRetryPolicy(retryTestOptions))
            .build();

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

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RequestRetryPolicy(retryTestOptions))
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

        SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline),
            () -> sendRequest(pipeline)
        );

        assertEquals(3, closeCalls.get());
    }

    @SyncAsyncTest
    public void fixedDelayRetry() {
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
            .policies(new RequestRetryPolicy(
                new RequestRetryOptions(RetryPolicyType.FIXED, maxRetries, null, Duration.ofMillis(delayMillis),
                    Duration.ofMillis(delayMillis), null)))
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline),
            () -> sendRequest(pipeline)
        );
        assertEquals(500, response.getStatusCode());
    }

    @Test
    public void exponentialDelayRetrySync() {
        final int maxRetries = 5;
        final long retryDelayMillis = 100;
        final long maxDelayMillis = 1000;
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                int count = -1;
                long previousAttemptMadeAt = -1;

                private void beforeSendingRequest() {
                    if (count > 0) {
                        long requestMadeAt = System.currentTimeMillis();
                        long expectedToBeMadeAt =
                            previousAttemptMadeAt + (((powOfTwo(count - 1) - 1L)) * (retryDelayMillis));
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
            .policies(new RequestRetryPolicy(
                new RequestRetryOptions(RetryPolicyType.EXPONENTIAL, 6, 2, retryDelayMillis, maxDelayMillis,
                    "SecondaryHost")))
            .build();

        HttpResponse response = sendRequestSync(pipeline);

        assertEquals(503, response.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("retryPolicyRetriesExceptionsSupplier")
    public void retryPolicyRetriesExceptions(Throwable throwable, boolean shouldBeRetried) {
        assertEquals(shouldBeRetried, RequestRetryPolicy.shouldErrorBeRetried(throwable, 0, 1).canBeRetried);
    }

    /*@ParameterizedTest
    @MethodSource("retryPolicyRetriesStatusCodeSupplier")
    public void retryPolicyRetriesStatusCode(int statusCode, boolean isPrimary, boolean shouldBeRetried) {
        assertEquals(shouldBeRetried, RequestRetryPolicy.shouldResponseBeRetried(statusCode, isPrimary, null));
    }

    @ParameterizedTest
    @MethodSource("retryPolicyRetriesStatusCodeSupplier")
    public void retryPolicyRetriesResponse(int statusCode, boolean isPrimary, boolean shouldBeRetried) {
        MockHttpResponse response = new MockHttpResponse(null, 404,
            new HttpHeaders().set(HttpHeaderName.fromString("x-ms-copy-source-error-code"), "" + statusCode));

        assertEquals(shouldBeRetried, RequestRetryPolicy.shouldResponseBeRetried(0, isPrimary, response));

    }*/

    @ParameterizedTest
    @MethodSource("retryPolicyRetriesStatusCodeSupplier")
    public void retryPolicyRetriesStatusCode(int statusCode, boolean isPrimary, boolean shouldBeRetried) {
        assertEquals(shouldBeRetried, RequestRetryPolicy.shouldStatusCodeBeRetried(statusCode, isPrimary));
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
        return Stream.of(
            Arguments.of(new IOException(), true),
            Arguments.of(new TimeoutException(), true),
            Arguments.of(new RuntimeException(), false),
            Arguments.of(Exceptions.propagate(new IOException()), true),
            Arguments.of(Exceptions.propagate(new TimeoutException()), true),
            Arguments.of(Exceptions.propagate(new RuntimeException()), false),
            Arguments.of(new Exception(new IOException()), true),
            Arguments.of(new Exception(new TimeoutException()), true),
            Arguments.of(new Exception(new RuntimeException()), false),
            Arguments.of(Exceptions.propagate(new Exception(new IOException())), true),
            Arguments.of(Exceptions.propagate(new Exception(new TimeoutException())), true),
            Arguments.of(Exceptions.propagate(new Exception(new RuntimeException())), false)
        );
    }

    static Stream<Arguments> retryPolicyRetriesStatusCodeSupplier() {
        return Stream.of(
            Arguments.of(429, true, true),
            Arguments.of(429, false, true),
            Arguments.of(500, true, true),
            Arguments.of(500, false, true),
            Arguments.of(503, true, true),
            Arguments.of(503, false, true),
            Arguments.of(404, true, false),
            Arguments.of(404, false, true),
            Arguments.of(400, true, false)
            );
    }
}
