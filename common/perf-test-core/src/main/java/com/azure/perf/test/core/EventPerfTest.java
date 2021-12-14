// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
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

    private AtomicInteger completedOperations;

    private AtomicBoolean errorRaised;

    private long startTime;

    /**
     * Creates an instance of performance test.
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public EventPerfTest(TOptions options) {
        super(options);
    }

    public void eventRaised() {
        completedOperations.getAndIncrement();
        lastCompletionNanoTime = System.nanoTime() - startTime;
    }

    public void errorRaised() {
        errorRaised.set(true);
        lastCompletionNanoTime = System.nanoTime() - startTime;
    }

    public void runAll(long endNanoTime) {
        startTime = System.nanoTime();
        completedOperations.set(0);
        lastCompletionNanoTime = 0;
        while (true) {
            if (errorRaised.get()) {
                break;
            }
        }
    }

    @Override
    int runTest() {
        return 0;
    }

    @Override
    Mono<Integer> runTestAsync() {
        return Mono.empty();
    }

}
