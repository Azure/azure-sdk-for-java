// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.perf;

import reactor.core.publisher.Mono;

/**
 * Represents the abstraction of a Performance test class.
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
