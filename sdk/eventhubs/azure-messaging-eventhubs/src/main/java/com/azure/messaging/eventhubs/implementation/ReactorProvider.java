// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.messaging.eventhubs.implementation.handler.CustomIOHandler;
import com.azure.messaging.eventhubs.implementation.handler.ReactorHandler;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.ReactorOptions;

import java.io.IOException;
import java.util.Objects;

public class ReactorProvider {
    private final Object lock = new Object();
    private Reactor reactor;
    private ReactorDispatcher reactorDispatcher;

    Reactor getReactor() {
        synchronized (lock) {
            return reactor;
        }
    }

    ReactorDispatcher getReactorDispatcher() {
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
    Reactor createReactor(String connectionId, int maxFrameSize) throws IOException {
        final CustomIOHandler globalHandler = new CustomIOHandler(connectionId);
        final ReactorHandler reactorHandler = new ReactorHandler(connectionId);

        return createReactor(maxFrameSize, globalHandler, reactorHandler);
    }

    /**
     * Creates a new reactor with the given reactor handler and IO handler.
     *
     * @param globalHandler The global handler for reactor instance. Useful for logging events that were missed.
     * @param baseHandlers Handler for reactor instance. Usually: {@link ReactorHandler}
     * @return A new reactor instance.
     */
    private Reactor createReactor(final int maxFrameSize, final Handler globalHandler, final BaseHandler... baseHandlers) throws IOException {
        Objects.requireNonNull(baseHandlers);
        Objects.requireNonNull(globalHandler);

        if (maxFrameSize <= 0) {
            throw new IllegalArgumentException("'maxFrameSize' must be a positive number.");
        }

        final ReactorOptions reactorOptions = new ReactorOptions();
        reactorOptions.setMaxFrameSize(maxFrameSize);
        reactorOptions.setEnableSaslByDefault(true);

        final Reactor reactor = Proton.reactor(reactorOptions, baseHandlers);
        reactor.setGlobalHandler(globalHandler);

        final ReactorDispatcher dispatcher = new ReactorDispatcher(reactor);

        synchronized (lock) {
            this.reactor = reactor;
            this.reactorDispatcher = dispatcher;
        }

        return this.reactor;
    }
}
