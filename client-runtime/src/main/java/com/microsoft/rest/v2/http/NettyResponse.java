/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import org.reactivestreams.Subscription;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;

/**
 * A HttpResponse that is implemented using Netty.
 */
class NettyResponse extends HttpResponse {
    private final io.netty.handler.codec.http.HttpResponse rxnRes;
    private final Flowable<ByteBuf> contentStream;

    NettyResponse(io.netty.handler.codec.http.HttpResponse rxnRes, Flowable<ByteBuf> emitter) {
        this.rxnRes = rxnRes;
        this.contentStream = emitter;
    }

    @Override
    public int statusCode() {
        return rxnRes.status().code();
    }

    @Override
    public String headerValue(String headerName) {
        return rxnRes.headers().get(headerName);
    }

    @Override
    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        for (Entry<String, String> header : rxnRes.headers()) {
            headers.set(header.getKey(), header.getValue());
        }
        return headers;
    }

    private Single<ByteBuf> collectContent() {
        return contentStream.toList().map(new Function<List<ByteBuf>, ByteBuf>() {
            @Override
            public ByteBuf apply(List<ByteBuf> l) {
                ByteBuf[] bufs = new ByteBuf[l.size()];
                return Unpooled.wrappedBuffer(l.toArray(bufs));
            }
        });
    }

    @Override
    public Single<byte[]> bodyAsByteArrayAsync() {
        return collectContent().map(new Function<ByteBuf, byte[]>() {
            @Override
            public byte[] apply(ByteBuf byteBuf) {
                return toByteArray(byteBuf);
            }
        });
    }

    static byte[] toByteArray(ByteBuf byteBuf) {
        byte[] res = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(res);
        byteBuf.release();
        return res;
    }

    @Override
    public Flowable<ByteBuffer> streamBodyAsync() {
        return contentStream.map(new Function<ByteBuf, ByteBuffer>() {
            @Override
            public ByteBuffer apply(ByteBuf byteBuf) {
                ByteBuffer dst = ByteBuffer.allocate(byteBuf.readableBytes());
                byteBuf.readBytes(dst);
                byteBuf.release();
                dst.flip();
                return dst;
            }
        });
    }

    @Override
    public Single<String> bodyAsStringAsync() {
        return collectContent().map(new Function<ByteBuf, String>() {
            @Override
            public String apply(ByteBuf byteBuf) {
                return byteBuf.toString(StandardCharsets.UTF_8);
            }
        });
    }

    @Override
    public void close() {
        contentStream.subscribe(new FlowableSubscriber<ByteBuf>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.cancel();
            }

            @Override
            public void onNext(ByteBuf byteBuf) {
                // no-op
            }

            @Override
            public void onError(Throwable ignored) {
                // May receive a "multiple subscription not allowed" error here, but we don't care
            }

            @Override
            public void onComplete() {
                // no-op
            }
        });
    }
}
