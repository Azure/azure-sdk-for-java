/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map.Entry;

/**
 * A HttpResponse that is implemented using Netty.
 */
class NettyResponse extends HttpResponse {
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private final io.netty.handler.codec.http.HttpResponse rxnRes;
    private final long contentLength;
    private final Observable<ByteBuf> emitter;

    NettyResponse(io.netty.handler.codec.http.HttpResponse rxnRes, Observable<ByteBuf> emitter) {
        this.rxnRes = rxnRes;
        this.contentLength = getContentLength(rxnRes);
        this.emitter = emitter;
    }

    private static long getContentLength(io.netty.handler.codec.http.HttpResponse rxnRes) {
        long result;
        try {
            result = Long.parseLong(rxnRes.headers().get(HEADER_CONTENT_LENGTH));
        } catch (NullPointerException | NumberFormatException e) {
            result = 0;
        }
        return result;
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
            headers.add(header.getKey(), header.getValue());
        }
        return headers;
    }

    private Single<ByteBuf> collectContent() {
        return emitter.toList().map(new Func1<List<ByteBuf>, ByteBuf>() {
            @Override
            public ByteBuf call(List<ByteBuf> l) {
                ByteBuf[] bufs = new ByteBuf[l.size()];
                return Unpooled.wrappedBuffer(l.toArray(bufs));
            }
        }).toSingle();
    }

    @Override
    public Single<? extends InputStream> bodyAsInputStreamAsync() {
        return collectContent().map(new Func1<ByteBuf, InputStream>() {
            @Override
            public InputStream call(ByteBuf byteBuf) {
                return new ClosableByteBufInputStream(byteBuf);
            }
        });
    }

    @Override
    public Single<byte[]> bodyAsByteArrayAsync() {
        return collectContent().map(new Func1<ByteBuf, byte[]>() {
            @Override
            public byte[] call(ByteBuf byteBuf) {
                if (byteBuf.hasArray()) {
                    return byteBuf.array();
                } else {
                    byte[] res = new byte[byteBuf.readableBytes()];
                    byteBuf.readBytes(res);
                    byteBuf.release();
                    return res;
                }
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
