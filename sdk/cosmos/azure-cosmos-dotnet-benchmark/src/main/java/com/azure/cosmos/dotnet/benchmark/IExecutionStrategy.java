package com.azure.cosmos.dotnet.benchmark;

import com.azure.cosmos.implementation.guava25.base.Function;
import reactor.core.publisher.Mono;

interface IExecutionStrategy {
    public RunSummary execute(
        int serialExecutorConcurrency,
        int serialExecutorIterationCount,
        boolean traceFailures,
        double warmupFraction);

    public static IExecutionStrategy startNew(
        BenchmarkConfig config,
        Function<Void, IBenchmarkOperation> benchmarkOperation) {

        return new ParallelExecutionStrategy(config, benchmarkOperation);
    }
}
