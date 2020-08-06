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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

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
    private static final long MINIMUM_TIMEOUT = MILLISECONDS.toMillis(1);
    private static final String WRITE_TIMED_OUT_MESSAGE = "Channel write operation timed out after %d milliseconds.";
    private static final String RESPONSE_TIMED_OUT_MESSAGE = "Channel response timed out after %d milliseconds.";
    private static final String READ_TIMED_OUT_MESSAGE = "Channel read timed out after %d milliseconds.";

    private final long writeTimeoutMillis;
    private final long responseTimeoutMillis;
    private final long readTimeoutMillis;

    private final Set<WriteTask> writeTasks = new HashSet<>();

    private long outstandingWriteOperations = 0;
    private boolean finalWriteProcessed = false;
    private boolean readingHasBegun = false;
    private ScheduledFuture<?> responseTimeout;
    private ScheduledFuture<?> readTimeout;
    private long lastReadMillis;
    private boolean closed;

    /**
     * Constructs a {@link TimeoutHandler}.
     * <p>
     * If a {@code null} timeout is passed a default of 60 seconds will be used. If a timeout is less than or equal to
     * 0 then no timeout is used.
     *
     * @param writeTimeout Write timeout duration.
     * @param responseTimeout Response timeout duration.
     * @param readTimeout Read timeout duration.
     */
    public TimeoutHandler(Duration writeTimeout, Duration responseTimeout, Duration readTimeout) {
        this.writeTimeoutMillis = getTimeoutMillis(writeTimeout);
        this.responseTimeoutMillis = getTimeoutMillis(responseTimeout);
        this.readTimeoutMillis = getTimeoutMillis(readTimeout);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg == LastHttpContent.EMPTY_LAST_CONTENT || msg instanceof FullHttpRequest) {
            finalWriteProcessed = true;
        }

        /*
         * If a write operation timeout is set add a write timeout task. Otherwise, add a write watcher that tracks the
         * write operation completing to determine if the response timeout should begin.
         */
        promise = promise.unvoid();
        addWriteTimeoutTask(ctx, promise, writeTimeoutMillis > 0);

        ctx.write(msg, promise);
    }

    /*
     * Add a new write task. This will create a new future
     */
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
        if (!finalWriteProcessed || outstandingWriteOperations != 0 || responseTimeoutMillis == 0) {
            return;
        }

        // Start the timeout task.
        responseTimeout = ctx.executor().schedule(() -> operationTimedOut(ctx, () ->
            String.format(RESPONSE_TIMED_OUT_MESSAGE, readTimeoutMillis)), responseTimeoutMillis, MILLISECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        this.readingHasBegun = true;
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        /*
         * Only trigger operations if reading has begun. Occasionally channelReadComplete will fire before the response
         * begins to read, we don't want to inadvertently cancel our response timeout and start our read timeout if
         * that happens.
         */
        if (readingHasBegun) {
            lastReadMillis = System.currentTimeMillis();

            /*
             * If there is an on-going response cancel it as we've began reading the response.
             */
            if (responseTimeout != null && !responseTimeout.isDone()) {
                responseTimeout.cancel(false);
                responseTimeout = null;
            }

            if (readTimeoutMillis > 0 && readTimeout == null) {
                readTimeout = ctx.executor().schedule(() -> readTask(ctx), readTimeoutMillis, MILLISECONDS);
            }
        }

        ctx.fireChannelReadComplete();
    }

    private void readTask(ChannelHandlerContext ctx) {
        if (readTimeoutMillis - (System.currentTimeMillis() - lastReadMillis) <= 0) {
            operationTimedOut(ctx, () -> String.format(READ_TIMED_OUT_MESSAGE, readTimeoutMillis));
        } else {
            readTimeout = ctx.executor().schedule(() -> readTask(ctx), readTimeoutMillis, MILLISECONDS);
        }
    }

    private void operationTimedOut(ChannelHandlerContext ctx, Supplier<String> errorMessageSupplier) {
        if (!closed) {
            ctx.fireExceptionCaught(new TimeoutException(errorMessageSupplier.get()));
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
        for (WriteTask writeTask : writeTasks) {
            if (writeTask.scheduledTimeout != null && !writeTask.scheduledTimeout.isDone()) {
                writeTask.scheduledTimeout.cancel(false);
            }
        }

        if (responseTimeout != null && !responseTimeout.isDone()) {
            responseTimeout.cancel(false);
            responseTimeout = null;
        }

        if (readTimeout != null && !readTimeout.isDone()) {
            readTimeout.cancel(false);
            readTimeout = null;
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
                ? ctx.executor().schedule(this, writeTimeoutMillis, MILLISECONDS)
                : null;
        }

        @Override
        public void run() {
            /*
             * If the promise hasn't completed before the timeout Runnable is triggered throw a TimeoutException and
             * attempt to cancel all currently running write tasks.
             */
            if (!promise.isDone()) {
                operationTimedOut(ctx, () -> String.format(WRITE_TIMED_OUT_MESSAGE, writeTimeoutMillis));
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
     * Helper function to convert the timeout duration into MILLISECONDS. If the duration is null, 0, or negative there
     * is no timeout period, so return 0. Otherwise, return the maximum of the duration and the minimum timeout period.
     */
    private static long getTimeoutMillis(Duration timeout) {
        if (timeout == null) {
            return TimeUnit.SECONDS.toMillis(60);
        }

        if (timeout.isZero() || timeout.isNegative()) {
            return 0;
        }

        return Math.max(timeout.toMillis(), MINIMUM_TIMEOUT);
    }
}
