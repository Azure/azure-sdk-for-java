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
import com.azure.core.http.clients.NoOpHttpClient;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class RequestIdPolicyTests {
    static HttpRequest HTTP_REQUEST;

    static {
        try {
            HTTP_REQUEST = createHttpRequest("https://www.bing.com");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private final HttpResponse mockResponse = new HttpResponse(null) {
        @Override
        public int getStatusCode() {
            return 500;
        }

        @Override
        public String getHeaderValue(String name) {
            return null;
        }

        @Override
        public HttpHeaders getHeaders() {
            return new HttpHeaders();
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return Mono.empty();
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return Flux.empty();
        }

        @Override
        public Mono<String> getBodyAsString() {
            return Mono.empty();
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return Mono.empty();
        }
    };

    private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";

    @SyncAsyncTest
    public void newRequestIdForEachCall() throws Exception {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                String firstRequestId = null;
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    if (firstRequestId != null) {
                        String newRequestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
                        Assertions.assertNotNull(newRequestId);
                        Assertions.assertNotEquals(newRequestId, firstRequestId);
                    }

                    firstRequestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
                    if (firstRequestId == null) {
                        Assertions.fail();
                    }
                    return Mono.just(mockResponse);
                }
            })
            .policies(new RequestIdPolicy())
            .build();

        SyncAsyncExtension.execute(
            () -> pipeline.sendSync(HTTP_REQUEST,Context.NONE),
            () -> pipeline.send(HTTP_REQUEST)
        );
    }

    @SyncAsyncTest
    public void sameRequestIdForRetry() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                String firstRequestId = null;

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    if (firstRequestId != null) {
                        String newRequestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
                        Assertions.assertNotNull(newRequestId);
                        Assertions.assertEquals(newRequestId, firstRequestId);
                    }
                    firstRequestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
                    if (firstRequestId == null) {
                        Assertions.fail();
                    }
                    return Mono.just(mockResponse);
                }
            })
            .policies(new RequestIdPolicy(), new RetryPolicy(new FixedDelay(1, Duration.of(0, ChronoUnit.SECONDS))))
            .build();

        SyncAsyncExtension.execute(
            () -> pipeline.sendSync(HTTP_REQUEST,Context.NONE),
            () -> pipeline.send(HTTP_REQUEST)
        );
    }

    private static HttpRequest createHttpRequest(String url) throws MalformedURLException {
        return new HttpRequest(HttpMethod.GET, new URL(url));
    }
}
