// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.SyncAsyncExtension;
import com.azure.core.SyncAsyncTest;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.HttpPipeline;
import io.clientcore.core.http.HttpPipelineBuilder;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.clients.NoOpHttpClient;
import io.clientcore.core.util.Context;
import org.junit.jupiter.api.Assertions;

import static com.azure.core.CoreTestUtils.createUrl;

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

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Response<?>> send(HttpRequest request) {
                Assertions.assertEquals(request.getHeaders().getValue(HttpHeaderName.X_MS_CLIENT_REQUEST_ID),
                    customRequestId);
                Assertions.assertEquals(request.getHeaders().getValue(HttpHeaderName.REFERER), "my-header1-value");
                Assertions.assertEquals(request.getHeaders().getValue(HttpHeaderName.LOCATION), "my-header2-value");
                return mockResponse);
            }
        }).policies(new RequestIdPolicy()).policies(new AddHeadersFromContextPolicy()).build();

        SyncAsyncExtension.execute(
            () -> pipeline.send(new HttpRequest(HttpMethod.GET, createUrl("http://localhost/")),
                new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers)),
            () -> pipeline.send(new HttpRequest(HttpMethod.GET, createUrl("http://localhost/")),
                new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers)));
    }
}
