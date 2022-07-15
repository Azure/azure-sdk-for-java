// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.SyncAsyncExtension;
import com.azure.core.SyncAsyncTest;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.clients.NoOpHttpClient;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Mono;

import java.net.URL;

public class AddHeadersFromContextPolicyTest {

    private final HttpResponse mockResponse = new MockHttpResponse(null, 500, new HttpHeaders());

    @SyncAsyncTest
    public void clientProvidedMultipleHeaderTest() throws Exception {
        // Create custom Headers
        String customRequestId = "request-id-value";
        final HttpHeaders headers = new HttpHeaders();
        headers.set("x-ms-client-request-id", customRequestId);
        headers.set("my-header1", "my-header1-value");
        headers.set("my-header2", "my-header2-value");

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Assertions.assertEquals(request.getHeaders().getValue("x-ms-client-request-id"), customRequestId);
                    Assertions.assertEquals(request.getHeaders().getValue("my-header1"), "my-header1-value");
                    Assertions.assertEquals(request.getHeaders().getValue("my-header2"), "my-header2-value");
                    return Mono.just(mockResponse);
                }
            })
            .policies(new RequestIdPolicy())
            .policies(new AddHeadersFromContextPolicy())
            .build();

        SyncAsyncExtension.execute(
            () -> pipeline.sendSync(new HttpRequest(HttpMethod.GET, new URL("http://localhost/")), new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers)),
            () -> pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/")), new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers))
        );
    }
}
