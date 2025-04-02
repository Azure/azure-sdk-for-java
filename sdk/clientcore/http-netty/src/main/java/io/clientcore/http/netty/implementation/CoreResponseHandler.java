// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty.implementation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.CoreUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.http.netty.implementation.NettyUtility.awaitLatch;

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
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            HttpHeaders headers = (response.headers() instanceof WrappedHttpHeaders)
                ? ((WrappedHttpHeaders) response.headers()).getCoreHeaders()
                : NettyUtility.convertHeaders(response.headers());
            responseReference.set(createResponse(request, response.status().code(), headers, ctx.channel()));
            ctx.pipeline().remove(this);
            latch.countDown();
        }

        ctx.fireChannelRead(msg);
    }

    private static Response<BinaryData> createResponse(HttpRequest request, int statusCode, HttpHeaders headers,
        Channel channel) {
        if (request.getHttpMethod() == HttpMethod.HEAD) {
            // HEAD requests should not have a response body. Drain the channel and close it.
            CountDownLatch latch = new CountDownLatch(1);
            // Do we need to watch the channelRead() events and release the messages sent? Or does that happen in the
            // channel reading loop? Add a test to verify this behavior, we don't want to leak memory.
            channel.pipeline().addLast(new EagerConsumeNetworkResponseHandler(latch, ignored -> {
            }));
            awaitLatch(latch);
            return new Response<>(request, statusCode, headers, BinaryData.empty());
        }

        // Check the Content-Type header to determine how the response body should be handled.
        String contentType = headers.getValue(HttpHeaderName.CONTENT_TYPE);
        if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            // Stream the response body for application/octet-stream content types.
            // autoRead should have been disabled already but lets make sure that it is.
            channel.config().setAutoRead(false);
            String contentLength = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
            Long length = null;
            if (!CoreUtils.isNullOrEmpty(contentLength)) {
                try {
                    length = Long.parseLong(contentLength);
                } catch (NumberFormatException ignored) {
                    // Ignore, we'll just read until the channel is closed.
                }
            }
            return new Response<>(request, statusCode, headers, new NettyChannelBinaryData(channel, length));
        } else {
            // For other content types, buffer the response body.
            CountDownLatch latch = new CountDownLatch(1);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            channel.pipeline().addLast(new EagerConsumeNetworkResponseHandler(latch, buf -> {
                try {
                    buf.readBytes(outputStream, buf.readableBytes());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }));
            awaitLatch(latch);
            return new Response<>(request, statusCode, headers, BinaryData.fromBytes(outputStream.toByteArray()));
        }
    }
}
