// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.Context;
import com.azure.core.util.DateTimeRfc1123;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests {@link AddDatePolicy}.
 */
public class AddDatePolicyTests {
    @Test
    public void dateHeaderIsAlwaysOverridden() {
        String initialDateHeader = new DateTimeRfc1123(OffsetDateTime.now().minusDays(1)).toString();

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new AddDatePolicy())
            .httpClient(request -> {
                assertNotEquals(initialDateHeader, request.getHeaders().getValue(HttpHeaderName.DATE));

                return Mono.empty();
            })
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "https://azure.com")
            .setHeaders(new HttpHeaders().set(HttpHeaderName.DATE, initialDateHeader))))
            .verifyComplete();

        assertDoesNotThrow(() -> pipeline.sendSync(new HttpRequest(HttpMethod.GET, "https://azure.com")
            .setHeaders(new HttpHeaders().set(HttpHeaderName.DATE, initialDateHeader)), Context.NONE));
    }

    @Test
    public void dateHeaderIsSetOnEachRetry() {
        AtomicReference<String> firstDateHeader = new AtomicReference<>();

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(new FixedDelay(1, Duration.ofSeconds(2))), new AddDatePolicy())
            .httpClient(request -> {
                if (firstDateHeader.compareAndSet(null, request.getHeaders().getValue(HttpHeaderName.DATE))) {
                    return Mono.error(new IOException());
                }

                assertNotEquals(firstDateHeader.get(), request.getHeaders().getValue(HttpHeaderName.DATE));

                return Mono.empty();
            })
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://azure.com");

        StepVerifier.create(pipeline.send(request)).verifyComplete();

        firstDateHeader.set(null);

        assertDoesNotThrow(() -> pipeline.sendSync(request, Context.NONE));
    }
}
