// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.HandlerException;
import org.apache.qpid.proton.reactor.Reactor;
import reactor.core.scheduler.Scheduler;

import java.io.Closeable;
import java.nio.channels.UnresolvedAddressException;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class ReactorExecutor implements Closeable {
    private static final String LOG_MESSAGE = "connectionId[{}], message[{}]";

    private final ClientLogger logger = new ClientLogger(ReactorExecutor.class);
    private final AtomicBoolean hasStarted = new AtomicBoolean();
    private final AtomicBoolean isDisposed = new AtomicBoolean();

    private final Object lock = new Object();
    private final Reactor reactor;
    private final Scheduler scheduler;
    private final String connectionId;
    private final Duration timeout;
    private final AmqpExceptionHandler exceptionHandler;
    private final String hostname;

    ReactorExecutor(Reactor reactor, Scheduler scheduler, String connectionId, AmqpExceptionHandler exceptionHandler,
        Duration timeout, String hostname) {
        this.reactor = Objects.requireNonNull(reactor, "'reactor' cannot be null.");
        this.scheduler = Objects.requireNonNull(scheduler, "'scheduler' cannot be null.");
        this.connectionId = Objects.requireNonNull(connectionId, "'connectionId' cannot be null.");
        this.timeout = Objects.requireNonNull(timeout, "'timeout' cannot be null.");
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "'exceptionHandler' cannot be null.");
        this.hostname = Objects.requireNonNull(hostname, "'hostname' cannot be null.");
    }

    /**
     * Starts the reactor and will begin processing any reactor events until there are no longer any left or {@link
     * #close()} is called.
     */
    void start() {
        if (hasStarted.getAndSet(true)) {
            logger.warning("ReactorExecutor has already started.");
            return;
        }

        logger.info(LOG_MESSAGE, connectionId, "Starting reactor.");
        reactor.start();
        scheduler.schedule(this::run);
    }

    /**
     * Worker loop that tries to process events in the reactor. If there are pending items to process, will reschedule
     * the run() method again.
     */
    private void run() {
        // If this hasn't been disposed of, and we're trying to run work items on it, log a warning and return.
        if (!isDisposed.get() && !hasStarted.get()) {
            logger.warning("Cannot run work items on ReactorExecutor if ReactorExecutor.start() has not been invoked.");
            return;
        }

        boolean rescheduledReactor = false;

        try {
            final boolean shouldReschedule;
            synchronized (lock) {
                shouldReschedule = hasStarted.get() && !Thread.interrupted() && reactor.process();
            }

            if (shouldReschedule) {
                try {
                    scheduler.schedule(this::run);
                    rescheduledReactor = true;
                } catch (RejectedExecutionException exception) {
                    logger.warning(LOG_MESSAGE, connectionId,
                        "Scheduling reactor failed because the scheduler has been shut down.", exception);

                    this.reactor.attachments()
                        .set(RejectedExecutionException.class, RejectedExecutionException.class, exception);
                }
            }
        } catch (HandlerException handlerException) {
            Throwable cause = handlerException.getCause() == null
                ? handlerException
                : handlerException.getCause();

            logger.warning(LOG_MESSAGE, connectionId,
                "Unhandled exception while processing events in reactor, report this error.", handlerException);

            final String message = !CoreUtils.isNullOrEmpty(cause.getMessage())
                ? cause.getMessage()
                : !CoreUtils.isNullOrEmpty(handlerException.getMessage())
                    ? handlerException.getMessage()
                    : "Reactor encountered unrecoverable error";

            final AmqpException exception;
            final AmqpErrorContext errorContext = new AmqpErrorContext(hostname);

            if (cause instanceof UnresolvedAddressException) {
                exception = new AmqpException(true,
                    String.format(Locale.US, "%s. This is usually caused by incorrect hostname or network "
                            + "configuration. Check correctness of namespace information. %s",
                        message, StringUtil.getTrackingIdAndTimeToLog()),
                    cause, errorContext);
            } else {
                exception = new AmqpException(true,
                    String.format(Locale.US, "%s, %s", message, StringUtil.getTrackingIdAndTimeToLog()),
                    cause, errorContext);
            }

            this.exceptionHandler.onConnectionError(exception);
        } finally {
            if (!rescheduledReactor) {
                if (hasStarted.getAndSet(false)) {
                    scheduleCompletePendingTasks();
                } else {
                    final String reason =
                        "Stopping the reactor because thread was interrupted or the reactor has no more events to "
                            + "process.";

                    logger.info(LOG_MESSAGE, connectionId, reason);
                    close(false, reason);
                }
            }
        }
    }

    /**
     * Schedules the release of the current reactor after operation timeout has elapsed.
     */
    private void scheduleCompletePendingTasks() {
        this.scheduler.schedule(() -> {
            logger.info(LOG_MESSAGE, connectionId, "Processing all pending tasks and closing old reactor.");
            try {
                reactor.stop();
                reactor.process();
            } catch (HandlerException e) {
                logger.warning(LOG_MESSAGE, connectionId,
                    StringUtil.toStackTraceString(e, "scheduleCompletePendingTasks - exception occurred while "
                        + "processing events."));
            } finally {
                try {
                    reactor.free();
                } catch (IllegalStateException ignored) {
                    // Since reactor is not thread safe, it is possible that another thread has already disposed of the
                    // session before we were able to schedule this work.
                }
            }
        }, timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        if (hasStarted.get()) {
            close(true, "ReactorExecutor.close() was called.");
        }
    }

    private void close(boolean isUserInitialized, String reason) {
        if (isUserInitialized) {
            scheduleCompletePendingTasks();
        }

        exceptionHandler.onConnectionShutdown(new AmqpShutdownSignal(false, isUserInitialized, reason));
        scheduler.dispose();
    }
}
