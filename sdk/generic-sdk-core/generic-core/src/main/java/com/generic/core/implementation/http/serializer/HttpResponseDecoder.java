// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.serializer;

import com.generic.core.http.models.HttpResponse;
import com.generic.core.util.serializer.ObjectSerializer;

/**
 * Decode an {@link HttpResponse}.
 */
public final class HttpResponseDecoder {
    // The adapter for deserialization.
    private final ObjectSerializer serializer;

    /**
     * Creates HttpResponseDecoder.
     *
     * @param serializer The serializer.
     */
    public HttpResponseDecoder(ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * Decodes a {@link HttpResponse}.
     *
     * @param response The response to be decoded.
     * @param decodeData The necessary data required to decode the response.
     *
     * @return The decoded {@link HttpResponse}.
     */
    public HttpResponse<?> decode(HttpResponse<?> response, HttpResponseDecodeData decodeData) {
        response.setSerializer(this.serializer);
        response.setDecodeData(decodeData);

        return response;
    }
}
