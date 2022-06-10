// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.perf.models.MockHttpResponse;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class MockHttpClient implements HttpClient {

    private final Function<HttpRequest, HttpResponse> responseSupplier;

    public MockHttpClient(Function<HttpRequest, HttpResponse> responseSupplier) {
        if (responseSupplier == null) {
            this.responseSupplier = request -> new MockHttpResponse(request, 200);
        } else {
            this.responseSupplier = responseSupplier;
        }
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        String url = request.getUrl().toString();
        if (url.contains("RawData") || url.contains("UserDatabase") || url.contains("BinaryData")) {
            if (request.getHttpMethod() == HttpMethod.GET || request.getHttpMethod() == HttpMethod.HEAD) {
                return Mono.just(responseSupplier.apply(request));
            } else {
                // consume body
                return FluxUtil.collectBytesInByteBufferStream(request.getBody())
                    .map(bytes -> responseSupplier.apply(request));
            }
        } else {
            return Mono.<HttpResponse>just(new MockHttpResponse(request, 404));
        }
    }
}
