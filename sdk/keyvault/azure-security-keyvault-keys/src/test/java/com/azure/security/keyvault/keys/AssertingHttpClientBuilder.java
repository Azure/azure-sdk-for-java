// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * HTTP client builder that helps in running sync and async assertion checks.
 */
public class AssertingHttpClientBuilder {
    private final HttpClient delegate;

    private final List<Predicate<HttpRequest>> syncAssertions;
    private final List<Predicate<HttpRequest>> asyncAssertions;

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
     * Creates a new {@link com.azure.core.http.HttpClient Assertion Http client} instance
     * that helps in providing assertions for invoking http client implementations.
     *
     * @return A new {@link AssertingClient} instance.
     */
    public HttpClient build() {
        return new AssertingClient(this.delegate, syncAssertions, asyncAssertions);
    }
}
