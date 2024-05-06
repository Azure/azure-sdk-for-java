// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.vertx.implementation.BufferedVertxHttpResponse;
import com.azure.core.http.vertx.implementation.VertxHttpAsyncResponse;
import com.azure.core.http.vertx.implementation.VertxRequestWriteSubscriber;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.ByteBufferContent;
import com.azure.core.implementation.util.HttpUtils;
import com.azure.core.implementation.util.SerializableContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.ProgressReporter;
import io.netty.buffer.Unpooled;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.azure.core.http.vertx.implementation.VertxUtils.wrapVertxException;

/**
 * {@link HttpClient} implementation for the Vert.x {@link io.vertx.core.http.HttpClient}.
 */
class VertxAsyncHttpClient implements HttpClient {
    final io.vertx.core.http.HttpClient client;
    private final Duration responseTimeout;

    /**
     * Constructs a {@link VertxAsyncHttpClient}.
     *
     * @param client The Vert.x {@link io.vertx.core.http.HttpClient}
     */
    VertxAsyncHttpClient(io.vertx.core.http.HttpClient client, Duration responseTimeout) {
        this.client = Objects.requireNonNull(client, "client cannot be null");
        this.responseTimeout = responseTimeout;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        boolean eagerlyReadResponse = (boolean) context.getData(HttpUtils.AZURE_EAGERLY_READ_RESPONSE).orElse(false);
        boolean ignoreResponseBody = (boolean) context.getData(HttpUtils.AZURE_IGNORE_RESPONSE_BODY).orElse(false);
        Duration perCallTimeout = (Duration) context.getData(HttpUtils.AZURE_RESPONSE_TIMEOUT)
            .filter(timeoutDuration -> timeoutDuration instanceof Duration)
            .orElse(responseTimeout);

        ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();

        RequestOptions options = new RequestOptions().setMethod(HttpMethod.valueOf(request.getHttpMethod().name()))
            .setAbsoluteURI(request.getUrl());

        return Mono.create(sink -> client.request(options, requestResult -> {
            if (requestResult.failed()) {
                sink.error(wrapVertxException(requestResult.cause()));
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

            io.vertx.core.Future<HttpClientResponse> bodySendFuture
                = sendBody(sink, request, progressReporter, vertxRequest);

            if (!perCallTimeout.isZero() && !perCallTimeout.isNegative()) {
                bodySendFuture = bodySendFuture.timeout(perCallTimeout.toMillis(), TimeUnit.MILLISECONDS);
            }

            bodySendFuture.andThen(event -> {
                if (event.succeeded()) {
                    HttpClientResponse vertxHttpResponse = event.result();
                    vertxHttpResponse.exceptionHandler(exception -> sink.error(wrapVertxException(exception)));

                    // TODO (alzimmer)
                    // For now Vertx will always use a buffered response until reliability issues when using streaming
                    // can be resolved.
                    if (eagerlyReadResponse || ignoreResponseBody) {
                        vertxHttpResponse.body(bodyEvent -> {
                            if (bodyEvent.succeeded()) {
                                sink.success(
                                    new BufferedVertxHttpResponse(request, vertxHttpResponse, bodyEvent.result()));
                            } else {
                                sink.error(wrapVertxException(bodyEvent.cause()));
                            }
                        });
                    } else {
                        sink.success(new VertxHttpAsyncResponse(request, vertxHttpResponse));
                    }
                } else {
                    sink.error(wrapVertxException(event.cause()));
                }
            }).onFailure(cause -> sink.error(wrapVertxException(cause)));
        }));
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        return send(request, context).block();
    }

    @SuppressWarnings("deprecation")
    private io.vertx.core.Future<HttpClientResponse> sendBody(MonoSink<HttpResponse> sink, HttpRequest azureRequest,
        ProgressReporter progressReporter, HttpClientRequest vertxRequest) {
        BinaryData body = azureRequest.getBodyAsBinaryData();
        if (body == null) {
            return vertxRequest.send();
        }

        io.vertx.core.Future<?> writeAndEnd;
        BinaryDataContent bodyContent = BinaryDataHelper.getContent(body);
        if (bodyContent instanceof ByteArrayContent
            || bodyContent instanceof StringContent
            || bodyContent instanceof SerializableContent) {
            byte[] content = bodyContent.toBytes();
            writeAndEnd = vertxRequest.write(Buffer.buffer(content))
                .onSuccess(ignored -> reportProgress(content.length, progressReporter))
                .compose(ignored -> vertxRequest.end());
        } else if (bodyContent instanceof ByteBufferContent) {
            long contentLength = bodyContent.getLength();
            writeAndEnd = vertxRequest.write(Buffer.buffer(Unpooled.wrappedBuffer(bodyContent.toByteBuffer())))
                .onSuccess(ignored -> reportProgress(contentLength, progressReporter))
                .compose(ignored -> vertxRequest.end());
        } else {
            // Right now both Flux<ByteBuffer> and InputStream bodies are being handled reactively.
            io.vertx.core.Promise<Void> promise = io.vertx.core.Promise.promise();
            azureRequest.getBody()
                .subscribe(
                    new VertxRequestWriteSubscriber(vertxRequest, promise, progressReporter, sink.contextView()));
            writeAndEnd = promise.future();
        }

        return writeAndEnd.compose(ignored -> vertxRequest.response());
    }

    private static void reportProgress(long progress, ProgressReporter progressReporter) {
        if (progressReporter != null) {
            progressReporter.reportProgress(progress);
        }
    }

}
