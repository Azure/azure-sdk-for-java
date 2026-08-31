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
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Mono;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StorageEtagPolicyTest {
    private static final String ETAG_VALUE = "Dummy-Etag";

    @SyncAsyncTest
    public void scrubEtagDoNothing() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaderName.ETAG, ETAG_VALUE);
        HttpResponse mockResponse = new MockHttpResponse(null, 200, headers);

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(mockResponse);
            }
        }).policies(new StorageEtagPolicy()).build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> pipeline.sendSync(new HttpRequest(HttpMethod.GET, new URL("http://localhost/")), Context.NONE),
            () -> pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"))));

        assertEquals(ETAG_VALUE, response.getHeaderValue(HttpHeaderName.ETAG.toString()));
    }

    @SyncAsyncTest
    public void scrubEtagWithQuotes() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaderName.ETAG, String.format("\"%s\"", ETAG_VALUE));
        HttpResponse mockResponse = new MockHttpResponse(null, 200, headers);

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(mockResponse);
            }
        }).policies(new StorageEtagPolicy()).build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> pipeline.sendSync(new HttpRequest(HttpMethod.GET, new URL("http://localhost/")), Context.NONE),
            () -> pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/"))));

        assertEquals(ETAG_VALUE, response.getHeaderValue(HttpHeaderName.ETAG.toString()));
    }

    @SyncAsyncTest
    public void normalizeRequestEtagsAndPreserveResponseFormat() {
        final HttpHeaders responseHeaders = new HttpHeaders().set(HttpHeaderName.ETAG, "\"response-etag\"");
        HttpResponse mockResponse = new MockHttpResponse(null, 200, responseHeaders);

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                HttpHeaders requestHeaders = request.getHeaders();
                assertEquals("\"if-match\"", requestHeaders.getValue(HttpHeaderName.IF_MATCH));
                assertEquals("W/\"if-none-match\"", requestHeaders.getValue(HttpHeaderName.IF_NONE_MATCH));
                assertEquals("\"source-if-match\"", requestHeaders.getValue(Constants.HeaderConstants.SOURCE_IF_MATCH));
                assertEquals("*", requestHeaders.getValue(Constants.HeaderConstants.SOURCE_IF_NONE_MATCH));
                assertEquals("\"blob-if-match\"", requestHeaders.getValue(Constants.HeaderConstants.BLOB_IF_MATCH));
                assertEquals("\"\"", requestHeaders.getValue(Constants.HeaderConstants.BLOB_IF_NONE_MATCH));
                return Mono.just(mockResponse);
            }
        }).policies(new StorageEtagPolicy()).build();

        HttpResponse response
            = SyncAsyncExtension.execute(() -> pipeline.sendSync(createConditionalRequest(), Context.NONE),
                () -> pipeline.send(createConditionalRequest(), Context.NONE));

        assertEquals("response-etag", response.getHeaderValue(HttpHeaderName.ETAG));
    }

    @SyncAsyncTest
    public void legacyPolicyRetainsResponseOnlyBehavior() {
        final HttpHeaders responseHeaders = new HttpHeaders().set(HttpHeaderName.ETAG, "\"response-etag\"");
        HttpResponse mockResponse = new MockHttpResponse(null, 200, responseHeaders);

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertEquals("if-match", request.getHeaders().getValue(HttpHeaderName.IF_MATCH));
                return Mono.just(mockResponse);
            }
        }).policies(new ScrubEtagPolicy()).build();

        HttpResponse response
            = SyncAsyncExtension.execute(() -> pipeline.sendSync(createConditionalRequest(), Context.NONE),
                () -> pipeline.send(createConditionalRequest(), Context.NONE));

        assertEquals("response-etag", response.getHeaderValue(HttpHeaderName.ETAG));
    }

    private static HttpRequest createConditionalRequest() {
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost/");
        request.getHeaders()
            .set(HttpHeaderName.IF_MATCH, "if-match")
            .set(HttpHeaderName.IF_NONE_MATCH, "W/\"if-none-match\"")
            .set(Constants.HeaderConstants.SOURCE_IF_MATCH, "source-if-match")
            .set(Constants.HeaderConstants.SOURCE_IF_NONE_MATCH, "*")
            .set(Constants.HeaderConstants.BLOB_IF_MATCH, "blob-if-match")
            .set(Constants.HeaderConstants.BLOB_IF_NONE_MATCH, "");
        return request;
    }
}
