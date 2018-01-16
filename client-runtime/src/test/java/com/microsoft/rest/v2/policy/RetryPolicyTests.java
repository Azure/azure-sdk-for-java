/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;


import com.microsoft.rest.v2.http.*;
import org.junit.Assert;
import org.junit.Test;

import io.reactivex.Single;

import java.net.URL;

public class RetryPolicyTests {
    @Test
    public void exponentialRetryEndOn501() throws Exception {
        HttpPipeline pipeline = HttpPipeline.build(
            new MockHttpClient() {
                // Send 408, 500, 502, all retried, with a 501 ending
                private final int[] codes = new int[]{408, 500, 502, 501};
                private int count = 0;

                public Single<HttpResponse> sendRequestAsync(HttpRequest request) {
                    return Single.<HttpResponse>just(new MockHttpResponse(codes[count++]));
                }
            },
            new RetryPolicyFactory(3));

        HttpResponse response = pipeline.sendRequestAsync(
                new HttpRequest(
                        "exponentialRetryEndOn501",
                        HttpMethod.GET,
                        new URL("http://localhost/"))).blockingGet();

        Assert.assertEquals(501, response.statusCode());
    }

    @Test
    public void exponentialRetryMax() throws Exception {
        final int maxRetries = 5;

        HttpPipeline pipeline = HttpPipeline.build(
            new MockHttpClient() {
                int count = -1;

                public Single<HttpResponse> sendRequestAsync(HttpRequest request) {
                    Assert.assertTrue(count++ < maxRetries);
                    return Single.<HttpResponse>just(new MockHttpResponse(500));
                }
            },
            new RetryPolicyFactory(maxRetries));

        HttpResponse response = pipeline.sendRequestAsync(
                new HttpRequest(
                        "exponentialRetryMax",
                        HttpMethod.GET,
                        new URL("http://localhost/"))).blockingGet();

        Assert.assertEquals(500, response.statusCode());
    }
}
