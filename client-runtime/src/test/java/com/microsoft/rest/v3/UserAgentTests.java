/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3;

import com.microsoft.rest.v3.http.HttpMethod;
import com.microsoft.rest.v3.http.HttpPipeline;
import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.http.MockHttpClient;
import com.microsoft.rest.v3.http.MockHttpResponse;
import com.microsoft.rest.v3.policy.UserAgentPolicyFactory;
import org.junit.Assert;
import org.junit.Test;

import reactor.core.publisher.Mono;

import java.net.URL;

public class UserAgentTests {
    @Test
    public void defaultUserAgentTests() throws Exception {
        HttpPipeline pipeline = HttpPipeline.build(
            new MockHttpClient() {

                @Override
                public Mono<HttpResponse> sendRequestAsync(HttpRequest request) {
                    Assert.assertEquals(
                            request.headers().value("User-Agent"),
                            "AutoRest-Java");
                    return Mono.<HttpResponse>just(new MockHttpResponse(200));
                }
            },
            new UserAgentPolicyFactory("AutoRest-Java"));

        HttpResponse response = pipeline.sendRequestAsync(new HttpRequest(
                "defaultUserAgentTests",
                HttpMethod.GET, new URL("http://localhost"), null)).block();

        Assert.assertEquals(200, response.statusCode());
    }

    @Test
    public void customUserAgentTests() throws Exception {
        HttpPipeline pipeline = HttpPipeline.build(
            new MockHttpClient() {

                @Override
                public Mono<HttpResponse> sendRequestAsync(HttpRequest request) {
                    String header = request.headers().value("User-Agent");
                    Assert.assertEquals("Awesome", header);
                    return Mono.<HttpResponse>just(new MockHttpResponse(200));
                }
            },
            new UserAgentPolicyFactory("Awesome"));

        HttpResponse response = pipeline.sendRequestAsync(new HttpRequest("customUserAgentTests", HttpMethod.GET, new URL("http://localhost"), null)).block();
        Assert.assertEquals(200, response.statusCode());
    }
}
