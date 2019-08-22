// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.handler.DispatchHandler;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.apache.qpid.proton.reactor.Selectable.Callback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe;
import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;

/**
 * {@link Reactor} is not thread-safe - all calls to {@link Proton} APIs should be on the Reactor Thread.
 * {@link Reactor} works out-of-box for all event driven API - ex: onReceive - which could raise upon onSocketRead.
 * {@link Reactor} doesn't support APIs like send() out-of-box - which could potentially run on different thread to that
 * of the Reactor thread.
 *
 * <p>
 * The following utility class is used to generate an Event to hook into {@link Reactor}'s event delegation pattern.
 * It uses a {@link Pipe} as the IO on which Reactor listens to.
 * </p>
 *
 * <p>
 * Cardinality: Multiple {@link ReactorDispatcher}'s could be attached to 1 {@link Reactor}.
 * Each {@link ReactorDispatcher} should be initialized synchronously - as it calls API in {@link Reactor} which is not
 * thread-safe.
 * </p>
 */
public final class ReactorDispatcher {
    private final ClientLogger logger = new ClientLogger(ReactorDispatcher.class);
    private final CloseHandler onClose;
    private final Reactor reactor;
    private final Pipe ioSignal;
    private final ConcurrentLinkedQueue<Work> workQueue;
    private final WorkScheduler workScheduler;

    ReactorDispatcher(final Reactor reactor) throws IOException {
        this.reactor = reactor;
        this.ioSignal = Pipe.open();
        this.workQueue = new ConcurrentLinkedQueue<>();
        this.onClose = new CloseHandler();
        this.workScheduler = new WorkScheduler();

        initializeSelectable();
    }

    private void initializeSelectable() {
        Selectable schedulerSelectable = this.reactor.selectable();

        schedulerSelectable.setChannel(this.ioSignal.source());
        schedulerSelectable.onReadable(this.workScheduler);
        schedulerSelectable.onFree(this.onClose);

        schedulerSelectable.setReading(true);
        this.reactor.update(schedulerSelectable);
    }

    public void invoke(final Runnable work) throws IOException {
        this.throwIfSchedulerError();

        this.workQueue.offer(new Work(work));
        this.signalWorkQueue();
    }

    public void invoke(final Runnable work, final Duration delay) throws IOException {
        this.throwIfSchedulerError();

        this.workQueue.offer(new Work(work, delay));
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
        try {
            ByteBuffer oneByteBuffer = ByteBuffer.allocate(1);
            while (this.ioSignal.sink().write(oneByteBuffer) == 0) {
                oneByteBuffer = ByteBuffer.allocate(1);
            }
        } catch (ClosedChannelException ignorePipeClosedDuringReactorShutdown) {
            logger.info("signalWorkQueue failed with an error: {}", ignorePipeClosedDuringReactorShutdown);
        }
    }

    // Schedules work to be executed in reactor.
    private final class WorkScheduler implements Callback {
        @Override
        public void run(Selectable selectable) {
            try {
                ByteBuffer oneKbByteBuffer = ByteBuffer.allocate(1024);
                while (ioSignal.source().read(oneKbByteBuffer) > 0) {
                    // read until the end of the stream
                    oneKbByteBuffer = ByteBuffer.allocate(1024);
                }
            } catch (ClosedChannelException ignorePipeClosedDuringReactorShutdown) {
                logger.info("WorkScheduler.run() failed with an error: %s", ignorePipeClosedDuringReactorShutdown);
            } catch (IOException ioException) {
                logger.error("WorkScheduler.run() failed with an error: %s", ioException);
                throw new RuntimeException(ioException);
            }

            Work topWork;
            while ((topWork = workQueue.poll()) != null) {
                if (topWork.delay != null) {
                    reactor.schedule((int) topWork.delay.toMillis(), topWork.dispatchHandler);
                } else {
                    topWork.dispatchHandler.onTimerTask(null);
                }
            }
        }
    }

    // Disposes of the IO pipe when the reactor closes.
    private final class CloseHandler implements Callback {

        @Override
        public void run(Selectable selectable) {
            try {
                if (ioSignal.sink().isOpen()) {
                    ioSignal.sink().close();
                }
            } catch (IOException ioException) {
                logger.error("CloseHandler.run() sink().close() failed with an error. %s", ioException);
            }

            workScheduler.run(null);

            try {
                if (ioSignal.source().isOpen()) {
                    ioSignal.source().close();
                }
            } catch (IOException ioException) {
                logger.error("CloseHandler.run() source().close() failed with an error %s", ioException);
            }
        }
    }

    // Work items that are dispatched to reactor.
    private static final class Work {
        // The work item that is dispatched to Reactor.
        private final DispatchHandler dispatchHandler;
        private final Duration delay;

        Work(Runnable work) {
            this(work, null);
        }

        Work(Runnable work, Duration delay) {
            this.dispatchHandler = new DispatchHandler(work);
            this.delay = delay;
        }
    }
}
