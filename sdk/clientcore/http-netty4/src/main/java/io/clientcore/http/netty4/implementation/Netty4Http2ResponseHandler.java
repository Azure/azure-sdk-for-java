// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2Frame;
import io.netty.handler.codec.http2.Http2GoAwayFrame;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2MultiplexHandler;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.http.netty4.implementation.Netty4Utility.fromPossibleAsciiString;
import static io.clientcore.http.netty4.implementation.Netty4Utility.readByteBufIntoOutputStream;
import static io.clientcore.http.netty4.implementation.Netty4Utility.setOrSuppressError;

/**
 * A {@link ChannelInboundHandler} implementation that appropriately handles {@code HTTP/2} responses by using the
 * response headers to determine how to read the response from the server.
 * <p>
 * When used with {@code NettyHttpClient} this handler must be added to the pipeline so that the
 * {@link Http2MultiplexHandler} is able to decode the data of the response.
 */
public final class Netty4Http2ResponseHandler extends ChannelInboundHandlerAdapter {
    private final HttpRequest request;
    private final AtomicReference<ResponseStateInfo> responseReference;
    private final AtomicReference<Throwable> errorReference;
    private final CountDownLatch latch;

    private int statusCode;
    private HttpHeaders headers;

    private boolean receivedHttp2HeaderFrame;
    private boolean responseStarted;

    // Maintain an OutputStream that'll be used to hold eagerly read content from the Netty pipeline.
    // Eager content occurs when the first buffer(s) read from the network contains both HTTP status line and headers
    // and initial response body content.
    private final ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
    private boolean complete;

    /**
     * Creates an instance of {@link Netty4Http2ResponseHandler}.
     *
     * @param request The request that resulted in the response.
     * @param responseReference The reference to the {@link Response} that will be created from the response headers and
     * body.
     * @param errorReference The reference to the {@link Throwable} that will be set if an error occurs while reading
     * the response.
     * @param latch The latch to wait for the response to be processed.
     * @throws NullPointerException If {@code request}, {@code responseReference}, or {@code latch} is null.
     */
    public Netty4Http2ResponseHandler(HttpRequest request, AtomicReference<ResponseStateInfo> responseReference,
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
    public boolean isSharable() {
        return false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        setOrSuppressError(errorReference, cause);
        latch.countDown();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // If the msg isn't an HTTP/2 frame, ignore it.
        if (!(msg instanceof Http2Frame)) {
            ctx.fireChannelRead(msg);
            return;
        }

        if (msg instanceof Http2GoAwayFrame) {
            // If a GOAWAY frame is received, it indicates that the server is shutting down the connection.
            // We should close the channel and notify the response reference.
            setOrSuppressError(errorReference, new IllegalStateException("Received GOAWAY frame: " + msg));
            latch.countDown();
            ctx.close();
            return;
        }

        this.receivedHttp2HeaderFrame = true;
        if (msg instanceof Http2HeadersFrame) {
            // TODO (alzimmer): A super optimized version of HTTP/2 would be modifying the Http2HeadersDecoder used to
            //  push the headers directly into an instance of ClientCore HttpHeaders. As it is possible to receive
            //  multiple Http2HeadersFrames for a single response. This isn't a priority at the moment as baseline
            //  functionality needs to be implemented first.
            Http2HeadersFrame headersFrame = (Http2HeadersFrame) msg;
            if (!this.responseStarted) {
                this.responseStarted = true;
                this.headers = new HttpHeaders();
                this.statusCode = headersFrame.headers().getInt(Http2Headers.PseudoHeaderName.STATUS.value());
            }

            for (Map.Entry<CharSequence, CharSequence> header : headersFrame.headers()) {
                HttpHeaderName headerName = fromPossibleAsciiString(header.getKey());
                this.headers.add(headerName, header.getValue().toString());
            }

            if (headersFrame.isEndStream()) {
                complete = true;
            }
        }

        if (!responseStarted) {
            // Haven't received the HttpResponse, discard this message.
            return;
        }

        if (msg instanceof Http2DataFrame) {
            Http2DataFrame dataFrame = (Http2DataFrame) msg;
            ByteBuf content = dataFrame.content();
            readByteBufIntoOutputStream(content, eagerContent);

            if (dataFrame.isEndStream()) {
                complete = true;
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // Reading hasn't started yet.
        if (!responseStarted) {
            ctx.fireChannelReadComplete();

            if (this.receivedHttp2HeaderFrame) {
                // If an HTTP/2 frame was received, but it wasn't a header frame, fire another read.
                ctx.read();
            }
            return;
        }

        ctx.pipeline().remove(this);
        ctx.fireChannelReadComplete();

        if (complete) {
            ctx.close();
        }

        responseReference.set(new ResponseStateInfo(ctx.channel(), complete, statusCode, headers, eagerContent,
            ResponseBodyHandling.getBodyHandling(request, headers), true));
        latch.countDown();
    }
}
