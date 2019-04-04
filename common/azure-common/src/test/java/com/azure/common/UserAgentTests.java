/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common;

import com.azure.common.http.HttpMethod;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.HttpRequest;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.MockHttpClient;
import com.azure.common.http.MockHttpResponse;
import com.azure.common.http.policy.UserAgentPolicy;
import org.junit.Assert;
import org.junit.Test;

import reactor.core.publisher.Mono;

import java.net.URL;

public class UserAgentTests {
    @Test
    public void defaultUserAgentTests() throws Exception {
        final HttpPipeline pipeline = new HttpPipeline(new MockHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Assert.assertEquals(
                            request.headers().value("User-Agent"),
                            "AutoRest-Java");
                    return Mono.<HttpResponse>just(new MockHttpResponse(request, 200));
                }
            },
            new UserAgentPolicy("AutoRest-Java"));

        HttpResponse response = pipeline.send(new HttpRequest(
                HttpMethod.GET, new URL("http://localhost"))).block();

        Assert.assertEquals(200, response.statusCode());
    }

    @Test
    public void customUserAgentTests() throws Exception {
        final HttpPipeline pipeline = new HttpPipeline(new MockHttpClient() {
            @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    String header = request.headers().value("User-Agent");
                    Assert.assertEquals("Awesome", header);
                    return Mono.<HttpResponse>just(new MockHttpResponse(request, 200));
                }
            },
            new UserAgentPolicy("Awesome"));

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
                new URL("http://localhost"))).block();
        Assert.assertEquals(200, response.statusCode());
    }
}
