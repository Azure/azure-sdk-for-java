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
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.reactive.ReactiveEntityProducer;
import org.apache.hc.core5.reactive.ReactiveResponseConsumer;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Objects;

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

        return Mono.create(sink -> sink.onRequest(value -> {

            final String contentLength = request.getHeaders().getValue(HttpHeaders.CONTENT_LENGTH);

            // Request Producer
            final BasicRequestProducer requestProducer = new BasicRequestProducer(
                getApacheHttpRequest(request),
                new ReactiveEntityProducer(getRequestBody(request),
                    CoreUtils.isNullOrEmpty(contentLength) ? -1 : Long.parseLong(contentLength), null, null));

            // Response Consumer
            final ReactiveResponseConsumer consumer = new ReactiveResponseConsumer(new ApacheHttpFutureCallback(
                sink, request
            ));

            // Execute the request
            apacheClient.execute(requestProducer, consumer, null, toApacheContext(context), null);
        }));
    }

    private static class ApacheHttpFutureCallback implements
        FutureCallback<Message<org.apache.hc.core5.http.HttpResponse, Publisher<ByteBuffer>>> {

        private final MonoSink<HttpResponse> sink;
        private final HttpRequest request;

        private ApacheHttpFutureCallback(MonoSink<HttpResponse> sink, HttpRequest request) {
            this.sink = sink;
            this.request = request;
        }

        @Override
        public void completed(Message<org.apache.hc.core5.http.HttpResponse, Publisher<ByteBuffer>> apacheResponse) {
            sink.success(new ApacheHttpAsyncResponse(apacheResponse, request));
        }

        @Override
        public void failed(Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof UnexpectedLengthException) {
                sink.error(cause);
            }
            sink.error(e);
        }

        @Override
        public void cancelled() {
            sink.error(new IOException("Cancelled"));
        }
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
