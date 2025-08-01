// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpRequest;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.DelegatingDecompressorFrameListener;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandler;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapterBuilder;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.http.netty4.implementation.Netty4Utility.createCodec;
import static io.clientcore.http.netty4.implementation.Netty4Utility.sendHttp11Request;
import static io.clientcore.http.netty4.implementation.Netty4Utility.setOrSuppressError;

/**
 * Handler that deals with application protocol negotiation (ALPN) and configures the {@link ChannelPipeline} to use
 * either HTTP/1.1 or HTTP/2 based on the result of negotiation.
 */
public final class Netty4AlpnHandler extends ApplicationProtocolNegotiationHandler {
    private static final int TWO_FIFTY_SIX_KB = 256 * 1024;
    private final HttpRequest request;
    private final AtomicReference<ResponseStateInfo> responseReference;
    private final AtomicReference<Throwable> errorReference;
    private final CountDownLatch latch;

    /**
     * Creates a new instance of {@link Netty4AlpnHandler} with a fallback to using HTTP/1.1.
     *
     * @param request The request to send once ALPN negotiation completes.
     * @param errorReference An AtomicReference keeping track of errors during the request lifecycle.
     * @param latch A CountDownLatch that will be released once the request completes.
     */
    public Netty4AlpnHandler(HttpRequest request, AtomicReference<ResponseStateInfo> responseReference,
        AtomicReference<Throwable> errorReference, CountDownLatch latch) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.request = request;
        this.responseReference = responseReference;
        this.errorReference = errorReference;
        this.latch = latch;
    }

    @Override
    public boolean isSharable() {
        return false;
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            // TODO (alzimmer): InboundHttp2ToHttpAdapter buffers the entire response into a FullHttpResponse. Need to
            //  create a streaming version of this to support huge response payloads.
            Http2Connection http2Connection = new DefaultHttp2Connection(false);
            Http2Settings settings = new Http2Settings().headerTableSize(4096)
                .maxHeaderListSize(TWO_FIFTY_SIX_KB)
                .pushEnabled(false)
                .initialWindowSize(TWO_FIFTY_SIX_KB);
            Http2FrameListener frameListener = new DelegatingDecompressorFrameListener(http2Connection,
                new InboundHttp2ToHttpAdapterBuilder(http2Connection).maxContentLength(Integer.MAX_VALUE)
                    .propagateSettings(true)
                    .validateHttpHeaders(true)
                    .build(),
                0);

            HttpToHttp2ConnectionHandler connectionHandler
                = new HttpToHttp2ConnectionHandlerBuilder().initialSettings(settings)
                    .frameListener(frameListener)
                    .connection(http2Connection)
                    .validateHeaders(true)
                    .build();

            if (ctx.pipeline().get(Netty4HandlerNames.PROGRESS_AND_TIMEOUT) != null) {
                ctx.pipeline()
                    .addAfter(Netty4HandlerNames.PROGRESS_AND_TIMEOUT, Netty4HandlerNames.HTTP_RESPONSE,
                        new Netty4ResponseHandler(request, responseReference, errorReference, latch));
                ctx.pipeline()
                    .addBefore(Netty4HandlerNames.PROGRESS_AND_TIMEOUT, Netty4HandlerNames.HTTP_CODEC,
                        connectionHandler);
            } else {
                ctx.pipeline().addAfter(Netty4HandlerNames.SSL, Netty4HandlerNames.HTTP_CODEC, connectionHandler);
                ctx.pipeline()
                    .addAfter(Netty4HandlerNames.HTTP_CODEC, Netty4HandlerNames.HTTP_RESPONSE,
                        new Netty4ResponseHandler(request, responseReference, errorReference, latch));
            }

            sendHttp11Request(request, ctx.channel(), errorReference)
                .addListener((ChannelFutureListener) sendListener -> {
                    if (!sendListener.isSuccess()) {
                        setOrSuppressError(errorReference, sendListener.cause());
                        sendListener.channel().close();
                        latch.countDown();
                    } else {
                        sendListener.channel().read();
                    }
                });
        } else if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
            if (ctx.pipeline().get(Netty4HandlerNames.PROGRESS_AND_TIMEOUT) != null) {
                ctx.pipeline()
                    .addAfter(Netty4HandlerNames.PROGRESS_AND_TIMEOUT, Netty4HandlerNames.HTTP_RESPONSE,
                        new Netty4ResponseHandler(request, responseReference, errorReference, latch));
                ctx.pipeline()
                    .addBefore(Netty4HandlerNames.PROGRESS_AND_TIMEOUT, Netty4HandlerNames.HTTP_CODEC, createCodec());
            } else {
                ctx.pipeline().addAfter(Netty4HandlerNames.SSL, Netty4HandlerNames.HTTP_CODEC, createCodec());
                ctx.pipeline()
                    .addAfter(Netty4HandlerNames.HTTP_CODEC, Netty4HandlerNames.HTTP_RESPONSE,
                        new Netty4ResponseHandler(request, responseReference, errorReference, latch));
            }

            sendHttp11Request(request, ctx.channel(), errorReference)
                .addListener((ChannelFutureListener) sendListener -> {
                    if (!sendListener.isSuccess()) {
                        setOrSuppressError(errorReference, sendListener.cause());
                        sendListener.channel().close();
                        latch.countDown();
                    } else {
                        sendListener.channel().read();
                    }
                });
        } else {
            throw new IllegalStateException("unknown protocol: " + protocol);
        }
    }
}
