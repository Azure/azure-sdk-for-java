// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import java.net.http.HttpRequest.BodyPublisher;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Flow;

import static java.net.http.HttpResponse.BodyHandlers.*;
import static java.net.http.HttpRequest.BodyPublishers.*;

/**
 * HttpClient implementation for the JDK 11 HttpClient.
 */
class JdkAsyncHttpClient implements HttpClient {
    private final java.net.http.HttpClient jdk11HttpClient;

    private static final Set<String> JDK11_RESTRICTED_HEADERS;
    static {
        TreeSet<String> treeSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        treeSet.addAll(Set.of("connection",
            "content-length",
            "date", // BUG: Java11: https://bugs.openjdk.java.net/browse/JDK-8213189 (Fixed in JDK12)
            "expect",
            "from",
            "host",
            "upgrade",
            "via",
            "warning"));
        JDK11_RESTRICTED_HEADERS = Collections.unmodifiableSet(treeSet);
    }

    JdkAsyncHttpClient(java.net.http.HttpClient httpClient) {
        this.jdk11HttpClient = httpClient;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return toJdk11HttpRequest(request)
            .flatMap(jdk11Request -> Mono.fromCompletionStage(jdk11HttpClient.sendAsync(jdk11Request, ofPublisher()))
                .map(innerResponse -> new Jdk11HttpResponse(request, innerResponse)));
    }

    /**
     * Converts the given azure-core request to the JDK 11 HttpRequest type.
     *
     * @param request the azure-core request
     * @return the Mono emitting HttpRequest
     */
    private static Mono<java.net.http.HttpRequest> toJdk11HttpRequest(HttpRequest request) {
        return Mono.fromCallable(() -> {
            final java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder();
            try {
                builder.uri(request.getUrl().toURI());
            } catch (URISyntaxException e) {
                throw Exceptions.propagate(e);
            }
            final HttpHeaders headers = request.getHeaders();
            if (headers != null) {
                for (HttpHeader header : headers) {
                    final String headerName = header.getName();
                    if (!JDK11_RESTRICTED_HEADERS.contains(headerName)) {
                        final String headerValue = header.getValue();
                        builder.setHeader(headerName, headerValue);
                    }
                }
            }
            switch (request.getHttpMethod()) {
                case GET:
                    return builder.GET().build();
                case HEAD:
                    return builder.method("HEAD", noBody()).build();
                default:
                    final String contentLength = request.getHeaders().getValue("content-length");
                    final BodyPublisher bodyPublisher = toBodyPublisher(request.getBody(), contentLength);
                    return builder.method(request.getHttpMethod().toString(), bodyPublisher).build();
            }
        });
    }

    /**
     * Create BodyPublisher from the given java.nio.ByteBuffer publisher.
     *
     * @param bbPublisher stream of java.nio.ByteBuffer representing request content
     * @return the BodyPublisher
     */
    private static BodyPublisher toBodyPublisher(Flux<ByteBuffer> bbPublisher, String contentLength) {
        if (bbPublisher == null) {
            return noBody();
        }
        final Flow.Publisher<ByteBuffer> bbFlowPublisher = JdkFlowAdapter.publisherToFlowPublisher(bbPublisher);
        if (CoreUtils.isNullOrEmpty(contentLength)) {
            return fromPublisher(bbFlowPublisher);
        } else {
            long contentLengthLong = Long.parseLong(contentLength);
            if (contentLengthLong < 1) {
                return fromPublisher(bbFlowPublisher);
            } else {
                return fromPublisher(bbFlowPublisher, contentLengthLong);
            }
        }
    }

    private static class Jdk11HttpResponse extends HttpResponse {
        private final int statusCode;
        private final HttpHeaders headers;
        private final Flux<ByteBuffer> contentFlux;
        private volatile boolean disposed = false;

        protected Jdk11HttpResponse(final HttpRequest request,
                                    java.net.http.HttpResponse<Flow.Publisher<List<ByteBuffer>>> innerResponse) {
            super(request);
            this.statusCode = innerResponse.statusCode();
            this.headers = fromJdk11HttpHeaders(innerResponse.headers());
            this.contentFlux = JdkFlowAdapter.flowPublisherToFlux(innerResponse.body())
                .flatMapSequential(Flux::fromIterable);
        }

        @Override
        public int getStatusCode() {
            return this.statusCode;
        }

        @Override
        public String getHeaderValue(String name) {
            return this.headers.getValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return this.headers;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return this.contentFlux
                .doFinally(signalType -> disposed = true);
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return FluxUtil.collectBytesInByteBufferStream(getBody())
                .flatMap(bytes -> bytes.length == 0 ? Mono.empty() : Mono.just(bytes));
        }

        @Override
        public Mono<String> getBodyAsString() {
            return getBodyAsByteArray()
                .map(bytes -> new String(bytes));
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return getBodyAsByteArray()
                .map(bytes -> new String(bytes, charset));
        }

        @Override
        public void close() {
            if (!this.disposed) {
                this.contentFlux
                    .subscribe()
                    .dispose();
            }
        }

        private static HttpHeaders fromJdk11HttpHeaders(java.net.http.HttpHeaders headers) {
            final HttpHeaders httpHeaders = new HttpHeaders();
            for (final String key : headers.map().keySet()) {
                for (final String value : headers.allValues(key)) {
                    httpHeaders.put(key, value);
                }
            }
            return httpHeaders;
        }
    }
}
