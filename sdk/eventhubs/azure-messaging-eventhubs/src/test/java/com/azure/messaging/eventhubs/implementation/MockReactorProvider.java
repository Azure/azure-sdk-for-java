// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import org.apache.qpid.proton.reactor.Reactor;

class MockReactorProvider extends ReactorProvider {
    private final Reactor reactor;
    private final ReactorDispatcher dispatcher;

    MockReactorProvider(Reactor reactor, ReactorDispatcher dispatcher) {
        this.reactor = reactor;
        this.dispatcher = dispatcher;
    }

    @Override
    Reactor createReactor(String connectionId, int maxFrameSize) {
        return reactor;
    }

    @Override
    Reactor getReactor() {
        return reactor;
    }

    @Override
    ReactorDispatcher getReactorDispatcher() {
        return dispatcher;
    }
}
