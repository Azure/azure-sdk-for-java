package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class HttpResponse extends com.azure.core.http.HttpResponse {

    private final HttpHeaders headers;
    private final int responseCode;
    private final byte[] body;

    protected HttpResponse(HttpRequest request, HttpHeaders headers, int responseCode, byte[] body) {
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
     * @param s
     * @deprecated
     */
    @Override
    public String getHeaderValue(String s) {
        return this.headers.get(s).toString();
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.headers;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return null;
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.just(this.body);
    }

    @Override
    public Mono<String> getBodyAsString() {
        return Mono.just(this.body.toString());
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return Mono.just(new String(this.body, charset));
    }
}
