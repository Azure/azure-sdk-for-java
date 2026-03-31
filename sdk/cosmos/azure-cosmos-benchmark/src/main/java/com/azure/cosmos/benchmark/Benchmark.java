// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

/**
 * Common contract for all benchmark workloads.
 * Implementations are created by {@link BenchmarkOrchestrator} and participate
 * in its lifecycle loop (create → run → shutdown → settle × N cycles).
 */
public interface Benchmark {
    void run() throws Exception;
    void shutdown();
}
