/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest;


import com.microsoft.rest.v2.http.*;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyChain;
import com.microsoft.rest.v2.policy.RetryPolicy;
import org.junit.Assert;
import org.junit.Test;

import rx.Single;

public class RetryPolicyTests {
    @Test
    public void exponentialRetryEndOn501() throws Exception {
        RequestPolicy.Factory mockResponseFactory = new RequestPolicy.Factory() {
            @Override
            public RequestPolicy create(final RequestPolicy next) {
                return new RequestPolicy() {
                    // Send 408, 500, 502, all retried, with a 501 ending
                    private final int[] codes = new int[]{408, 500, 502, 501};
                    private int count = 0;

                    @Override
                    public Single<HttpResponse> sendAsync(HttpRequest request) {
                        return Single.<HttpResponse>just(new MockHttpResponse(codes[count++]));
                    }
                };
            }
        };

        RequestPolicyChain chain = new RequestPolicyChain(
                new RetryPolicy.Factory(3),
                mockResponseFactory);

        HttpResponse response = chain.sendRequest(
                new HttpRequest(
                        "exponentialRetryEndOn501",
                        "GET",
                        "http://localhost/"));

        Assert.assertEquals(501, response.statusCode());
    }

    @Test
    public void exponentialRetryMax() throws Exception {
        final int maxRetries = 5;

        RequestPolicy.Factory auditorFactory = new RequestPolicy.Factory() {
            @Override
            public RequestPolicy create(RequestPolicy next) {
                return new RequestPolicy() {
                    int count = -1;
                    @Override
                    public Single<HttpResponse> sendAsync(HttpRequest request) {
                        Assert.assertTrue(count++ < maxRetries);
                        return Single.<HttpResponse>just(new MockHttpResponse(500));
                    }
                };
            }
        };

        RequestPolicyChain chain = new RequestPolicyChain(new RetryPolicy.Factory(maxRetries), auditorFactory);
        HttpResponse response = chain.sendRequest(
                new HttpRequest(
                        "exponentialRetryMax",
                        "GET",
                        "http://localhost/"));

        Assert.assertEquals(500, response.statusCode());
    }
}
