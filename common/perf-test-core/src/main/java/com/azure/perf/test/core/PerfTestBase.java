package com.azure.perf.test.core;

import reactor.core.publisher.Mono;


public abstract class PerfTestBase<TOptions extends PerfStressOptions> {
    protected final TOptions options;
    public long lastCompletionNanoTime;
    public long completedOperations;

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
     */
    public Mono<Void> runAllAsync(long endNanoTime) {
        return Mono.empty();
    }

    /**
     * Runs the sync perf tests until specified system nano time.
     * @param endNanoTime the target time to run the performance test for.
     */
    public Mono<Void> preCleanupAsync() {
        return Mono.empty();
    }

    /**
     * Runs the sync perf tests until specified system nano time.
     * @param endNanoTime the target time to run the performance test for.
     */
    public Mono<Void> cleanupAsync() {
        return Mono.empty();
    }

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

    long getCompletedOperations() {
        return completedOperations;
    }
}
