/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import rx.Observable;
import rx.Single;
import rx.functions.Func1;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

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
    public Single<? extends InputStream> bodyAsInputStreamAsync() {
        return bodyAsByteArrayAsync()
            .map(new Func1<byte[], InputStream>() {
                @Override
                public InputStream call(byte[] bytes) {
                    return new ByteArrayInputStream(bytes);
                }
            });
    }

    @Override
    public Single<byte[]> bodyAsByteArrayAsync() {
        if (body == null) {
            body = innerHttpResponse.bodyAsByteArrayAsync()
                    .map(new Func1<byte[], byte[]>() {
                        @Override
                        public byte[] call(byte[] bytes) {
                            body = Single.just(bytes);
                            return bytes;
                        }
                    });
        }
        return body;
    }

    @Override
    public Observable<byte[]> streamBodyAsync() {
        // FIXME: maybe need to enable streaming/collecting in here
        return bodyAsByteArrayAsync().toObservable();
    }

    @Override
    public Single<String> bodyAsStringAsync() {
        return bodyAsByteArrayAsync()
                .map(new Func1<byte[], String>() {
                    @Override
                    public String call(byte[] bytes) {
                        return bytes == null ? null : new String(bytes);
                    }
                });
    }

    @Override
    public BufferedHttpResponse buffer() {
        return this;
    }
}
