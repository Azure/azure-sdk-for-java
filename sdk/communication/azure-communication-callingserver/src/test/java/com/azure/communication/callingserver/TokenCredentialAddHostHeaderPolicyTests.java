// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

public class TokenCredentialAddHostHeaderPolicyTests {

    private class NoOpHttpClient implements HttpClient {
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.empty(); // NOP
        }
    }

    private final String HOST_NAME = "host.communication.azure.com";

    private final HttpPipelinePolicy verifyHeadersPolicy = (context, next) -> {
        HttpRequest request = context.getHttpRequest();
        String hostHeaderValue = request.getHeaders().getValue("x-ms-host");
        assertEquals(HOST_NAME, hostHeaderValue);
        return next.process();
    };

    @Test
    public void getRequestTest() throws MalformedURLException {
        final TokenCredentialAddHostHeaderPolicy clientPolicy = new TokenCredentialAddHostHeaderPolicy(HOST_NAME);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(clientPolicy, verifyHeadersPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("https://localhost?id=b93a5ef4-f622-44d8-a80b-ff983122554e"));
        StepVerifier.create(pipeline.send(request))
            .verifyComplete();
    }

    @Test
    public void postRequestTest() throws MalformedURLException {
        final TokenCredentialAddHostHeaderPolicy clientPolicy = new TokenCredentialAddHostHeaderPolicy(HOST_NAME);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(clientPolicy, verifyHeadersPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL("https://localhost?id=b93a5ef4-f622-44d8-a80b-ff983122554e"));
        StepVerifier.create(pipeline.send(request))
            .verifyComplete();
    }

    @Test
    public void patchRequestTest() throws MalformedURLException {
        final TokenCredentialAddHostHeaderPolicy clientPolicy = new TokenCredentialAddHostHeaderPolicy(HOST_NAME);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(clientPolicy, verifyHeadersPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.PATCH, new URL("https://localhost?id=b93a5ef4-f622-44d8-a80b-ff983122554e"));
        StepVerifier.create(pipeline.send(request))
            .verifyComplete();
    }


    @Test
    public void putRequestTest() throws MalformedURLException {
        final TokenCredentialAddHostHeaderPolicy clientPolicy = new TokenCredentialAddHostHeaderPolicy(HOST_NAME);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(clientPolicy, verifyHeadersPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.PUT, new URL("https://localhost?id=b93a5ef4-f622-44d8-a80b-ff983122554e"));
        StepVerifier.create(pipeline.send(request))
            .verifyComplete();
    }

    @Test
    public void deleteRequestTest() throws MalformedURLException {
        final TokenCredentialAddHostHeaderPolicy clientPolicy = new TokenCredentialAddHostHeaderPolicy(HOST_NAME);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(clientPolicy, verifyHeadersPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.DELETE, new URL("https://localhost?id=b93a5ef4-f622-44d8-a80b-ff983122554e"));
        StepVerifier.create(pipeline.send(request))
            .verifyComplete();
    }
}
