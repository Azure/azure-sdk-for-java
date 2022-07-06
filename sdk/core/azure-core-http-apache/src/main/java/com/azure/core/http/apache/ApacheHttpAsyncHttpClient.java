// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.apache;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.apache.implementation.ApacheHttpAsyncResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import org.apache.hc.client5.http.async.HttpAsyncClient;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.reactive.ReactiveEntityProducer;
import org.apache.hc.core5.reactive.ReactiveResponseConsumer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * HttpClient implementation for Apache Component.
 */
class ApacheHttpAsyncHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(ApacheHttpAsyncHttpClient.class);

    final HttpAsyncClient apacheClient;

    /**
     * Create ApacheHttpAsyncHttpClient with provided http client.
     *
     * @param apacheClient the apache http client.
     */
    ApacheHttpAsyncHttpClient(HttpAsyncClient apacheClient) {
        this.apacheClient = apacheClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        Objects.requireNonNull(request.getHttpMethod(), "'request.getHttpMethod()' cannot be null.");
        Objects.requireNonNull(request.getUrl(), "'request.getUrl()' cannot be null.");
        Objects.requireNonNull(request.getUrl().getProtocol(), "'request.getUrl().getProtocol()' cannot be null.");

        final String contentLength = request.getHeaders().getValue(HttpHeaders.CONTENT_LENGTH);
        // Request Producer
        final BasicRequestProducer requestProducer = new BasicRequestProducer(
            getApacheHttpRequest(request),
            new ReactiveEntityProducer(getRequestBody(request),
                CoreUtils.isNullOrEmpty(contentLength) ? -1 : Long.parseLong(contentLength), null, null));

        // Response Consumer
        final ReactiveResponseConsumer consumer = new ReactiveResponseConsumer();

        // Execute the request
        apacheClient.execute(requestProducer, consumer, null, toApacheContext(context), null);

        // Convert and return Azure response
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            try {
                // TODO (mssfang): look at making this non-blocking
                return consumer.getResponseFuture().get();
            } catch (InterruptedException | ExecutionException e) {
                throw LOGGER.logExceptionAsError(new UnexpectedLengthException(e.getMessage(), 0L, 0L));
            }
        })).publishOn(Schedulers.boundedElastic())
                   .map(response -> new ApacheHttpAsyncResponse(response, request));
    }

    private static HttpUriRequestBase getApacheHttpRequest(HttpRequest request) {
        final URI uri;
        try {
            uri = request.getUrl().toURI();
        } catch (URISyntaxException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }

        HttpUriRequestBase httpUriRequestBase = new HttpUriRequestBase(request.getHttpMethod().name(), uri);

        // Add headers to Apache request
        if (request.getHeaders() != null) {
            for (HttpHeader hdr : request.getHeaders()) {
                hdr.getValuesList().forEach(value -> httpUriRequestBase.addHeader(hdr.getName(), value));
            }
        }

        return httpUriRequestBase;
    }

    private Flux<ByteBuffer> getRequestBody(HttpRequest request) {
        if (request.getBody() == null) {
            return Flux.empty();
        }
        return request.getBody();
    }

    private HttpContext toApacheContext(Context context) {
        final HttpCoreContext httpCoreContext = new HttpCoreContext();

        context.getValues().forEach((k, v) -> {
            if (k instanceof String) {
                httpCoreContext.setAttribute((String) k, v);
            }
        });

        return httpCoreContext;
    }
}
