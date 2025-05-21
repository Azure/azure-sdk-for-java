// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.reactor.netty.implementation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.netty.util.AsciiString;
import reactor.netty.http.client.HttpClientResponse;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Base response class for Reactor Netty with implementations for response metadata.
 */
public abstract class ReactorNettyAsyncHttpResponseBase extends Response<BinaryData> {
    protected final HttpClientResponse reactorNettyResponse;

    // We use a wrapper for the Netty-returned headers, so we are not forced to pay up-front the cost of converting
    // from Netty HttpHeaders to azure-core HttpHeaders. Instead, by wrapping it, there is no cost to pay as we map
    // the Netty HttpHeaders API into the azure-core HttpHeaders API.
    // private NettyToAzureCoreHttpHeadersWrapper headers;

    @SuppressWarnings("deprecation")
    ReactorNettyAsyncHttpResponseBase(HttpClientResponse reactorNettyResponse, HttpRequest httpRequest,
                                      boolean headersEagerlyConverted) {
        super(httpRequest, reactorNettyResponse.status().code(),
            convertNettyHeaders(reactorNettyResponse, headersEagerlyConverted), null);
        this.reactorNettyResponse = reactorNettyResponse;
    }

    private static HttpHeaders convertNettyHeaders(HttpClientResponse reactorNettyResponse,
        boolean headersEagerlyConverted) {
        // Retain a reference to the Netty HttpHeaders as Reactor Netty uses a volatile field to hold the value.
        io.netty.handler.codec.http.HttpHeaders nettyHeaders = reactorNettyResponse.responseHeaders();
        HttpHeaders httpHeaders;
        if (headersEagerlyConverted) {
            httpHeaders = new HttpHeaders((int) (nettyHeaders.size() / 0.75F));
            // Use iteratorCharSequence as iterator has overhead converting to Map.Entry<String, String> in the common
            // case and this can be handled here instead.
            Iterator<Map.Entry<CharSequence, CharSequence>> nettyHeadersIterator = nettyHeaders.iteratorCharSequence();
            while (nettyHeadersIterator.hasNext()) {
                Map.Entry<CharSequence, CharSequence> next = nettyHeadersIterator.next();
                // Value may be null and that needs to be guarded but key should never be null.
                String value = Objects.toString(next.getValue(), null);
                CharSequence key = next.getKey();

                // Check for the header name being a Netty AsciiString as it has optimizations around lowercasing.
                if (key instanceof AsciiString) {
                    // Hook into optimizations exposed through shared implementation to speed up the conversion.
                    AsciiString asciiString = (AsciiString) key;
                    HttpHeadersAccessHelper.addInternal(httpHeaders, asciiString.toLowerCase().toString(),
                        asciiString.toString(), value);
                } else {
                    // If it isn't an AsciiString, then fallback to the shared, albeit, slower path.
                    httpHeaders.add(HttpHeaderName.fromString(key.toString()), value);
                }
            }
        } else {
            httpHeaders = new ReactorNettyToClientCoreHttpHeadersWrapper(nettyHeaders);
        }
        return httpHeaders;
    }

    public final String getHeaderValue(HttpHeaderName headerName) {
        return getHeaders().getValue(headerName);
    }

}
