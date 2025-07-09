// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.models.HttpRequest;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.util.AttributeKey;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.PIPELINE_CLEANUP;
import static io.clientcore.http.netty4.implementation.Netty4Utility.configureHttpsPipeline;
import static io.clientcore.http.netty4.implementation.Netty4Utility.sendHttp11Request;
import static io.clientcore.http.netty4.implementation.Netty4Utility.sendHttp2Request;
import static io.clientcore.http.netty4.implementation.Netty4Utility.setOrSuppressError;

/**
 * Handler that deals with application protocol negotiation (ALPN) and configures the {@link ChannelPipeline} to use
 * either HTTP/1.1 or HTTP/2 based on the result of negotiation.
 */
public final class Netty4AlpnHandler extends ApplicationProtocolNegotiationHandler {

    /**
     * An Attribute key for the channel storing the HTTP protocol version that was negotiated.
     * This information will be used in case the same channel is reused in the future, so we can
     * adjust the correct handlers because there's no need for ALPN to run again.
     */
    public static final AttributeKey<HttpProtocolVersion> HTTP_PROTOCOL_VERSION_KEY
        = AttributeKey.valueOf("http-protocol-version");
    private final HttpRequest request;
    private final AtomicReference<ResponseStateInfo> responseReference;
    private final AtomicReference<Throwable> errorReference;
    private final CountDownLatch latch;
    private final Netty4ConnectionPool connectionPool;

    /**
     * Creates a new instance of {@link Netty4AlpnHandler} with a fallback to using HTTP/1.1.
     *
     * @param request        The request to send once ALPN negotiation completes.
     * @param errorReference An AtomicReference keeping track of errors during the request lifecycle.
     * @param latch          A CountDownLatch that will be released once the request completes.
     * @param connectionPool The connection pool.
     */
    public Netty4AlpnHandler(HttpRequest request, AtomicReference<ResponseStateInfo> responseReference,
        AtomicReference<Throwable> errorReference, CountDownLatch latch, Netty4ConnectionPool connectionPool) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.request = request;
        this.responseReference = responseReference;
        this.errorReference = errorReference;
        this.latch = latch;
        this.connectionPool = connectionPool;
    }

    /**
     * Creates a new instance of {@link Netty4AlpnHandler} with a fallback to using HTTP/1.1.
     *
     * @param request        The request to send once ALPN negotiation completes.
     * @param errorReference An AtomicReference keeping track of errors during the request lifecycle.
     * @param latch          A CountDownLatch that will be released once the request completes.
     */
    public Netty4AlpnHandler(HttpRequest request, AtomicReference<ResponseStateInfo> responseReference,
        AtomicReference<Throwable> errorReference, CountDownLatch latch) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.request = request;
        this.responseReference = responseReference;
        this.errorReference = errorReference;
        this.latch = latch;
        this.connectionPool = null;
    }

    @Override
    public boolean isSharable() {
        return false;
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
        HttpProtocolVersion protocolVersion;
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            protocolVersion = HttpProtocolVersion.HTTP_2;
        } else if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
            protocolVersion = HttpProtocolVersion.HTTP_1_1;
        } else {
            ctx.fireExceptionCaught(new IllegalStateException("unknown protocol: " + protocol));
            return;
        }

        // Store the negotiated protocol for connection reuse.
        ctx.channel().attr(HTTP_PROTOCOL_VERSION_KEY).set(protocolVersion);

        configureHttpsPipeline(ctx.pipeline(), request, protocolVersion, responseReference, errorReference, latch);

        ctx.pipeline()
            .addLast(PIPELINE_CLEANUP, new Netty4PipelineCleanupHandler(connectionPool, errorReference, latch));

        if (protocolVersion == HttpProtocolVersion.HTTP_2) {
            sendHttp2Request(request, ctx.channel(), errorReference, latch);
        } else {
            sendHttp11Request(request, ctx.channel(), errorReference)
                .addListener((ChannelFutureListener) sendListener -> {
                    if (!sendListener.isSuccess()) {
                        setOrSuppressError(errorReference, sendListener.cause());
                        sendListener.channel().pipeline().fireExceptionCaught(sendListener.cause());
                        latch.countDown();
                    } else {
                        sendListener.channel().read();
                    }
                });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        setOrSuppressError(errorReference, cause);
        ctx.fireExceptionCaught(cause);
        latch.countDown();
    }
}
