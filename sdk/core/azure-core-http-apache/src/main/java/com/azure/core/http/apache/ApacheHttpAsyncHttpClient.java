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
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * HttpClient implementation for Apache Component.
 */
class ApacheHttpAsyncHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(ApacheHttpAsyncHttpClient.class);

    private static final String AZURE_EAGERLY_READ_RESPONSE = "azure-eagerly-read-response";

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

//        final Message<org.apache.hc.core5.http.HttpResponse, Publisher<ByteBuffer>> httpResponsePublisherMessage;
//        try {
//            httpResponsePublisherMessage = consumer.getResponseFuture().get();
//        } catch (Exception ex) {
//            throw new UnexpectedLengthException(ex.getMessage(), 0L, 0L);
//        }
//        return Mono.just(new ApacheHttpAsyncResponse(httpResponsePublisherMessage, request));

        // Convert and return Azure response
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            try {
                // TODO (mssfang): look at making this non-blocking
                return consumer.getResponseFuture().get();
            } catch (InterruptedException | ExecutionException e) {
                throw new UnexpectedLengthException(e.getMessage(), 0L, 0L);
            }
        })).publishOn(Schedulers.boundedElastic())
                   .map(response -> new ApacheHttpAsyncResponse(response, request));


//        return Mono.create(sink -> sink.onRequest(value -> {
//            // Using MonoSink::onRequest for back pressure support.
//
//            // The blocking behavior toOkHttpRequest(r).subscribe call:
//            //
//            // The okhttp3.Request emitted by toOkHttpRequest(r) is chained from the body of request Flux<ByteBuffer>:
//            //   1. If Flux<ByteBuffer> synchronous and send(r) caller does not apply subscribeOn then
//            //      subscribe block on caller thread.
//            //   2. If Flux<ByteBuffer> synchronous and send(r) caller apply subscribeOn then
//            //      does not block caller thread but block on scheduler thread.
//            //   3. If Flux<ByteBuffer> asynchronous then subscribe does not block caller thread
//            //      but block on the thread backing flux. This ignore any subscribeOn applied to send(r)
//            //
//            toApacheHttpRequest(request).subscribe(apacheRequest -> {
//                try {
//                    final BasicRequestProducer requestProducer = new BasicRequestProducer(
//                        apacheRequest,
//                        new ReactiveEntityProducer(getRequestBody(request), contentLen, null, null));
//                    // Response Consumer
//                    final ReactiveResponseConsumer consumer = new ReactiveResponseConsumer(
//                        new ApacheHttpCallback(sink, request, false));
//
//                    // Execute the request
//                    apacheClient.execute(requestProducer, consumer, null, toApacheContext(context), null);
//
////                    consumer.getEntityDetails();
////
////                    final Message<org.apache.hc.core5.http.HttpResponse, Publisher<ByteBuffer>> httpResponsePublisherMessage;
////                    try {
////                        httpResponsePublisherMessage = consumer.getResponseFuture().get();
////                    } catch (Exception ex) {
////                        throw new UnexpectedLengthException(ex.getMessage(), 0L, 0L);
////                    }
////                    return new ApacheHttpAsyncResponse(responsePublisherMessage, request);
////                    sink.onCancel(call::cancel);
//                } catch (Exception ex) {
//                    sink.error(ex);
//                }
//            }, sink::error);
//        }));
    }


    private static Mono<HttpUriRequestBase> toApacheHttpRequest(HttpRequest request) {
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

        return Mono.just(httpUriRequestBase);
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


    private static class ApacheHttpCallback implements
        FutureCallback<Message<org.apache.hc.core5.http.HttpResponse, Publisher<ByteBuffer>>> {
        private final MonoSink<HttpResponse> sink;
        private final HttpRequest request;
        private final boolean eagerlyReadResponse;

        ApacheHttpCallback(MonoSink<HttpResponse> sink, HttpRequest request, boolean eagerlyReadResponse) {
            this.sink = sink;
            this.request = request;
            this.eagerlyReadResponse = eagerlyReadResponse;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public void failed(Exception e) {
            sink.error(e);
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public void completed(Message<org.apache.hc.core5.http.HttpResponse, Publisher<ByteBuffer>> response) {
            /*
             * Use a buffered response when we are eagerly reading the response from the network and the body isn't
             * empty.
             */
            if (eagerlyReadResponse) {
                System.out.println("Eagerly completed");
                sink.success(new ApacheHttpAsyncResponse(response, request));
            } else {
                System.out.println("completed!!");
                sink.success(new ApacheHttpAsyncResponse(response, request));
            }
        }

        @Override
        public void cancelled() {
            System.out.println("Cancelled");
        }
    }
}
