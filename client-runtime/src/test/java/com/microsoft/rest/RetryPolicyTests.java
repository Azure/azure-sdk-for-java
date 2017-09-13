/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest;


import com.microsoft.rest.v2.http.*;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RetryPolicy;
import org.junit.Assert;
import org.junit.Test;

import rx.Single;

import java.util.Collections;

public class RetryPolicyTests {
    @Test
    public void exponentialRetryEndOn501() throws Exception {
        HttpClient client = new HttpClient(Collections.<RequestPolicy.Factory>singletonList(new RetryPolicy.Factory(3))) {
            // Send 408, 500, 502, all retried, with a 501 ending
            private final int[] codes = new int[]{408, 500, 502, 501};
            private int count = 0;

            @Override
            protected Single<? extends HttpResponse> sendRequestInternalAsync(HttpRequest request) {
                return Single.<HttpResponse>just(new MockHttpResponse(codes[count++]));
            }
        };

        HttpResponse response = client.sendRequest(
                new HttpRequest(
                        "exponentialRetryEndOn501",
                        "GET",
                        "http://localhost/"));

        Assert.assertEquals(501, response.statusCode());
    }

    @Test
    public void exponentialRetryMax() throws Exception {
        final int maxRetries = 5;

        HttpClient client = new HttpClient(Collections.<RequestPolicy.Factory>singletonList(new RetryPolicy.Factory(maxRetries))) {
            int count = -1;
            @Override
            public Single<? extends HttpResponse> sendRequestInternalAsync(HttpRequest request) {
                Assert.assertTrue(count++ < maxRetries);
                return Single.<HttpResponse>just(new MockHttpResponse(500));
            }
        };

        HttpResponse response = client.sendRequest(
                new HttpRequest(
                        "exponentialRetryMax",
                        "GET",
                        "http://localhost/"));

        Assert.assertEquals(500, response.statusCode());
    }
}
