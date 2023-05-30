// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.vertx.implementation.BufferedVertxHttpResponse;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.SerializableContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.ProgressReporter;
import io.netty.buffer.Unpooled;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.reactivestreams.ReactiveReadStream;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;

/**
 * {@link HttpClient} implementation for the Vert.x {@link io.vertx.core.http.HttpClient}.
 */
class VertxAsyncHttpClient implements HttpClient {
    private final Vertx vertx;
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
        this.vertx = vertx;
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

            HttpClientRequest vertxRequest = requestResult.result();
            for (HttpHeader header : request.getHeaders()) {
                // Potential optimization here would be creating a MultiMap wrapper around azure-core's
                // HttpHeaders and using RequestOptions.setHeaders(MultiMap)
                vertxRequest.putHeader(header.getName(), header.getValuesList());
            }

            if (request.getHeaders().get(HttpHeaderName.CONTENT_LENGTH) == null) {
                vertxRequest.setChunked(true);
            }

            vertxRequest.response(event -> {
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

            sendBody(sink, request, progressReporter, vertxRequest);
        }));
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        return send(request, context).block();
    }

    private void sendBody(MonoSink<HttpResponse> sink, HttpRequest azureRequest,
        ProgressReporter progressReporter, HttpClientRequest vertxRequest) {
        BinaryData body = azureRequest.getBodyAsBinaryData();
        if (body == null) {
            vertxRequest.send(result -> {
                if (result.failed()) {
                    sink.error(result.cause());
                }
            });
            return;
        }

        BinaryDataContent bodyContent = BinaryDataHelper.getContent(body);
        if (bodyContent instanceof ByteArrayContent
            || bodyContent instanceof StringContent
            || bodyContent instanceof SerializableContent) {
            byte[] content = bodyContent.toBytes();
            vertxRequest.send(Buffer.buffer(Unpooled.wrappedBuffer(content)), result -> {
                if (result.succeeded()) {
                    reportProgress(content.length, progressReporter);
                } else {
                    sink.error(result.cause());
                }
            });
        } else if (bodyContent instanceof FileContent) {
            FileContent fileContent = (FileContent) bodyContent;
            vertx.fileSystem().open(fileContent.getFile().toString(), new OpenOptions().setRead(true), event -> {
                if (event.succeeded()) {
                    AsyncFile file = event.result();
                    file.setReadPos(fileContent.getPosition());
                    if (fileContent.getLength() != null) {
                        file.setReadLength(fileContent.getLength());
                    }

                    vertxRequest.send(file, result -> {
                        if (result.succeeded()) {
                            reportProgress(fileContent.getLength(), progressReporter);
                        } else {
                            sink.error(result.cause());
                        }
                    });
                } else {
                    sink.error(event.cause());
                }
            });
        } else {
            // Right now both Flux<ByteBuffer> and InputStream bodies are being handled reactively.
            // Create a ReadStream<Buffer> implementation that sends the InputStream without the use of Reactor.
            ReactiveReadStream<Buffer> reactiveSender = ReactiveReadStream.<Buffer>readStream(1)
                .exceptionHandler(sink::error);

            azureRequest.getBody().map(byteBuffer -> Buffer.buffer(Unpooled.wrappedBuffer(byteBuffer)))
                .doOnNext(data -> reportProgress(data.length(), progressReporter))
                .doOnError(sink::error)
                .subscribeOn(scheduler)
                .subscribe(reactiveSender);

            vertxRequest.send(reactiveSender, result -> {
                if (result.failed()) {
                    sink.error(result.cause());
                }
            });
        }
    }

    private static void reportProgress(long progress, ProgressReporter progressReporter) {
        if (progressReporter != null) {
            progressReporter.reportProgress(progress);
        }
    }

    private static Future<HttpResponse> handleVertxResponse(HttpRequest request, HttpClientResponse vertxResponse) {
        return vertxResponse.body().map(body -> new BufferedVertxHttpResponse(request, vertxResponse, body));
    }
}
