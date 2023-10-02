// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.policy;

import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpPipelineBuilder;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.MockHttpResponse;
import com.typespec.core.util.Context;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests {@link AddDatePolicy}.
 */
public class AddDatePolicyTests {
    @Test
    public void dateIsRefreshedOnRetryAsync() {
        StepVerifier.create(getPipeline().send(new HttpRequest(HttpMethod.GET, "https://azure.com")))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

    }

    @Test
    public void dateIsRefreshedOnRetrySync() {
        try (HttpResponse response = getPipeline().sendSync(new HttpRequest(HttpMethod.GET, "https://azure.com"),
            Context.NONE)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    private static HttpPipeline getPipeline() {
        return new HttpPipelineBuilder()
            .policies(new RetryPolicy(new FixedDelay(1, Duration.ofSeconds(2))), new AddDatePolicy())
            .httpClient(new HttpClient() {
                private final AtomicReference<String> firstDate = new AtomicReference<>();

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    return send(request, Context.NONE);
                }

                @Override
                public Mono<HttpResponse> send(HttpRequest request, Context context) {
                    return Mono.fromCallable(() -> sendSync(request, context));
                }

                @Override
                public HttpResponse sendSync(HttpRequest request, Context context) {
                    String date = request.getHeaders().getValue(HttpHeaderName.DATE);
                    if (!firstDate.compareAndSet(null, date)) {
                        assertNotEquals(firstDate.get(), date);
                        return new MockHttpResponse(request, 200);
                    } else {
                        return new MockHttpResponse(request, 429);
                    }
                }
            })
            .build();
    }
}
