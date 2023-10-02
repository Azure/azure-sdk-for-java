// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.serializer;

import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.util.serializer.SerializerAdapter;
import reactor.core.publisher.Mono;

import java.io.Closeable;

/**
 * Decode {@link HttpResponse} to {@link HttpDecodedResponse}.
 */
public final class HttpResponseDecoder {
    // The adapter for deserialization
    private final SerializerAdapter serializer;

    /**
     * Creates HttpResponseDecoder.
     *
     * @param serializer the serializer
     */
    public HttpResponseDecoder(SerializerAdapter serializer) {
        this.serializer = serializer;
    }

    /**
     * Asynchronously decodes a {@link HttpResponse}.
     *
     * @param response the publisher that emits response to be decoded
     * @param decodeData the necessary data required to decode the response emitted by {@code response}
     * @return a publisher that emits decoded HttpResponse upon subscription
     */
    public Mono<HttpDecodedResponse> decode(Mono<HttpResponse> response, HttpResponseDecodeData decodeData) {
        return response.map(r -> new HttpDecodedResponse(r, this.serializer, decodeData));
    }

    /**
     * Synchronously decodes a {@link HttpResponse}.
     *
     * @param response the response to be decoded
     * @param decodeData the necessary data required to decode the response
     * @return the decoded HttpResponse
     */
    public HttpDecodedResponse decodeSync(HttpResponse response, HttpResponseDecodeData decodeData) {
        return new HttpDecodedResponse(response, this.serializer, decodeData);
    }

    /**
     * A decorated HTTP response which has subscribable body and headers that supports lazy decoding.
     * <p>
     * Subscribing to body kickoff http content reading, it's decoding then emission of decoded object. Subscribing to
     * header kickoff header decoding and emission of decoded object.
     */
    public static class HttpDecodedResponse implements Closeable {
        private final HttpResponse response;
        private final SerializerAdapter serializer;
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
        HttpDecodedResponse(final HttpResponse response, SerializerAdapter serializer,
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
         * @return The decoded body.
         */
        public Object getDecodedBody(byte[] body) {
            if (this.bodyCached == null) {
                this.bodyCached = HttpResponseBodyDecoder.decodeByteArray(body, response, serializer, decodeData);
            }
            return this.bodyCached;
        }

        /**
         * Gets the decoded {@link HttpHeaders} object.
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
