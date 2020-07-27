// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * This class observes a network calls request and response and manages triggering a timeout if the operation stalls.
 * <p>
 * Timeout tracking is split into three distinct categories of write, response, and read timeout. All timeout operations
 * use a minimum timeout period of 1 millisecond.
 * <p>
 * Write timeouts track data being sent through the network sockets. Queueing and callbacks are used to ensure the write
 * operation isn't being stalled or timing out. Each time a write operation is processed in this handler an event may be
 * added to the tracking queue depending on how quickly the data is sent through the network socket. If the write
 * doesn't complete instantaneously a write callback will be added into the tracking queue, if this completes before the
 * timeout period it will be cancelled and removed. If the write operation progresses too slowly a {@link
 * TimeoutException} will be thrown and all other write operations being tracked in the queue will be cancelled.
 * <p>
 * Response timeout tracks whether the server responds in a timely manner once the request has completed sending. This
 * timeout begins once all write operations have processed.
 * <p>
 * Read timeout tracks that network response read operations are periodically happening. The first channel read
 * completion will cancel the response timeout and begin read timeout tracking. Each time a new read completes the
 * timeout resets, this is different than write timeouts which have individual timeouts for each write operation. The
 * read timeout is removed when the channel handler is removed or when the channel becomes inactive after the full
 * response is consumed.
 */
public final class TimeoutHandler extends ChannelDuplexHandler {
    public static final ByteBuf FINAL_WRITE_BUF = new EmptyByteBuf(UnpooledByteBufAllocator.DEFAULT);

    private static final long MINIMUM_TIMEOUT_NANOS = MILLISECONDS.toNanos(1);
    private static final String WRITE_TIMED_OUT_MESSAGE = "Channel write operation timed out.";
    private static final String RESPONSE_TIMED_OUT_MESSAGE = "Channel response timed out.";
    private static final String READ_TIMED_OUT_MESSAGE = "Channel read timed out.";

    private final long writeTimeoutNanos;
    private final long responseTimeoutNanos;
    private final long readTimeoutNanos;

    private final Set<WriteTask> writeTasks = new HashSet<>();

    private long outstandingWriteOperations = 0;
    private boolean finalWriteProcessed = false;
    private ScheduledFuture<?> responseTimeout;
    private ScheduledFuture<?> readTimeout;
    private long lastReadNanos;
    private boolean closed;

    /**
     * Constructs a {@link TimeoutHandler}.
     *
     * @param writeTimeoutSeconds Write timeout in seconds, if less than {@code 0} there is no timeout.
     * @param responseTimeoutSeconds Response timeout in seconds, if less than {@code 0} there is no timeout.
     * @param readTimeoutSeconds Read timeout in seconds, if less than {@code 0} there is no timeout.
     */
    public TimeoutHandler(long writeTimeoutSeconds, long responseTimeoutSeconds, long readTimeoutSeconds) {
        this(writeTimeoutSeconds, responseTimeoutSeconds, readTimeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * Constructs a {@link TimeoutHandler}.
     *
     * @param writeTimeout Write timeout, if less than {@code 0} there is no timeout.
     * @param responseTimeout Response timeout, if less than {@code 0} there is no timeout.
     * @param readTimeout Read timeout, if less than {@code 0} there is no timeout.
     * @param timeUnit {@link TimeUnit} used by the timeouts.
     */
    public TimeoutHandler(long writeTimeout, long responseTimeout, long readTimeout, TimeUnit timeUnit) {
        writeTimeoutNanos = (writeTimeout <= 0)
            ? 0
            : Math.max(timeUnit.toNanos(writeTimeout), MINIMUM_TIMEOUT_NANOS);

        responseTimeoutNanos = (responseTimeout <= 0)
            ? 0
            : Math.max(timeUnit.toNanos(responseTimeout), MINIMUM_TIMEOUT_NANOS);

        readTimeoutNanos = (readTimeout <= 0)
            ? 0
            : Math.max(timeUnit.toNanos(readTimeout), MINIMUM_TIMEOUT_NANOS);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg == FINAL_WRITE_BUF) {
            finalWriteProcessed = true;
        }

        /*
         * If a write operation timeout is set add a write timeout task. Otherwise, add a write watcher that tracks the
         * write operation completing to determine if the response timeout should begin.
         */
        promise = promise.unvoid();
        if (writeTimeoutNanos > 0) {
            addWriteTimeoutTask(ctx, promise);
        } else {
            addWriteWatcherTask(promise);
        }

        ctx.write(msg, promise);
    }

    private void addWriteTimeoutTask(ChannelHandlerContext ctx, ChannelPromise promise) {
        final WriteTask writeTask = new WriteTask(ctx, promise);

        if (!writeTask.scheduledTimeout.isDone()) {
            writeTasks.add(writeTask);
            promise.addListener(writeTask);
        }
    }

    private void addWriteWatcherTask(ChannelPromise promise) {
        final WriteTask writeTask = new WriteTask(null, promise);

        writeTasks.add(writeTask);
        promise.addListener(writeTask);
    }

    private void removeWriteTask(WriteTask writeTask, ChannelHandlerContext ctx) {
        writeTasks.remove(writeTask);
        outstandingWriteOperations -= 1;
        attemptToBeginResponseTimeoutTask(ctx);
    }

    private void attemptToBeginResponseTimeoutTask(ChannelHandlerContext ctx) {
        /*
         * If we haven't processed the final write operation or have outstanding write operations that are still running
         * don't begin the response timeout task.
         */
        if (!finalWriteProcessed || outstandingWriteOperations != 0) {
            return;
        }

        // Start the timeout task.
        responseTimeout = ctx.executor().schedule(() -> operationTimedOut(ctx, RESPONSE_TIMED_OUT_MESSAGE),
            responseTimeoutNanos, NANOSECONDS);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        lastReadNanos = System.nanoTime();

        if (readTimeoutNanos > 0 && readTimeout == null) {
            readTimeout = ctx.executor().schedule(() -> readTask(ctx), readTimeoutNanos, NANOSECONDS);
        }

        ctx.fireChannelReadComplete();
    }

    private void readTask(ChannelHandlerContext ctx) {
        if (readTimeoutNanos - (System.nanoTime() - lastReadNanos) <= 0) {
            operationTimedOut(ctx, READ_TIMED_OUT_MESSAGE);
        } else {
            readTimeout = ctx.executor().schedule(() -> readTask(ctx), readTimeoutNanos, NANOSECONDS);
        }
    }

    private void operationTimedOut(ChannelHandlerContext ctx, String errorMessage) {
        if (!closed) {
            ctx.fireExceptionCaught(new TimeoutException(errorMessage));
            ctx.close();
            closed = true;
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        cleanupHandler();
        super.handlerRemoved(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        cleanupHandler();
        super.channelInactive(ctx);
    }

    /*
     * Cleanup any remaining tasks when the handler is removed or when the channel becomes inactive.
     */
    private void cleanupHandler() {
        for (WriteTask writeTask : writeTasks) {
            if (writeTask.scheduledTimeout != null && !writeTask.scheduledTimeout.isDone()) {
                writeTask.scheduledTimeout.cancel(false);
            }
        }

        if (responseTimeout != null && !responseTimeout.isDone()) {
            responseTimeout.cancel(false);
        }

        if (readTimeout != null && !readTimeout.isDone()) {
            readTimeout.cancel(false);
        }
    }

    private final class WriteTask implements Runnable, ChannelFutureListener {
        private final ChannelHandlerContext ctx;
        private final ChannelPromise promise;

        final ScheduledFuture<?> scheduledTimeout;

        private WriteTask(ChannelHandlerContext ctx, ChannelPromise promise) {
            this.ctx = ctx;
            this.promise = promise;

            /*
             * ChannelHandlerContext will be passed if we are adding a write timeout task as we will need to use it to
             * fire an exception through the pipeline. If ChannelHandlerContext is null we are adding a write watcher
             * task which will keep track of write operations as they complete and decrement the outstanding write
             * operations counter.
             */
            if (ctx != null) {
                this.scheduledTimeout = ctx.executor().schedule(this, 0, NANOSECONDS);
            } else {
                this.scheduledTimeout = null;
            }
        }

        @Override
        public void run() {
            /*
             * If the promise hasn't completed before the timeout Runnable is triggered throw a TimeoutException and
             * attempt to cancel all currently running write tasks.
             */
            if (!promise.isDone()) {
                operationTimedOut(ctx, WRITE_TIMED_OUT_MESSAGE);
            }

            removeWriteTask(this, ctx);
        }

        @Override
        public void operationComplete(ChannelFuture channelFuture) {
            if (scheduledTimeout != null) {
                scheduledTimeout.cancel(false);
            }

            removeWriteTask(this, ctx);
        }
    }
}
