// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpClient;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.policy.UserAgentPolicy;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.net.URL;

public class UserAgentTests {
    @Test
    public void defaultUserAgentTests() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new MockHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Assert.assertEquals(
                            request.headers().value("User-Agent"),
                            "AutoRest-Java");
                    return Mono.<HttpResponse>just(new MockHttpResponse(request, 200));
                }
            })
            .policies(new UserAgentPolicy("AutoRest-Java"))
            .build();

        HttpResponse response = pipeline.send(new HttpRequest(
                HttpMethod.GET, new URL("http://localhost"))).block();

        Assert.assertEquals(200, response.statusCode());
    }

    @Test
    public void customUserAgentTests() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new MockHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    String header = request.headers().value("User-Agent");
                    Assert.assertEquals("Awesome", header);
                    return Mono.<HttpResponse>just(new MockHttpResponse(request, 200));
                }
            })
            .policies(new UserAgentPolicy("Awesome"))
            .build();

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
                new URL("http://localhost"))).block();
        Assert.assertEquals(200, response.statusCode());
    }
}
