// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.ByteString;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * HttpClient implementation for OkHttp.
 */
class OkHttpAsyncHttpClient implements HttpClient {
    private final OkHttpClient httpClient;
    //
    private static final Mono<okio.ByteString> EMPTY_BYTE_STRING_MONO = Mono.just(okio.ByteString.EMPTY);
    private static final MediaType MEDIA_TYPE_OCTET_STREAM = MediaType.parse("application/octet-stream");

    OkHttpAsyncHttpClient(OkHttpClient httpClient) {
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
                Call call = httpClient.newCall(okHttpRequest);
                call.enqueue(new OkHttpCallback(sink, request));
                sink.onCancel(() -> call.cancel());
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
                rb.url(request.getUrl());
                if (request.getHeaders() != null) {
                    Map<String, String> headers = new HashMap<>();
                    for (HttpHeader hdr : request.getHeaders()) {
                        if (hdr.getValue() != null) {
                            headers.put(hdr.getName(), hdr.getValue());
                        }
                    }
                    return rb.headers(okhttp3.Headers.of(headers));
                } else {
                    return rb.headers(okhttp3.Headers.of(new HashMap<>()));
                }
            })
            .flatMap((Function<Request.Builder, Mono<Request.Builder>>) rb -> {
                if (request.getHttpMethod() == HttpMethod.GET) {
                    return Mono.just(rb.get());
                } else if (request.getHttpMethod() == HttpMethod.HEAD) {
                    return Mono.just(rb.head());
                } else {
                    return toOkHttpRequestBody(request.getBody(), request.getHeaders())
                            .map(requestBody -> rb.method(request.getHttpMethod().toString(), requestBody));
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
    private static Mono<RequestBody> toOkHttpRequestBody(Flux<ByteBuffer> bbFlux, HttpHeaders headers) {
        Mono<okio.ByteString> bsMono = bbFlux == null
            ? EMPTY_BYTE_STRING_MONO
            : toByteString(bbFlux);

        return bsMono.map(bs -> {
            String contentType = headers.getValue("Content-Type");
            if (contentType == null) {
                return RequestBody.create(bs, MEDIA_TYPE_OCTET_STREAM);
            } else {
                return RequestBody.create(bs, MediaType.parse(contentType));
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
    private static Mono<ByteString> toByteString(Flux<ByteBuffer> bbFlux) {
        Objects.requireNonNull(bbFlux, "'bbFlux' cannot be null.");
        return Mono.using(okio.Buffer::new,
            buffer -> bbFlux.reduce(buffer, (b, byteBuffer) -> {
                try {
                    b.write(byteBuffer);
                    return b;
                } catch (IOException ioe) {
                    throw Exceptions.propagate(ioe);
                }
            })
            .map(b -> ByteString.of(b.readByteArray())),
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
     * An implementation of {@link HttpResponse} for OkHttp.
     */
    private static class OkHttpResponse extends HttpResponse {
        private final int statusCode;
        private final HttpHeaders headers;
        private final Mono<ResponseBody> responseBodyMono;
        // using 4K as default buffer size: https://stackoverflow.com/a/237495/1473510
        private static final int BYTE_BUFFER_CHUNK_SIZE = 4096;

        OkHttpResponse(Response innerResponse, HttpRequest request) {
            super(request);
            this.statusCode = innerResponse.code();
            this.headers = fromOkHttpHeaders(innerResponse.headers());
            if (innerResponse.body() == null) {
                // innerResponse.body() getter will not return null for server returned responses.
                // It can be null:
                // [a]. if response is built manually with null body (e.g for mocking)
                // [b]. for the cases described here
                // [ref](https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response/body/).
                //
                this.responseBodyMono = Mono.empty();
            } else {
                this.responseBodyMono = Mono.using(() -> innerResponse.body(),
                    rb -> Mono.just(rb),
                    // Resource cleanup
                    // square.github.io/okhttp/4.x/okhttp/okhttp3/-response-body/#the-response-body-must-be-closed
                    ResponseBody::close, /* Change in behavior since reactor-core 3.3.0.RELEASE */ false);
            }
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
                .flatMapMany(irb -> toFluxByteBuffer(irb.byteStream()));
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return this.responseBodyMono
                .flatMap(rb -> {
                    try {
                        byte[] content = rb.bytes();
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
                        String content = rb.string();
                        return content.length() == 0 ? Mono.empty() : Mono.just(content);
                    } catch (IOException ioe) {
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
         * Creates azure-core HttpHeaders from okhttp headers.
         *
         * @param headers okhttp headers
         * @return azure-core HttpHeaders
         */
        private static HttpHeaders fromOkHttpHeaders(Headers headers) {
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
