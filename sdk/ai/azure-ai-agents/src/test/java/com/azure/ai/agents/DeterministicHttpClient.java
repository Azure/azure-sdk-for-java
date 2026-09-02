// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

final class DeterministicHttpClient implements HttpClient {
    private final Deque<Function<HttpRequest, HttpResponse>> responses = new ArrayDeque<>();
    private final List<HttpRequest> requests = new ArrayList<>();

    DeterministicHttpClient enqueueJson(int statusCode, String body) {
        return enqueue(statusCode, new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
            body.getBytes(StandardCharsets.UTF_8));
    }

    DeterministicHttpClient enqueueJson(int statusCode, HttpHeaders headers, String body) {
        HttpHeaders responseHeaders = new HttpHeaders(headers).set(HttpHeaderName.CONTENT_TYPE, "application/json");
        return enqueue(statusCode, responseHeaders, body.getBytes(StandardCharsets.UTF_8));
    }

    DeterministicHttpClient enqueueSse(String body) {
        return enqueue(200, new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/event-stream"),
            body.getBytes(StandardCharsets.UTF_8));
    }

    DeterministicHttpClient enqueue(int statusCode, HttpHeaders headers, byte[] body) {
        return enqueueResponse(request -> new MockHttpResponse(request, statusCode, headers, body));
    }

    DeterministicHttpClient enqueueResponse(Function<HttpRequest, HttpResponse> responseFactory) {
        responses.addLast(responseFactory);
        return this;
    }

    List<HttpRequest> getRequests() {
        return Collections.unmodifiableList(requests);
    }

    HttpRequest getRequest(int index) {
        return requests.get(index);
    }

    void assertResponsesConsumed() {
        if (!responses.isEmpty()) {
            throw new AssertionError(responses.size() + " deterministic response(s) were not consumed");
        }
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        requests.add(request);
        Function<HttpRequest, HttpResponse> responseFactory = responses.pollFirst();
        if (responseFactory == null) {
            return Mono.error(new IllegalStateException("No deterministic response queued for " + request.getUrl()));
        }
        return Mono.just(responseFactory.apply(request));
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        return send(request);
    }
}
