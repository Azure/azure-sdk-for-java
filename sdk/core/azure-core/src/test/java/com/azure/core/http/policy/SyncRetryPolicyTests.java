// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.clients.NoOpHttpClient;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.core.http.policy.RetryPolicyTestUtil.sendRequestSync;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

/**
 * Tests sync {@link RetryPolicy}.
 */
public class SyncRetryPolicyTests {
    @ParameterizedTest
    @ValueSource(ints = {408, 429, 500, 502, 503})
    public void defaultRetryPolicyRetriesExpectedErrorCodes(int returnCode) {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(new NoOpHttpClient() {
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

    @Disabled
    @ParameterizedTest
    @ValueSource(ints = {400, 401, 402, 403, 404, 409, 412, 501, 505})
    public void defaultRetryPolicyDoesntRetryOnErrorCodes(int returnCode) {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(new NoOpHttpClient() {
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
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void defaultRetryPolicyRetriesAllExceptions() {
        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .httpClient(new NoOpHttpClient() {
                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
                    int count = attemptCount.getAndIncrement();
                    if (count == 0) {
                        throw new RuntimeException();
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
    @MethodSource("com.azure.core.http.policy.RetryPolicyTestUtil#customRetryPolicyCanDetermineRetryStatusCodesSupplier")
    public void customRetryPolicyCanDetermineRetryStatusCodes(RetryStrategy retryStrategy, int[] statusCodes,
                                                              int expectedStatusCode) {
        AtomicInteger attempt = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(retryStrategy))
            .httpClient(new NoOpHttpClient() {
                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
                    return new MockHttpResponse(request, statusCodes[attempt.getAndIncrement()]);
                }
            })
            .build();

        HttpResponse response = sendRequestSync(pipeline);
        assertEquals(expectedStatusCode, response.getStatusCode());
    }
    @Disabled
    @ParameterizedTest
    @MethodSource("com.azure.core.http.policy.RetryPolicyTestUtil#customRetryPolicyCanDetermineRetryExceptionsSupplier")
    public void customRetryPolicyCanDetermineRetryExceptions(RetryStrategy retryStrategy, Throwable[] exceptions,
                                                             Class<? extends Throwable> expectedException) {
        AtomicInteger attempt = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(retryStrategy))
            .httpClient(new NoOpHttpClient() {
                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
                    try {
                        throw exceptions[attempt.getAndIncrement()];
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            })
            .build();

        assertThrowsExactly(expectedException, () -> sendRequestSync(pipeline));
    }
}
