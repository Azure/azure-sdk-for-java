// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

final class BatchClient implements HttpClient {
    private final Consumer<HttpRequest> sendCallback;

    BatchClient(Consumer<HttpRequest> sendCallback) {
        this.sendCallback = sendCallback;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        sendCallback.accept(request);
        return Mono.empty();
    }
}
