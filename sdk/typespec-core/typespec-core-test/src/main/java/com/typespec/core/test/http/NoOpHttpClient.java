// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.http;

import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
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
