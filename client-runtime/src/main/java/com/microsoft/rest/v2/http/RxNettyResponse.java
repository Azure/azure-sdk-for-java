package com.microsoft.rest.v2.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.reactivex.netty.channel.ContentSource;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;
import rx.Single;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class RxNettyResponse extends HttpResponse {
    private final HttpClientResponse<ByteBuf> rxnRes;

    public RxNettyResponse(HttpClientResponse<ByteBuf> rxnRes) {
        this.rxnRes = rxnRes;
    }

    private Single<ByteBuf> collectContent() {
        // Reading entire response into memory-- not sure if this is OK
        int contentLength = (int)rxnRes.getContentLength();
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
    public Single<? extends InputStream> getBodyAsInputStreamAsync() {
        return collectContent().map(new Func1<ByteBuf, InputStream>() {
            @Override
            public InputStream call(ByteBuf byteBuf) {
                return new ByteArrayInputStream(byteBuf.array());
            }
        });
    }

    @Override
    public Single<byte[]> getBodyAsByteArrayAsync() {
        return collectContent().map(new Func1<ByteBuf, byte[]>() {
            @Override
            public byte[] call(ByteBuf byteBuf) {
                return byteBuf.array();
            }
        });
    }

    @Override
    public Single<String> getBodyAsStringAsync() {
        return collectContent().map(new Func1<ByteBuf, String>() {
            @Override
            public String call(ByteBuf byteBuf) {
                return byteBuf.toString(Charset.defaultCharset());
            }
        });
    }
}
