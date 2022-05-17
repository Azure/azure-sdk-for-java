// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.vertx.implementation.BufferedVertxHttpResponse;
import com.azure.core.http.vertx.implementation.VertxHttpAsyncResponse;
import com.azure.core.util.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.RequestOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * {@link HttpClient} implementation for the Vert.x {@link io.vertx.core.http.HttpClient}.
 */
class VertxAsyncHttpClient implements HttpClient {
    private static final int BYTE_BUFFER_CHUNK_SIZE = 4096;
    private final Scheduler scheduler;
    final io.vertx.core.http.HttpClient client;

    /**
     * Constructs a {@link VertxAsyncHttpClient}.
     *
     * @param client The Vert.x {@link io.vertx.core.http.HttpClient}
     */
    VertxAsyncHttpClient(io.vertx.core.http.HttpClient client, Vertx vertx) {
        Objects.requireNonNull(client, "client cannot be null");
        Objects.requireNonNull(vertx, "vertx cannot be null");
        this.client = client;
        this.scheduler = Schedulers.fromExecutor(vertx.nettyEventLoopGroup());
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        boolean eagerlyReadResponse = (boolean) context.getData("azure-eagerly-read-response").orElse(false);
        return Mono.create(sink ->
            toVertxHttpRequest(request).subscribe(vertxHttpRequest -> {
                vertxHttpRequest.exceptionHandler(sink::error);

                HttpHeaders requestHeaders = request.getHeaders();
                if (requestHeaders != null) {
                    requestHeaders.stream().forEach(header -> vertxHttpRequest.putHeader(header.getName(), header.getValuesList()));
                    if (request.getHeaders().get("Content-Length") == null) {
                        vertxHttpRequest.setChunked(true);
                    }
                } else {
                    vertxHttpRequest.setChunked(true);
                }

                vertxHttpRequest.response(event -> {
                    if (event.succeeded()) {
                        HttpClientResponse vertxHttpResponse = event.result();
                        vertxHttpResponse.exceptionHandler(sink::error);

                        if (eagerlyReadResponse) {
                            vertxHttpResponse.body(bodyEvent -> {
                                if (bodyEvent.succeeded()) {
                                    sink.success(new BufferedVertxHttpResponse(request, vertxHttpResponse, bodyEvent.result()));
                                } else {
                                    sink.error(bodyEvent.cause());
                                }
                            });
                        } else {
                            sink.success(new VertxHttpAsyncResponse(request, vertxHttpResponse));
                        }
                    }
                });

                getRequestBody(request).subscribeOn(scheduler).subscribe(buffer -> {
                    while (buffer.hasRemaining()) {
                        int bufferSize;
                        if (buffer.remaining() > BYTE_BUFFER_CHUNK_SIZE) {
                            bufferSize = BYTE_BUFFER_CHUNK_SIZE;
                        } else {
                            bufferSize = buffer.remaining();
                        }

                        byte[] bytes = new byte[bufferSize];
                        buffer.get(bytes, 0, bytes.length);

                        vertxHttpRequest.write(Buffer.buffer(bytes));
                    }
                }, sink::error, vertxHttpRequest::end);
            }, sink::error));
    }

    private Mono<HttpClientRequest> toVertxHttpRequest(HttpRequest request) {
        HttpMethod httpMethod = request.getHttpMethod();
        io.vertx.core.http.HttpMethod requestMethod = io.vertx.core.http.HttpMethod.valueOf(httpMethod.name());

        RequestOptions options = new RequestOptions();
        options.setMethod(requestMethod);
        options.setAbsoluteURI(request.getUrl());
        return Mono.fromCompletionStage(client.request(options).toCompletionStage());
    }

    private Flux<ByteBuffer> getRequestBody(HttpRequest request) {
        if (request.getBody() == null) {
            return Flux.empty();
        }
        return request.getBody();
    }
}
