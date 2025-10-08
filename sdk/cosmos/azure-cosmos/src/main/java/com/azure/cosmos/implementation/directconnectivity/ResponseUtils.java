// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import reactor.core.publisher.Mono;

class ResponseUtils {
    private static final byte[] EMPTY_BYTE_ARRAY = {};

    static Mono<StoreResponse> toStoreResponse(HttpResponse httpClientResponse, String endpoint) {

        HttpHeaders httpResponseHeaders = httpClientResponse.headers();

        Mono<ByteBuf> contentObservable = httpClientResponse.body().switchIfEmpty(Mono.just(Unpooled.EMPTY_BUFFER));

        return contentObservable.map(byteBufContent -> {
            // transforms to Mono<StoreResponse>
            int size = 0;
            if (byteBufContent == null || (size = byteBufContent.readableBytes()) == 0) {
                try {
                    return new StoreResponse(
                        endpoint,
                        httpClientResponse.statusCode(),
                        HttpUtils.unescape(httpResponseHeaders.toMap()),
                        null,
                        0,
                        null);
                } catch (Exception e) {
                    throw reactor.core.Exceptions.propagate(e);
                }
            }

            try {
                return new StoreResponse(
                    endpoint,
                    httpClientResponse.statusCode(),
                    HttpUtils.unescape(httpResponseHeaders.toMap()),
                    new ByteBufInputStream(byteBufContent, true),
                    size,
                    null);
            } catch (Exception e) {
                throw reactor.core.Exceptions.propagate(e);
            }
        });
    }
}
