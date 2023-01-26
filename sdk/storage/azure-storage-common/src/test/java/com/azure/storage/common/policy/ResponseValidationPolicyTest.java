// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Mono;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResponseValidationPolicyTest {
    private static HttpPipelinePolicy getResponseValidationPolicy() {
        return new ResponseValidationPolicyBuilder()
            .addOptionalEcho("x-ms-client-request-id")
            .build();
    }
    @SyncAsyncTest
    public void responseValidationPolicyTestHeader() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("x-ms-client-request-id", "test-client-request-id");
        HttpResponse mockResponse = new MockHttpResponse(null, 200, headers);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    return Mono.just(mockResponse);
                }
            })
            .policies(getResponseValidationPolicy())
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> pipeline.sendSync(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"), headers), Context.NONE),
            () -> pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"), headers))
        );

        assertEquals("test-client-request-id", response.getHeaderValue(Constants.HeaderConstants.CLIENT_REQUEST_ID));
    }

    @SyncAsyncTest
    public void responseValidationPolicyTestError() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("x-ms-client-request-id", "test-client-request-id");
        HttpResponse mockResponse = new MockHttpResponse(null, 200, headers);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    return Mono.just(mockResponse);
                }
            })
            .policies(getResponseValidationPolicy())
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> pipeline.sendSync(new HttpRequest(HttpMethod.GET, new URL("http://localhost/")), Context.NONE),
            () -> pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/")))
        );

        assertEquals("test-client-request-id", response.getHeaderValue("x-ms-client-request-id"));
    }
}
