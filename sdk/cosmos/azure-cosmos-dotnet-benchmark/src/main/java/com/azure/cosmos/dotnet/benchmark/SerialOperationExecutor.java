package com.azure.cosmos.dotnet.benchmark;

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
        Runnable completionCallback) {

        LOGGER.info(String.format("Executor %s started", this.executorId));

        return this.operation.prepare().flatMap((dummy) -> Flux
            .range(0, iterationCount)
            .flatMapSequential(
                (i) -> {
                    TelemetrySpan telemetry = TelemetrySpan.startNew(isWarmup);
                    return this.operation.executeOnce()
                                         .onErrorResume((ex) -> {
                           telemetry.close();

                           if (traceFailures) {
                               Utility.traceInformation(ex.toString(), Ansi.Color.RED);
                           }

                           this.failedOperationCount++;

                           // TODO fabianm extract RU charge from CosmosException and add to totalRuCharges
                           // if (ex instanceof CosmosException) {
                           //}

                           return Mono.empty();
                        })
                                         .map((r) -> {
                            telemetry.close();

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
                        completionCallback.run();
                    }

                    return Mono.empty();
                })
            .then());
    }
}
