// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty.implementation;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * Implementation of HttpResponse for Netty synchronous HTTP client.
 */
public class NettyResponse extends Response<BinaryData> {
    private final FullHttpResponse nettyResponse;

    /**
     * Creates an instance of {@link NettyResponse}.
     *
     * @param body The response body.
     * @param statusCode The response status code.
     * @param request The request that resulted in the response.
     * @param nettyResponse The Netty HttpResponse.
     */
    public NettyResponse(BinaryData body, int statusCode, HttpRequest request, FullHttpResponse nettyResponse) {
        super(request, statusCode, convertHeaders(nettyResponse.headers()), body);
        this.nettyResponse = nettyResponse;
    }

    /**
     * Converts Netty HttpHeaders to core HttpHeaders.
     *
     * @param nettyHeaders Netty HttpHeaders.
     * @return Converted core HttpHeaders.
     */
    private static HttpHeaders convertHeaders(io.netty.handler.codec.http.HttpHeaders nettyHeaders) {
        HttpHeaders coreHeaders = new HttpHeaders();
        for (String name : nettyHeaders.names()) {
            List<String> values = nettyHeaders.getAll(name);
            coreHeaders.add(new HttpHeader(HttpHeaderName.fromString(name), values));
        }
        return coreHeaders;
    }

    @Override
    public void close() {
        // TODO (alzimmer): Need to manage reference counting here better to prevent possible release when refCount
        //  has already reached 0.
        if (nettyResponse != null) {
            ReferenceCountUtil.safeRelease(nettyResponse);
        }
    }
}
