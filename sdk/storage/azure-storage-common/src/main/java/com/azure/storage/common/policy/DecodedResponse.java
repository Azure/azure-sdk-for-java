// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Decoded HTTP response that wraps the original response with a decoded body stream.
 */
class DecodedResponse extends HttpResponse {
    private final Flux<ByteBuffer> decodedBody;
    private final HttpHeaders httpHeaders;
    private final int statusCode;

    DecodedResponse(HttpResponse httpResponse, Flux<ByteBuffer> decodedBody) {
        super(httpResponse.getRequest());
        this.decodedBody = decodedBody;
        this.statusCode = httpResponse.getStatusCode();
        this.httpHeaders = httpResponse.getHeaders();
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getHeaderValue(String name) {
        return httpHeaders.getValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return httpHeaders;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return decodedBody;
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return FluxUtil.collectBytesInByteBufferStream(decodedBody);
    }

    @Override
    public Mono<String> getBodyAsString() {
        return FluxUtil.collectBytesInByteBufferStream(decodedBody).map(String::new);
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return FluxUtil.collectBytesInByteBufferStream(decodedBody).map(b -> new String(b, charset));
    }
}
