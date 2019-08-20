// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.clients;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public class NoOpHttpClient implements HttpClient {

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return Mono.empty(); // NOP
    }

    @Override
    public HttpClient proxy(Supplier<ProxyOptions> proxyOptions) {
        throw new IllegalStateException("MockHttpClient.proxy");
    }

    @Override
    public HttpClient wiretap(boolean enableWiretap) {
        throw new IllegalStateException("MockHttpClient.wiretap");
    }

    @Override
    public HttpClient port(int port) {
        throw new IllegalStateException("MockHttpClient.port");
    }
}
