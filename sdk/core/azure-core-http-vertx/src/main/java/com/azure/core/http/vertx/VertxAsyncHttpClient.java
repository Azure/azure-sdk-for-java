// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.vertx.implementation.AzureReactiveReadStreamWrapper;
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

        return Mono.create(sink -> client.request(options).compose(vertxRequest -> {
            for (HttpHeader header : request.getHeaders()) {
                // Potential optimization here would be creating a MultiMap wrapper around azure-core's
                // HttpHeaders and using RequestOptions.setHeaders(MultiMap)
                vertxRequest.putHeader(header.getName(), header.getValuesList());
            }

            if (request.getHeaders().get(HttpHeaderName.CONTENT_LENGTH) == null) {
                vertxRequest.setChunked(true);
            }

            return sendBodyFuture(sink, request, progressReporter, vertxRequest);
        }).compose(vertxResponse -> vertxResponse.body()
            .map(body -> new BufferedVertxHttpResponse(request, vertxResponse, body)))
        .andThen(result -> {
            if (result.succeeded()) {
                sink.success(result.result());
            } else {
                sink.error(result.cause());
            }
        }));
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        return send(request, context).block();
    }

    private Future<HttpClientResponse> sendBodyFuture(MonoSink<HttpResponse> sink, HttpRequest azureRequest,
        ProgressReporter progressReporter, HttpClientRequest vertxRequest) {
        BinaryData body = azureRequest.getBodyAsBinaryData();
        if (body == null) {
            return vertxRequest.send();
        }

        BinaryDataContent bodyContent = BinaryDataHelper.getContent(body);
        if (bodyContent instanceof ByteArrayContent
            || bodyContent instanceof StringContent
            || bodyContent instanceof SerializableContent) {
            byte[] content = bodyContent.toBytes();
            return vertxRequest.send(Buffer.buffer(Unpooled.wrappedBuffer(content)))
                .onSuccess(ignored -> reportProgress(content.length, progressReporter));
        } else if (bodyContent instanceof FileContent) {
            FileContent fileContent = (FileContent) bodyContent;
            return vertx.fileSystem().open(fileContent.getFile().toString(), new OpenOptions().setRead(true))
                .compose(file -> {
                    file.setReadPos(fileContent.getPosition()).setReadLength(fileContent.getLength());

                    return vertxRequest.send(file)
                        .onSuccess(ignored -> reportProgress(fileContent.getLength(), progressReporter));
                });
        } else {
            // Right now both Flux<ByteBuffer> and InputStream bodies are being handled reactively.
            ReactiveReadStream<Buffer> readStream = new AzureReactiveReadStreamWrapper(ReactiveReadStream.readStream(1),
                sink);

            azureRequest.getBody().map(buffer -> {
                reportProgress(buffer.remaining(), progressReporter);
                return Buffer.buffer(Unpooled.wrappedBuffer(buffer));
            }).subscribeOn(scheduler).subscribe(readStream);

            return vertxRequest.send(readStream);
        }
    }

    private static void reportProgress(long progress, ProgressReporter progressReporter) {
        if (progressReporter != null) {
            progressReporter.reportProgress(progress);
        }
    }
}
