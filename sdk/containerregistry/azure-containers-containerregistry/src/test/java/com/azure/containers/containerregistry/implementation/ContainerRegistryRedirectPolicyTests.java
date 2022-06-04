// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ContainerRegistryRedirectPolicyTests {
    @Test
    @Disabled("MockHttpResponse.getHeader() returns a new HttpHeader list instead of response header directly.")
    public void redirectHeaderCheck() throws Exception {
        HttpRequest callThatRedirects = new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/somecall"));

        HttpRequest redirectedRequest = new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/redirect"));

        HttpResponse responseThatRedirects = new MockHttpResponse(callThatRedirects, 308, new HttpHeaders()
            .add("Location", "http://localhost/redirect")
            .add(UtilsImpl.DOCKER_DIGEST_HEADER_NAME, "sha:somevalue"));

        HttpResponse finalResponse = new MockHttpResponse(redirectedRequest, 200)
            .addHeader("some", "run");

        Hashtable<String, HttpResponse> requestToResponse = new Hashtable<String, HttpResponse>();
        requestToResponse.put(getRequestUrl(callThatRedirects), responseThatRedirects);
        requestToResponse.put(getRequestUrl(redirectedRequest), finalResponse);
        MockHttpClient httpClient = new MockHttpClient(requestToResponse);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new ContainerRegistryRedirectPolicy())
            .build();

        HttpResponse response = pipeline.send(callThatRedirects).block();
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getHeaders().getValue(UtilsImpl.DOCKER_DIGEST_HEADER_NAME));
    }

    public static String getRequestUrl(HttpRequest request) {
        return request.getHttpMethod() + request.getUrl().toString();
    }

    static class MockHttpClient implements HttpClient {
        private final Dictionary<String, HttpResponse> requestToResponse;

        MockHttpClient(Dictionary<String, HttpResponse> requestToResponse) {
            this.requestToResponse = requestToResponse;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest httpRequest) {
            return Mono.just(requestToResponse.get(getRequestUrl(httpRequest)));
        }
    }
}
