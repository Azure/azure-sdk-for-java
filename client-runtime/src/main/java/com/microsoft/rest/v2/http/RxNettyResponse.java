/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Single;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * A HttpResponse that is implemented using RxNetty.
 */
class RxNettyResponse extends HttpResponse {
    private final HttpClientResponse<ByteBuf> rxnRes;

    RxNettyResponse(HttpClientResponse<ByteBuf> rxnRes) {
        this.rxnRes = rxnRes;
    }

    @Override
    public int statusCode() {
        return rxnRes.getStatus().code();
    }

    private Single<ByteBuf> collectContent() {
        // Reading entire response into memory-- not sure if this is OK
        int contentLength = (int) rxnRes.getContentLength();
        final ByteBuf collector = Unpooled.buffer(contentLength);
        Single<ByteBuf> collected = rxnRes.getContent()
                .collect(
                        new Func0<ByteBuf>() {
                            @Override
                            public ByteBuf call() {
                                return collector;
                            }
                        },
                        new Action2<ByteBuf, ByteBuf>() {
                            @Override
                            public void call(ByteBuf collector, ByteBuf chunk) {
                                collector.writeBytes(chunk);
                            }
                        })
                .toSingle();
        return collected;
    }

    @Override
    public Single<? extends InputStream> bodyAsInputStreamAsync() {
        return collectContent().map(new Func1<ByteBuf, InputStream>() {
            @Override
            public InputStream call(ByteBuf byteBuf) {
                return new ByteArrayInputStream(byteBuf.array());
            }
        });
    }

    @Override
    public Single<byte[]> bodyAsByteArrayAsync() {
        return collectContent().map(new Func1<ByteBuf, byte[]>() {
            @Override
            public byte[] call(ByteBuf byteBuf) {
                return byteBuf.array();
            }
        });
    }

    @Override
    public Single<String> bodyAsStringAsync() {
        return collectContent().map(new Func1<ByteBuf, String>() {
            @Override
            public String call(ByteBuf byteBuf) {
                return byteBuf.toString(Charset.defaultCharset());
            }
        });
    }
}
