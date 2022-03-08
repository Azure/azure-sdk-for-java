// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.implementation.handler.DispatchHandler;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.apache.qpid.proton.reactor.Selectable.Callback;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe;
import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.core.amqp.implementation.AmqpLoggingUtils.createContextWithConnectionId;

/**
 * The following utility class is used to generate an event to hook into {@link Reactor}'s event delegation pattern. It
 * uses a {@link Pipe} as the IO on which Reactor listens to.
 *
 * <p>
 * {@link Reactor} is not thread-safe - all calls to {@link Proton} APIs should be on the Reactor Thread. {@link
 * Reactor} works out-of-box for all event driven API - ex: onReceive - which could raise upon onSocketRead. {@link
 * Reactor} doesn't support APIs like send() out-of-box - which could potentially run on different thread to that of the
 * Reactor thread.
 * </p>
 *
 * <p>
 * Cardinality: Multiple {@link ReactorDispatcher}'s could be attached to 1 {@link Reactor}. Each {@link
 * ReactorDispatcher} should be initialized synchronously - as it calls API in {@link Reactor} which is not thread-safe.
 * </p>
 */
public final class ReactorDispatcher {
    private final ClientLogger logger;
    private final CloseHandler onClose;
    private final String connectionId;
    private final Reactor reactor;
    private final Pipe ioSignal;
    private final ConcurrentLinkedQueue<Work> workQueue;
    private final WorkScheduler workScheduler;
    private final AtomicInteger wip = new AtomicInteger();
    private final AtomicBoolean isIOPipeInterrupted = new AtomicBoolean();
    private final AtomicBoolean isClosed = new AtomicBoolean();
    private final Sinks.One<AmqpShutdownSignal> shutdownSignal = Sinks.one();

    /**
     * Creates an instance. The {@code ioSignal} is associated with {@code reactor} as a child {@link Selectable}.
     *
     * @param connectionId The connection id.
     * @param reactor The reactor instance.
     * @param ioSignal IO pipe to signal work on the {@code reactor}.
     */
    public ReactorDispatcher(final String connectionId, final Reactor reactor, final Pipe ioSignal) {
        this.connectionId = connectionId;
        this.reactor = reactor;
        this.ioSignal = ioSignal;
        this.workQueue = new ConcurrentLinkedQueue<>();
        this.onClose = new CloseHandler();
        this.workScheduler = new WorkScheduler();
        this.logger = new ClientLogger(ReactorDispatcher.class, createContextWithConnectionId(connectionId));
        // The Proton-J reactor goes quiescent when there is no work to do, and it only wakes up when a Selectable (by
        // default, the network connection) signals that data is available.
        //
        // That's a problem in the send-only scenario, which is a common scenario, or any scenario where activity is
        // sparse. If the reactor has gone quiescent, the SDK can put a pending send in the work queue, but it will just
        // sit there until a Selectable wakes the reactor up. The pipe gives the SDK code a guaranteed way to ensure the
        // reactor is awake.
        final Selectable schedulerSelectable = this.reactor.selectable();

        schedulerSelectable.setChannel(this.ioSignal.source());
        schedulerSelectable.onReadable(this.workScheduler);
        schedulerSelectable.onFree(this.onClose);

        schedulerSelectable.setReading(true);
        this.reactor.update(schedulerSelectable);
    }

    public Mono<AmqpShutdownSignal> getShutdownSignal() {
        return shutdownSignal.asMono();
    }

    /**
     * Schedules work to be performed on the underlying proton-j reactor.
     *
     * @param work Work to be run on the underlying proton-j reactor.
     *
     * @throws IOException If the underlying IO pipe cannot be signalled saying that there is more work to be done.
     * @throws RejectedExecutionException if the reactor instance has already been closed or the underlying IO Pipe
     *     has been closed.
     */
    public void invoke(final Runnable work) throws IOException {
        this.throwIfSchedulerError();

        this.workQueue.offer(new Work(work));
        this.signalWorkQueue();
    }

    /**
     * Schedules work to be performed on the proton-j reactor after {@code delay}.
     *
     * @param work Work to be run on the underlying proton-j reactor.
     * @param delay Delay before work should scheduled for execution.
     *
     * @throws IOException If the underlying IO pipe cannot be signalled saying that there is more work to be done.
     * @throws RejectedExecutionException if the reactor instance has already been closed or the underlying IO Pipe
     *     has been closed.
     */
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
            throw logger.logExceptionAsWarning(new RejectedExecutionException(
                "Underlying Reactor was already disposed. Should not continue dispatching work to this. "
                    + rejectedException.getMessage(), rejectedException));
        }

        // throw when the pipe is in closed state - in which case,
        // signalling the new event-dispatch will fail
        if (!this.ioSignal.sink().isOpen()) {
            throw logger.logExceptionAsWarning(new RejectedExecutionException(
                "ReactorDispatcher instance is closed. Should not continue dispatching work to this reactor."));
        }
    }

    private void signalWorkQueue() throws IOException {
        try {
            ByteBuffer oneByteBuffer = ByteBuffer.allocate(1);
            while (this.ioSignal.sink().write(oneByteBuffer) == 0) {
                oneByteBuffer = ByteBuffer.allocate(1);
            }
        } catch (ClosedChannelException ignorePipeClosedDuringReactorShutdown) {
            if (!isClosed.get()) {
                logger.warning("signalWorkQueue failed before reactor closed.", ignorePipeClosedDuringReactorShutdown);
                if (!isIOPipeInterrupted.getAndSet(true)) {
                    shutdownSignal.emitError(new RuntimeException(String.format(
                        "connectionId[%s] IO Sink was interrupted before reactor closed.", connectionId),
                        ignorePipeClosedDuringReactorShutdown), Sinks.EmitFailureHandler.FAIL_FAST);
                } else {
                    logger.verbose("shutdown signal is already emitted. Can be ignored.", ignorePipeClosedDuringReactorShutdown);
                }
            } else {
                logger.verbose("signalWorkQueue failed with an error after closed. Can be ignored.", ignorePipeClosedDuringReactorShutdown);
            }
        }
    }

    // Schedules work to be executed in reactor.
    private final class WorkScheduler implements Callback {
        @Override
        public void run(Selectable selectable) {
            // If there are multiple threads that enter this, they'll have incremented the wip number, and we'll know
            // how many were 'missed'.
            if (wip.getAndIncrement() != 0) {
                return;
            }

            int missed = 1;

            while (missed != 0) {
                try {
                    ByteBuffer oneKbByteBuffer = ByteBuffer.allocate(1024);
                    while (ioSignal.source().read(oneKbByteBuffer) > 0) {
                        // read until the end of the stream
                        oneKbByteBuffer = ByteBuffer.allocate(1024);
                    }
                } catch (ClosedChannelException ignorePipeClosedDuringReactorShutdown) {
                    if (!isClosed.get()) {
                        logger.warning("WorkScheduler.run() failed before reactor was closed.", ignorePipeClosedDuringReactorShutdown);
                        shutdownSignal.emitError(new RuntimeException(String.format(
                            "connectionId[%s] IO Source was interrupted before reactor closed.", connectionId),
                            ignorePipeClosedDuringReactorShutdown), Sinks.EmitFailureHandler.FAIL_FAST);
                    } else {
                        logger.verbose("WorkScheduler.run() failed with an error. Can be ignored.", ignorePipeClosedDuringReactorShutdown);
                    }

                    break;
                } catch (IOException ioException) {
                    shutdownSignal.emitError(logger.logExceptionAsError(new RuntimeException(
                        "WorkScheduler.run() failed with an error.", ioException)), Sinks.EmitFailureHandler.FAIL_FAST);
                    break;
                }

                Work topWork;
                while ((topWork = workQueue.poll()) != null) {
                    if (topWork.delay != null) {
                        reactor.schedule((int) topWork.delay.toMillis(), topWork.dispatchHandler);
                    } else {
                        topWork.dispatchHandler.onTimerTask(null);
                    }
                }

                // If there are multiple threads that tried to enter this, we would have missed some, so we'll go back
                // through the loop until we have not missed any other work.
                missed = wip.addAndGet(-missed);
            }
        }
    }

    // Disposes of the IO pipe when the reactor closes.
    private final class CloseHandler implements Callback {
        @Override
        public void run(Selectable selectable) {
            if (isClosed.getAndSet(true)) {
                return;
            }

            logger.info("Reactor selectable is being disposed.");

            shutdownSignal.emitValue(new AmqpShutdownSignal(false, false,
                    String.format("connectionId[%s] Reactor selectable is disposed.", connectionId)),
                Sinks.EmitFailureHandler.FAIL_FAST);

            try {
                if (ioSignal.sink().isOpen()) {
                    ioSignal.sink().close();
                }
            } catch (IOException ioException) {
                logger.error("CloseHandler.sink().close() failed with an error.", ioException);
            }

            workScheduler.run(null);

            try {
                if (ioSignal.source().isOpen()) {
                    ioSignal.source().close();
                }
            } catch (IOException ioException) {
                logger.error("CloseHandler.source().close() failed with an error.", ioException);
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
