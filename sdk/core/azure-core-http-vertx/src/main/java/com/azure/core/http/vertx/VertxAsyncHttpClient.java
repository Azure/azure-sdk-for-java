// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.vertx.implementation.BufferedVertxHttpResponse;
import com.azure.core.http.vertx.implementation.FluxByteBufferWriteSubscriber;
import com.azure.core.http.vertx.implementation.VertxHttpAsyncResponse;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.ByteBufferContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.SerializableContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.ProgressReporter;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;

import static io.netty.buffer.Unpooled.wrappedBuffer;

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
        this.vertx = Objects.requireNonNull(vertx, "vertx cannot be null");
        this.client = Objects.requireNonNull(client, "client cannot be null");
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

        HttpMethod requestMethod = HttpMethod.valueOf(request.getHttpMethod().name());

        RequestOptions options = new RequestOptions()
            .setMethod(requestMethod)
            .setAbsoluteURI(request.getUrl());

        return Mono.create(sink -> client.request(options, handler -> {
            if (handler.failed()) {
                sink.error(handler.cause());
                return;
            }

            HttpClientRequest vertxHttpRequest = handler.result();
            boolean consumedContentLength = false; // Minor optimization to reduce String comparisons
            for (HttpHeader header : request.getHeaders()) {
                if (!consumedContentLength && "Content-Length".equalsIgnoreCase(header.getName())) {
                    vertxHttpRequest.setChunked(header.getValue() == null);
                    consumedContentLength = true;
                }

                vertxHttpRequest.putHeader(header.getName(), header.getValuesList());
            }

            if (!consumedContentLength) {
                vertxHttpRequest.setChunked(true);
            }

            // Handle any errors in the HTTP request using the MonoSink.
            vertxHttpRequest.exceptionHandler(sink::error);

            BinaryData requestBody = request.getBodyAsBinaryData();
            if (requestBody == null) {
                vertxHttpRequest.send().onComplete(responseHandler -> consumeVertxHttpResponse(responseHandler, sink,
                    eagerlyReadResponse, request));
            } else {
                BinaryDataContent bodyContent = BinaryDataHelper.getContent(requestBody);
                if (bodyContent instanceof ByteArrayContent
                    || bodyContent instanceof StringContent
                    || bodyContent instanceof SerializableContent) {
                    writeWithProgress(vertxHttpRequest.send(Buffer.buffer(wrappedBuffer(bodyContent.toBytes()))),
                        progressReporter, bodyContent.getLength())
                        .onComplete(responseHandler -> consumeVertxHttpResponse(responseHandler, sink,
                            eagerlyReadResponse, request));
                } else if (bodyContent instanceof ByteBufferContent) {
                    writeWithProgress(vertxHttpRequest.send(Buffer.buffer(wrappedBuffer(bodyContent.toByteBuffer()))),
                        progressReporter, bodyContent.getLength())
                        .onComplete(responseHandler -> consumeVertxHttpResponse(responseHandler, sink,
                            eagerlyReadResponse, request));
                } else if (bodyContent instanceof FileContent) {
                    FileContent fileContent = (FileContent) bodyContent;
                    vertx.fileSystem().open(fileContent.getFile().toString(), new OpenOptions().setRead(true))
                        .compose(file -> {
                            file = file.setReadPos(fileContent.getPosition()).setReadLength(fileContent.getLength());
                            return writeWithProgress(vertxHttpRequest.send(file), progressReporter,
                                bodyContent.getLength());
                        })
                        .onComplete(responseHandler -> consumeVertxHttpResponse(responseHandler, sink,
                            eagerlyReadResponse, request));
                } else {
                    // The response handler needs to be set before sending the request.
                    vertxHttpRequest.response(responseHandler -> consumeVertxHttpResponse(responseHandler, sink,
                        eagerlyReadResponse, request));

                    bodyContent.toFluxByteBuffer()
                        .subscribeOn(scheduler)
                        .subscribe(new FluxByteBufferWriteSubscriber(sink, vertxHttpRequest, progressReporter));
                }
            }
        }));
    }

    private static void consumeVertxHttpResponse(AsyncResult<HttpClientResponse> responseHandler,
        MonoSink<HttpResponse> sink, boolean eagerlyReadResponse, HttpRequest request) {
        if (responseHandler.failed()) {
            sink.error(responseHandler.cause());
            return;
        }

        HttpClientResponse vertxHttpResponse = responseHandler.result();
        if (eagerlyReadResponse) {
            vertxHttpResponse.body(responseBodyHandler -> {
                if (responseBodyHandler.failed()) {
                    sink.error(responseHandler.cause());
                    return;
                }

                sink.success(new BufferedVertxHttpResponse(request, vertxHttpResponse, responseBodyHandler.result()));
            });
        } else {
            // Pause the ReadStream before building the response as building the response may take time
            // and it's unknown on whether the stream is hot.
            vertxHttpResponse.pause();
            sink.success(new VertxHttpAsyncResponse(request, vertxHttpResponse));
        }
    }

    private static <T> Future<T> writeWithProgress(Future<T> write, ProgressReporter progressReporter, long progress) {
        return write.onSuccess(ignored -> {
            if (progressReporter != null) {
                progressReporter.reportProgress(progress);
            }
        });
    }
}
