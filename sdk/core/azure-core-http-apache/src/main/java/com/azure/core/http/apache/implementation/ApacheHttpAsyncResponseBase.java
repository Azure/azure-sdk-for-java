// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.apache.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Base response class for Apache Http with implementations for response metadata.
 */
abstract class ApacheHttpAsyncResponseBase extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;

    /**
     * Creates an instance of {@link HttpResponse}.
     *
     * @param request The {@link HttpRequest} that resulted in this {@link HttpResponse}.
     */
    protected ApacheHttpAsyncResponseBase(
        Message<org.apache.hc.core5.http.HttpResponse, Publisher<ByteBuffer>> response, HttpRequest request) {
        super(request);
        this.statusCode = response.getHead().getCode();
        this.headers = fromApacheHttpHeaders(response.getHead().getHeaders());
    }

    @Override
    public final int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public final String getHeaderValue(String name) {
        return this.headers.getValue(name);
    }

    @Override
    public final HttpHeaders getHeaders() {
        return this.headers;
    }

    @Override
    public final Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(bytes -> CoreUtils.bomAwareToString(bytes, getHeaderValue("Content-Type")));
    }

    @Override
    public final Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }

    /**
     * Creates azure-core HttpHeaders from Apache Http headers.
     *
     * @param apacheHttpHeaders apache http headers
     * @return azure-core HttpHeaders
     */
    private static HttpHeaders fromApacheHttpHeaders(Header[] apacheHttpHeaders) {
        HttpHeaders azureHeaders = new HttpHeaders();
        for (Header header : apacheHttpHeaders) {
            azureHeaders.set(header.getName(), header.getValue());
        }
        return azureHeaders;
    }
}
