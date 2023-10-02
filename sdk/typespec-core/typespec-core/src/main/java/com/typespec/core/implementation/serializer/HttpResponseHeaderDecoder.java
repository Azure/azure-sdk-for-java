// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.serializer;

import com.typespec.core.exception.HttpResponseException;
import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.util.serializer.SerializerAdapter;
import reactor.core.Exceptions;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * {@link HttpResponse} {@link HttpHeaders} decoder.
 */
final class HttpResponseHeaderDecoder {
    private static final String MALFORMED_HEADERS_MESSAGE = "HTTP response has malformed headers";

    /**
     * Decodes the {@link HttpHeaders} in an {@link HttpResponse}.
     * <p>
     * If there is no decoded headers type for the API that was called null will be returned.
     *
     * @param response The response containing the headers to decode.
     * @param serializer The adapter that will perform decoding.
     * @param decodedHeadersType The decoded headers type.
     * @return The decoded headers object, or null if {@code decodedHeadersType} is null.
     * @throws HttpResponseException If the response headers fail to decode to the decoded headers type.
     */
    static Object decode(HttpResponse response, SerializerAdapter serializer, Type decodedHeadersType) {
        // There is no type to decode into, return null.
        if (decodedHeadersType == null) {
            return null;
        }

        try {
            return serializer.deserialize(response.getHeaders(), decodedHeadersType);
        } catch (IOException ex) {
            throw Exceptions.propagate(new HttpResponseException(MALFORMED_HEADERS_MESSAGE, response, ex));
        }
    }
}
