// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.http;

import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.util.Context;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * An HTTP Client that helps in providing assertions for invoking http client implementations.
 */
public final class AssertingClient implements HttpClient {
    private final HttpClient delegate;

    private final List<Predicate<HttpRequest>> syncAssertions;
    private final List<Predicate<HttpRequest>> asyncAssertions;
    private final BiFunction<HttpRequest, Context, Boolean> skipRequestBiFunction;

    AssertingClient(HttpClient delegate, List<Predicate<HttpRequest>> syncAssertions,
                    List<Predicate<HttpRequest>> asyncAssertions, BiFunction<HttpRequest, Context, Boolean> skipRequestBiFunction) {
        this.delegate = delegate;
        this.syncAssertions = syncAssertions;
        this.asyncAssertions = asyncAssertions;
        this.skipRequestBiFunction = skipRequestBiFunction;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        for (Predicate<HttpRequest> asyncAssertion : asyncAssertions) {
            if (!asyncAssertion.test(request)) {
                return Mono.error(new IllegalStateException("unexpected request"));
            }
        }
        return delegate.send(request);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        skipRequest(request, asyncAssertions, context);
        for (Predicate<HttpRequest> asyncAssertion : asyncAssertions) {
            if (!asyncAssertion.test(request)) {
                return Mono.error(new IllegalStateException("unexpected request"));
            }
        }
        return delegate.send(request, context);
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        skipRequest(request, syncAssertions, context);
        for (Predicate<HttpRequest> syncAssertion : syncAssertions) {
            if (!syncAssertion.test(request)) {
                throw new IllegalStateException("unexpected request");
            }
        }
        return delegate.sendSync(request, context);
    }

    private void skipRequest(HttpRequest request, List<Predicate<HttpRequest>> assertions, Context context) {

        if (skipRequestBiFunction != null && skipRequestBiFunction.apply(request, context) && !assertions.isEmpty()) {
            assertions.remove(assertions.size() - 1);
        }
    }
}
