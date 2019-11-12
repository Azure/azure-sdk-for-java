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
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserAgentTests {
    @Test
    public void defaultUserAgentTests() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    assertEquals(
                        request.getHeaders().getValue("User-Agent"),
                        "AutoRest-Java");
                    return Mono.just(new MockHttpResponse(request, 200));
                }
            })
            .policies(new UserAgentPolicy("AutoRest-Java"))
            .build();

        HttpResponse response = pipeline.send(new HttpRequest(
            HttpMethod.GET, new URL("http://localhost"))).block();

        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void customUserAgentTests() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    String header = request.getHeaders().getValue("User-Agent");
                    assertEquals("Awesome", header);
                    return Mono.just(new MockHttpResponse(request, 200));
                }
            })
            .policies(new UserAgentPolicy("Awesome"))
            .build();

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost"))).block();
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void defaultApplicationIdUserAgentTest() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    String header = request.getHeaders().getValue("User-Agent");
                    String expectedHeaderPrefix = "azsdk-java-package.name";
                    Assertions.assertTrue(header.startsWith(expectedHeaderPrefix));
                    return Mono.just(new MockHttpResponse(request, 200));
                }
            })
            .policies(new UserAgentPolicy(
                null,
                "package.name",
                "package_version",
                Configuration.NONE,
                () -> "1.0"))
            .build();

        Mono<HttpResponse> response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost")));
        StepVerifier.create(response)
            .expectNextMatches(httpResponse -> {
                assertEquals(200, httpResponse.getStatusCode());
                assertTrue(httpResponse.getRequest().getHeaders().getValue("User-Agent").startsWith("azsdk"));
                return true;
            })
            .verifyComplete();
    }

    @Test
    public void customApplicationIdUserAgentTest() throws Exception {
        final String testSdkName = "sdk.name";
        final String testAppId = "user_specified_appId";
        final String testPackageVersion = "package_version";
        String javaVersion = Configuration.getGlobalConfiguration().get("java.version");
        String osName = Configuration.getGlobalConfiguration().get("os.name");
        String osVersion = Configuration.getGlobalConfiguration().get("os.version");
        String testPlatformInfo = javaVersion + "; " + osName + " " + osVersion;
        String expectedHeader = testAppId + " " + "azsdk-java-" + testSdkName + "/"
            + testPackageVersion + " " + testPlatformInfo;

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    String header = request.getHeaders().getValue("User-Agent");
                    assertEquals(header, expectedHeader);
                    return Mono.just(new MockHttpResponse(request, 200));
                }
            })
            .policies(new UserAgentPolicy(
                testAppId,
                testSdkName,
                testPackageVersion,
                Configuration.NONE,
                () -> "1.0"))
            .build();

        Mono<HttpResponse> response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost")));
        StepVerifier.create(response)
            .expectNextMatches(httpResponse -> {
                assertEquals(200, httpResponse.getStatusCode());
                assertEquals(expectedHeader, httpResponse.getRequest().getHeaders().getValue("User-Agent"));
                return true;
            })
            .verifyComplete();
    }
}
