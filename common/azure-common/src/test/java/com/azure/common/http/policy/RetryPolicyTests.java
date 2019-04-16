// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.http.policy;

import com.azure.common.http.HttpMethod;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.HttpRequest;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.MockHttpClient;
import com.azure.common.http.MockHttpResponse;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class RetryPolicyTests {
    @Test
    public void exponentialRetryEndOn501() throws Exception {
        final HttpPipeline pipeline = new HttpPipeline(new MockHttpClient() {
            // Send 408, 500, 502, all retried, with a 501 ending
            private final int[] codes = new int[]{408, 500, 502, 501};
            private int count = 0;

            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.<HttpResponse>just(new MockHttpResponse(request, codes[count++]));
            }
        }, new RetryPolicy(3, Duration.of(0, ChronoUnit.MILLIS)));

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
                        new URL("http://localhost/"))).block();

        Assert.assertEquals(501, response.statusCode());
    }

    @Test
    public void exponentialRetryMax() throws Exception {
        final int maxRetries = 5;
        final HttpPipeline pipeline = new HttpPipeline(new MockHttpClient() {
            int count = -1;

            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                Assert.assertTrue(count++ < maxRetries);
                return Mono.<HttpResponse>just(new MockHttpResponse(request, 500));
            }
        },
            new RetryPolicy(maxRetries, Duration.of(0, ChronoUnit.MILLIS)));


        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
                        new URL("http://localhost/"))).block();

        Assert.assertEquals(500, response.statusCode());
    }
}
