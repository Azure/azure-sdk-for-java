// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;

import static io.clientcore.core.http.pipeline.PipelineTestHelpers.sendRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ParameterizedClass(name = "isAsync={0}")
@ValueSource(booleans = { false, true })
public class HttpPipelineTests {
    private final boolean isAsync;

    public HttpPipelineTests(boolean isAsync) {
        this.isAsync = isAsync;
    }

    @Test
    public void constructorWithNoArguments() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> null).build();

        assertEquals(0, pipeline.getPolicies().size());
        assertNotNull(pipeline.getHttpClient());
    }

    @Test
    public void withRequestPolicy() {
        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(new HttpRetryPolicy()).httpClient(request -> null).build();

        assertEquals(1, pipeline.getPolicies().size());
        assertEquals(HttpRetryPolicy.class, pipeline.getPolicies().get(0).getClass());
        assertNotNull(pipeline.getHttpClient());
    }

    @Test
    public void withRequestContext() {
        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(new HttpRetryPolicy()).httpClient(request -> null).build();

        assertNotNull(pipeline.getHttpClient());
    }

    @Test
    public void withNoRequestPolicies() {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URI expectedUri = URI.create("http://my.site.com");
        final HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(request -> {
            assertEquals(0, request.getHeaders().getSize());
            assertEquals(expectedHttpMethod, request.getHttpMethod());
            assertEquals(expectedUri, request.getUri());

            return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
        }).build();

        HttpRequest request = new HttpRequest().setMethod(expectedHttpMethod).setUri(expectedUri);
        try (Response<BinaryData> response = sendRequest(httpPipeline, request, isAsync)) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void sendWithPolicies() {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URI expectedUri = URI.create("http://my.site.com");
        final HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(request -> {
            assertEquals(0, request.getHeaders().getSize());
            assertEquals(expectedHttpMethod, request.getHttpMethod());
            assertEquals(expectedUri, request.getUri());

            return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
        }).build();

        HttpRequest request = new HttpRequest().setMethod(expectedHttpMethod).setUri(expectedUri);
        try (Response<BinaryData> response = sendRequest(httpPipeline, request, isAsync)) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }
    }
}
