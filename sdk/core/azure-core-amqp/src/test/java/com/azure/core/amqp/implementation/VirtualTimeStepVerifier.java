// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.util.function.Supplier;

/**
 * An AutoCloseable wrapper over Reactor VirtualTime Verifier.
 */
final class VirtualTimeStepVerifier implements AutoCloseable {
    private final VirtualTimeScheduler scheduler;

    VirtualTimeStepVerifier() {
        scheduler = VirtualTimeScheduler.create();
    }

    <T> StepVerifier.Step<T> create(Supplier<Flux<T>> scenarioSupplier) {
        // VirtualTime Verifier with demand on subscription set to 0.
        return StepVerifier.withVirtualTime(scenarioSupplier, () -> scheduler, 0);
    }

    @Override
    public void close() {
        scheduler.dispose();
    }
}
