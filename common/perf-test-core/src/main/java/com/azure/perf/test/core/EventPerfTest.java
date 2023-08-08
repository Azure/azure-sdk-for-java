// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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

    private final AtomicLong completedOps;

    private volatile boolean errorRaised;

    private long startTime;
    private Throwable throwable;

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
        completedOps = new AtomicLong(0);
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
    public void errorRaised(Throwable throwable) {
        synchronized (this) {
            errorRaised = true;
            lastCompletionNanoTime = System.nanoTime() - startTime;
            this.throwable = throwable;
            notify();
        }
    }

    @Override
    public void runAll(long endNanoTime) {
        startTime = System.nanoTime();
        completedOps.set(0);
        errorRaised = false;
        lastCompletionNanoTime = 0;

        synchronized (this) {
            try {
                wait((endNanoTime - startTime) / 1000000);
            } catch (InterruptedException e) { }
            if (errorRaised) {
                throw new RuntimeException(throwable);
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
    public long getCompletedOperations() {
        return completedOps.get();
    }

}
