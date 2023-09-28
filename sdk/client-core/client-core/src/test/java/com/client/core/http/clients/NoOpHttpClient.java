// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.http.clients;

import com.client.core.http.HttpClient;
import com.client.core.http.HttpRequest;
import com.client.core.http.HttpResponse;
import reactor.core.publisher.Mono;

public class NoOpHttpClient implements HttpClient {

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return Mono.empty(); // NOP
    }

}
