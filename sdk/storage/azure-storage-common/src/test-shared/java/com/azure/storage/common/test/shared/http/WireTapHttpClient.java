// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class WireTapHttpClient implements HttpClient {

    private final HttpClient delegate;
    private final AtomicInteger requestCount = new AtomicInteger();
    private Consumer<HttpRequest> requestInspector;
    private volatile HttpRequest lastRequest;

    public WireTapHttpClient(HttpClient delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates a wire tap that hands every request to {@code requestInspector} as it goes on the wire, letting a test
     * observe the final headers set by the pipeline's policies.
     *
     * @param delegate The client that actually sends the request.
     * @param requestInspector Invoked with each request before it is sent.
     */
    public WireTapHttpClient(HttpClient delegate, Consumer<HttpRequest> requestInspector) {
        this(delegate);
        this.requestInspector = requestInspector;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        inspect(request);
        return delegate.send(request);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        inspect(request);
        return delegate.send(request, context);
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        inspect(request);
        return delegate.sendSync(request, context);
    }

    public HttpRequest getLastRequest() {
        return lastRequest;
    }

    public int getRequestCount() {
        return requestCount.get();
    }

    private void inspect(HttpRequest request) {
        lastRequest = request;
        requestCount.incrementAndGet();
        if (requestInspector != null) {
            requestInspector.accept(request);
        }
    }
}
