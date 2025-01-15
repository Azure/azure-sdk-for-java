// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.IterableStream;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

class OnePageResponse<T> implements PagedResponse<T> {

    List<T> properties = null;

    OnePageResponse(List<T> properties) {
        this.properties = properties;
    }

    @Override
    public IterableStream<T> getElements() {
        Flux<T> flux = Flux.fromIterable(properties);
        return new IterableStream<T>(flux);
    }

    @Override
    public String getContinuationToken() {
        return null;
    }

    @Override
    public int getStatusCode() {
        return 0;
    }

    @Override
    public HttpHeaders getHeaders() {
        return null;
    }

    @Override
    public HttpRequest getRequest() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
