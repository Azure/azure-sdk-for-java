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
import org.junit.Assert;
import org.junit.Test;
import io.reactivex.Single;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
        public Single<? extends InputStream> bodyAsInputStreamAsync() {
            return Single.just(new InputStream() {
                @Override
                public int read() throws IOException {
                    return -1;
                }
            });
        }

        @Override
        public Single<byte[]> bodyAsByteArrayAsync() {
            return Single.just(new byte[0]);
        }

        @Override
        public Flowable<byte[]> streamBodyAsync() {
            return Flowable.just(new byte[0]);
        }

        @Override
        public Single<String> bodyAsStringAsync() {
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

        pipeline.sendRequestAsync(new HttpRequest("newRequestIdForEachCall", HttpMethod.GET, new URL("http://localhost/"))).blockingGet();
        pipeline.sendRequestAsync(new HttpRequest("newRequestIdForEachCall", HttpMethod.GET, new URL("http://localhost/"))).blockingGet();
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
            new RetryPolicyFactory(1));

        pipeline.sendRequestAsync(new HttpRequest("sameRequestIdForRetry", HttpMethod.GET, new URL("http://localhost/"))).blockingGet();
    }
}
