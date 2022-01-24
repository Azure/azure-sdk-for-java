// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import reactor.core.publisher.Mono;

/**
 * Performance Test Base class.
 * @param <TOptions> the options used to configure the options class.
 */
public abstract class PerfTestBase<TOptions extends PerfStressOptions> {
    protected final TOptions options;
    protected long lastCompletionNanoTime;
    protected long completedOperations;

    /**
     * Creates an instance of Perf Test Base class.
     * @param options options used to configure the perf test.
     */
    public PerfTestBase(TOptions options) {
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
     * Runs the sync perf test until specified system nano time.
     * @param endNanoTime the target time to run the performance test for.
     */
    public void runAll(long endNanoTime) { }

    /**
     * Runs the async perf test until specified system nano time.
     * @param endNanoTime the target time to run the performance test for.
     * @return A {@link Mono} containing void.
     */
    public Mono<Void> runAllAsync(long endNanoTime) {
        return Mono.empty();
    }

    /**
     * Runs before cleanup stage.
     *
     * @return A {@link Mono} containing void.
     */
    public Mono<Void> preCleanupAsync() {
        return Mono.empty();
    }

    /**
     * Runs after performance test finishes.
     * @return A {@link Mono} containing void.
     */
    public Mono<Void> cleanupAsync() {
        return Mono.empty();
    }

    /**
     * Runs before performance test is triggered.
     * @return A {@link Mono} containing void.
     */
    public Mono<Void> globalCleanupAsync() {
        return Mono.empty();
    }

    /**
     * Stops playback tests.
     * @return An empty {@link Mono}.
     */
    public Mono<Void> stopPlaybackAsync() {
        return Mono.empty();
    }

    /**
     * Records responses and starts tests in playback mode.
     */
    public void postSetup() { }

    /**
     * Get completed operations.
     * @return the completed operations.
     */
    public long getCompletedOperations() {
        return completedOperations;
    }
}
