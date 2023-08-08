// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.netty.http.client.HttpClientResponse;

import java.util.Iterator;
import java.util.Map;

/**
 * Base response class for Reactor Netty with implementations for response metadata.
 */
public abstract class NettyAsyncHttpResponseBase extends HttpResponse {
    private final HttpClientResponse reactorNettyResponse;

    // We use a wrapper for the Netty-returned headers, so we are not forced to pay up-front the cost of converting
    // from Netty HttpHeaders to azure-core HttpHeaders. Instead, by wrapping it, there is no cost to pay as we map
    // the Netty HttpHeaders API into the azure-core HttpHeaders API.
    //private NettyToAzureCoreHttpHeadersWrapper headers;
    private final HttpHeaders headers;

    @SuppressWarnings("deprecation")
    NettyAsyncHttpResponseBase(HttpClientResponse reactorNettyResponse, HttpRequest httpRequest,
        boolean headersEagerlyConverted) {
        super(httpRequest);
        this.reactorNettyResponse = reactorNettyResponse;

        // Retain a reference to the Netty HttpHeaders as Reactor Netty uses a volatile field to hold the value.
        io.netty.handler.codec.http.HttpHeaders nettyHeaders = reactorNettyResponse.responseHeaders();
        if (headersEagerlyConverted) {
            this.headers = new HttpHeaders((int) (nettyHeaders.size() / 0.75F));
            // Use iteratorCharSequence as iterator has overhead converting to Map.Entry<String, String> in the common
            // case and this can be handled here instead.
            Iterator<Map.Entry<CharSequence, CharSequence>> nettyHeadersIterator = nettyHeaders.iteratorCharSequence();
            while (nettyHeadersIterator.hasNext()) {
                Map.Entry<CharSequence, CharSequence> next = nettyHeadersIterator.next();
                // Value may be null and that needs to be guarded but key should never be null.
                CharSequence value = next.getValue();
                this.headers.add(next.getKey().toString(), (value == null) ? null : value.toString());
            }
        } else {
            this.headers = new NettyToAzureCoreHttpHeadersWrapper(nettyHeaders);
        }
    }

    @Override
    public final int getStatusCode() {
        return reactorNettyResponse.status().code();
    }

    @Override
    @Deprecated
    public final String getHeaderValue(String name) {
        return headers.getValue(name);
    }

    @Override
    public final String getHeaderValue(HttpHeaderName headerName) {
        return headers.getValue(headerName);
    }

    @Override
    public final HttpHeaders getHeaders() {
        return headers;
    }
}
