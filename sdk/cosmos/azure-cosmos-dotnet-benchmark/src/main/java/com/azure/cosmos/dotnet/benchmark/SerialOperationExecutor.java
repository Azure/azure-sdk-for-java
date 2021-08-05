// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.dotnet.benchmark;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SerialOperationExecutor implements IExecutor {

    private final IBenchmarkOperation operation;
    private final String executorId;

    private final AtomicInteger successOperationCount;
    private final AtomicInteger failedOperationCount;
    private final AtomicReference<Double> totalRuCharges;

    public SerialOperationExecutor(
        String executorId,
        IBenchmarkOperation benchmarkOperation) {

        this.operation = benchmarkOperation;
        this.executorId = executorId;
        this.successOperationCount = new AtomicInteger();
        this.failedOperationCount = new AtomicInteger();
        this.totalRuCharges = new AtomicReference<>();
        this.totalRuCharges.set(0d);
    }

    @Override
    public int getSuccessOperationCount() {
        return this.successOperationCount.get();
    }

    @Override
    public int getFailedOperationCount() {
        return this.failedOperationCount.get();
    }

    @Override
    public double getTotalRuCharges() {
        return this.totalRuCharges.get();
    }

    @Override
    public Mono<OperationResult> execute(
        int iterationCount,
        boolean isWarmup,
        boolean traceFailures,
        Runnable completionCallback) {

        Utility.traceInformation(String.format("Executor %s started", this.executorId));

        return Flux
            .range(0, iterationCount)
            .subscribeOn(Schedulers.elastic())
            .flatMapSequential((i) -> {
                    final TelemetrySpan telemetry = TelemetrySpan.createNew(isWarmup);

                    return this.operation
                        .prepare()
                        .flatMap((alwaysVoid) -> this.operation
                            .executeOnce()
                            .doFirst(telemetry::start)
                            .onErrorResume((ex) -> {
                                telemetry.close();

                                if (traceFailures) {
                                    Utility.traceInformation(ex.toString());
                                }

                                this.failedOperationCount.incrementAndGet();

                                return Mono.just(new OperationResult());
                            })
                            .map((r) -> {
                                telemetry.close();

                                this.successOperationCount.incrementAndGet();
                                this.totalRuCharges.getAndUpdate((oldValue) -> oldValue + r.getRuCharges());

                                return r;
                            }));
                },
                1,
                1
            )
            .doFinally((signalType) -> {
                Utility.traceInformation(
                    String.format("Executor %s completed - %s", this.executorId, signalType.toString()));

                if (completionCallback != null) {
                    completionCallback.run();
                }
            })
            .collectList()
            .map((results) -> results.get(results.size() - 1));

    }
}
