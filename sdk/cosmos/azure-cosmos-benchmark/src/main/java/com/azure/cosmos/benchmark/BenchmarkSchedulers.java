// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Custom Reactor schedulers for benchmark workloads.
 * Uses dedicated thread pools with identifiable names (visible in thread dumps and logs)
 * to avoid polluting the global Reactor schedulers.
 */
final class BenchmarkSchedulers {

    private static final String BENCHMARK_DISPATCH_THREAD_NAME = "benchmark-dispatch";
    private static final int TTL_FOR_SCHEDULER_WORKER_IN_SECONDS = 60;

    /**
     * Bounded elastic scheduler for benchmark workload dispatch.
     * Used by the orchestrator to subscribe to benchmark operations.
     * Supports both blocking (SyncBenchmark) and non-blocking (async) workloads.
     */
    static final Scheduler BENCHMARK_DISPATCH = Schedulers.newBoundedElastic(
        Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
        Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE,
        BENCHMARK_DISPATCH_THREAD_NAME,
        TTL_FOR_SCHEDULER_WORKER_IN_SECONDS,
        true
    );

    private BenchmarkSchedulers() {
    }
}
