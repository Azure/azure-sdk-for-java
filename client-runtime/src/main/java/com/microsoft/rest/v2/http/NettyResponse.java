/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import java.io.InputStream;
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
    public Single<? extends InputStream> bodyAsInputStreamAsync() {
        return collectContent().map(new Function<ByteBuf, InputStream>() {
            @Override
            public InputStream apply(ByteBuf byteBuf) {
                return new ClosableByteBufInputStream(byteBuf);
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
        if (byteBuf.hasArray()) {
            return byteBuf.array();
        } else {
            byte[] res = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(res);
            byteBuf.release();
            return res;
        }
    }

    @Override
    public Flowable<byte[]> streamBodyAsync() {
        return contentStream.map(new Function<ByteBuf, byte[]>() {
            @Override
            public byte[] apply(ByteBuf byteBuf) {
                return toByteArray(byteBuf);
            }
        });
    }

    @Override
    public Single<String> bodyAsStringAsync() {
        return collectContent().map(new Function<ByteBuf, String>() {
            @Override
            public String apply(ByteBuf byteBuf) {
                return byteBuf.toString(Charsets.UTF_8);
            }
        });
    }

    /**
     * Extends the ByreBufInputStream so that underlying ByteBuf can be returned to pool.
     */
    private class ClosableByteBufInputStream extends io.netty.buffer.ByteBufInputStream {
        private final ByteBuf buffer;

        ClosableByteBufInputStream(ByteBuf buffer) {
            super(buffer);
            this.buffer = buffer;
        }

        @Override
        public void close() {
            ReferenceCountUtil.release(this.buffer);
        }
    }
}
