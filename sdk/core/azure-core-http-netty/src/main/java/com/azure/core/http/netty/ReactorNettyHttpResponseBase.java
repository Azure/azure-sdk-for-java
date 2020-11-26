// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.netty.http.client.HttpClientResponse;

/**
 * Base response class for Reactor Netty with implementations for response metadata.
 */
abstract class ReactorNettyHttpResponseBase extends HttpResponse {
    private final HttpClientResponse reactorNettyResponse;

    ReactorNettyHttpResponseBase(HttpClientResponse reactorNettyResponse, HttpRequest httpRequest) {
        super(httpRequest);
        this.reactorNettyResponse = reactorNettyResponse;
    }

    @Override
    public final int getStatusCode() {
        return reactorNettyResponse.status().code();
    }

    @Override
    public final String getHeaderValue(String name) {
        return reactorNettyResponse.responseHeaders().get(name);
    }

    @Override
    public final HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        reactorNettyResponse.responseHeaders().forEach(e -> headers.put(e.getKey(), e.getValue()));
        return headers;
    }
}
