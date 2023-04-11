// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.vertx.implementation.BufferedVertxHttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressReporter;
import io.netty.buffer.Unpooled;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
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
        // boolean eagerlyReadResponse = (boolean) context.getData("azure-eagerly-read-response").orElse(false);
        ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();

        RequestOptions options = new RequestOptions()
            .setMethod(HttpMethod.valueOf(request.getHttpMethod().name()))
            .setAbsoluteURI(request.getUrl());

        return Mono.create(sink -> client.request(options, requestResult -> {
            if (requestResult.failed()) {
                sink.error(requestResult.cause());
                return;
            }

            HttpClientRequest vertxHttpRequest = requestResult.result();
            vertxHttpRequest.exceptionHandler(sink::error);

            request.getHeaders().stream()
                .forEach(header -> vertxHttpRequest.putHeader(header.getName(), header.getValuesList()));

            if (request.getHeaders().get(HttpHeaderName.CONTENT_LENGTH) == null) {
                vertxHttpRequest.setChunked(true);
            }

            vertxHttpRequest.response(event -> {
                if (event.succeeded()) {
                    HttpClientResponse vertxHttpResponse = event.result();
                    vertxHttpResponse.exceptionHandler(sink::error);

                    // TODO (alzimmer)
                    // For now Vertx will always use a buffered response until reliability issues when using streaming
                    // can be resolved.
                    vertxHttpResponse.body(bodyEvent -> {
                        if (bodyEvent.succeeded()) {
                            sink.success(new BufferedVertxHttpResponse(request, vertxHttpResponse, bodyEvent.result()));
                        } else {
                            sink.error(bodyEvent.cause());
                        }
                    });
                } else {
                    sink.error(event.cause());
                }
            });

            // TODO (alzimmer)
            // For now Vertx will always use a buffered request until reliability issues when using streamin can be
            // resolved.
            Flux<ByteBuffer> requestBody = request.getBody();
            if (requestBody == null) {
                vertxHttpRequest.end();
            } else {
                if (progressReporter != null) {
                    requestBody = requestBody.map(buffer -> {
                        progressReporter.reportProgress(buffer.remaining());
                        return buffer;
                    });
                }

                FluxUtil.collectBytesFromNetworkResponse(requestBody, request.getHeaders())
                    .subscribeOn(scheduler)
                    .subscribe(bytes -> vertxHttpRequest.write(Buffer.buffer(Unpooled.wrappedBuffer(bytes))),
                        sink::error, vertxHttpRequest::end);
            }
        }));
    }
}
