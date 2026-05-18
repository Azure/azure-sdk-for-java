// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import reactor.core.publisher.Mono;

/**
 * Common contract for all benchmark workloads.
 * Implementations are created by {@link BenchmarkOrchestrator} and participate
 * in its lifecycle loop (create → run → shutdown → settle × N cycles).
 *
 * <p>Benchmarks implement {@link #performSingleOperation()}.
 * The orchestrator calls this for each operation slot; each benchmark tracks its own
 * operation index internally.</p>
 */
public interface Benchmark {

    void shutdown();

    /**
     * Execute a single operation for this benchmark. The orchestrator calls this
     * when randomly selecting this tenant for an operation slot. Each benchmark
     * maintains its own operation counter internally.
     *
     * @return a Mono that completes when the operation finishes
     */
    Mono<?> performSingleOperation();
}
