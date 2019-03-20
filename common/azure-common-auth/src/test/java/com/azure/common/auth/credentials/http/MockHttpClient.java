/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.auth.credentials.http;

import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpRequest;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.ProxyOptions;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * This HttpClient attempts to mimic the behavior of http://httpbin.org without ever making a network call.
 */
public class MockHttpClient implements HttpClient {
    private static final HttpResponse mockResponse = new MockHttpResponse(200);
    private final List<HttpRequest> requests;

    public MockHttpClient() {
        requests = new ArrayList<>();
    }

    public List<HttpRequest> requests() {
        return requests;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        requests.add(request);

        return Mono.just(mockResponse);
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
