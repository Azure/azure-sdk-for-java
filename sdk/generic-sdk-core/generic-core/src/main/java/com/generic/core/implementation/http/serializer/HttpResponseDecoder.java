// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.serializer;

import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.Headers;
import com.generic.core.util.serializer.ObjectSerializer;

import java.io.Closeable;

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
    public HttpDecodedResponse decode(HttpResponse response, HttpResponseDecodeData decodeData) {
        return new HttpDecodedResponse(response, this.serializer, decodeData);
    }

    /**
     * A decorated HTTP response which has subscribable body and headers that supports lazy decoding.
     *
     * <p>Subscribing to body kickoff http content reading, it's decoding then emission of decoded object. Subscribing
     * to header kickoff header decoding and emission of decoded object.
     */
    public static class HttpDecodedResponse implements Closeable {
        private final HttpResponse response;
        private final ObjectSerializer serializer;
        private final HttpResponseDecodeData decodeData;
        private Object bodyCached;
        private Object headersCached;

        /**
         * Creates HttpDecodedResponse. Package private Ctr.
         *
         * @param response the publisher that emits the raw response upon subscription which needs to be decoded
         * @param serializer the decoder
         * @param decodeData the necessary data required to decode a Http response
         */
        HttpDecodedResponse(final HttpResponse response, ObjectSerializer serializer,
                            HttpResponseDecodeData decodeData) {
            this.response = response;
            this.serializer = serializer;
            this.decodeData = decodeData;
        }

        /**
         * @return get the raw response that this decoded response based on
         */
        public HttpResponse getSourceResponse() {
            return this.response;
        }

        /**
         * Decodes either the retrieved {@code body} or the bytes returned by the {@link HttpResponse}.
         *
         * @param body The retrieve body.
         *
         * @return The decoded body.
         */
        public Object getDecodedBody(byte[] body) {
            if (this.bodyCached == null) {
                this.bodyCached = HttpResponseBodyDecoder.decodeByteArray(body, response, serializer, decodeData);
            }

            return this.bodyCached;
        }

        /**
         * Gets the decoded {@link Headers} object.
         * <p>
         * Null is returned if the headers aren't able to be decoded or if there is no decoded headers type.
         *
         * @return The decoded headers object, or null if they aren't able to be decoded.
         */
        public Object getDecodedHeaders() {
            if (headersCached == null) {
                headersCached = HttpResponseHeaderDecoder.decode(response, serializer, decodeData.getHeadersType());
            }

            return this.headersCached;
        }

        @Override
        public void close() {
            this.response.close();
        }
    }
}
