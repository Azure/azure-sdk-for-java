// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;

class ResponseUtils {
    private static final byte[] EMPTY_BYTE_ARRAY = {};

    static Mono<StoreResponse> toStoreResponse(HttpResponse httpClientResponse, HttpRequest httpRequest) {

        HttpHeaders httpResponseHeaders = httpClientResponse.headers();

        Mono<byte[]> contentObservable = httpClientResponse.bodyAsByteArray().switchIfEmpty(Mono.just(EMPTY_BYTE_ARRAY));

        return contentObservable.map(byteArrayContent -> {
            // transforms to Mono<StoreResponse>
            return new StoreResponse(httpClientResponse.statusCode(), HttpUtils.unescape(httpResponseHeaders.toMap().entrySet()), byteArrayContent);
        });
    }
}
