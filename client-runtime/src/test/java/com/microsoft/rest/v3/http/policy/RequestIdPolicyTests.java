/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http.policy;

import com.microsoft.rest.v3.http.HttpHeaders;
import com.microsoft.rest.v3.http.HttpMethod;
import com.microsoft.rest.v3.http.HttpPipeline;
import com.microsoft.rest.v3.http.HttpPipelineOptions;
import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.http.MockHttpClient;
import io.netty.buffer.ByteBuf;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.temporal.ChronoUnit;

public class RequestIdPolicyTests {
    private final HttpResponse mockResponse = new HttpResponse() {
        @Override
        public int statusCode() {
            return 500;
        }

        @Override
        public String headerValue(String name) {
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
        public Flux<ByteBuf> body() {
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
        HttpPipeline pipeline = new HttpPipeline(new MockHttpClient() {
            String firstRequestId = null;
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
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
        },
        new HttpPipelineOptions(null),
        new RequestIdPolicy());

        pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"), null)).block();
        pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"), null)).block();
    }

    @Test
    public void sameRequestIdForRetry() throws Exception {
        final HttpPipeline pipeline = new HttpPipeline(new MockHttpClient() {
            String firstRequestId = null;

            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
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
        new HttpPipelineOptions(null),
        new RequestIdPolicy(),
        new RetryPolicy(1, 0, ChronoUnit.SECONDS));

        pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"), null)).block();
    }
}
