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
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResponseValidationPolicyTest {

    public static final String X_MS_CLIENT_REQUEST_ID = "x-ms-client-request-id";
    public static final String TEST_CLIENT_REQUEST_ID = "test-client-request-id";

    private static HttpPipelinePolicy getResponseValidationPolicy() {
        return new ResponseValidationPolicyBuilder()
            .addOptionalEcho(X_MS_CLIENT_REQUEST_ID)
            .build();
    }
    @SyncAsyncTest
    public void responseValidationPolicyTestHeader() throws MalformedURLException {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(X_MS_CLIENT_REQUEST_ID, TEST_CLIENT_REQUEST_ID);
        HttpResponse mockResponse = new MockHttpResponse(getRequestWithHeaders(headers), 200, headers);

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
            () -> pipeline.sendSync(getRequestWithHeaders(headers), Context.NONE),
            () -> pipeline.send(getRequestWithHeaders(headers))
        );

        assertEquals(TEST_CLIENT_REQUEST_ID, response.getHeaderValue(X_MS_CLIENT_REQUEST_ID));
    }

    @NotNull
    private static HttpRequest getRequestWithHeaders(HttpHeaders headers) throws MalformedURLException {
        return new HttpRequest(HttpMethod.GET, new URL("http://localhost/"), headers);
    }

    @SyncAsyncTest
    public void responseValidationPolicyTestError() throws MalformedURLException {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(X_MS_CLIENT_REQUEST_ID, TEST_CLIENT_REQUEST_ID);
        HttpResponse mockResponse = new MockHttpResponse(getRequest(), 200, headers);

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
            () -> pipeline.sendSync(getRequest(), Context.NONE),
            () -> pipeline.send(getRequest())
        );

        assertEquals(TEST_CLIENT_REQUEST_ID, response.getHeaderValue(X_MS_CLIENT_REQUEST_ID));
    }

    @NotNull
    private static HttpRequest getRequest() throws MalformedURLException {
        return new HttpRequest(HttpMethod.GET, new URL("http://localhost/"));
    }
}
