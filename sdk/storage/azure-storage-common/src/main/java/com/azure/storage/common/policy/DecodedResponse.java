// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * {@link HttpResponse} wrapper that exposes a decoded body stream while preserving the request, status code, and
 * headers of the original response.
 *
 * <p>The policy hands this class a Flux that already represents validated, framing-stripped bytes (produced by the
 * decoder pipeline). This class's only job is to make that Flux look like the body of the original
 * {@link HttpResponse}. Status code, headers, and request remain identical to the underlying response so callers
 * cannot distinguish a validated download from a normal one – the validation is transparent.</p>
 */
class DecodedResponse extends HttpResponse {
    private final HttpResponse originalResponse;
    private final Flux<ByteBuffer> decodedBody;

    /**
     * Wraps {@code httpResponse} with a body backed by {@code decodedBody}.
     *
     * @param httpResponse The original response from the storage service. Its request, status code, and headers
     * are preserved verbatim.
     * @param decodedBody The Flux of CRC-validated, framing-stripped payload bytes produced by the decoder
     * pipeline.
     */
    DecodedResponse(HttpResponse httpResponse, Flux<ByteBuffer> decodedBody) {
        super(httpResponse.getRequest());
        this.originalResponse = httpResponse;
        this.decodedBody = decodedBody;
    }

    @Override
    public int getStatusCode() {
        return originalResponse.getStatusCode();
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getHeaderValue(String name) {
        return originalResponse.getHeaderValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return originalResponse.getHeaders();
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
        return getBodyAsByteArray().map(b -> CoreUtils.bomAwareToString(b, originalResponse.getHeaderValue(HttpHeaderName.CONTENT_TYPE)));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return FluxUtil.collectBytesInByteBufferStream(decodedBody).map(b -> new String(b, charset));
    }
}
