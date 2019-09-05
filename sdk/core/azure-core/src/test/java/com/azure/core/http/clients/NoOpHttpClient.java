// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.clients;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

public class NoOpHttpClient implements HttpClient {

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return Mono.empty(); // NOP
    }

}
