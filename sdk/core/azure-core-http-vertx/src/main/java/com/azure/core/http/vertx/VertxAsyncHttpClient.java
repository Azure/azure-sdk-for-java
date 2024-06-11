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
import com.azure.core.http.vertx.implementation.VertxUtils;
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
import com.azure.core.util.logging.ClientLogger;
import io.netty.buffer.Unpooled;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * {@link HttpClient} implementation for the Vert.x {@link io.vertx.core.http.HttpClient}.
 */
class VertxAsyncHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(VertxAsyncHttpClient.class);

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
        return Mono.deferContextual(contextView -> Mono.fromFuture(sendInternal(request, context, contextView)))
            .onErrorMap(VertxUtils::wrapVertxException);
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        try {
            return sendInternal(request, context, reactor.util.context.Context.empty()).get();
        } catch (Exception e) {
            Throwable mapped = e;
            if (e instanceof ExecutionException) {
                mapped = e.getCause();
            }

            mapped = VertxUtils.wrapVertxException(mapped);
            if (mapped instanceof Error) {
                throw LOGGER.logThrowableAsError((Error) mapped);
            } else if (mapped instanceof IOException) {
                throw LOGGER.logExceptionAsError(new UncheckedIOException((IOException) mapped));
            } else if (mapped instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) mapped);
            } else {
                throw LOGGER.logExceptionAsError(new RuntimeException(mapped));
            }
        }
    }

    private CompletableFuture<HttpResponse> sendInternal(HttpRequest request, Context context,
        ContextView contextView) {
        boolean eagerlyReadResponse = (boolean) context.getData(HttpUtils.AZURE_EAGERLY_READ_RESPONSE).orElse(false);
        boolean ignoreResponseBody = (boolean) context.getData(HttpUtils.AZURE_IGNORE_RESPONSE_BODY).orElse(false);
        Duration perCallTimeout = (Duration) context.getData(HttpUtils.AZURE_RESPONSE_TIMEOUT)
            .filter(timeoutDuration -> timeoutDuration instanceof Duration)
            .orElse(responseTimeout);

        ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();

        RequestOptions options = new RequestOptions().setMethod(HttpMethod.valueOf(request.getHttpMethod().name()))
            .setAbsoluteURI(request.getUrl());

        // This is the shared design for sending a request and receiving a response in Vert.x. The design relies on
        // using a Vert.x Promise and the handler methods to control the flow of the request-response cycle. The Promise
        // will be completed upon failure during the request-response cycle or upon completion of the response. This can
        // be shared between sync and async as Reactor can use Mono.fromCompletionStage to convert the Promise to a Mono
        // and the sync path can convert the Promise to a Future and block it.

        // Create the Promise that will be returned. Promise.promise() is an uncompleted Promise.
        Promise<HttpResponse> promise = Promise.promise();
        client.request(options, requestResult -> {
            if (requestResult.failed()) {
                promise.fail(requestResult.cause());
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

            Future<HttpClientResponse> responseFuture;
            if (!perCallTimeout.isZero() && !perCallTimeout.isNegative()) {
                responseFuture = vertxRequest.response().timeout(perCallTimeout.toMillis(), TimeUnit.MILLISECONDS);
            } else {
                responseFuture = vertxRequest.response();
            }

            responseFuture = responseFuture.onFailure(promise::fail);

            if (eagerlyReadResponse || ignoreResponseBody) {
                responseFuture.andThen(responseResult -> {
                    if (responseResult.failed()) {
                        promise.fail(responseResult.cause());
                        return;
                    }

                    HttpClientResponse vertxHttpResponse = responseResult.result();
                    vertxHttpResponse.body().andThen(bodyResult -> {
                        if (bodyResult.succeeded()) {
                            promise.complete(
                                new BufferedVertxHttpResponse(request, vertxHttpResponse, bodyResult.result()));
                        } else {
                            promise.fail(bodyResult.cause());
                        }
                    });
                });
            } else {
                responseFuture.andThen(responseResult -> {
                    if (responseResult.succeeded()) {
                        promise.complete(new VertxHttpAsyncResponse(request, responseResult.result()));
                    } else {
                        promise.fail(responseResult.cause());
                    }
                });
            }

            sendBody(contextView, request, progressReporter, vertxRequest, promise);
        });

        return promise.future().toCompletionStage().toCompletableFuture();
    }

    @SuppressWarnings("deprecation")
    private void sendBody(ContextView contextView, HttpRequest azureRequest, ProgressReporter progressReporter,
        HttpClientRequest vertxRequest, Promise<HttpResponse> promise) {
        BinaryData body = azureRequest.getBodyAsBinaryData();

        if (body == null) {
            vertxRequest.send().onFailure(promise::fail);
        } else {
            BinaryDataContent bodyContent = BinaryDataHelper.getContent(body);
            if (bodyContent instanceof ByteArrayContent
                || bodyContent instanceof ByteBufferContent
                || bodyContent instanceof StringContent
                || bodyContent instanceof SerializableContent) {
                long contentLength = bodyContent.getLength();
                vertxRequest.send(Buffer.buffer(Unpooled.wrappedBuffer(bodyContent.toByteBuffer())))
                    .onSuccess(ignored -> reportProgress(contentLength, progressReporter))
                    .onFailure(promise::fail);
            } else {
                // Right now both Flux<ByteBuffer> and InputStream bodies are being handled reactively.
                azureRequest.getBody()
                    .subscribe(new VertxRequestWriteSubscriber(vertxRequest, promise, progressReporter, contextView));
            }
        }
    }

    private static void reportProgress(long progress, ProgressReporter progressReporter) {
        if (progressReporter != null) {
            progressReporter.reportProgress(progress);
        }
    }

}
