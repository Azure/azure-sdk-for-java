// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.LastHttpContent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
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
     * @param writeTimeout Write timeout duration, if {@code null} or less than or equal {@code 0} there is no timeout.
     * @param responseTimeout Response timeout duration, if {@code null} or less than or equal {@code 0} there is no
     * timeout.
     * @param readTimeout Read timeout duration, if {@code null} or less than or equal {@code 0} there is no timeout.
     */
    public TimeoutHandler(Duration writeTimeout, Duration responseTimeout, Duration readTimeout) {
        this.writeTimeoutNanos = getTimeoutNanos(writeTimeout);
        this.responseTimeoutNanos = getTimeoutNanos(responseTimeout);
        this.readTimeoutNanos = getTimeoutNanos(readTimeout);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg == LastHttpContent.EMPTY_LAST_CONTENT || msg instanceof FullHttpRequest) {
            System.out.println("Final write handled for request.");
            finalWriteProcessed = true;
        }

        /*
         * If a write operation timeout is set add a write timeout task. Otherwise, add a write watcher that tracks the
         * write operation completing to determine if the response timeout should begin.
         */
        promise = promise.unvoid();
        addWriteTimeoutTask(ctx, promise, writeTimeoutNanos > 0);

        ctx.write(msg, promise);
    }

    private void addWriteTimeoutTask(ChannelHandlerContext ctx, ChannelPromise promise, boolean hasTimeout) {
        final WriteTask writeTask = new WriteTask(ctx, promise, hasTimeout);
        outstandingWriteOperations += 1;

        writeTasks.add(writeTask);
        promise.addListener(writeTask);
    }

    private void removeWriteTask(WriteTask writeTask, ChannelHandlerContext ctx, boolean operationTimedOut) {
        writeTasks.remove(writeTask);
        outstandingWriteOperations -= 1;

        if (!operationTimedOut) {
            attemptToBeginResponseTimeoutTask(ctx);
        }
    }

    private void attemptToBeginResponseTimeoutTask(ChannelHandlerContext ctx) {
        /*
         * If we haven't processed the final write operation or have outstanding write operations that are still running
         * don't begin the response timeout task.
         */
        if (!finalWriteProcessed || outstandingWriteOperations != 0 || responseTimeoutNanos == 0) {
            return;
        }

        // Start the timeout task.
        System.out.println("Beginning response timeout.");
        responseTimeout = ctx.executor().schedule(() -> operationTimedOut(ctx, RESPONSE_TIMED_OUT_MESSAGE),
            responseTimeoutNanos, NANOSECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ctx.fireChannelRead(msg);
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
        System.out.println(errorMessage);
        if (!closed) {
            ctx.fireExceptionCaught(new TimeoutException(errorMessage));
            ctx.close();
            closed = true;
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        cleanupHandler();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        cleanupHandler();
        ctx.fireChannelInactive();
    }

    /*
     * Cleanup any remaining tasks when the handler is removed or when the channel becomes inactive.
     */
    private void cleanupHandler() {
        System.out.println("Channel complete.");
        for (WriteTask writeTask : writeTasks) {
            if (writeTask.scheduledTimeout != null && !writeTask.scheduledTimeout.isDone()) {
                System.out.println("Cancelling outstanding write operation.");
                writeTask.scheduledTimeout.cancel(false);
            }
        }

        if (responseTimeout != null && !responseTimeout.isDone()) {
            System.out.println("Cancelling outstanding response timeout.");
            responseTimeout.cancel(false);
        }

        if (readTimeout != null && !readTimeout.isDone()) {
            System.out.println("Cancelling outstanding read timeout.");
            readTimeout.cancel(false);
        }
    }

    private final class WriteTask implements Runnable, ChannelFutureListener {
        private final ChannelHandlerContext ctx;
        private final ChannelPromise promise;

        final ScheduledFuture<?> scheduledTimeout;

        private WriteTask(ChannelHandlerContext ctx, ChannelPromise promise, boolean hasTimeout) {
            this.ctx = ctx;
            this.promise = promise;

            /*
             * ChannelHandlerContext will be passed if we are adding a write timeout task as we will need to use it to
             * fire an exception through the pipeline. If ChannelHandlerContext is null we are adding a write watcher
             * task which will keep track of write operations as they complete and decrement the outstanding write
             * operations counter.
             */
            this.scheduledTimeout = (hasTimeout)
                ? ctx.executor().schedule(this, writeTimeoutNanos, NANOSECONDS)
                : null;
        }

        @Override
        public void run() {
            /*
             * If the promise hasn't completed before the timeout Runnable is triggered throw a TimeoutException and
             * attempt to cancel all currently running write tasks.
             */
            if (!promise.isDone()) {
                operationTimedOut(ctx, WRITE_TIMED_OUT_MESSAGE);
                removeWriteTask(this, ctx, true);

                return;
            }

            removeWriteTask(this, ctx, false);
        }

        @Override
        public void operationComplete(ChannelFuture channelFuture) {
            if (scheduledTimeout != null) {
                scheduledTimeout.cancel(false);
            }

            removeWriteTask(this, ctx, false);
        }
    }

    /*
     * Helper function to convert the timeout duration into nanoseconds. If the duration is null, 0, or negative there
     * is no timeout period, so return 0. Otherwise, return the maximum of the duration and the minimum timeout period.
     */
    private static long getTimeoutNanos(Duration timeout) {
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            return 0;
        }

        return Math.max(timeout.get(ChronoUnit.NANOS), MINIMUM_TIMEOUT_NANOS);
    }
}
