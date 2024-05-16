// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.apache.qpid.proton.reactor.Selectable.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link Reactor} is not thread-safe - all calls to {@link Proton} API's should be - on the Reactor Thread.
 * {@link Reactor} works out-of-box for all event driven API - ex: onReceive - which could raise upon onSocketRead.
 * {@link Reactor} didn't support API's like Send() out-of-box - which could potentially run on different thread to that of Reactor.
 * So, the following utility class is used to generate an Event to hook into {@link Reactor}'s event delegation pattern.
 * It uses a {@link Pipe} as the IO on which Reactor Listens to.
 * Cardinality: multiple {@link ReactorDispatcher}'s could be attached to 1 {@link Reactor}.
 * Each {@link ReactorDispatcher} should be initialized Synchronously - as it calls API in {@link Reactor} which is not thread-safe.
 */
public final class ReactorDispatcher {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ReactorDispatcher.class);
    private final Reactor reactor;
    private final Pipe ioSignal;
    private final ConcurrentLinkedQueue<BaseHandler> workQueue;
    private final ScheduleHandler workScheduler;
    private AtomicBoolean dequeueInProgress;

    public ReactorDispatcher(final Reactor reactor) throws IOException {
        this.reactor = reactor;
        this.ioSignal = Pipe.open();
        this.workQueue = new ConcurrentLinkedQueue<>();
        this.workScheduler = new ScheduleHandler();
        this.dequeueInProgress = new AtomicBoolean(false);

        initializeSelectable();
    }

    private void initializeSelectable() {
        Selectable schedulerSelectable = this.reactor.selectable();

        schedulerSelectable.setChannel(this.ioSignal.source());
        schedulerSelectable.onReadable(this.workScheduler);
        schedulerSelectable.onFree(new CloseHandler());

        schedulerSelectable.setReading(true);
        this.reactor.update(schedulerSelectable);
    }

    public void invoke(final DispatchHandler timerCallback) throws IOException, RejectedExecutionException {
        this.throwIfSchedulerError();

        this.workQueue.offer(timerCallback);
        this.signalWorkQueue();
    }

    public void invoke(final int delay, final DispatchHandler timerCallback) throws IOException, RejectedExecutionException {
        this.throwIfSchedulerError();

        this.workQueue.offer(new DelayHandler(this.reactor, delay, timerCallback));
        this.signalWorkQueue();
    }

    private void throwIfSchedulerError() {
        // throw when the scheduler on which Reactor is running is already closed
        final RejectedExecutionException rejectedException = this.reactor.attachments()
            .get(RejectedExecutionException.class, RejectedExecutionException.class);
        if (rejectedException != null) {
            throw new RejectedExecutionException(rejectedException.getMessage(), rejectedException);
        }

        // throw when the pipe is in closed state - in which case,
        // signalling the new event-dispatch will fail
        if (!this.ioSignal.sink().isOpen()) {
            throw new RejectedExecutionException("ReactorDispatcher instance is closed.");
        }
    }

    private void signalWorkQueue() throws IOException {
        if (this.dequeueInProgress.get()) {
            return;
        }

        try {
            ByteBuffer oneByteBuffer = ByteBuffer.allocate(1);
            while (this.ioSignal.sink().write(oneByteBuffer) == 0) {
                oneByteBuffer = ByteBuffer.allocate(1);
            }
        } catch (ClosedChannelException ignorePipeClosedDuringReactorShutdown) {
            TRACE_LOGGER.info("signalWorkQueue failed with an error", ignorePipeClosedDuringReactorShutdown);
        }
    }

    private static final class DelayHandler extends BaseHandler {
        final int delay;
        final BaseHandler timerCallback;
        final Reactor reactor;

        DelayHandler(final Reactor reactor, final int delay, final DispatchHandler timerCallback) {
            this.delay = delay;
            this.timerCallback = timerCallback;
            this.reactor = reactor;
        }

        @Override
        public void onTimerTask(Event e) {
            this.reactor.schedule(this.delay, this.timerCallback);
        }
    }

    private final class ScheduleHandler implements Callback {
        @Override
        public void run(Selectable selectable) {
            ReactorDispatcher.this.dequeueInProgress.set(true);

            try {
                ByteBuffer oneKbByteBuffer = ByteBuffer.allocate(1024);
                while (ioSignal.source().read(oneKbByteBuffer) > 0) {
                    // read until the end of the stream
                    oneKbByteBuffer = ByteBuffer.allocate(1024);
                }
            } catch (ClosedChannelException ignorePipeClosedDuringReactorShutdown) {
                TRACE_LOGGER.info("ScheduleHandler.run() failed with an error", ignorePipeClosedDuringReactorShutdown);
            } catch (IOException ioException) {
                TRACE_LOGGER.warn("ScheduleHandler.run() failed with an error", ioException);
                throw new RuntimeException(ioException);
            }

            BaseHandler topWork;
            while ((topWork = workQueue.poll()) != null) {
                topWork.onTimerTask(null);
            }

            ReactorDispatcher.this.dequeueInProgress.set(false);

            // drain items to make sure there are no pending items
            while ((topWork = workQueue.poll()) != null) {
                topWork.onTimerTask(null);
            }
        }
    }

    private final class CloseHandler implements Callback {
        @Override
        public void run(Selectable selectable) {
            try {
                if (ioSignal.sink().isOpen()) {
                    ioSignal.sink().close();
                }
            } catch (IOException ioException) {
                TRACE_LOGGER.info("CloseHandler.run() sink().close() failed with an error", ioException);
            }

            workScheduler.run(null);

            try {
                if (ioSignal.source().isOpen()) {
                    ioSignal.source().close();
                }
            } catch (IOException ioException) {
                TRACE_LOGGER.info("CloseHandler.run() source().close() failed with an error", ioException);
            }
        }
    }
}
