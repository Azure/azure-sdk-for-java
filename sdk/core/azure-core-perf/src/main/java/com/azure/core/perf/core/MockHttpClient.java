// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class MockHttpClient implements HttpClient {
    private final Function<HttpRequest, HttpResponse> responseSupplier;

    public MockHttpClient(Function<HttpRequest, HttpResponse> responseSupplier) {
        this.responseSupplier = responseSupplier;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return Mono.just(responseSupplier.apply(request));
    }
}
