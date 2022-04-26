// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.vertx.implementation.VertxHttpRequest;
import com.azure.core.http.vertx.implementation.VertxHttpResponseHandler;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * {@link HttpClient} implementation for the Vert.x {@link WebClient}.
 */
public final class VertxAsyncHttpClient implements HttpClient {

    private static final ClientLogger LOGGER = new ClientLogger(VertxAsyncHttpClient.class);
    final WebClient client;

    /**
     * Constructs a {@link VertxAsyncHttpClient}.
     *
     * @param client The Vert.x {@link WebClient}
     */
    public VertxAsyncHttpClient(WebClient client) {
        Objects.requireNonNull(client, "client cannot be null");
        this.client = client;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        boolean eagerlyReadResponse = (boolean) context.getData("azure-eagerly-read-response").orElse(false);
        return Mono.create(sink -> sink.onRequest(value -> {
            toVertxHttpRequest(request).subscribe(vertxHttpRequest -> {
                vertxHttpRequest.send(new VertxHttpResponseHandler(request, sink, eagerlyReadResponse));
            }, sink::error);
        }));
    }

    String getRequestUrlAsString(HttpRequest request) {
        URL url = request.getUrl();
        if (url.getPath().isEmpty()) {
            try {
                url = new URL(url.getProtocol(), url.getHost(), url.getPort(), "/" + url.getFile());
            } catch (MalformedURLException e) {
                throw LOGGER.logThrowableAsError(new RuntimeException(e));
            }
        }
        return url.toString();
    }


    private Mono<VertxHttpRequest> toVertxHttpRequest(HttpRequest request) {
        return Mono.from(convertBodyToBuffer(request))
                .map(buffer -> {
                    HttpMethod httpMethod = request.getHttpMethod();
                    io.vertx.core.http.HttpMethod requestMethod = io.vertx.core.http.HttpMethod.valueOf(httpMethod.name());

                    io.vertx.ext.web.client.HttpRequest<Buffer> delegate = client
                            .requestAbs(requestMethod, getRequestUrlAsString(request));

                    if (request.getHeaders() != null) {
                        request.getHeaders()
                                .stream()
                                .forEach(httpHeader -> delegate.putHeader(httpHeader.getName(),
                                        httpHeader.getValuesList()));
                    }

                    return new VertxHttpRequest(delegate, buffer);
                });
    }

    private Mono<Buffer> convertBodyToBuffer(HttpRequest request) {
        return Mono.using(() -> Buffer.buffer(),
                buffer -> getBody(request).reduce(buffer, (b, byteBuffer) -> {
                    for (int i = 0; i < byteBuffer.limit(); i++) {
                        b.appendByte(byteBuffer.get(i));
                    }
                    return b;
                }), buffer -> buffer.getClass());
    }

    private Flux<ByteBuffer> getBody(HttpRequest request) {
        long contentLength = 0;
        String contentLengthHeader = request.getHeaders().getValue("content-length");
        if (contentLengthHeader != null) {
            contentLength = Long.parseLong(contentLengthHeader);
        }

        Flux<ByteBuffer> body = request.getBody();
        if (body == null || contentLength <= 0) {
            body = Flux.just(Buffer.buffer().getByteBuf().nioBuffer());
        }

        return body;
    }

}
