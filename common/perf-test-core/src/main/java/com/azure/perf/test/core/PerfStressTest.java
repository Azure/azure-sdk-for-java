// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import reactor.core.publisher.Mono;

/**
 * Represents the abstraction of a Performance test class.
 *
 * <p>
 *     The performance test class needs to extend this class. The test class should override {@link PerfStressTest#run()}
 *     and {@link PerfStressTest#runAsync()} methods and the synchronous and asynchronous test logic respectively.
 *     To add any test setup and logic the test class should override {@link PerfStressTest#globalSetupAsync()}
 *     and {@link PerfStressTest#globalCleanupAsync()} methods .
 * </p>
 *
 *
 * @param <TOptions> the options configured for the test.
 */
public abstract class PerfStressTest<TOptions extends PerfStressOptions> {
    protected final TOptions options;

    /**
     * Creates an instance of performance test.
     * @param options the options configured for the test.
     */
    public PerfStressTest(TOptions options) {
        this.options = options;
    }

    /**
     * Runs the setup required prior to running the performance test.
     * @return An empty {@link Mono}
     */
    public Mono<Void> globalSetupAsync() {
        return Mono.empty();
    }

    /**
     * Runs the setup required prior to running an individual thread in the performance test.
     * @return An empty {@link Mono}
     */
    public Mono<Void> setupAsync() {
        return Mono.empty();
    }

    /**
     * Runs the performance test.
     */
    public abstract void run();

    /**
     * Runs the performance test asynchronously.
     * @return An empty {@link Mono}
     */
    public abstract Mono<Void> runAsync();

    /**
     * Runs the cleanup logic after an individual thread finishes in the performance test.
     * @return An empty {@link Mono}
     */
    public Mono<Void> cleanupAsync() {
        return Mono.empty();
    }

    /**
     * Runs the cleanup logic after the performance test finishes.
     * @return An empty {@link Mono}
     */
    public Mono<Void> globalCleanupAsync() {
        return Mono.empty();
    }
}
