package com.azure.cosmos.dotnet.benchmark;

import com.azure.cosmos.implementation.guava25.base.Function;

interface IExecutionStrategy {
    RunSummary execute(
        int serialExecutorConcurrency,
        int serialExecutorIterationCount,
        boolean traceFailures,
        double warmupFraction);

    static IExecutionStrategy startNew(
        BenchmarkConfig config,
        Function<Void, IBenchmarkOperation> benchmarkOperation) {

        return new ParallelExecutionStrategy(config, benchmarkOperation);
    }
}
