// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataLocalityPolicyTests {
    @SyncAsyncTest
    public void bareHostPortEndpointRewritesHostAndPreservesOriginalHeader() throws Exception {
        assertRequestUrlRewritesHostAndPreservesOriginalHeader("layout.example.net:8443",
            "https://layout.example.net:8443/container/blob");
    }

    @SyncAsyncTest
    public void fullUrlEndpointRewritesHostAndPreservesOriginalHeader() throws Exception {
        assertRequestUrlRewritesHostAndPreservesOriginalHeader("https://layout.example.net",
            "https://layout.example.net/container/blob");
    }

    @SyncAsyncTest
    public void fullUrlEndpointRewritesExplicitPortAndPreservesOriginalHeader() throws Exception {
        assertRequestUrlRewritesHostAndPreservesOriginalHeader("https://layout.example.net:8443",
            "https://layout.example.net:8443/container/blob");
    }

    @SyncAsyncTest
    public void absentAndEmptyEndpointDoNotRewriteRequestUrl() throws Exception {
        assertRequestUrlDoesNotRewrite(Context.NONE);
        assertRequestUrlDoesNotRewrite(new Context(DataLocalityPolicy.LAYOUT_ENDPOINT_KEY, ""));
    }

    @SyncAsyncTest
    public void malformedEndpointThrowsClearError() {
        assertMalformedEndpointThrows("http://");
    }

    @SyncAsyncTest
    public void whitespaceEndpointThrowsClearError() {
        assertMalformedEndpointThrows("   ");
    }

    private void assertMalformedEndpointThrows(String endpoint) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> sendRequest(new Context(DataLocalityPolicy.LAYOUT_ENDPOINT_KEY, endpoint)));

        assertTrue(exception.getMessage().contains("Invalid data locality endpoint '" + endpoint + "'"));
        assertTrue(exception.getMessage().contains("host[:port]"));
    }

    private void assertRequestUrlRewritesHostAndPreservesOriginalHeader(String layoutEndpoint, String expectedUrl)
        throws Exception {
        HttpRequest seenRequest = sendRequest(new Context(DataLocalityPolicy.LAYOUT_ENDPOINT_KEY, layoutEndpoint));

        assertEquals(expectedUrl, seenRequest.getUrl().toString());
        assertEquals("storage.example.com", seenRequest.getHeaders().getValue(HttpHeaderName.HOST));
    }

    private void assertRequestUrlDoesNotRewrite(Context context) throws Exception {
        HttpRequest seenRequest = sendRequest(context);

        assertEquals("https://storage.example.com/container/blob", seenRequest.getUrl().toString());
        assertEquals("storage.example.com", seenRequest.getHeaders().getValue(HttpHeaderName.HOST));
    }

    private HttpRequest sendRequest(Context context) throws Exception {
        final HttpRequest[] seenRequest = new HttpRequest[1];
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                seenRequest[0] = request;
                return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders()));
            }
        }).policies(new DataLocalityPolicy()).build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("https://storage.example.com/container/blob"));
        request.setHeader(HttpHeaderName.HOST, "storage.example.com");

        SyncAsyncExtension.execute(() -> pipeline.sendSync(request, context), () -> pipeline.send(request, context));

        return seenRequest[0];
    }
}
