// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpExceptionHandler;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.core.implementation.util.ImplUtils;
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

    private final ServiceLogger logger = new ServiceLogger(ReactorExecutor.class);
    private final AtomicBoolean hasStarted = new AtomicBoolean();
    private final Object lock = new Object();
    private final Reactor reactor;
    private final Scheduler scheduler;
    private final String connectionId;
    private final Duration timeout;
    private final AmqpExceptionHandler exceptionHandler;

    ReactorExecutor(Reactor reactor, Scheduler scheduler, String connectionId, AmqpExceptionHandler exceptionHandler,
                    Duration timeout) {
        Objects.requireNonNull(reactor);
        Objects.requireNonNull(scheduler);
        Objects.requireNonNull(connectionId);
        Objects.requireNonNull(exceptionHandler);
        Objects.requireNonNull(timeout);

        this.reactor = reactor;
        this.scheduler = scheduler;
        this.connectionId = connectionId;
        this.timeout = timeout;
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Starts the reactor and will begin processing any reactor events until there are no longer any left or
     * {@link #close()} is called.
     */
    void start() {
        if (hasStarted.get()) {
            logger.asWarning().log("ReactorExecutor has already started.");
            return;
        }

        logger.asInformational().log(LOG_MESSAGE, connectionId, "Starting reactor.");

        hasStarted.set(true);
        synchronized (lock) {
            reactor.start();
        }

        scheduler.schedule(this::run);
    }

    boolean hasStarted() {
        return hasStarted.get();
    }

    /**
     * Worker loop that tries to process events in the reactor. If there are pending items to process, will reschedule
     * the run() method again.
     */
    private void run() {
        if (!hasStarted.get()) {
            logger.asWarning().log("Cannot start ReactorExecutor if ReactorExecutor.start() has not invoked.");
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
                    logger.asWarning().log(LOG_MESSAGE, connectionId,
                        StringUtil.toStackTraceString(exception, "Scheduling reactor failed because the executor has been shut down"));

                    this.reactor.attachments().set(RejectedExecutionException.class, RejectedExecutionException.class, exception);
                }
            }
        } catch (HandlerException handlerException) {
            Throwable cause = handlerException.getCause() == null
                ? handlerException
                : handlerException.getCause();

            logger.asWarning().log(LOG_MESSAGE, connectionId, StringUtil.toStackTraceString(handlerException,
                "Unhandled exception while processing events in reactor, report this error."));

            final String message = !ImplUtils.isNullOrEmpty(cause.getMessage())
                ? cause.getMessage()
                : !ImplUtils.isNullOrEmpty(handlerException.getMessage())
                ? handlerException.getMessage()
                : "Reactor encountered unrecoverable error";

            final AmqpException exception;

            if (cause instanceof UnresolvedAddressException) {
                exception = new AmqpException(true,
                    String.format(Locale.US, "%s. This is usually caused by incorrect hostname or network configuration. Check correctness of namespace information. %s",
                        message, StringUtil.getTrackingIDAndTimeToLog()),
                    cause);
            } else {
                exception = new AmqpException(true,
                    String.format(Locale.US, "%s, %s", message, StringUtil.getTrackingIDAndTimeToLog()),
                    cause);
            }

            this.exceptionHandler.onConnectionError(exception);
        } finally {
            if (!rescheduledReactor) {
                if (hasStarted.get()) {
                    scheduleCompletePendingTasks();
                } else {
                    final String reason = "Stopping the reactor because thread was interrupted or the reactor has no more events to process.";

                    logger.asInformational().log(LOG_MESSAGE, connectionId, reason);
                    close(false, reason);
                }
            }
        }
    }

    private void scheduleCompletePendingTasks() {
        hasStarted.set(false);

        this.scheduler.schedule(() -> {
            logger.asInformational().log(LOG_MESSAGE, connectionId, "Processing all pending tasks and closing old reactor.");
            try {
                reactor.stop();
                reactor.process();
            } catch (HandlerException e) {
                logger.asWarning().log(LOG_MESSAGE, connectionId,
                    StringUtil.toStackTraceString(e, "scheduleCompletePendingTasks - exception occurred while processing events."));
            } finally {
                reactor.free();
            }
        }, timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        close(true, "Dispose called.");
    }

    private void close(boolean isUserInitialized, String reason) {
        if (hasStarted.getAndSet(false)) {
            logger.asInformational().log(LOG_MESSAGE, connectionId, "Stopping the reactor.");

            synchronized (lock) {
                this.reactor.stop();
                this.reactor.free();
            }

            exceptionHandler.onConnectionShutdown(new AmqpShutdownSignal(false, isUserInitialized, reason));
        }
    }
}
