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
    private final HttpHeaders adjustedHeaders;

    /**
     * Wraps {@code httpResponse} with a body backed by {@code decodedBody}.
     *
     * <p>{@code Content-Length} is overridden to {@code decodedContentLength} so callers see the size of the bytes
     * they will actually read from the decoded payload, not the larger wire size of the structured message.</p>
     *
     * @param httpResponse The original response from the storage service.
     * @param decodedBody The Flux of CRC-validated, framing-stripped payload bytes produced by the decoder pipeline.
     * @param decodedContentLength The size of the decoded payload that callers will consume.
     */
    DecodedResponse(HttpResponse httpResponse, Flux<ByteBuffer> decodedBody, long decodedContentLength) {
        super(httpResponse.getRequest());
        this.originalResponse = httpResponse;
        this.decodedBody = decodedBody;
        HttpHeaders headers = new HttpHeaders();
        httpResponse.getHeaders().stream().forEach(h -> headers.set(h.getName(), h.getValue()));
        headers.set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(decodedContentLength));
        this.adjustedHeaders = headers;
    }

    @Override
    public int getStatusCode() {
        return originalResponse.getStatusCode();
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getHeaderValue(String name) {
        return adjustedHeaders.getValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return adjustedHeaders;
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
        return getBodyAsByteArray()
            .map(b -> CoreUtils.bomAwareToString(b, originalResponse.getHeaderValue(HttpHeaderName.CONTENT_TYPE)));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return FluxUtil.collectBytesInByteBufferStream(decodedBody).map(b -> new String(b, charset));
    }
}
