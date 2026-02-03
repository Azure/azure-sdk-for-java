// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.http.netty4.implementation.Netty4Utility.readByteBufIntoOutputStream;
import static io.clientcore.http.netty4.implementation.Netty4Utility.setOrSuppressError;

/**
 * A {@link ChannelInboundHandler} implementation that appropriately handles {@code HTTP/1.1} responses by using the
 * response headers to determine how to read the response from the server.
 * <p>
 * When used with {@code NettyHttpClient} this handler must be added to the pipeline so that the {@link HttpClientCodec}
 * is able to decode the data of the response.
 */
public final class Netty4ResponseHandler extends ChannelInboundHandlerAdapter {
    private final HttpRequest request;
    private final AtomicReference<ResponseStateInfo> responseReference;
    private final AtomicReference<Throwable> errorReference;
    private final CountDownLatch latch;

    private int statusCode;
    private HttpHeaders headers;

    private boolean started;

    // Maintain an OutputStream that'll be used to hold eagerly read content from the Netty pipeline.
    // Eager content occurs when the first buffer(s) read from the network contains both HTTP status line and headers
    // and initial response body content.
    private final ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
    private boolean complete;
    private boolean isHttp2;

    /**
     * Creates an instance of {@link Netty4ResponseHandler}.
     *
     * @param request The request that resulted in the response.
     * @param responseReference The reference to the {@link Response} that will be created from the response headers and
     * body.
     * @param errorReference The reference to the {@link Throwable} that will be set if an error occurs while reading
     * the response.
     * @param latch The latch to wait for the response to be processed.
     * @throws NullPointerException If {@code request}, {@code responseReference}, or {@code latch} is null.
     */
    public Netty4ResponseHandler(HttpRequest request, AtomicReference<ResponseStateInfo> responseReference,
        AtomicReference<Throwable> errorReference, CountDownLatch latch) {
        this.request = Objects.requireNonNull(request,
            "Cannot create an instance of CoreResponseHandler with a null 'request'.");
        this.responseReference = Objects.requireNonNull(responseReference,
            "Cannot create an instance of CoreResponseHandler with a null 'responseReference'.");
        this.errorReference = Objects.requireNonNull(errorReference,
            "Cannot create an instance of CoreResponseHandler with a null 'errorReference'.");
        this.latch
            = Objects.requireNonNull(latch, "Cannot create an instance of CoreResponseHandler with a null 'latch'.");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        HttpProtocolVersion protocolVersion = ctx.channel().attr(Netty4AlpnHandler.HTTP_PROTOCOL_VERSION_KEY).get();
        this.isHttp2 = protocolVersion == HttpProtocolVersion.HTTP_2;
    }

    @Override
    public boolean isSharable() {
        return false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        setOrSuppressError(errorReference, cause);
        latch.countDown();
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // If the msg isn't HTTP object ignore it.
        if (!(msg instanceof HttpObject)) {
            ctx.fireChannelRead(msg);
            return;
        }

        // http-netty4 creates all Channels AUTO_READ=false, meaning that Netty will wait to read the response until a
        // 'Channel.read()' is called or 'Channel.config().setAutoRead(true)' is set. Our logic is that when the request
        // is successfully sent we'll call 'Channel.read()' to read the first chunk of data from the network. When that
        // happens one of three success states will occur:
        //
        // 1. The response has HTTP headers and no response body. This will result in an 'HttpResponse' object being
        // passed through 'channelRead'. Capture the headers and status code then check if the 'HttpResponse' is an
        // instance of 'LastHttpContent'. If it is then the response is complete, update the 'complete' flag and wait
        // for 'channelReadComplete()' to be called, which will set the response reference and drain anything remaining
        // in the network connection.
        //
        // 2. The response has HTTP headers and a response body that fit into a single 'ByteBuf' chunk read. This will
        // result in an 'HttpResponse' object being passed through 'channelRead' followed by a 'LastHttpContent' object.
        // Retain the content of the 'LastHttpContent' and handle it in the following way when creating the response in
        // 'channelReadComplete()':
        //     a. If the request was a HEAD request release the content and set the response body to an empty, as HEAD
        //     requests should not have a response body.
        //     b. Otherwise, copy the content into a 'BinaryData' and release it, then set that as the response body.
        //
        // 3. The response has HTTP headers and a response body that is larger than a single 'ByteBuf' chunk read. This
        // will result in an 'HttpResponse' object being passed through 'channelRead' followed by a 'HttpContent' object
        // that is the first bytes of the network response body. Handle this in the following ways once
        // 'channelReadComplete()' is called:
        //     a. If the request was a HEAD request release the captured content and drain the channel, then set the
        //     response body to an empty, as HEAD requests should not have a response body.
        //     b. If the response is being buffered, copy the captured content into the aggregator and request the
        //     remaining content of the network response to aggregate, then set the response body to the aggregated
        //     content.
        //     c. If the response is being streamed, retain the reference of the captured content and once the response
        //     body is being consumed send the captured content as the initial payload and stream the remaining content
        //     from the network connection.

        if (msg instanceof HttpResponse) {
            started = true;
            HttpResponse response = (HttpResponse) msg;
            this.statusCode = response.status().code();
            this.headers = (response.headers() instanceof WrappedHttp11Headers)
                ? ((WrappedHttp11Headers) response.headers()).getCoreHeaders()
                : Netty4Utility.convertHeaders(response.headers());

            if (msg instanceof FullHttpResponse) {
                complete = true;
                FullHttpResponse fullHttpResponse = (FullHttpResponse) msg;
                try {
                    readByteBufIntoOutputStream(fullHttpResponse.content(), eagerContent);
                } finally {
                    fullHttpResponse.release();
                }
            }
            return;
        }

        if (msg instanceof LastHttpContent) {
            complete = true;
            LastHttpContent lastHttpContent = (LastHttpContent) msg;
            try {
                readByteBufIntoOutputStream(lastHttpContent.content(), eagerContent);
            } finally {
                lastHttpContent.release();
            }
            return;
        }

        if (!started) {
            // This is an HttpContent that arrived before the HttpResponse.
            // It's unexpected, so we release it and discard it.
            ReferenceCountUtil.release(msg);
            return;
        }

        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            try {
                readByteBufIntoOutputStream(httpContent.content(), eagerContent);
            } finally {
                httpContent.release();
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // Reading hasn't started yet.
        if (!started) {
            ctx.read();
            ctx.fireChannelReadComplete();
            return;
        }
        ctx.fireChannelReadComplete();

        responseReference.set(new ResponseStateInfo(ctx.channel(), complete, statusCode, headers, eagerContent,
            ResponseBodyHandling.getBodyHandling(request, headers), isHttp2));
        latch.countDown();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        setOrSuppressError(errorReference,
            new IOException("The channel became inactive before a response was received."));
        ctx.fireChannelInactive();
        latch.countDown();
    }
}
