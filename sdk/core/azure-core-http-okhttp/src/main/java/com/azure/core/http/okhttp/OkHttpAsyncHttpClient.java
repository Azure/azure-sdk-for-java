// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

/**
 * HttpClient implementation for OkHttp.
 */
class OkHttpAsyncHttpClient implements HttpClient {
    private final ClientLogger logger = new ClientLogger(OkHttpAsyncHttpClient.class);
    private final okhttp3.OkHttpClient httpClient;

    private static final Mono<okio.ByteString> EMPTY_BYTE_STRING_MONO = Mono.just(okio.ByteString.EMPTY);
    private static final okhttp3.MediaType MEDIA_TYPE_OCTET_STREAM =
        okhttp3.MediaType.parse("application/octet-stream");

    OkHttpAsyncHttpClient(okhttp3.OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return Mono.create(sink -> sink.onRequest(value -> {
            // Using MonoSink::onRequest for back pressure support.

            // The blocking behavior toOkHttpRequest(r).subscribe call:
            //
            // The okhttp3.Request emitted by toOkHttpRequest(r) is chained from the body of request Flux<ByteBuffer>:
            //   1. If Flux<ByteBuffer> synchronous and send(r) caller does not apply subscribeOn then
            //      subscribe block on caller thread.
            //   2. If Flux<ByteBuffer> synchronous and send(r) caller apply subscribeOn then
            //      does not block caller thread but block on scheduler thread.
            //   3. If Flux<ByteBuffer> asynchronous then subscribe does not block caller thread
            //      but block on the thread backing flux. This ignore any subscribeOn applied to send(r)
            //
            toOkHttpRequest(request).subscribe(okHttpRequest -> {
                httpClient.newCall(okHttpRequest).enqueue(new OkHttpCallback(sink, request));
            }, sink::error);
        }));
    }

    /**
     * Converts the given azure-core request to okhttp request.
     *
     * @param request the azure-core request
     * @return the Mono emitting okhttp request
     */
    private static Mono<okhttp3.Request> toOkHttpRequest(HttpRequest request) {
        return Mono.just(new okhttp3.Request.Builder())
            .map(rb -> {
                rb.url(request.url());
                if (request.headers() != null) {
                    return rb.headers(okhttp3.Headers.of(request.headers().toMap()));
                } else {
                    return rb.headers(okhttp3.Headers.of(new HashMap<>()));
                }
            })
            .flatMap((Function<Request.Builder, Mono<Request.Builder>>) rb -> {
                if (request.httpMethod() == HttpMethod.GET) {
                    return Mono.just(rb.get());
                } else if (request.httpMethod() == HttpMethod.HEAD) {
                    return Mono.just(rb.head());
                } else {
                    return toOkHttpRequestBody(request.body(), request.headers())
                            .map(requestBody -> rb.method(request.httpMethod().toString(), requestBody));
                }
            })
            .map(rb -> rb.build());
    }

    /**
     * Create a Mono of okhttp3.RequestBody from the given java.nio.ByteBuffer Flux.
     *
     * @param bbFlux stream of java.nio.ByteBuffer representing request content
     * @param headers the headers associated with the original request
     * @return the Mono emitting okhttp3.RequestBody
     */
    private static Mono<okhttp3.RequestBody> toOkHttpRequestBody(Flux<ByteBuffer> bbFlux, HttpHeaders headers) {
        Mono<okio.ByteString> bsMono = bbFlux == null
                ? EMPTY_BYTE_STRING_MONO
                : aggregate(bbFlux);

        return bsMono.map(byteString1 -> {
            String contentType = headers.value("Content-Type");
            if (contentType == null) {
                return RequestBody.create(byteString1, MEDIA_TYPE_OCTET_STREAM);
            } else {
                return RequestBody.create(byteString1, okhttp3.MediaType.parse(contentType));
            }
        });
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

    private static class OkHttpCallback implements okhttp3.Callback {
        private final MonoSink<HttpResponse> sink;
        private final HttpRequest request;

        OkHttpCallback(MonoSink<HttpResponse> sink, HttpRequest request) {
            this.sink = sink;
            this.request = request;
        }

        @Override
        public void onFailure(okhttp3.Call call, IOException e) {
            sink.error(e);
        }

        @Override
        public void onResponse(okhttp3.Call call, okhttp3.Response response) {
            sink.success(new OkHttpResponse(response, request));
        }
    }

    /**
     * An implementation of azure-core HttpResponse for OkHttp.
     */
    private static class OkHttpResponse extends HttpResponse {
        private final okhttp3.Response inner;
        private final HttpHeaders headers;
        private static final int BYTE_BUFFER_CHUNK_SIZE = 1024;

        OkHttpResponse(okhttp3.Response inner, HttpRequest request) {
            Objects.requireNonNull(inner);
            Objects.requireNonNull(request);
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
                return Mono.using(this::responseBody,
                    rb -> {
                        try {
                            String content = rb.string();
                            return content.isEmpty() ? Mono.empty() : Mono.just(content);
                        } catch (IOException ioe) {
                            throw Exceptions.propagate(ioe);
                        }
                    },
                    ResponseBody::close);
            }
        }

        @Override
        public Mono<String> bodyAsString(Charset charset) {
            return bodyAsByteArray()
                    .map(bytes -> new String(bytes, charset));
        }

        @Override
        public void close() {
            final ResponseBody body = this.inner.body();
            if (body != null) {
                body.close();
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
