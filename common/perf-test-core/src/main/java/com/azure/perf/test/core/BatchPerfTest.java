// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import reactor.core.publisher.Mono;

/**
 * Represents the abstraction of a Performance test class running operations in batches.
 *
 * <p>
 * The performance test class needs to extend this class. The test class should override {@link BatchPerfTest#runBatch()}
 * and {@link BatchPerfTest#runBatchAsync()} methods and the synchronous and asynchronous test logic respectively.
 * To add any test setup and logic the test class should override {@link BatchPerfTest#globalSetupAsync()}
 * and {@link BatchPerfTest#globalCleanupAsync()} methods .
 * </p>
 * @param <TOptions> the options configured for the test.
 */
public abstract class BatchPerfTest<TOptions extends PerfStressOptions> extends ApiPerfTestBase<TOptions> {

    /**
     * Creates an instance of Batch performance test.
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public BatchPerfTest(TOptions options) {
        super(options);
    }


    /**
     * Run batch operation API perf test.
     * @return the number of operations successfully completed.
     */
    public abstract int runBatch();

    /**
     * Run batch operation async API perf test.
     * @return A {@link Mono} containing number of operations successfully completed.
     */
    public abstract Mono<Integer> runBatchAsync();

    @Override
    int runTest() {
        return runBatch();
    }

    @Override
    Mono<Integer> runTestAsync() {
        return runBatchAsync();
    }
}
