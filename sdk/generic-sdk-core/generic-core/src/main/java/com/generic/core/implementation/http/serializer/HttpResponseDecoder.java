// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.serializer;

import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.Headers;
import com.generic.core.util.serializer.ObjectSerializer;

import java.io.IOException;

/**
 * Decode {@link HttpResponse} to {@link HttpDecodedResponse}.
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
     * Synchronously decodes a {@link HttpResponse}.
     *
     * @param response The response to be decoded.
     * @param decodeData The necessary data required to decode the response.
     *
     * @return The decoded HttpResponse.
     */
    public HttpDecodedResponse<?> decode(HttpResponse<?> response, HttpResponseDecodeData decodeData) {
        return new HttpDecodedResponse<>(response, this.serializer, decodeData);
    }

    /**
     * A decorated HTTP response that supports lazy decoding.
     */
    public static class HttpDecodedResponse<T> extends HttpResponse<T> {
        private final HttpResponse<T> response;
        private final ObjectSerializer serializer;
        private final HttpResponseDecodeData decodeData;
        private Object cachedBody;

        /**
         * Creates HttpDecodedResponse. Package private Ctr.
         *
         * @param response the publisher that emits the raw response upon subscription which needs to be decoded
         * @param serializer the decoder
         * @param decodeData the necessary data required to decode a Http response
         */
        HttpDecodedResponse(final HttpResponse<T> response, ObjectSerializer serializer,
                            HttpResponseDecodeData decodeData) {
            super(response.getRequest(), response.getBody().toBytes());

            this.response = response;
            this.serializer = serializer;
            this.decodeData = decodeData;
        }

        /**
         * @return get the raw response that this decoded response based on
         */
        public HttpResponse<T> getSourceResponse() {
            return response;
        }

        /**
         * Decodes either the retrieved {@code body} or the bytes returned by the {@link HttpResponse}.
         *
         * @param body The retrieve body.
         *
         * @return The decoded body.
         */
        public Object getDecodedBody(byte[] body) {
            if (cachedBody == null) {
                cachedBody = HttpResponseBodyDecoder.decodeByteArray(body, response, serializer, decodeData);
            }

            return cachedBody;
        }

        @Override
        public int getStatusCode() {
            return response.getStatusCode();
        }

        @Override
        public Headers getHeaders() {
            return response.getHeaders();
        }

        @Override
        public T getValue() {
            return response.getValue(); // getBody().toStream()? -> RestProxyImpl:173
        }

        @Override
        public void close() throws IOException {
            this.response.close();
        }
    }
}
