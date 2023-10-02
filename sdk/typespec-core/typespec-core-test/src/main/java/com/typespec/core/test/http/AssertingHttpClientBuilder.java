// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.http;

import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.util.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * HTTP client builder that helps in running sync and async assertion checks.
 */
public class AssertingHttpClientBuilder {
    private final HttpClient delegate;

    private final List<Predicate<HttpRequest>> syncAssertions;
    private final List<Predicate<HttpRequest>> asyncAssertions;
    private BiFunction<HttpRequest, Context, Boolean> biFunction;

    /**
     * Create an instance of {@link AssertingHttpClientBuilder} with the provided HttpClient.
     * @param delegate the HttpClient.
     */
    public AssertingHttpClientBuilder(HttpClient delegate) {
        this.delegate = delegate;
        syncAssertions = new ArrayList<>();
        asyncAssertions = new ArrayList<>();
    }

    /**
     * Asserts that only sync implementations are invoked from within the implementation call stack.
     * Cause tests to fail if async {@link HttpClient#send(HttpRequest) or HttpClient#send(HttpRequest, Context)} invoked.
     * @return the AssertingHttpClientBuilder itself.
     */
    public AssertingHttpClientBuilder assertSync() {
        asyncAssertions.add(request -> false);
        return this;
    }

    /**
     * Asserts that only async implementations are invoked from within the implementation call stack.
     * Cause tests to fail if sync {@link HttpClient#sendSync(HttpRequest, Context)} is invoked.
     * @return the AssertingHttpClientBuilder itself.
     */
    public AssertingHttpClientBuilder assertAsync() {
        syncAssertions.add(request -> false);
        return this;
    }

    /**
     * Creates a new {@link com.typespec.core.http.HttpClient Assertion Http client} instance
     * that helps in providing assertions for invoking http client implementations.
     *
     * @return A new {@link AssertingClient} instance.
     */
    public HttpClient build() {
        return new AssertingClient(this.delegate, syncAssertions, asyncAssertions, biFunction);
    }

    /**
     * Method used to specify http requests to be skipped when executing assertions.
     * @param skipRequestFunction the function used to specify http requests to be skipped for sync assertions.
     * @return the AssertingHttpClientBuilder itself.
     */
    public AssertingHttpClientBuilder skipRequest(BiFunction<HttpRequest, Context, Boolean> skipRequestFunction) {
        this.biFunction = skipRequestFunction;
        return this;
    }
}
