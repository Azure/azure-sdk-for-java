// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Supplier;

class OkHttpAsyncHttpClient implements HttpClient {
    private final ClientLogger logger = new ClientLogger(OkHttpAsyncHttpClient.class);
    private final okhttp3.OkHttpClient httpClient;
    //
    private final static Mono<okio.ByteString> EMPTY_BYTE_STRING_MONO = Mono.just(okio.ByteString.EMPTY);
    private final static okhttp3.MediaType MEDIA_TYPE_OCTET_STREAM = okhttp3.MediaType.parse("application/octet-stream");

    public OkHttpAsyncHttpClient(okhttp3.OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return Mono.create(sink -> sink.onRequest(value -> {
            // Using MonoSink::onRequest to enable back pressure.
            okhttp3.Request okHttpRequest;
            try {
                okHttpRequest = toOkHttpRequest(request);
            } catch (Throwable t) {
                sink.error(t);
                return;
            }

            okhttp3.Call call = httpClient.newCall(okHttpRequest);
            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    sink.error(e);
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                    sink.success(new OkHttpResponse(response, request));
                }
            });
        }));
    }

    /**
     * Converts the given azure-core request to okhttp request.
     *
     * @param request the azure-core request
     * @return okhttp request
     */
    private static okhttp3.Request toOkHttpRequest(HttpRequest request) {
        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
        requestBuilder.url(request.url());
        if (request.headers() != null) {
            requestBuilder.headers(okhttp3.Headers.of(request.headers().toMap()));
        } else {
            requestBuilder.headers(okhttp3.Headers.of((new HashMap<>())));
        }
        if (request.httpMethod() == HttpMethod.GET) {
            requestBuilder.get();
        } else if (request.httpMethod() == HttpMethod.HEAD) {
            requestBuilder.head();
        } else {
            requestBuilder.method(request.httpMethod().toString(), toOkHttpRequestBody(request.body(), request.headers()));
        }
        return requestBuilder.build();
    }

    /**
     * Create a okhttp3.RequestBody from the given java.nio.ByteBuffer Flux.
     *
     * This method aggregate the Flux and blocks until it finishes.
     *
     *
     * @param bbFlux stream of java.nio.ByteBuffer representing request content
     * @param headers the headers associated with the original request
     * @return the okhttp3.RequestBody
     */
    private static okhttp3.RequestBody toOkHttpRequestBody(Flux<ByteBuffer> bbFlux, HttpHeaders headers) {
        okio.ByteString byteString = okio.ByteString.EMPTY;
        if (bbFlux != null) {
            byteString = aggregate(bbFlux).block();
        }
        String contentType = headers.value("Content-Type");
        if (contentType == null) {
            return okhttp3.RequestBody.create(byteString, MEDIA_TYPE_OCTET_STREAM);
        } else {
            return okhttp3.RequestBody.create(byteString, okhttp3.MediaType.parse(contentType));
        }
    }

    /**
     * Aggregate Flux of java.nio.ByteBuffer to single okio.ByteString.
     *
     * Pooled okio.Buffer type is used to buffer emitted ByteBuffer instances.
     * Content of each ByteBuffer will be written (i.e copied) to the internal okio.Buffer slots.
     * Once the stream terminates, the contents of all slots get copied to one single byte array
     * and okio.ByteString will be created referring this byte array.
     * Finally the initial okio.Buffer will be returned to the pool.
     *
     * @param bbFlux the Flux of ByteBuffer to aggregate
     * @return a mono emitting aggregated ByteString
     */
    private static Mono<okio.ByteString> aggregate(Flux<ByteBuffer> bbFlux) {
        Objects.requireNonNull(bbFlux);
        return Mono.using(okio.Buffer::new,
                buffer -> bbFlux.reduce(buffer, (b, byteBuffer) -> {
                    try {
                        b.write(byteBuffer);
                        return b;
                    } catch (IOException ioe) {
                        throw Exceptions.propagate(ioe);
                    }
                })
                .map(b -> okio.ByteString.of(b.readByteArray())),
                okio.Buffer::clear)
            .switchIfEmpty(EMPTY_BYTE_STRING_MONO);
    }

    @Override
    public HttpClient proxy(Supplier<ProxyOptions> proxyOptions) {
        throw new RuntimeException("Functionality moved to Builder, TODO: remove it, once core HttpClient is fixed");
    }

    @Override
    public HttpClient wiretap(boolean enableWiretap) {
        throw new RuntimeException("Not a valid configuration for OkHttp, TODO: remove it, once core HttpClient is fixed");
    }

    @Override
    public HttpClient port(int port) {
        throw new RuntimeException("Port is property of okhttp3.Request not client, TODO: remove it, once core HttpClient is fixed");
    }

    /**
     * An implementation of azure-core HttpResponse for OkHttp.
     */
    public static class OkHttpResponse extends HttpResponse {
        private final okhttp3.Response inner;
        private final HttpHeaders headers;
        private final static int BYTE_BUFFER_CHUNK_SIZE = 1024;

        public OkHttpResponse(okhttp3.Response inner, HttpRequest request) {
            this.inner = inner;
            this.headers = fromOkHttpHeaders(this.inner.headers());
            super.request(request);
        }

        @Override
        public int statusCode() {
            return this.inner.code();
        }

        @Override
        public String headerValue(String name) {
            return this.headers.value(name);
        }

        @Override
        public HttpHeaders headers() {
            return this.headers;
        }

        @Override
        public Flux<ByteBuffer> body() {
            return this.responseBody() != null
                    ? toFluxByteBuffer(this.responseBody().byteStream())
                    : Flux.empty();
        }

        @Override
        public Mono<byte[]> bodyAsByteArray() {
            if (this.responseBody() == null) {
                return Mono.empty();
            } else {
                return Mono.using(() -> this.responseBody(),
                        rb -> {
                            try {
                                byte[] content = rb.bytes();
                                return content.length == 0 ? Mono.empty() : Mono.just(content);
                            } catch (IOException ioe) {
                                throw Exceptions.propagate(ioe);
                            }
                        },
                        rb -> rb.close());
            }
        }

        @Override
        public Mono<String> bodyAsString() {
            if (this.responseBody() == null) {
                return Mono.empty();
            } else {
                return Mono.using(() -> this.responseBody(),
                        rb -> {
                            try {
                                String content = rb.string();
                                return content.length() == 0 ? Mono.empty() : Mono.just(content);
                            } catch (IOException ioe) {
                                throw Exceptions.propagate(ioe);
                            }
                        },
                        rb -> rb.close());
            }
        }

        @Override
        public Mono<String> bodyAsString(Charset charset) {
            return bodyAsByteArray()
                    .map(bytes -> new String(bytes, charset));
        }

        @Override
        public void close() {
            if (this.inner.body() != null) {
                this.inner.body().close();
            }
        }

        private okhttp3.ResponseBody responseBody() {
            return this.inner.body();
        }

        /**
         * Creates azure-core HttpHeaders from okhttp headers.
         *
         * @param headers okhttp headers
         * @return azure-core HttpHeaders
         */
        private static HttpHeaders fromOkHttpHeaders(okhttp3.Headers headers) {
            HttpHeaders httpHeaders = new HttpHeaders();
            for (String headerName : headers.names()) {
                httpHeaders.put(headerName, headers.get(headerName));
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
            return Flux.using(() -> inputStream,
                    // Read input stream chunk by chunk and emit each as java.nio.ByteBuffer
                    is -> Flux.just(true)
                            .repeat()
                            .map(ignore -> {
                                byte[] buffer = new byte[BYTE_BUFFER_CHUNK_SIZE];
                                try {
                                    int numBytes = is.read(buffer);
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
                            .map(p -> p.buffer()),
                    // Resource cleanup
                    // https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response-body/#the-response-body-must-be-closed
                    is -> {
                        try {
                            is.close();
                        } catch (IOException ioe) {
                            throw Exceptions.propagate(ioe);
                        }
                    }
            );
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
