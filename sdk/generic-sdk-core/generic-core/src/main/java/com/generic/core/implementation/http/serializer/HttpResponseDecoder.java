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
    public HttpDecodedResponse decode(HttpResponse<?> response, HttpResponseDecodeData decodeData) {
        return new HttpDecodedResponse(response, this.serializer, decodeData);
    }

    /**
     * A decorated HTTP response that supports lazy decoding.
     */
    public static class HttpDecodedResponse extends HttpResponse<Object> {
        private final HttpResponse<?> response;
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
        HttpDecodedResponse(final HttpResponse<?> response, ObjectSerializer serializer,
                            HttpResponseDecodeData decodeData) {
            super(response.getRequest(), response.getBody().toBytes());

            this.response = response;
            this.serializer = serializer;
            this.decodeData = decodeData;
        }

        /**
         * Decodes the body in this response.
         *
         * @return The decoded body.
         */
        public Object getDecodedBody() {
            if (cachedBody == null) {
                cachedBody =
                    HttpResponseBodyDecoder.decodeByteArray(getBody().toBytes(), response, serializer, decodeData);
            }

            return cachedBody;
        }

        /**
         * Returns the cached decoded body in this response.
         *
         * @return The cached decoded body.
         */
        public Object getCachedBody() {
            return cachedBody;
        }

        /**
         * Set a decoded body to cache in this response.
         *
         * @param bodyToCache The decoded body to cache.
         */
        public void setCachedBody(Object bodyToCache) {
            cachedBody = bodyToCache;
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
        public Object getValue() {
            return getDecodedBody(); // getBody().toStream()? -> RestProxyImpl:173
        }

        @Override
        public void close() throws IOException {
            response.close();
        }
    }
}
