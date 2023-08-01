package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class HttpResponse extends com.azure.core.http.HttpResponse {


    protected HttpResponse(HttpRequest request) {
        super(request);
    }

    @Override
    public int getStatusCode() {
        return 0;
    }

    /**
     * @param s
     * @deprecated
     */
    @Override
    public String getHeaderValue(String s) {
        return null;
    }

    @Override
    public HttpHeaders getHeaders() {
        return null;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return null;
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return null;
    }

    @Override
    public Mono<String> getBodyAsString() {
        return null;
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return null;
    }
}
