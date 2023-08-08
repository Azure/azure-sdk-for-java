// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.dotnet.benchmark;

import reactor.core.publisher.Mono;

public interface IExecutor {
    int getSuccessOperationCount();

    int getFailedOperationCount();

    double getTotalRuCharges();

    Mono<OperationResult> execute(
        int iterationCount,
        boolean isWarmup,
        boolean traceFailures,
        Runnable completionCallback);
}
