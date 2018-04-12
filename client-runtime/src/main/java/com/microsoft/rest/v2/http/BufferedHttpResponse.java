/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import org.reactivestreams.Publisher;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * HTTP response which will buffer the response's body when/if it is read.
 */
public final class BufferedHttpResponse extends HttpResponse {
    private final HttpResponse innerHttpResponse;
    private Single<byte[]> body;

    /**
     * Creates a buffered HTTP response.
     * @param innerHttpResponse The HTTP response to buffer.
     */
    public BufferedHttpResponse(HttpResponse innerHttpResponse) {
        this.innerHttpResponse = innerHttpResponse;
        this.body = null;
    }

    @Override
    public int statusCode() {
        return innerHttpResponse.statusCode();
    }

    @Override
    public String headerValue(String headerName) {
        return innerHttpResponse.headerValue(headerName);
    }

    @Override
    public HttpHeaders headers() {
        return innerHttpResponse.headers();
    }

    @Override
    public Single<byte[]> bodyAsByteArray() {
        if (body == null) {
            body = innerHttpResponse.bodyAsByteArray()
                    .map(new Function<byte[], byte[]>() {
                        @Override
                        public byte[] apply(byte[] bytes) {
                            body = Single.just(bytes);
                            return bytes;
                        }
                    });
        }
        return body;
    }

    @Override
    public Flowable<ByteBuffer> body() {
        return bodyAsByteArray().flatMapPublisher(new Function<byte[], Publisher<? extends ByteBuffer>>() {
            @Override
            public Publisher<? extends ByteBuffer> apply(byte[] bytes) throws Exception {
                return Flowable.just(ByteBuffer.wrap(bytes));
            }
        });
    }

    @Override
    public Single<String> bodyAsString() {
        return bodyAsByteArray()
                .map(new Function<byte[], String>() {
                    @Override
                    public String apply(byte[] bytes) {
                        return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
                    }
                });
    }

    @Override
    public BufferedHttpResponse buffer() {
        return this;
    }

    @Override
    public Object deserializedHeaders() {
        return innerHttpResponse.deserializedHeaders();
    }

    @Override
    public HttpResponse withDeserializedHeaders(Object deserializedHeaders) {
        innerHttpResponse.withDeserializedHeaders(deserializedHeaders);
        return this;
    }

    @Override
    public Object deserializedBody() {
        return innerHttpResponse.deserializedBody();
    }

    @Override
    public HttpResponse withDeserializedBody(Object deserializedBody) {
        innerHttpResponse.withDeserializedBody(deserializedBody);
        return this;
    }
}
