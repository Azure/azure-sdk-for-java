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
import com.azure.core.util.FluxUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RetryPolicyTests {

    @Test
    public void retryEndOn501() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                // Send 408, 500, 502, all retried, with a 501 ending
                private final int[] codes = new int[]{408, 500, 502, 501};
                private int count = 0;

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    return Mono.just(new MockHttpResponse(request, codes[count++]));
                }
            })
            .policies(new RetryPolicy(new FixedDelay(3, Duration.of(0, ChronoUnit.MILLIS))))
            .build();

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();

        assertEquals(501, response.getStatusCode());
    }

    @Test
    public void retryMax() throws Exception {
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
            .policies(new RetryPolicy(new FixedDelay(maxRetries, Duration.of(0, ChronoUnit.MILLIS))))
            .build();

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();

        assertEquals(500, response.getStatusCode());
    }

    @Test
    public void fixedDelayRetry() throws Exception {
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

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();
    }

    @Test
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

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    if (count > 0) {
                        long requestMadeAt = System.currentTimeMillis();
                        long expectedToBeMadeAt =
                            previousAttemptMadeAt + ((1 << (count - 1)) * (long) (baseDelayMillis * 0.95));
                        Assertions.assertTrue(requestMadeAt >= expectedToBeMadeAt);
                    }
                    Assertions.assertTrue(count++ < maxRetries);
                    previousAttemptMadeAt = System.currentTimeMillis();
                    return Mono.just(new MockHttpResponse(request, 503));
                }
            })
            .policies(new RetryPolicy(exponentialBackoff))
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"))))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void retryConsumesBody() {
        final AtomicInteger bodyConsumptionCount = new AtomicInteger();
        Flux<ByteBuffer> errorBody = Flux.generate(sink -> {
            bodyConsumptionCount.incrementAndGet();
            sink.next(ByteBuffer.wrap("Should be consumed".getBytes(StandardCharsets.UTF_8)));
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
}
