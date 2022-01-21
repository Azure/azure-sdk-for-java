// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents the abstraction of a Performance test class.
 *
 * <p>
 * The performance test class needs to extend this class.
 * To add any test setup and logic the test class should override {@link EventPerfTest#globalSetupAsync()}
 * and {@link EventPerfTest#globalCleanupAsync()} methods .
 * </p>
 * @param <TOptions> the options configured for the test.
 */
public abstract class EventPerfTest<TOptions extends PerfStressOptions> extends PerfTestBase<TOptions> {

    private AtomicInteger completedOps;

    private volatile boolean errorRaised;

    private long startTime;

    /**
     * Creates an instance of performance test.
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public EventPerfTest(TOptions options) {
        super(options);
        if (options.getTestProxies() != null && options.getTestProxies().size() > 0) {
            throw new IllegalStateException("Test Proxies are not supported for Event Perf Tests.");
        }
        completedOps = new AtomicInteger(0);
    }

    /**
     * Indicates an event was raised, and records its count internally.
     */
    public void eventRaised() {
        completedOps.getAndIncrement();
        lastCompletionNanoTime = System.nanoTime() - startTime;
    }
    /**
     * Indicates an error was raised, and stops the performance test flow.
     */
    public void errorRaised() {
        errorRaised = true;
        lastCompletionNanoTime = System.nanoTime() - startTime;
    }

    @Override
    public void runAll(long endNanoTime) {
        startTime = System.nanoTime();
        completedOps.set(0);
        errorRaised = false;
        lastCompletionNanoTime = 0;
        while (System.nanoTime() < endNanoTime) {
            if (errorRaised) {
                break;
            }
        }
    }

    @Override
    public Mono<Void> runAllAsync(long endNanoTime) {
        return Mono.fromCallable(() -> {
            runAll(endNanoTime);
            return Mono.empty();
        }).then();
    }

    @Override
    long getCompletedOperations() {
        return completedOps.longValue();
    }

}
