// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.clients.NoOpHttpClient;
import com.azure.core.http.policy.UserAgentPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.URL;

public class UserAgentTests {
    @Test
    public void defaultUserAgentTests() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Assertions.assertEquals(
                            request.getHeaders().getValue("User-Agent"),
                            "AutoRest-Java");
                    return Mono.just(new MockHttpResponse(request, 200));
                }
            })
            .policies(new UserAgentPolicy("AutoRest-Java"))
            .build();

        HttpResponse response = pipeline.send(new HttpRequest(
                HttpMethod.GET, new URL("http://localhost"))).block();

        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void customUserAgentTests() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    String header = request.getHeaders().getValue("User-Agent");
                    Assertions.assertEquals("Awesome", header);
                    return Mono.just(new MockHttpResponse(request, 200));
                }
            })
            .policies(new UserAgentPolicy("Awesome"))
            .build();

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
                new URL("http://localhost"))).block();
        Assertions.assertEquals(200, response.getStatusCode());
    }
}
