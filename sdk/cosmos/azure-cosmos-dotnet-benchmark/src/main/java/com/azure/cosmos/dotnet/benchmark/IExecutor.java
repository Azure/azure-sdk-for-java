package com.azure.cosmos.dotnet.benchmark;

import com.azure.cosmos.implementation.guava25.base.Function;
import reactor.core.publisher.Mono;

public interface IExecutor {
    int getSuccessOperationCount();

    int getFailedOperationCount();

    double getTotalRuCharges();

    Mono<Void> execute(
        int iterationCount,
        boolean isWarmup,
        boolean traceFailures,
        Runnable completionCallback);
}
