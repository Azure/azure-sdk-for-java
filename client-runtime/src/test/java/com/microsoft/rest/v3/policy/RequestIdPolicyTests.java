/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.policy;

import com.microsoft.rest.v3.http.HttpHeaders;
import com.microsoft.rest.v3.http.HttpMethod;
import com.microsoft.rest.v3.http.HttpPipeline;
import com.microsoft.rest.v3.http.HttpPipelineBuilder;
import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.http.MockHttpClient;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.nio.ByteBuffer;
import java.time.temporal.ChronoUnit;

public class RequestIdPolicyTests {
    private final HttpResponse mockResponse = new HttpResponse() {
        @Override
        public int statusCode() {
            return 500;
        }

        @Override
        public String headerValue(String headerName) {
            return null;
        }

        @Override
        public HttpHeaders headers() {
            return new HttpHeaders();
        }

        @Override
        public Mono<byte[]> bodyAsByteArray() {
            return Mono.empty();
        }

        @Override
        public Flux<ByteBuffer> body() {
            return Flux.empty();
        }

        @Override
        public Mono<String> bodyAsString() {
            return Mono.empty();
        }
    };

    private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";

    @Test
    public void newRequestIdForEachCall() throws Exception {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .withRequestPolicy(new RequestIdPolicyFactory())
            .withHttpClient(new MockHttpClient() {
                String firstRequestId = null;

                @Override
                public Mono<HttpResponse> sendRequestAsync(HttpRequest request) {
                    if (firstRequestId != null) {
                        String newRequestId = request.headers().value(REQUEST_ID_HEADER);
                        Assert.assertNotNull(newRequestId);
                        Assert.assertNotEquals(newRequestId, firstRequestId);
                    }

                    firstRequestId = request.headers().value(REQUEST_ID_HEADER);
                    if (firstRequestId == null) {
                        Assert.fail();
                    }
                    return Mono.just(mockResponse);
                }
            })
            .build();

        pipeline.sendRequestAsync(new HttpRequest("newRequestIdForEachCall", HttpMethod.GET, new URL("http://localhost/"), null)).block();
        pipeline.sendRequestAsync(new HttpRequest("newRequestIdForEachCall", HttpMethod.GET, new URL("http://localhost/"), null)).block();
    }

    @Test
    public void sameRequestIdForRetry() throws Exception {
        HttpPipeline pipeline = HttpPipeline.build(
            new MockHttpClient() {
                String firstRequestId = null;

                @Override
                public Mono<HttpResponse> sendRequestAsync(HttpRequest request) {
                    if (firstRequestId != null) {
                        String newRequestId = request.headers().value(REQUEST_ID_HEADER);
                        Assert.assertNotNull(newRequestId);
                        Assert.assertEquals(newRequestId, firstRequestId);
                    }

                    firstRequestId = request.headers().value(REQUEST_ID_HEADER);
                    if (firstRequestId == null) {
                        Assert.fail();
                    }
                    return Mono.just(mockResponse);
                }
            },
            new RequestIdPolicyFactory(),
            new RetryPolicyFactory(1, 0, ChronoUnit.SECONDS));

        pipeline.sendRequestAsync(new HttpRequest("sameRequestIdForRetry", HttpMethod.GET, new URL("http://localhost/"), null)).block();
    }
}
