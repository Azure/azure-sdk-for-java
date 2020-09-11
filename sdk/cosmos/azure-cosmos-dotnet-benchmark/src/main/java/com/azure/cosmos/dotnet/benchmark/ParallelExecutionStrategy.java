package com.azure.cosmos.dotnet.benchmark;

import com.azure.cosmos.implementation.guava25.base.Function;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

public class ParallelExecutionStrategy implements IExecutionStrategy {
    private final Function<Void, IBenchmarkOperation> benchmarkOperation;
    private final AtomicInteger pendingExecutorCount = new AtomicInteger();

    public ParallelExecutionStrategy(Function<Void, IBenchmarkOperation> benchmarkOperation) {
        this.benchmarkOperation = benchmarkOperation;
    }

    @Override
    public Mono<RunSummary> execute(int serialExecutorConcurrency,
                                    int serialExecutorIterationCount, boolean traceFailures,
                                    double warmupFraction) {

        IExecutor warmupExecutor = new SerialOperationExecutor(
            "Warmup",
            this.benchmarkOperation.apply(null));

        this.pendingExecutorCount.set(serialExecutorConcurrency);

        return warmupExecutor
            .execute(
                (int)(serialExecutorIterationCount * warmupFraction),
                true,
                traceFailures,
                null)
            .flatMap((dummy) -> {
                IExecutor[] executors = new IExecutor[serialExecutorConcurrency];
                for (int i = 0; i < serialExecutorConcurrency; i++) {
                    executors[i] = new SerialOperationExecutor(
                        String.valueOf(i),
                        this.benchmarkOperation.apply(null));
                }

                Mono<Void> processingTask = Flux
                            .range(0, serialExecutorConcurrency)
                            .flatMap(
                                (index) -> {
                                    return executors[i].execute(
                                        serialExecutorIterationCount,
                                        false,
                                        traceFailures,
                                        (noInput) -> {
                                            this.pendingExecutorCount.decrementAndGet();
                                            return null; });
                                },
                                serialExecutorConcurrency)
                            .then();

                Mono<RunSummary> monitoringTask = Mono

                    .then();
            });
    }
}
