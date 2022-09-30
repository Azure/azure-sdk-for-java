// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.vertx.implementation.BufferedVertxHttpResponse;
import com.azure.core.http.vertx.implementation.VertxHttpAsyncResponse;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.ProgressReporter;
import io.netty.buffer.Unpooled;
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
        ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();
        return Mono.create(sink -> toVertxHttpRequest(request).subscribe(vertxHttpRequest -> {
            HttpHeaders azureRequestHeaders = request.getHeaders();
            if (azureRequestHeaders != null) {
                // Transfer Azure request headers to vertx
                azureRequestHeaders.forEach(
                    header -> vertxHttpRequest.putHeader(header.getName(), header.getValuesList()));
                if (azureRequestHeaders.get("Content-Length") == null) {
                    vertxHttpRequest.setChunked(true);
                }
            } else {
                vertxHttpRequest.setChunked(true);
            }

            vertxHttpRequest.response(event -> {
                if (event.succeeded()) {
                    HttpClientResponse vertxHttpResponse = event.result();
                    if (eagerlyReadResponse) {
                        vertxHttpResponse.body(bodyEvent -> {
                            if (bodyEvent.succeeded()) {
                                sink.success(new BufferedVertxHttpResponse(request, vertxHttpResponse,
                                    bodyEvent.result()));
                            } else {
                                sink.error(bodyEvent.cause());
                            }
                        });
                    } else {
                        vertxHttpResponse.pause();
                        sink.success(new VertxHttpAsyncResponse(request, vertxHttpResponse));
                    }
                } else {
                    sink.error(event.cause());
                }
            });

            getRequestBody(request, progressReporter)
                .subscribeOn(scheduler)
                .map(Unpooled::wrappedBuffer)
                .map(Buffer::buffer)
                .subscribe(vertxHttpRequest::write, sink::error, vertxHttpRequest::end);
        }, sink::error));
    }

    private Mono<HttpClientRequest> toVertxHttpRequest(HttpRequest request) {
        return Mono.fromCompletionStage(
            client.request(
                new RequestOptions()
                    .setMethod(io.vertx.core.http.HttpMethod.valueOf(request.getHttpMethod().name()))
                    .setAbsoluteURI(request.getUrl()))
                .toCompletionStage());
    }

    private Flux<ByteBuffer> getRequestBody(HttpRequest request, ProgressReporter progressReporter) {
        Flux<ByteBuffer> body = request.getBody();
        if (body == null) {
            return Flux.empty();
        }

        if (progressReporter != null) {
            body = body.map(buffer -> {
                progressReporter.reportProgress(buffer.remaining());
                return buffer;
            });
        }

        return body;
    }
}
