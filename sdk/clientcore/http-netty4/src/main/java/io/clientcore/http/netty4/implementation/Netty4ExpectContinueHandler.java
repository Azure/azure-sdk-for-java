// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.CHUNKED_WRITER;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.PROGRESS_AND_TIMEOUT;
import static io.clientcore.http.netty4.implementation.Netty4Utility.setOrSuppressError;
import static io.clientcore.http.netty4.implementation.Netty4Utility.writeExpectContinueBody;

final class Netty4ExpectContinueHandler extends ChannelInboundHandlerAdapter {
    static final AttributeKey<Boolean> INCOMPLETE_REQUEST = AttributeKey.valueOf("clientcore.incomplete-request");
    private static final long CONTINUE_WAIT_MILLIS = 1000;

    private final HttpRequest request;
    private final AtomicReference<Throwable> errorReference;
    private ScheduledFuture<?> continueTimer;
    private ChannelFuture bodyFuture;
    private State state = State.WAITING;
    private boolean ignoringInformationalContent;

    Netty4ExpectContinueHandler(HttpRequest request, AtomicReference<Throwable> errorReference) {
        this.request = request;
        this.errorReference = errorReference;
    }

    void start(Channel channel) {
        if (state != State.WAITING) {
            return;
        }

        ChannelHandlerContext progressContext = channel.pipeline().context(PROGRESS_AND_TIMEOUT);
        if (progressContext != null) {
            Netty4ProgressAndTimeoutHandler progress = (Netty4ProgressAndTimeoutHandler) progressContext.handler();
            progress.endWriteTracking();
            progress.startResponseTracking(progressContext);
        }
        continueTimer
            = channel.eventLoop().schedule(() -> sendBody(channel), CONTINUE_WAIT_MILLIS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpResponse) {
            int status = ((HttpResponse) msg).status().code();
            if (status >= 100 && status < 200 && status != 101) {
                if (status == 100) {
                    sendBody(ctx.channel());
                }
                ignoringInformationalContent = !(msg instanceof FullHttpResponse);
                ReferenceCountUtil.release(msg);
                return;
            }
            finalResponseHeaders(ctx.channel());
        } else if (ignoringInformationalContent && msg instanceof HttpContent) {
            if (msg instanceof LastHttpContent) {
                ignoringInformationalContent = false;
            }
            ReferenceCountUtil.release(msg);
            return;
        }
        ctx.fireChannelRead(msg);
    }

    void finalResponseHeaders(Channel channel) {
        if (state == State.FINAL) {
            return;
        }
        if (state != State.COMPLETE) {
            channel.attr(INCOMPLETE_REQUEST).set(true);
        }
        state = State.FINAL;
        cancelTimer();
        if (bodyFuture != null && bodyFuture.cancel(false) && channel.pipeline().get(CHUNKED_WRITER) != null) {
            channel.pipeline().remove(CHUNKED_WRITER);
        }

        ChannelHandlerContext progressContext = channel.pipeline().context(PROGRESS_AND_TIMEOUT);
        if (progressContext != null) {
            Netty4ProgressAndTimeoutHandler progress = (Netty4ProgressAndTimeoutHandler) progressContext.handler();
            progress.finalResponseReceived(progressContext);
        }
    }

    private void sendBody(Channel channel) {
        if (state != State.WAITING || !channel.isActive()) {
            return;
        }
        state = State.WRITING;
        cancelTimer();

        ChannelHandlerContext progressContext = channel.pipeline().context(PROGRESS_AND_TIMEOUT);
        if (progressContext != null) {
            ((Netty4ProgressAndTimeoutHandler) progressContext.handler()).endResponseTracking();
        }

        try {
            bodyFuture = writeExpectContinueBody(request, channel, errorReference);
        } catch (RuntimeException error) {
            fail(channel, error);
            return;
        }

        bodyFuture.addListener(future -> {
            if (!future.isSuccess() && !(state == State.FINAL && future.isCancelled())) {
                fail(channel, future.cause());
            } else if (state == State.WRITING) {
                state = State.COMPLETE;
            }
        });
    }

    private void fail(Channel channel, Throwable error) {
        cancelTimer();
        setOrSuppressError(errorReference, error);
        channel.pipeline().fireExceptionCaught(error);
        channel.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cancelTimer();
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        cancelTimer();
        ctx.fireChannelInactive();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        cancelTimer();
    }

    private void cancelTimer() {
        if (continueTimer != null) {
            continueTimer.cancel(false);
            continueTimer = null;
        }
    }

    private enum State {
        WAITING, WRITING, COMPLETE, FINAL
    }
}
