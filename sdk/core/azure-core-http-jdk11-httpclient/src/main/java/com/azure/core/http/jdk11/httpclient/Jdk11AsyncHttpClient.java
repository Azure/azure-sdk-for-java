// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk11.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

import java.net.http.HttpRequest.BodyPublisher;
import java.util.concurrent.Flow;

import static java.net.http.HttpResponse.BodyHandlers.*;
import static java.net.http.HttpRequest.BodyPublishers.*;
import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;

/**
 * HttpClient implementation for the JDK 11 HttpClient.
 */
class Jdk11AsyncHttpClient implements HttpClient {
    private final java.net.http.HttpClient jdk11HttpClient;

    private static final Mono<BodyPublisher> EMPTY_BODY_PUBLISHER_MONO = Mono.just(noBody());

    Jdk11AsyncHttpClient(java.net.http.HttpClient httpClient) {
        this.jdk11HttpClient = httpClient;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        // FIXME there used to be a MonoSink here - should we use that approach here too?
        return toJdk11HttpRequest(request)
            .map(jdk11Request -> {
                switch (request.getHttpMethod()) {
                    case HEAD: {
                        return jdk11HttpClient.sendAsync(jdk11Request, discarding())
                                   .thenApply(response -> Jdk11HttpResponse.fromVoidResponse(response, request));
                    }
                    default: {
                        // FIXME we might get better performance with `ofPublisher` here
                        return jdk11HttpClient.sendAsync(jdk11Request, ofInputStream())
                                                   .thenApply(response -> Jdk11HttpResponse.fromInputStreamResponse(response, request));
                    }
                }
            }).map(CompletableFuture::join); // FIXME
    }

    /**
     * Converts the given azure-core request to the JDK 11 HttpRequest type.
     *
     * @param request the azure-core request
     * @return the Mono emitting HttpRequest
     */
    private static Mono<java.net.http.HttpRequest> toJdk11HttpRequest(HttpRequest request) {
        return Mono.just(java.net.http.HttpRequest.newBuilder())
            .map(builder -> {
                try {
                    builder.uri(request.getUrl().toURI());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                if (request.getHeaders() != null) {
                    for (HttpHeader hdr : request.getHeaders()) {
                        // the HttpClient spec says some implementations will not allow certain headers, such as
                        // content-length, so we need to filter them out here
                        switch (hdr.getName().toLowerCase()) {
                            case "content-length": continue;
                            default: {
                                if (hdr.getValue() != null) {
                                    builder.header(hdr.getName(), hdr.getValue());
                                }
                            }
                        }
                    }
                }
                return builder;
            })
            .flatMap(builder -> {
                if (request.getHttpMethod() == HttpMethod.GET) {
                    return Mono.just(builder.GET());
                } else if (request.getHttpMethod() == HttpMethod.HEAD) {
                    return Mono.just(builder.method("HEAD", noBody()));
                } else {
                    return toJdk11HttpRequestBody(request.getBody(), request.getHeaders())
                               .map(requestBody -> builder.method(request.getHttpMethod().toString(), requestBody));
                }
            })
            .map(java.net.http.HttpRequest.Builder::build);
    }

    /**
     * Create a Mono of BodyPublisher from the given java.nio.ByteBuffer Flux.
     *
     * @param bbFlux stream of java.nio.ByteBuffer representing request content
     * @return the Mono emitting BodyPublisher
     */
    private static Mono<BodyPublisher> toJdk11HttpRequestBody(Flux<ByteBuffer> bbFlux, HttpHeaders headers) {
        if (bbFlux == null) {
            return EMPTY_BODY_PUBLISHER_MONO;
        }

        return Mono.defer(() -> {
            final Flow.Publisher pub =JdkFlowAdapter.publisherToFlowPublisher(bbFlux);
            final String contentLengthStr = headers.getValue("content-length");
            if (contentLengthStr != null && !contentLengthStr.isEmpty()) {
                long contentLength = Long.parseLong(contentLengthStr);
                return Mono.just(fromPublisher(pub, contentLength));
            } else {
                return Mono.just(fromPublisher(pub));
            }
        });
    }

    /**
     * An implementation of {@link HttpResponse} for JDK 11 HttpResponse.
     */
    private static class Jdk11HttpResponse extends HttpResponse {
        private int statusCode;
        private HttpHeaders headers;
        private Mono<InputStream> responseBodyMono;
        private static final int BYTE_BUFFER_CHUNK_SIZE = 4096;

        private Jdk11HttpResponse(HttpRequest request) {
            super(request);
        }

        static Jdk11HttpResponse fromVoidResponse(java.net.http.HttpResponse<Void> innerResponse, HttpRequest request) {
            Jdk11HttpResponse response = new Jdk11HttpResponse(request);
            response.statusCode = innerResponse.statusCode();
            response.headers = fromJdk11HttpHeaders(innerResponse.headers());
            response.responseBodyMono = Mono.empty();
            return response;
        }

        static Jdk11HttpResponse fromInputStreamResponse(java.net.http.HttpResponse<InputStream> innerResponse, HttpRequest request) {
            Jdk11HttpResponse response = new Jdk11HttpResponse(request);

            response.statusCode = innerResponse.statusCode();
            response.headers = fromJdk11HttpHeaders(innerResponse.headers());
            if (innerResponse.body() == null) {
                response.responseBodyMono = Mono.empty();
            } else {
                response.responseBodyMono = Mono.using(innerResponse::body,
                    Mono::just,
                    inputStream -> {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }, /* Change in behavior since reactor-core 3.3.0.RELEASE */ false);
            }
            return response;
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
            return this.responseBodyMono
                .flatMapMany(Jdk11HttpResponse::toFluxByteBuffer);
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return this.responseBodyMono
                .flatMap(rb -> {
                    try {
                        // FIXME
                        byte[] content = rb.readAllBytes();
                        return content.length == 0 ? Mono.empty() : Mono.just(content);
                    } catch (IOException ioe) {
                        throw Exceptions.propagate(ioe);
                    }
                });
        }

        @Override
        public Mono<String> getBodyAsString() {
            return this.responseBodyMono
                .flatMap(rb -> {
                    try {
                        // FIXME
                        String content = new String(rb.readAllBytes());
                        return content.isEmpty() ? Mono.empty() : Mono.just(content);
                    } catch (final IOException ioe) {
                        throw Exceptions.propagate(ioe);
                    }
                });
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return getBodyAsByteArray()
                .map(bytes -> new String(bytes, charset));
        }

        @Override
        public void close() {
            this.responseBodyMono.subscribe().dispose();
        }

        /**
         * Creates azure-core HttpHeaders from JDK 11 HttpHeaders.
         *
         * @param headers JDK 11 HttpHeaders
         * @return azure-core HttpHeaders
         */
        private static HttpHeaders fromJdk11HttpHeaders(java.net.http.HttpHeaders headers) {
            final HttpHeaders httpHeaders = new HttpHeaders();
            for (final String key : headers.map().keySet()) {
                for (final String value : headers.allValues(key)) {
                    httpHeaders.put(key, value);
                }
            }
            return httpHeaders;
        }

        /**
         * Creates a Flux of ByteBuffer, with each ByteBuffer wrapping bytes read from the given
         * InputStream.
         *
         * @param inputStream InputStream to back the Flux
         * @return Flux of ByteBuffer backed by the InputStream
         */
        private static Flux<ByteBuffer> toFluxByteBuffer(InputStream inputStream) {
            Pair pair = new Pair();
            return Flux.just(true)
                .repeat()
                .map(ignore -> {
                    byte[] buffer = new byte[BYTE_BUFFER_CHUNK_SIZE];
                    try {
                        int numBytes = inputStream.read(buffer);
                        if (numBytes > 0) {
                            return pair.buffer(ByteBuffer.wrap(buffer, 0, numBytes)).readBytes(numBytes);
                        } else {
                            return pair.buffer(null).readBytes(numBytes);
                        }
                    } catch (IOException ioe) {
                        throw Exceptions.propagate(ioe);
                    }
                })
                .takeUntil(p -> p.readBytes() == -1)
                .filter(p -> p.readBytes() > 0)
                .map(Pair::buffer);
        }

        private static class Pair {
            private ByteBuffer byteBuffer;
            private int readBytes;

            ByteBuffer buffer() {
                return this.byteBuffer;
            }

            int readBytes() {
                return this.readBytes;
            }

            Pair buffer(ByteBuffer byteBuffer) {
                this.byteBuffer = byteBuffer;
                return this;
            }

            Pair readBytes(int cnt) {
                this.readBytes = cnt;
                return this;
            }
        }
    }
}
