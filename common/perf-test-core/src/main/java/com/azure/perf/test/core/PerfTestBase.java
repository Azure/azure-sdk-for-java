// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Performance Test Base class.
 * @param <TOptions> the options used to configure the options class.
 */
public abstract class PerfTestBase<TOptions extends PerfStressOptions> {
    protected final TOptions options;
    protected long lastCompletionNanoTime;
    protected final int parallelIndex;

    private static final AtomicInteger GLOBAL_PARALLEL_INDEX = new AtomicInteger();

    /**
     * Creates an instance of Perf Test Base class.
     * @param options options used to configure the perf test.
     */
    public PerfTestBase(TOptions options) {
        this.options = options;
        this.parallelIndex = GLOBAL_PARALLEL_INDEX.getAndIncrement();

    }

    /**
     * Runs the setup required prior to running the performance test.
     * @return An empty {@link Mono}
     */
    public Mono<Void> globalSetupAsync() {
        return Mono.empty();
    }

    /**
     * Runs the setup required prior to running the performance test.
     */
    public void globalSetup() {
        globalSetupAsync().block();
    }

    /**
     * Runs the setup required prior to running an individual thread in the performance test.
     * @return An empty {@link Mono}
     */
    public Mono<Void> setupAsync() {
        return Mono.empty();
    }

    /**
     * Runs the setup required prior to running an individual thread in the performance test.
     */
    public void setup() {
        setupAsync().block();
    }

    /**
     * Runs the sync perf test until specified system nano time.
     * @param endNanoTime the target time to run the performance test for.
     */
    public abstract void runAll(long endNanoTime);

    /**
     * Runs the async perf test until specified system nano time.
     * @param endNanoTime the target time to run the performance test for.
     * @return A {@link Mono} containing void.
     */
    public abstract Mono<Void> runAllAsync(long endNanoTime);

    /**
     * Runs before cleanup stage.
     *
     * @return A {@link Mono} containing void.
     */
    Mono<Void> preCleanupAsync() {
        return Mono.empty();
    }

    /**
     * Runs before cleanup stage.
     */
    void preCleanup() {
        preCleanupAsync().block();
    }

    /**
     * Runs after performance test finishes.
     * @return A {@link Mono} containing void.
     */
    public Mono<Void> cleanupAsync() {
        return Mono.empty();
    }

    /**
     * Runs after performance test finishes.
     */
    public void cleanup() {
        cleanupAsync().block();
    }

    /**
     * Runs after performance test is triggered.
     * @return A {@link Mono} containing void.
     */
    public Mono<Void> globalCleanupAsync() {
        return Mono.empty();
    }

    /**
     * Runs after performance test is triggered.
     */
    public void globalCleanup() {
        globalCleanupAsync().block();
    }

    Mono<Void> postSetupAsync() {
        return Mono.empty();
    }

    void postSetup() {
        postSetupAsync().block();
    }

    /**
     * Get completed operations.
     * @return the completed operations.
     */
    public abstract long getCompletedOperations();
}
