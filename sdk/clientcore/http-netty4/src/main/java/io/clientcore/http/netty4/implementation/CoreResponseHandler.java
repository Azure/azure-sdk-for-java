// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.CoreUtils;
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
import io.netty.util.ReferenceCounted;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.core.http.models.HttpMethod.HEAD;
import static io.clientcore.http.netty4.implementation.NettyUtility.awaitLatch;
import static io.clientcore.http.netty4.implementation.NettyUtility.writeEagerContentsToStreamAndRelease;

/**
 * A {@link ChannelInboundHandler} implementation that appropriately handles the response reading from the server based
 * on the information provided from the headers.
 * <p>
 * When used with {@code NettyHttpClient} this handler must be added to the pipeline so that the {@link HttpClientCodec}
 * is able to decode the data of the response.
 */
public final class CoreResponseHandler extends ChannelInboundHandlerAdapter {
    private final HttpRequest request;
    private final AtomicReference<Response<BinaryData>> responseReference;
    private final CountDownLatch latch;

    private int statusCode;
    private HttpHeaders headers;

    private boolean started;

    // Another solution around eager content handling would be to store it in an OutputStream, rather than retaining the
    // HttpContents which leaves to do reference counting.
    private List<HttpContent> eagerContents;
    private boolean complete;

    /**
     * Creates an instance of {@link CoreResponseHandler}.
     *
     * @param request The request that resulted in the response.
     * @param responseReference The reference to the {@link Response} that will be created from the response headers and
     * body.
     * @param latch The latch to wait for the response to be processed.
     * @throws NullPointerException If {@code request}, {@code responseReference}, or {@code latch} is null.
     */
    public CoreResponseHandler(HttpRequest request, AtomicReference<Response<BinaryData>> responseReference,
        CountDownLatch latch) {
        this.request = Objects.requireNonNull(request,
            "Cannot create an instance of CoreResponseHandler with a null 'request'.");
        this.responseReference = Objects.requireNonNull(responseReference,
            "Cannot create an instance of CoreResponseHandler with a null 'responseReference'.");
        this.latch
            = Objects.requireNonNull(latch, "Cannot create an instance of CoreResponseHandler with a null 'latch'.");
    }

    @Override
    public boolean isSharable() {
        return false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        latch.countDown();
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
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
            this.headers = (response.headers() instanceof WrappedHttpHeaders)
                ? ((WrappedHttpHeaders) response.headers()).getCoreHeaders()
                : NettyUtility.convertHeaders(response.headers());

            if (msg instanceof FullHttpResponse) {
                complete = true;
                // Need to call retain here as this will be released in HttpClientCodec. We'll need to make sure to
                // release this when consuming it later.
                ((ReferenceCounted) msg).touch("CoreResponseHandler retaining for future use.");
                eagerContents = Collections.singletonList(((HttpContent) msg).retain());
            }

            return;
        }

        if (msg instanceof LastHttpContent) {
            complete = true;
            // Same reason as above.
            ((ReferenceCounted) msg).touch("CoreResponseHandler retaining for future use.");
            if (eagerContents != null) {
                eagerContents.add(((HttpContent) msg).retain());
            } else {
                eagerContents = Collections.singletonList(((HttpContent) msg).retain());
            }
            return;
        }

        if (!started) {
            // Haven't received the HttpResponse, discard this message.
            return;
        }

        if (eagerContents == null) {
            eagerContents = new LinkedList<>();
        }
        ((ReferenceCounted) msg).touch("CoreResponseHandler retaining for future use.");
        eagerContents.add((HttpContent) msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // Reading hasn't started yet.
        if (!started) {
            ctx.fireChannelReadComplete();
            return;
        }

        ctx.pipeline().remove(this);
        ctx.fireChannelReadComplete();
        BodyHandling bodyHandling = getBodyHandling(request, headers);
        if (complete) {
            // The network response is already complete, handle creating our Response based on the request method and
            // response headers.
            BinaryData body = BinaryData.empty();
            if (bodyHandling != BodyHandling.IGNORE && eagerContents != null) {
                // Set the response body as the first HttpContent received if the request wasn't a HEAD request and
                // there was body content.
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                writeEagerContentsToStreamAndRelease(eagerContents, outputStream);

                body = BinaryData.fromBytes(outputStream.toByteArray());
            }

            responseReference.set(new Response<>(request, statusCode, headers, body));

            // Close the Channel as we're done with it.
            ctx.close();
            latch.countDown();
        } else {
            // Otherwise we aren't finished, handle the remaining content according to the documentation in
            // 'channelRead()'.
            BinaryData body = BinaryData.empty();
            if (bodyHandling == BodyHandling.IGNORE) {
                // We're ignoring the response content.
                CountDownLatch latch = new CountDownLatch(1);
                for (HttpContent eagerContent : eagerContents) {
                    ReferenceCountUtil.release(eagerContent);
                }
                ctx.pipeline().addLast(new EagerConsumeNetworkResponseHandler(latch, ignored -> {
                }));
                awaitLatch(latch);
            } else if (bodyHandling == BodyHandling.STREAM) {
                // Body streaming uses a special BinaryData that tracks the firstContent read and the Channel it came
                // from so it can be consumed when the BinaryData is being used.
                // autoRead should have been disabled already but lets make sure that it is.
                ctx.channel().config().setAutoRead(false);
                String contentLength = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
                Long length = null;
                if (!CoreUtils.isNullOrEmpty(contentLength)) {
                    try {
                        length = Long.parseLong(contentLength);
                    } catch (NumberFormatException ignored) {
                        // Ignore, we'll just read until the channel is closed.
                    }
                }

                body = new NettyChannelBinaryData(eagerContents, ctx.channel(), length);
            } else {
                // All cases otherwise assume BUFFER.
                CountDownLatch latch = new CountDownLatch(1);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                writeEagerContentsToStreamAndRelease(eagerContents, outputStream);
                ctx.pipeline().addLast(new EagerConsumeNetworkResponseHandler(latch, buf -> {
                    try {
                        buf.readBytes(outputStream, buf.readableBytes());
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }));
                awaitLatch(latch);

                body = BinaryData.fromBytes(outputStream.toByteArray());
            }

            responseReference.set(new Response<>(request, statusCode, headers, body));
            latch.countDown();
        }
    }

    private BodyHandling getBodyHandling(HttpRequest request, HttpHeaders responseHeaders) {
        String contentType = responseHeaders.getValue(HttpHeaderName.CONTENT_TYPE);

        if (request.getHttpMethod() == HEAD) {
            return BodyHandling.IGNORE;
        } else if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            return BodyHandling.STREAM;
        } else {
            return BodyHandling.BUFFER;
        }
    }

    private enum BodyHandling {
        IGNORE, STREAM, BUFFER
    }
}
