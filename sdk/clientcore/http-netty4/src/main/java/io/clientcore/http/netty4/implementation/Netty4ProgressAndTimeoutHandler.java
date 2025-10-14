// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.clientcore.core.utils.ProgressReporter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.FileRegion;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Class containing all ChannelHandler concepts that ClientCore SDKs use.
 * <p>
 * This class manages write, response, and read timeouts and watching writing progress. Unlike the previous
 * implementations of multiple ChannelHandlers to handle functionality where the handlers would begin working when
 * added to the ChannelPipeline and stop working when removed from the channel pipeline this handler used start and end
 * methods. This reduces the number of mutations that happen to the ChannelPipeline when sending a request.
 */
public final class Netty4ProgressAndTimeoutHandler extends ChannelDuplexHandler {
    private final long writeTimeoutMillis;
    private final ProgressReporter progressReporter;
    private long lastWriteMillis;
    private long lastWriteProgress;
    private volatile boolean trackingWriteTimeout;
    private ScheduledFuture<?> writeTimeoutWatcher;

    private final long responseTimeoutMillis;
    private volatile boolean trackingResponseTimeout;
    private ScheduledFuture<?> responseTimeoutWatcher;

    private final long readTimeoutMillis;
    private long lastReadMillis;
    private boolean lastRead;
    private volatile boolean trackingReadTimeout;
    private ScheduledFuture<?> readTimeoutWatcher;

    private volatile boolean closed;

    /**
     * Constructs a channel that watches write, response, and reads and handles timing out the operation and tracking
     * write progress.
     *
     * @param progressReporter The {@link ProgressReporter} that will track writing content to the network.
     * @param writeTimeoutMillis The period of milliseconds before a channel's write is considered timed out.
     * @param responseTimeoutMillis The period of milliseconds before a channel's response is considered timed out.
     * @param readTimeoutMillis The period of milliseconds before a channel's read is considered timed out.
     */
    public Netty4ProgressAndTimeoutHandler(ProgressReporter progressReporter, long writeTimeoutMillis,
        long responseTimeoutMillis, long readTimeoutMillis) {
        this.progressReporter = progressReporter;
        this.writeTimeoutMillis = writeTimeoutMillis;
        this.responseTimeoutMillis = responseTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
    }

    @Override
    public boolean isSharable() {
        return false;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        disposeWriteTimeoutWatcher();
        disposeResponseTimeoutWatcher();
        disposeReadTimeoutWatcher();
        closed = true;
    }

    ScheduledFuture<?> getWriteTimeoutWatcher() {
        return writeTimeoutWatcher;
    }

    /**
     * Starts write tracking.
     * <p>
     * Write tracking involves write timeout and progress reporting.
     */
    void startWriteTracking(ChannelHandlerContext ctx) {
        trackingWriteTimeout = true;
        if (ctx != null && writeTimeoutMillis > 0) {
            this.writeTimeoutWatcher = ctx.executor()
                .scheduleAtFixedRate(() -> writeTimeoutRunnable(ctx, trackingWriteTimeout), writeTimeoutMillis,
                    writeTimeoutMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Ends write tracking.
     */
    void endWriteTracking() {
        trackingWriteTimeout = false;
        disposeWriteTimeoutWatcher();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!trackingWriteTimeout) {
            startWriteTracking(ctx);
        }

        if (progressReporter != null) {
            if (msg instanceof ByteBuf) {
                progressReporter.reportProgress(((ByteBuf) msg).readableBytes());
            } else if (msg instanceof ByteBufHolder) {
                progressReporter.reportProgress(((ByteBufHolder) msg).content().readableBytes());
            } else if (msg instanceof FileRegion) {
                progressReporter.reportProgress(((FileRegion) msg).count());
            }
        }

        if (writeTimeoutMillis > 0) {
            ctx.write(msg, promise.unvoid()).addListener((ChannelFutureListener) future -> {
                lastWriteMillis = System.currentTimeMillis();
                if (msg instanceof LastHttpContent) {
                    endWriteTracking();
                    startResponseTracking(ctx);
                }
            });
        } else {
            ctx.write(msg, promise.unvoid());
        }
    }

    void writeTimeoutRunnable(ChannelHandlerContext ctx, boolean trackingWriteTimeout) {
        // Write timeout tracking has been disabled before the method was called, exit.
        if (!trackingWriteTimeout) {
            return;
        }

        // Channel has completed a write operation since the last time the timeout event fired.
        if ((writeTimeoutMillis - (System.currentTimeMillis() - lastWriteMillis)) > 0) {
            return;
        }

        ChannelOutboundBuffer buffer = ctx.channel().unsafe().outboundBuffer();

        // Channel has an outbound buffer, check if the write progress has changed.
        if (buffer != null) {
            long writeProgress = buffer.currentProgress();

            // Write progress has changed since the last time the timeout event fired.
            if (writeProgress != lastWriteProgress) {
                this.lastWriteProgress = writeProgress;
                return;
            }
        }

        // No progress has been made since the last timeout event, channel has timed out.
        if (!closed) {
            disposeWriteTimeoutWatcher();
            // Fire the exception up the pipeline. The PipelineCleanupHandler will catch this
            // and release the channel. We do not close the channel here.
            ctx.fireExceptionCaught(new TimeoutException(
                "Channel write operation timed out after " + writeTimeoutMillis + " milliseconds."));
            closed = true;
        }
    }

    private void disposeWriteTimeoutWatcher() {
        trackingWriteTimeout = false;
        if (writeTimeoutWatcher != null) {
            writeTimeoutWatcher.cancel(false);
            writeTimeoutWatcher = null;
        }
    }

    ScheduledFuture<?> getResponseTimeoutWatcher() {
        return responseTimeoutWatcher;
    }

    /**
     * Starts response tracking.
     * <p>
     * Response timeout is overridable on a per-request basis.
     */
    void startResponseTracking(ChannelHandlerContext ctx) {
        trackingResponseTimeout = true;
        if (ctx != null && responseTimeoutMillis > 0) {
            this.responseTimeoutWatcher = ctx.executor()
                .schedule(() -> responseTimedOut(ctx, trackingResponseTimeout), responseTimeoutMillis,
                    TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Ends response tracking.
     */
    void endResponseTracking() {
        trackingResponseTimeout = false;
        disposeResponseTimeoutWatcher();
    }

    void responseTimedOut(ChannelHandlerContext ctx, boolean trackingResponseTimeout) {
        if (!closed && trackingResponseTimeout) {
            disposeResponseTimeoutWatcher();
            ctx.fireExceptionCaught(
                new TimeoutException("Channel response timed out after " + responseTimeoutMillis + " milliseconds."));
            closed = true;
        }
    }

    private void disposeResponseTimeoutWatcher() {
        trackingResponseTimeout = false;
        if (responseTimeoutWatcher != null) {
            responseTimeoutWatcher.cancel(false);
            responseTimeoutWatcher = null;
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (trackingResponseTimeout) {
            endResponseTracking();
            startReadTracking(ctx);
        }

        lastRead = msg instanceof LastHttpContent;
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        this.lastReadMillis = System.currentTimeMillis();
        if (lastRead && trackingReadTimeout) {
            endReadTracking();
        }
        ctx.fireChannelReadComplete();
    }

    ScheduledFuture<?> getReadTimeoutWatcher() {
        return readTimeoutWatcher;
    }

    /**
     * Starts read tracking.
     */
    void startReadTracking(ChannelHandlerContext ctx) {
        trackingReadTimeout = true;
        if (ctx != null && readTimeoutMillis > 0) {
            this.readTimeoutWatcher = ctx.executor()
                .scheduleAtFixedRate(() -> readTimeoutRunnable(ctx, trackingReadTimeout), readTimeoutMillis,
                    readTimeoutMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Ends read tracking.
     */
    private void endReadTracking() {
        trackingReadTimeout = false;
        disposeReadTimeoutWatcher();
    }

    void readTimeoutRunnable(ChannelHandlerContext ctx, boolean trackingReadTimeout) {
        // Read timeout tracking has been disabled before the method was called, exit.
        if (!trackingReadTimeout) {
            return;
        }

        // Channel has completed a read operation since the last time the timeout event fired.
        if ((readTimeoutMillis - (System.currentTimeMillis() - lastReadMillis)) > 0) {
            return;
        }

        // No progress has been made since the last timeout event, channel has timed out.
        if (!closed) {
            disposeReadTimeoutWatcher();
            ctx.fireExceptionCaught(
                new TimeoutException("Channel read timed out after " + readTimeoutMillis + " milliseconds."));
            closed = true;
        }
    }

    private void disposeReadTimeoutWatcher() {
        trackingReadTimeout = false;
        if (readTimeoutWatcher != null) {
            readTimeoutWatcher.cancel(false);
            readTimeoutWatcher = null;
        }
    }
}
