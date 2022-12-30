// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.handler.ReactorHandler;
import com.azure.core.amqp.implementation.handler.TransportHandler;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.ReactorOptions;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.time.Duration;
import java.util.Objects;

public class ReactorProvider {
    private static final ClientLogger LOGGER = new ClientLogger(ReactorProvider.class);
    private final Object lock = new Object();
    private Reactor reactor;
    private ReactorDispatcher reactorDispatcher;

    public Reactor getReactor() {
        synchronized (lock) {
            return reactor;
        }
    }

    public ReactorDispatcher getReactorDispatcher() {
        synchronized (lock) {
            return reactorDispatcher;
        }
    }

    /**
     * Creates a QPID Reactor.
     *
     * @param connectionId Identifier for Reactor.
     * @return The newly created reactor instance.
     * @throws IOException If the service could not create a Reactor instance.
     */
    public Reactor createReactor(String connectionId, int maxFrameSize) throws IOException {
        final TransportHandler transportHandler = new TransportHandler(connectionId);
        final ReactorHandler reactorHandler = new ReactorHandler(connectionId);

        synchronized (lock) {
            if (this.reactor != null) {
                return this.reactor;
            }

            if (maxFrameSize <= 0) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("'maxFrameSize' must be a positive number."));
            }

            final ReactorOptions reactorOptions = new ReactorOptions();
            reactorOptions.setMaxFrameSize(maxFrameSize);
            reactorOptions.setEnableSaslByDefault(true);

            final Reactor reactor = Proton.reactor(reactorOptions, reactorHandler);
            reactor.getGlobalHandler().add(transportHandler);

            final Pipe ioSignal = Pipe.open();
            final ReactorDispatcher dispatcher = new ReactorDispatcher(connectionId, reactor, ioSignal);

            this.reactor = reactor;
            this.reactorDispatcher = dispatcher;
        }

        return this.reactor;
    }

    public ReactorExecutor createExecutorForReactor(String connectionId,
                                                    String fullyQualifiedNamespace,
                                                    ReactorConnection.ReactorExceptionHandler reactorExceptionHandler,
                                                    AmqpRetryOptions retryOptions) {
        synchronized (lock) {
            Objects.requireNonNull(this.reactor, "'createReactor' should be called before 'createReactorExecutor'");

            final Duration timeoutDivided = retryOptions.getTryTimeout().dividedBy(2);
            final Duration pendingTasksDuration = ClientConstants.SERVER_BUSY_WAIT_TIME.compareTo(timeoutDivided) < 0
                ? ClientConstants.SERVER_BUSY_WAIT_TIME
                : timeoutDivided;
            // Use a new single-threaded scheduler to run QPID's Reactor's work as Reactor is not thread-safe.
            // Using Schedulers.single() will use the same thread for all connections in this process which
            // limits the scalability of the no. of concurrent connections a single process can have.
            // This could be a long timeout depending on the user's operation timeout. It's probable that the
            // connection's long disposed.
            final Scheduler scheduler = Schedulers.newSingle("reactor-executor");

            return new ReactorExecutor(this.reactor, scheduler, connectionId,
                reactorExceptionHandler, pendingTasksDuration,
                fullyQualifiedNamespace);
        }
    }
}
