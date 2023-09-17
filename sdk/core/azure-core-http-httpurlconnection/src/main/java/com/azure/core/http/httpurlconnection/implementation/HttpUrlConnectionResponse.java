package com.azure.core.http.httpurlconnection.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpUrlConnectionResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;
    private Flux<ByteBuffer> body;
    private List<ByteBuffer> bufferedBody;

    public HttpUrlConnectionResponse(HttpRequest request, int statusCode, Map<String, List<String>> headers, Flux<ByteBuffer> body) {
        super(request);
        this.statusCode = statusCode;
        this.headers = new HttpHeaders();
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            for (String headerValue : header.getValue()) {
                this.headers.add(header.getKey(), headerValue);
            }
        }
        this.body = body;
        this.bufferedBody = new ArrayList<>();
    }

    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    @Deprecated
    public final String getHeaderValue(String name) {
        return this.headers.getValue(name);
    }

    @Override
    public String getHeaderValue(HttpHeaderName name) {
        return headers.getValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.headers;
    }

    @Override
    public final Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(bytes -> CoreUtils.bomAwareToString(bytes,
            getHeaderValue(HttpHeaderName.CONTENT_TYPE)));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
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
    public Flux<ByteBuffer> getBody() {
        if (bufferedBody.isEmpty()) {
            return body.doOnNext(bufferedBody::add).doOnTerminate(() -> body = Flux.fromIterable(bufferedBody));
        } else {
            return Flux.fromIterable(bufferedBody);
        }
    }

    public HttpResponse buffer() {
        return this;
    }
}
