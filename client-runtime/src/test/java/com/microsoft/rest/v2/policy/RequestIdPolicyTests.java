/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpHeaders;
import com.microsoft.rest.v2.http.HttpMethod;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpPipelineBuilder;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.MockHttpClient;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

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
        public Single<byte[]> bodyAsByteArray() {
            return Single.just(new byte[0]);
        }

        @Override
        public Flowable<ByteBuffer> body() {
            return Flowable.just(ByteBuffer.allocate(0));
        }

        @Override
        public Single<String> bodyAsString() {
            return Single.just("");
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
                public Single<HttpResponse> sendRequestAsync(HttpRequest request) {
                    if (firstRequestId != null) {
                        String newRequestId = request.headers().value(REQUEST_ID_HEADER);
                        Assert.assertNotNull(newRequestId);
                        Assert.assertNotEquals(newRequestId, firstRequestId);
                    }

                    firstRequestId = request.headers().value(REQUEST_ID_HEADER);
                    if (firstRequestId == null) {
                        Assert.fail();
                    }

                    return Single.just(mockResponse);
                }
            })
            .build();

        pipeline.sendRequestAsync(new HttpRequest("newRequestIdForEachCall", HttpMethod.GET, new URL("http://localhost/"), null)).blockingGet();
        pipeline.sendRequestAsync(new HttpRequest("newRequestIdForEachCall", HttpMethod.GET, new URL("http://localhost/"), null)).blockingGet();
    }

    @Test
    public void sameRequestIdForRetry() throws Exception {
        HttpPipeline pipeline = HttpPipeline.build(
            new MockHttpClient() {
                String firstRequestId = null;
                @Override
                public Single<HttpResponse> sendRequestAsync(HttpRequest request) {
                    if (firstRequestId != null) {
                        String newRequestId = request.headers().value(REQUEST_ID_HEADER);
                        Assert.assertNotNull(newRequestId);
                        Assert.assertEquals(newRequestId, firstRequestId);
                    }

                    firstRequestId = request.headers().value(REQUEST_ID_HEADER);
                    if (firstRequestId == null) {
                        Assert.fail();
                    }
                    return Single.just(mockResponse);
                }
            },
            new RequestIdPolicyFactory(),
            new RetryPolicyFactory(1, 0, TimeUnit.SECONDS));

        pipeline.sendRequestAsync(new HttpRequest("sameRequestIdForRetry", HttpMethod.GET, new URL("http://localhost/"), null)).blockingGet();
    }
}
