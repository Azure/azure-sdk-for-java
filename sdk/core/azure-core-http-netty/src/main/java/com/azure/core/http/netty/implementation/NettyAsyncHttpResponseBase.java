// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.netty.http.client.HttpClientResponse;

/**
 * Base response class for Reactor Netty with implementations for response metadata.
 */
public abstract class NettyAsyncHttpResponseBase extends HttpResponse {
    private final HttpClientResponse reactorNettyResponse;

    // We use a wrapper for the Netty-returned headers, so we are not forced to pay up-front the cost of converting
    // from Netty HttpHeaders to azure-core HttpHeaders. Instead, by wrapping it, there is no cost to pay as we map
    // the Netty HttpHeaders API into the azure-core HttpHeaders API.
    private NettyToAzureCoreHttpHeadersWrapper headers;
    private HttpHeaders convertedHeaders;

    NettyAsyncHttpResponseBase(HttpClientResponse reactorNettyResponse, HttpRequest httpRequest,
        boolean headersEagerlyConverted) {
        super(httpRequest);
        this.reactorNettyResponse = reactorNettyResponse;

        if (headersEagerlyConverted) {
            io.netty.handler.codec.http.HttpHeaders nettyHeaders = reactorNettyResponse.responseHeaders();
            convertedHeaders = new HttpHeaders((int) (nettyHeaders.size() / 0.75F));
            reactorNettyResponse.responseHeaders().forEach(header ->
                convertedHeaders.add(header.getKey(), header.getValue()));
        }
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
        if (headers == null) {
            headers = new NettyToAzureCoreHttpHeadersWrapper(reactorNettyResponse.responseHeaders());
        }
        return headers;
    }
}
