// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.serializer.SerializerAdapter;
import reactor.core.Exceptions;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Decoder to decode header of HTTP response.
 */
final class HttpResponseHeaderDecoder {
    private static final String MALFORMED_HEADERS_MESSAGE = "HTTP response has malformed headers";

    /**
     * Decode headers of the http response.
     *
     * The decoding happens when caller subscribed to the returned {@code Mono<Object>}, if the response header is not
     * decodable then {@code Mono.empty()} will be returned.
     *
     * @param response the response containing the headers to be decoded
     * @param serializer the adapter to use for decoding
     * @param decodeData the necessary data required to decode a Http response
     * @return publisher that emits decoded response header upon subscription if header is decodable, no emission if the
     * header is not-decodable
     */
    static Object decode(HttpResponse response, SerializerAdapter serializer, HttpResponseDecodeData decodeData) {
        Type headerType = decodeData.getHeadersType();
        if (headerType == null) {
            return null;
        }

        try {
            return serializer.deserialize(response.getHeaders(), headerType);
        } catch (IOException ex) {
            throw Exceptions.propagate(new HttpResponseException(MALFORMED_HEADERS_MESSAGE, response, ex));
        }
    }
}
