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
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;
import org.reactivestreams.Subscription;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
        ByteBuf allContent = null;
        String contentLengthString = headerValue("Content-Length");
        if (contentLengthString != null) {
            try {
                int contentLength = Integer.parseInt(contentLengthString);
                allContent = Unpooled.buffer(contentLength);
            } catch (NumberFormatException ignored) {
            }
        }

        if (allContent == null) {
            allContent = Unpooled.buffer();
        }

        return contentStream.collectInto(allContent, new BiConsumer<ByteBuf, ByteBuf>() {
            @Override
            public void accept(ByteBuf allContent, ByteBuf chunk) throws Exception {
                //use try-finally to ensure chunk gets released
                try {
                    allContent.writeBytes(chunk);
                }
                finally {
                    chunk.release();
                }
            }
        });
    }

    @Override
    public Single<byte[]> bodyAsByteArray() {
        return collectContent().map(new Function<ByteBuf, byte[]>() {
            @Override
            public byte[] apply(ByteBuf byteBuf) throws Exception {
                byte[] result;
                if (byteBuf.readableBytes() == byteBuf.array().length) {
                    result = byteBuf.array();
                } else {
                    byte[] dst = new byte[byteBuf.readableBytes()];
                    byteBuf.readBytes(dst);
                    result = dst;
                }

                // This byteBuf is not pooled but Netty uses ref counting to track allocation metrics
                byteBuf.release();
                return result;
            }
        });
    }

    @Override
    public Flowable<ByteBuffer> body() {
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
    public Single<String> bodyAsString() {
        return collectContent().map(new Function<ByteBuf, String>() {
            @Override
            public String apply(ByteBuf byteBuf) throws Exception {
                String result = byteBuf.toString(StandardCharsets.UTF_8);
                byteBuf.release();
                return result;
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
