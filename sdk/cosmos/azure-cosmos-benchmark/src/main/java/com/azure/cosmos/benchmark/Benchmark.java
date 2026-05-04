// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Common contract for all benchmark workloads.
 * Implementations are created by {@link BenchmarkOrchestrator} and participate
 * in its lifecycle loop (create → run → shutdown → settle × N cycles).
 *
 * <p>Dispatchable benchmarks (the default) implement {@link #performSingleOperation()}.
 * The orchestrator calls this for each operation slot; each benchmark tracks its own
 * operation index internally.</p>
 *
 * <p>Non-dispatchable benchmarks override {@link #isDispatchable()} to return {@code false}.
 * The orchestrator calls {@link #run()} for those, which manages its own dispatch loop.
 * Non-dispatchable benchmarks receive dispatch parameters via
 * {@link #setDispatchParams(int, long, Duration)} before {@link #run()} is called.</p>
 */
public interface Benchmark {

    /**
     * Run the benchmark in self-dispatch mode.
     * Only used for non-dispatchable benchmarks (those where {@link #isDispatchable()} returns false).
     * Dispatchable benchmarks do not use this method — the orchestrator drives them via
     * {@link #performSingleOperation()}.
     *
     * @throws UnsupportedOperationException if the benchmark is dispatchable
     */
    default void run() throws Exception {
        throw new UnsupportedOperationException(
            getClass().getSimpleName() + " is dispatchable — use performSingleOperation() via the orchestrator");
    }

    void shutdown();

    /**
     * Execute a single operation for this benchmark. The orchestrator calls this
     * when randomly selecting this tenant for an operation slot. Each benchmark
     * maintains its own operation counter internally.
     *
     * @return a Mono that completes when the operation finishes
     */
    default Mono<?> performSingleOperation() {
        return Mono.error(new UnsupportedOperationException(
            getClass().getSimpleName() + " does not support per-operation dispatch"));
    }

    /**
     * Whether this benchmark supports per-operation dispatch from the orchestrator.
     * Non-dispatchable benchmarks are run via {@link #run()} in their own thread.
     */
    default boolean isDispatchable() {
        return true;
    }

    /**
     * Sets orchestrator-level dispatch parameters for non-dispatchable benchmarks.
     * Called by the orchestrator before {@link #run()} to communicate global dispatch settings.
     *
     * @param concurrency max concurrent operations
     * @param numberOfOperations total operations (ignored when maxRunningTime is set)
     * @param maxRunningTime wall-clock time limit (null = use numberOfOperations)
     */
    default void setDispatchParams(int concurrency, long numberOfOperations, Duration maxRunningTime) {
        // Default no-op for dispatchable benchmarks
    }
}
