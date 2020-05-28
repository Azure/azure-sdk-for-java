// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.core.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;

class ResponseUtils {
    private static byte[] EMPTY_BYTE_ARRAY = {};

    static Mono<StoreResponse> toStoreResponse(HttpResponse httpClientResponse, HttpRequest httpRequest) {

        HttpHeaders httpResponseHeaders = httpClientResponse.headers();

        Mono<byte[]> contentObservable;

        if (httpRequest.httpMethod() == HttpMethod.DELETE) {
            // for delete we don't expect any body
            contentObservable = Mono.just(EMPTY_BYTE_ARRAY);
        } else {
            contentObservable = httpClientResponse.bodyAsByteArray().switchIfEmpty(Mono.just(EMPTY_BYTE_ARRAY));
        }

        return contentObservable.map(byteArrayContent -> {
            // transforms to Mono<StoreResponse>
            com.azure.core.http.HttpHeaders responseHeaders = httpResponseHeaders;
            HttpUtils.OwnerFullName(responseHeaders);
            return new StoreResponse(httpClientResponse.statusCode(), responseHeaders, byteArrayContent);
        });
    }
}
