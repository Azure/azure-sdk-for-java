// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.http.policy;

import com.client.core.SyncAsyncExtension;
import com.client.core.SyncAsyncTest;
import com.client.core.http.HttpHeaderName;
import com.client.core.http.HttpHeaders;
import com.client.core.http.HttpMethod;
import com.client.core.http.HttpPipeline;
import com.client.core.http.HttpPipelineBuilder;
import com.client.core.http.HttpRequest;
import com.client.core.http.HttpResponse;
import com.client.core.http.MockHttpResponse;
import com.client.core.http.clients.NoOpHttpClient;
import com.client.core.util.Context;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Mono;

import static com.client.core.CoreTestUtils.createUrl;

public class AddHeadersFromContextPolicyTest {

    private final HttpResponse mockResponse = new MockHttpResponse(null, 500, new HttpHeaders());

    @SyncAsyncTest
    public void clientProvidedMultipleHeaderTest() throws Exception {
        // Create custom Headers
        String customRequestId = "request-id-value";
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaderName.X_MS_CLIENT_REQUEST_ID, customRequestId);
        headers.set(HttpHeaderName.REFERER, "my-header1-value");
        headers.set(HttpHeaderName.LOCATION, "my-header2-value");

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Assertions.assertEquals(request.getHeaders().getValue(HttpHeaderName.X_MS_CLIENT_REQUEST_ID),
                        customRequestId);
                    Assertions.assertEquals(request.getHeaders().getValue(HttpHeaderName.REFERER), "my-header1-value");
                    Assertions.assertEquals(request.getHeaders().getValue(HttpHeaderName.LOCATION), "my-header2-value");
                    return Mono.just(mockResponse);
                }
            })
            .policies(new RequestIdPolicy())
            .policies(new AddHeadersFromContextPolicy())
            .build();

        SyncAsyncExtension.execute(
            () -> pipeline.sendSync(new HttpRequest(HttpMethod.GET, createUrl("http://localhost/")),
                new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers)),
            () -> pipeline.send(new HttpRequest(HttpMethod.GET, createUrl("http://localhost/")),
                new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers))
        );
    }
}
