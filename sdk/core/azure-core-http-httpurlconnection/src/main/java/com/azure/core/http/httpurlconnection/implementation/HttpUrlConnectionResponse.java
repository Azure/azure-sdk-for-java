package com.azure.core.http.httpurlconnection.implementation;

import com.azure.core.http.*;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public final class HttpUrlConnectionResponse extends HttpResponse {

    private final HttpHeaders headers;
    private final int responseCode;
    private final byte[] body;

    public HttpUrlConnectionResponse(HttpRequest request, HttpHeaders headers, int responseCode, byte[] body) {
        super(request);
        this.headers = headers;
        this.responseCode = responseCode;
        this.body = body;
    }

    @Override
    public int getStatusCode() {
        return this.responseCode;
    }

    @Override
    @Deprecated
    public String getHeaderValue(String s) {
        HttpHeader header = this.headers.get(s);
        return header != null ? header.getValue() : null;
    }

    @Override
    public String getHeaderValue(HttpHeaderName headerName) {
        return headers.getValue(headerName);
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.headers;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        if (this.body.length == 0) {
            return Flux.empty();
        }
        return Flux.just(ByteBuffer.wrap(this.body));
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return FluxUtil.collectBytesFromNetworkResponse(getBody(), getHeaders())
            // Map empty byte[] into Mono.empty, this matches how the other HttpResponse implementations handle this.
            .flatMap(bytes -> (bytes == null || bytes.length == 0)
                ? Mono.empty()
                : Mono.just(bytes));
    }

    @Override
    public Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(bytes -> CoreUtils.bomAwareToString(bytes,
            getHeaderValue(HttpHeaderName.CONTENT_TYPE)));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }
}
