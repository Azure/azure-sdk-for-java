// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.implementation.handler.CustomIOHandler;
import com.azure.core.amqp.implementation.handler.ReactorHandler;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.ReactorOptions;

import java.io.IOException;
import java.nio.channels.Pipe;

public class ReactorProvider {
    private final ClientLogger logger = new ClientLogger(ReactorProvider.class);
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
     * Creates a reactor and replaces the existing instance of it.
     *
     * @param connectionId Identifier for Reactor.
     * @return The newly created reactor instance.
     * @throws IOException If the service could not create a Reactor instance.
     */
    public Reactor createReactor(String connectionId, int maxFrameSize) throws IOException {
        final CustomIOHandler globalHandler = new CustomIOHandler(connectionId);
        final ReactorHandler reactorHandler = new ReactorHandler(connectionId);

        synchronized (lock) {
            if (this.reactor != null) {
                return this.reactor;
            }

            if (maxFrameSize <= 0) {
                throw logger.logExceptionAsError(new IllegalArgumentException("'maxFrameSize' must be a positive number."));
            }

            final ReactorOptions reactorOptions = new ReactorOptions();
            reactorOptions.setMaxFrameSize(maxFrameSize);
            reactorOptions.setEnableSaslByDefault(true);

            final Reactor reactor = Proton.reactor(reactorOptions, globalHandler, reactorHandler);
            reactor.setGlobalHandler(globalHandler);

            final Pipe ioSignal = Pipe.open();
            final ReactorDispatcher dispatcher = new ReactorDispatcher(connectionId, reactor, ioSignal);

            this.reactor = reactor;
            this.reactorDispatcher = dispatcher;
        }

        return this.reactor;
    }
}
