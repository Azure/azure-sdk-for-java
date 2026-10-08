// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.handler.codec.http2.DefaultHttp2HeadersDecoder;
import io.netty.handler.codec.http2.Http2Headers;

final class CosmosHttp2HeadersDecoder extends DefaultHttp2HeadersDecoder {
    CosmosHttp2HeadersDecoder(long maxHeaderListSize) {
        this(true, maxHeaderListSize);
    }

    CosmosHttp2HeadersDecoder(boolean validateHeaders, long maxHeaderListSize) {
        super(validateHeaders, validateHeaders, maxHeaderListSize);
    }

    @Override
    protected Http2Headers newHeaders() {
        return new CosmosHttp2Headers(validateHeaders(), validateHeaderValues(), numberOfHeadersGuess());
    }
}
