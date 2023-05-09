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

public class ScrubEtagPolicyTest {
    private static final String ETAG_VALUE = "RU3FchB4PhtZdVy44UQO5CGkyZM";

    @SyncAsyncTest
    public void scrubEtagDoNothing() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaderName.ETAG, ETAG_VALUE);
        HttpResponse mockResponse = new MockHttpResponse(null, 200, headers);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    return Mono.just(mockResponse);
                }
            })
            .policies(new ScrubEtagPolicy())
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> pipeline.sendSync(new HttpRequest(HttpMethod.GET, new URL("http://localhost/")), Context.NONE),
            () -> pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/")))
        );

        assertEquals(ETAG_VALUE, response.getHeaderValue(HttpHeaderName.ETAG.toString()));
    }

    @SyncAsyncTest
    public void scrubEtagWithQuotes() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaderName.ETAG, "\"RU3FchB4PhtZdVy44UQO5CGkyZM\"");
        HttpResponse mockResponse = new MockHttpResponse(null, 200, headers);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    return Mono.just(mockResponse);
                }
            })
            .policies(new ScrubEtagPolicy())
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> pipeline.sendSync(new HttpRequest(HttpMethod.GET, new URL("http://localhost/")), Context.NONE),
            () -> pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/")))
        );

        assertEquals(ETAG_VALUE, response.getHeaderValue(HttpHeaderName.ETAG.toString()));
    }
}
