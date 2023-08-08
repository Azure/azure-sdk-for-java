// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

/**
 * An HttpClient instance that does not do anything.
 */
public class NoOpHttpClient implements HttpClient {

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return Mono.empty(); // NOP
    }

}
