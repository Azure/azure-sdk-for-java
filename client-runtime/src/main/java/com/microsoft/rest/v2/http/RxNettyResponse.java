/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Single;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;
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

    @Override
    public String headerValue(String headerName) {
        return rxnRes.getHeader(headerName);
    }

    @Override
    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        for (String headerName : rxnRes.getHeaderNames()) {
            headers.add(headerName, rxnRes.getHeader(headerName));
        }
        return headers;
    }

    private Single<ByteBuf> collectContent(boolean pooled) {
        // Reading entire response into memory-- not sure if this is OK
        int contentLength = (int) rxnRes.getContentLength();
        final ByteBuf collector;
        if (pooled) {
            collector = PooledByteBufAllocator.DEFAULT.heapBuffer(contentLength);
        } else {
            collector = UnpooledByteBufAllocator.DEFAULT.heapBuffer(contentLength);
        }
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
                                // Ensure to release upstream ByteBuf, this can be a PooledDirectBuf
                                ReferenceCountUtil.release(chunk);
                            }
                        })
                .toSingle();
        return collected;
    }

    @Override
    public Single<? extends InputStream> bodyAsInputStreamAsync() {
        return collectContent(true).map(new Func1<ByteBuf, InputStream>() {
            @Override
            public InputStream call(ByteBuf byteBuf) {
                return new ClosableByteBufInputStream(byteBuf);
            }
        });
    }

    @Override
    public Single<byte[]> bodyAsByteArrayAsync() {
        return collectContent(false).map(new Func1<ByteBuf, byte[]>() {
            @Override
            public byte[] call(ByteBuf byteBuf) {
                return byteBuf.array();
            }
        });
    }

    @Override
    public Single<String> bodyAsStringAsync() {
        return collectContent(true).map(new Func1<ByteBuf, String>() {
            @Override
            public String call(ByteBuf byteBuf) {
                try {
                    return byteBuf.toString(Charset.defaultCharset());
                } finally {
                    byteBuf.release();
                }
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
