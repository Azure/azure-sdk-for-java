// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.clients;

import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import reactor.core.publisher.Mono;

public class NoOpHttpClient implements HttpClient {

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return Mono.empty(); // NOP
    }

}
