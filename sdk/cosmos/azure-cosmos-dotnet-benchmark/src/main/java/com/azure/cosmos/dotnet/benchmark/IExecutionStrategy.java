// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.dotnet.benchmark;

import java.util.function.Supplier;

interface IExecutionStrategy {
    RunSummary execute(
        int serialExecutorConcurrency,
        int serialExecutorIterationCount,
        boolean traceFailures,
        double warmupFraction);

    static IExecutionStrategy startNew(
        BenchmarkConfig config,
        Supplier<IBenchmarkOperation> benchmarkOperation) {

        return new ParallelExecutionStrategy(config, benchmarkOperation);
    }
}
