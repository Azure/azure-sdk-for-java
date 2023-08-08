// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

public class NullHttpClient implements HttpClient {
    @Override
    public Mono<HttpResponse> send(HttpRequest httpRequest) {
        return Mono.just(new NullHttpResponse(httpRequest));
    }
}
