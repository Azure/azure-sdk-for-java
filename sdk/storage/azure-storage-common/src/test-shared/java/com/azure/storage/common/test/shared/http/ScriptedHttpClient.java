// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Small scripted transport for tests that need deterministic HTTP behavior.
 *
 * <p>The client supports a global FIFO queue, optional request-keyed FIFO queues, custom response factories,
 * explicit failures, and pending responses that can be completed later by the test.</p>
 */
public final class ScriptedHttpClient implements HttpClient {
    @FunctionalInterface
    public interface ResponseFactory {
        HttpResponse create(HttpRequest request);
    }

    private final Function<HttpRequest, String> requestKeySelector;
    private final ConcurrentLinkedQueue<ResponseAction> globalResponses = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<ResponseAction>> keyedResponses
        = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> requestCountsByKey = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, HttpRequest> lastRequestByKey = new ConcurrentHashMap<>();
    private final AtomicInteger requestCount = new AtomicInteger();
    private volatile HttpRequest lastRequest;
    private volatile ResponseFactory fallbackResponseFactory;

    /**
     * Creates a client with only a global FIFO queue.
     */
    public ScriptedHttpClient() {
        this(null);
    }

    /**
     * Creates a client with optional request-keyed FIFO queues.
     *
     * @param requestKeySelector Selects the queue key for each request.
     */
    public ScriptedHttpClient(Function<HttpRequest, String> requestKeySelector) {
        this.requestKeySelector = requestKeySelector;
    }

    /**
     * Sets a fallback response factory used when no queued response is available.
     *
     * @param responseFactory The fallback response factory.
     * @return This client.
     */
    public ScriptedHttpClient setFallbackResponseFactory(ResponseFactory responseFactory) {
        this.fallbackResponseFactory = Objects.requireNonNull(responseFactory, "responseFactory");
        return this;
    }

    /**
     * Queues a response that returns the given status code with no headers or body.
     *
     * @param statusCode The status code.
     * @return This client.
     */
    public ScriptedHttpClient enqueueResponse(int statusCode) {
        return enqueueResponse(statusCode, new HttpHeaders());
    }

    /**
     * Queues a response that returns the given status code and headers with no body.
     *
     * @param statusCode The status code.
     * @param headers The response headers.
     * @return This client.
     */
    public ScriptedHttpClient enqueueResponse(int statusCode, HttpHeaders headers) {
        return enqueueResponse(request -> new MockHttpResponse(request, statusCode, copyHeaders(headers)));
    }

    /**
     * Queues a response that returns the given status code, headers, and string body.
     *
     * @param statusCode The status code.
     * @param headers The response headers.
     * @param body The response body.
     * @return This client.
     */
    public ScriptedHttpClient enqueueResponse(int statusCode, HttpHeaders headers, String body) {
        return enqueueResponse(request -> new MockHttpResponse(request, statusCode, copyHeaders(headers),
            body == null ? null : body.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Queues a response that returns the given status code, headers, and binary body.
     *
     * @param statusCode The status code.
     * @param headers The response headers.
     * @param body The response body.
     * @return This client.
     */
    public ScriptedHttpClient enqueueResponse(int statusCode, HttpHeaders headers, byte[] body) {
        return enqueueResponse(request -> new MockHttpResponse(request, statusCode, copyHeaders(headers),
            body == null ? null : body.clone()));
    }

    /**
     * Queues a response that is built from the incoming request.
     *
     * @param responseFactory The response factory.
     * @return This client.
     */
    public ScriptedHttpClient enqueueResponse(ResponseFactory responseFactory) {
        globalResponses.add(new FactoryResponseAction(responseFactory));
        return this;
    }

    /**
     * Queues a request-keyed response that is built from the incoming request.
     *
     * @param requestKey The queue key.
     * @param responseFactory The response factory.
     * @return This client.
     */
    public ScriptedHttpClient enqueueResponse(String requestKey, ResponseFactory responseFactory) {
        enqueueKeyed(requestKey, new FactoryResponseAction(responseFactory));
        return this;
    }

    /**
     * Queues a failure for the next request.
     *
     * @param error The error to emit.
     * @return This client.
     */
    public ScriptedHttpClient enqueueFailure(Throwable error) {
        globalResponses.add(new FailureResponseAction(error));
        return this;
    }

    /**
     * Queues a request-keyed failure for the next request with the given key.
     *
     * @param requestKey The queue key.
     * @param error The error to emit.
     * @return This client.
     */
    public ScriptedHttpClient enqueueFailure(String requestKey, Throwable error) {
        enqueueKeyed(requestKey, new FailureResponseAction(error));
        return this;
    }

    /**
     * Queues a pending response that the test can complete later.
     *
     * @return The sink that completes the pending response.
     */
    public Sinks.One<HttpResponse> enqueuePendingResponse() {
        PendingResponseAction action = new PendingResponseAction();
        globalResponses.add(action);
        return action.sink;
    }

    /**
     * Queues a request-keyed pending response that the test can complete later.
     *
     * @param requestKey The queue key.
     * @return The sink that completes the pending response.
     */
    public Sinks.One<HttpResponse> enqueuePendingResponse(String requestKey) {
        PendingResponseAction action = new PendingResponseAction();
        enqueueKeyed(requestKey, action);
        return action.sink;
    }

    /**
     * Gets the total request count.
     *
     * @return The request count.
     */
    public int getRequestCount() {
        return requestCount.get();
    }

    /**
     * Gets the request count for a key.
     *
     * @param requestKey The queue key.
     * @return The request count for the key.
     */
    public int getRequestCount(String requestKey) {
        AtomicInteger count = requestCountsByKey.get(Objects.requireNonNull(requestKey, "requestKey"));
        return count == null ? 0 : count.get();
    }

    /**
     * Gets the most recent request.
     *
     * @return The most recent request.
     */
    public HttpRequest getLastRequest() {
        return lastRequest;
    }

    /**
     * Gets the most recent request for a key.
     *
     * @param requestKey The queue key.
     * @return The most recent request for the key.
     */
    public HttpRequest getLastRequest(String requestKey) {
        return lastRequestByKey.get(Objects.requireNonNull(requestKey, "requestKey"));
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        Objects.requireNonNull(request, "request");

        requestCount.incrementAndGet();
        lastRequest = request;

        String requestKey = requestKeySelector == null ? null : requestKeySelector.apply(request);
        if (requestKey != null) {
            requestCountsByKey.computeIfAbsent(requestKey, key -> new AtomicInteger()).incrementAndGet();
            lastRequestByKey.put(requestKey, request);
        }

        ResponseAction action = pollResponseAction(requestKey);
        if (action != null) {
            return action.send(request);
        }

        ResponseFactory responseFactory = fallbackResponseFactory;
        if (responseFactory != null) {
            return Mono.fromSupplier(() -> Objects.requireNonNull(responseFactory.create(request),
                "fallbackResponseFactory returned null"));
        }

        return Mono.error(new IllegalStateException("No scripted response available"
            + (requestKey == null ? "" : " for key '" + requestKey + "'") + "."));
    }

    private ResponseAction pollResponseAction(String requestKey) {
        ResponseAction response = pollKeyedResponse(requestKey);
        if (response != null) {
            return response;
        }

        return globalResponses.poll();
    }

    private ResponseAction pollKeyedResponse(String requestKey) {
        if (requestKey == null) {
            return null;
        }

        ConcurrentLinkedQueue<ResponseAction> responses = keyedResponses.get(requestKey);
        return responses == null ? null : responses.poll();
    }

    private void enqueueKeyed(String requestKey, ResponseAction action) {
        keyedResponses.computeIfAbsent(Objects.requireNonNull(requestKey, "requestKey"),
            key -> new ConcurrentLinkedQueue<>()).add(Objects.requireNonNull(action, "action"));
    }

    private static HttpHeaders copyHeaders(HttpHeaders headers) {
        return headers == null ? new HttpHeaders() : new HttpHeaders(headers);
    }

    private interface ResponseAction {
        Mono<HttpResponse> send(HttpRequest request);
    }

    private static final class FactoryResponseAction implements ResponseAction {
        private final ResponseFactory responseFactory;

        private FactoryResponseAction(ResponseFactory responseFactory) {
            this.responseFactory = Objects.requireNonNull(responseFactory, "responseFactory");
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.fromSupplier(() -> Objects.requireNonNull(responseFactory.create(request),
                "responseFactory returned null"));
        }
    }

    private static final class FailureResponseAction implements ResponseAction {
        private final Throwable error;

        private FailureResponseAction(Throwable error) {
            this.error = Objects.requireNonNull(error, "error");
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.error(error);
        }
    }

    private static final class PendingResponseAction implements ResponseAction {
        private final Sinks.One<HttpResponse> sink = Sinks.one();

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return sink.asMono();
        }
    }
}
