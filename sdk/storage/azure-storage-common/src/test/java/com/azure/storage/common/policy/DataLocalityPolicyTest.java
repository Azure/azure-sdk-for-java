// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link DataLocalityPolicy}.
 */
public class DataLocalityPolicyTest {
    @Test
    public void noOpWhenContextDataIsAbsent() {
        String originalUrl = "https://account.blob.core.windows.net/container/blob";
        HttpRequest request = new HttpRequest(HttpMethod.GET, originalUrl);
        AtomicReference<HttpRequest> capturedRequest = new AtomicReference<>();

        getPipeline(capturedRequest).sendSync(request, Context.NONE);

        assertSame(request, capturedRequest.get());
        assertEquals(originalUrl, request.getUrl().toString());
        assertNull(request.getHeaders().getValue(HttpHeaderName.HOST));
    }

    @Test
    public void rewritesHostAndPortAndPreservesOriginalHostHeader() {
        HttpRequest request
            = new HttpRequest(HttpMethod.GET, "https://myaccount.blob.core.windows.net/container/blob?comp=layout");
        AtomicReference<HttpRequest> capturedRequest = new AtomicReference<>();
        Context context = Context.NONE.addData(DataLocalityPolicy.LAYOUT_ENDPOINT_KEY, "https://10.0.0.5:8443");

        getPipeline(capturedRequest).sendSync(request, context);

        assertSame(request, capturedRequest.get());
        assertEquals("10.0.0.5", request.getUrl().getHost());
        assertEquals(8443, request.getUrl().getPort());
        assertEquals("/container/blob", request.getUrl().getPath());
        assertEquals("comp=layout", request.getUrl().getQuery());
        assertEquals("myaccount.blob.core.windows.net", request.getHeaders().getValue(HttpHeaderName.HOST));
    }

    @Test
    public void noOpWhenContextValueIsEmptyString() {
        String originalUrl = "https://account.blob.core.windows.net/container/blob";
        HttpRequest request = new HttpRequest(HttpMethod.GET, originalUrl);
        AtomicReference<HttpRequest> capturedRequest = new AtomicReference<>();
        Context context = Context.NONE.addData(DataLocalityPolicy.LAYOUT_ENDPOINT_KEY, "");

        getPipeline(capturedRequest).sendSync(request, context);

        assertSame(request, capturedRequest.get());
        assertEquals(originalUrl, request.getUrl().toString());
        assertNull(request.getHeaders().getValue(HttpHeaderName.HOST));
    }

    private static HttpPipeline getPipeline(AtomicReference<HttpRequest> capturedRequest) {
        return new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public HttpResponse sendSync(HttpRequest request, Context context) {
                capturedRequest.set(request);
                return new MockHttpResponse(request, 200);
            }

            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                capturedRequest.set(request);
                return Mono.just(new MockHttpResponse(request, 200));
            }
        }).policies(new DataLocalityPolicy()).build();
    }
}
