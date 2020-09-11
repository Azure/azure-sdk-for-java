package com.azure.cosmos.dotnet.benchmark;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.guava25.base.Function;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SerialOperationExecutor implements IExecutor {

    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private final IBenchmarkOperation operation;
    private final String executorId;

    private int successOperationCount;

    private int failedOperationCount;

    private double totalRuCharges;

    public SerialOperationExecutor(
        String executorId,
        IBenchmarkOperation benchmarkOperation) {

        this.operation = benchmarkOperation;
        this.executorId = executorId;
        this.successOperationCount = 0;
        this.failedOperationCount = 0;
        this.totalRuCharges = 0;
    }

    @Override
    public int getSuccessOperationCount() {
        return this.successOperationCount;
    }

    @Override
    public int getFailedOperationCount() {
        return this.failedOperationCount;
    }

    @Override
    public double getTotalRuCharges() {
        return this.totalRuCharges;
    }

    @Override
    public Mono<Void> execute(
        int iterationCount,
        boolean isWarmup,
        boolean traceFailures,
        Function<Void, Void> completionCallback) {

        LOGGER.info(String.format("Executor %s started", this.executorId));

        int currentIterationCount = 0;

        OperationResult operationResult = null;

        return this.operation.prepare().flatMap((dummy) -> {
            return Flux
                .range(0, iterationCount)
                .flatMapSequential(
                    (i) -> {
                        return this.operation.executeOnce()
                            .onErrorResume((ex) -> {
                               if (traceFailures) {
                                   Utility.traceInformation(ex.toString(), Ansi.Color.RED);
                               }

                               this.failedOperationCount++;

                               if (ex instanceof CosmosException) {
                                   // TODO extract RU charge from CosmosException and add to totalRuCharges
                               }

                               return Mono.empty();
                            })
                            .map((r) -> {
                                this.successOperationCount++;
                                this.totalRuCharges += r.getRuCharges();

                                return Mono.empty();
                            });
                    },
                    1,
                    0)
                .last()
                .map((nothing) -> {
                        LOGGER.info(String.format("Executor %s completed", this.executorId));

                        if (completionCallback != null) {
                            completionCallback.apply(null);
                        }

                        return Mono.empty();
                    })
                .then();
        });
    }
}
