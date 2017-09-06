/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestIdPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyChain;
import com.microsoft.rest.v2.policy.RetryPolicy;
import com.microsoft.rest.v2.policy.SendRequestPolicyFactory;
import org.junit.Assert;
import org.junit.Test;
import rx.Single;

import java.io.IOException;
import java.io.InputStream;

public class RequestIdPolicyTests {
    private final HttpResponse mockResponse = new HttpResponse() {
        @Override
        public int statusCode() {
            return 500;
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
        public Single<String> bodyAsStringAsync() {
            return Single.just("");
        }
    };

    private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";

    @Test
    public void newRequestIdForEachCall() throws Exception {
        RequestPolicyChain chain = new RequestPolicyChain(
                new RequestIdPolicy.Factory(),
                new SendRequestPolicyFactory(new HttpClient() {
                    String firstRequestId = null;
                    @Override
                    public Single<? extends HttpResponse> sendRequestAsync(HttpRequest request) {
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
                }));

        chain.sendRequest(new HttpRequest("newRequestIdForEachCall", "GET", "http://localhost/"));
        chain.sendRequest(new HttpRequest("newRequestIdForEachCall", "GET", "http://localhost/"));
    }

    @Test
    public void sameRequestIdForRetry() throws Exception {
        RequestPolicyChain chain = new RequestPolicyChain(
                new RequestIdPolicy.Factory(),
                new RetryPolicy.Factory(1),
                new SendRequestPolicyFactory(new HttpClient() {
                    String firstRequestId = null;
                    @Override
                    public Single<? extends HttpResponse> sendRequestAsync(HttpRequest request) {
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
                }));

        chain.sendRequest(new HttpRequest("sameRequestIdForRetry", "GET", "http://localhost/"));
    }
}
