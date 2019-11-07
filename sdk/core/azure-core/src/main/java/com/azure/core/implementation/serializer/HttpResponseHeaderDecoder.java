// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.serializer.SerializerAdapter;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Decoder to decode header of HTTP response.
 */
final class HttpResponseHeaderDecoder {
    /**
     * Decode headers of the http response.
     *
     * The decoding happens when caller subscribed to the returned {@code Mono<Object>},
     * if the response header is not decodable then {@code Mono.empty()} will be returned.
     *
     * @param httpResponse the response containing the headers to be decoded
     * @param serializer the adapter to use for decoding
     * @param decodeData the necessary data required to decode a Http response
     * @return publisher that emits decoded response header upon subscription if header is decodable,
     *     no emission if the header is not-decodable
     */
    static Mono<Object> decode(HttpResponse httpResponse, SerializerAdapter serializer,
                               HttpResponseDecodeData decodeData) {
        Type headerType = decodeData.getHeadersType();
        if (headerType == null) {
            return Mono.empty();
        } else {
            return Mono.fromCallable(() ->
                    serializer.deserialize(httpResponse.getHeaders(), decodeData.getHeadersType()))
                .onErrorResume(IOException.class, e -> Mono.error(new HttpResponseException(
                    "HTTP response has malformed headers", httpResponse, e)));
        }
    }
}
