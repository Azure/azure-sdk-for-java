package com.azure.core.http.httpurlconnection.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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

    /**
     * @deprecated
     */
    @Override
    public String getHeaderValue(String s) {
        HttpHeader header = this.headers.get(s);
        return header != null ? header.getValue() : null;
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
        return Mono.just(this.body);
    }

    @Override
    public Mono<String> getBodyAsString() {
        return getBodyAsString(StandardCharsets.UTF_8);
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return Mono.just(new String(this.body, charset));
    }
}
