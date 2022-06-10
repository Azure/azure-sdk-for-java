// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import reactor.core.publisher.Mono;

/**
 * Represents the abstraction of a Performance test class.
 *
 * <p>
 * The performance test class needs to extend this class. The test class should override {@link PerfStressTest#run()}
 * and {@link PerfStressTest#runAsync()} methods and the synchronous and asynchronous test logic respectively.
 * To add any test setup and logic the test class should override {@link PerfStressTest#globalSetupAsync()}
 * and {@link PerfStressTest#globalCleanupAsync()} methods .
 * </p>
 * @param <TOptions> the options configured for the test.
 */
public abstract class PerfStressTest<TOptions extends PerfStressOptions> extends ApiPerfTestBase<TOptions> {
    /**
     * Creates an instance of performance test.
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public PerfStressTest(TOptions options) {
        super(options);
    }

    @Override
    int runTest() {
        run();
        return 1;
    }

    @Override
    Mono<Integer> runTestAsync() {
        return runAsync().then(Mono.just(1));
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
}
