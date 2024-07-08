// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import io.clientcore.core.http.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.HttpPipeline;
import io.clientcore.core.http.HttpPipelineBuilder;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.util.Context;
import org.junit.jupiter.api.Test;

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
        try (Response<?> response
            = getPipeline().sendSync(new HttpRequest(HttpMethod.GET, "https://azure.com"), Context.none())) {
            assertEquals(200, response.getStatusCode());
        }
    }

    private static HttpPipeline getPipeline() {
        return new HttpPipelineBuilder()
            .policies(new RetryPolicy(new FixedDelay(1, Duration.ofSeconds(2))), new AddDatePolicy())
            .httpClient(new HttpClient() {
                private final AtomicReference<String> firstDate = new AtomicReference<>();

                @Override
                public Response<?>> send(HttpRequest request) {
                    return send(request);
                }

                @Override
                public Response<?>> send(HttpRequest request, Context context) {
                    return Mono.fromCallable(() -> sendSync(request, context));
                }

                @Override
                public Response<?> send(HttpRequest request) {
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
