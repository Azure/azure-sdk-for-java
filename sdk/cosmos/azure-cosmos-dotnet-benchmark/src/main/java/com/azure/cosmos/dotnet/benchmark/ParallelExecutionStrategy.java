// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.dotnet.benchmark;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

public class ParallelExecutionStrategy implements IExecutionStrategy {
    private static final long OUTPUT_LOOP_DELAY_IN_MS = 1000;
    private final Supplier<IBenchmarkOperation> benchmarkOperation;
    private final BenchmarkConfig config;
    private final AtomicInteger pendingExecutorCount = new AtomicInteger();

    public ParallelExecutionStrategy(BenchmarkConfig config,
                                     Supplier<IBenchmarkOperation> benchmarkOperation) {

        this.config = config;
        this.benchmarkOperation = benchmarkOperation;
    }

    @Override
    public RunSummary execute(
        int serialExecutorConcurrency,
        int serialExecutorIterationCount,
        boolean traceFailures,
        double warmupFraction) {

        IExecutor warmupExecutor = new SerialOperationExecutor(
            "Warmup",
            this.benchmarkOperation.get());

        // Block while warmup happens
        warmupExecutor.execute(
            (int)(serialExecutorIterationCount * warmupFraction),
            true,
            traceFailures,
            null).block();

        this.pendingExecutorCount.set(serialExecutorConcurrency);

        // Setting up executors and kick-off tests
        final IExecutor[] executors = new IExecutor[serialExecutorConcurrency];

        ArrayList<Double> perLoopCounters = new ArrayList<>();
        AtomicReference<Summary> lastSummary = new AtomicReference<>();
        lastSummary.set(new Summary());
        final AtomicLong startTime = new AtomicLong();

        Flux<OperationResult> processingFlux = Flux
            .range(0, serialExecutorConcurrency)
            .subscribeOn(Schedulers.elastic())
            .flatMap((i) -> {
                    executors[i] = new SerialOperationExecutor(
                        String.valueOf(i),
                        this.benchmarkOperation.get());

                    return executors[i].execute(
                        serialExecutorIterationCount,
                        false,
                        traceFailures,
                        this.pendingExecutorCount::decrementAndGet);
                },
                serialExecutorConcurrency)
            .doFirst(() -> {
                Utility.traceInformation("Starting executors...");
                startTime.set(System.nanoTime());
            });

        Flux<Boolean> monitoringTimer = Flux
            .interval(Duration.ofMillis(OUTPUT_LOOP_DELAY_IN_MS))
            .onBackpressureDrop()
            .map((time) -> {
                int pendingExecutorCountSnapshot = this.pendingExecutorCount.get();
                boolean isLastIterationCompleted = pendingExecutorCountSnapshot <= 0;

                Summary currentTotalSummary = new Summary();
                for (IExecutor executor : executors) {
                    if (executor == null) {
                        continue;
                    }

                    Summary executorSummary = new Summary(
                        executor.getSuccessOperationCount(),
                        executor.getFailedOperationCount(),
                        executor.getTotalRuCharges());

                    currentTotalSummary = currentTotalSummary.add(executorSummary);
                }

                // In-theory summary might be lower than real as its not transactional on time
                currentTotalSummary.setElapsedTimeInMs(
                    TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime.get(),
                        TimeUnit.NANOSECONDS));

                Summary lastSummarySnapshot = lastSummary.getAndSet(currentTotalSummary);
                Summary diff = currentTotalSummary.subtract(lastSummarySnapshot);

                diff.print(currentTotalSummary.getTotalOperationsCount());
                perLoopCounters.add(diff.getRps());

                return isLastIterationCompleted;
            })
            .takeUntil((isLastIterationCompleted) -> isLastIterationCompleted);

        monitoringTimer.subscribeOn(Schedulers.elastic()).subscribe();
        processingFlux.collectList().block();

        Utility.traceInformation("");
        Utility.traceInformation("Summary:");
        Utility.traceInformation(
            "--------------------------------------------------------------------- ");
        Summary lastSummarySnapshot = lastSummary.get();
        lastSummarySnapshot.print(lastSummarySnapshot.getTotalOperationsCount());

        RunSummary runSummary = new RunSummary();
        if (perLoopCounters.size() > 20) {
            Double[] summaryCounters = new Double[perLoopCounters.size() - 10];
            for (int i = 5; i < perLoopCounters.size() - 5; i++) {
                summaryCounters[i - 5] = perLoopCounters.get(i);
            }
            Arrays.sort(summaryCounters, Comparator.reverseOrder());

            Utility.traceInformation("After Excluding outliers");
            Supplier<DoubleStream> rpsCounters =
                () -> Arrays.stream(summaryCounters).mapToDouble(d -> d);
            long signalCount = summaryCounters.length;

            runSummary.setTop10PercentAverageRps(
                rpsCounters.get().limit((int)(0.1 * signalCount)).average().orElse(-1));
            runSummary.setTop20PercentAverageRps(
                rpsCounters.get().limit((int)(0.2 * signalCount)).average().orElse(-1));
            runSummary.setTop30PercentAverageRps(
                rpsCounters.get().limit((int)(0.3 * signalCount)).average().orElse(-1));
            runSummary.setTop40PercentAverageRps(
                rpsCounters.get().limit((int)(0.4 * signalCount)).average().orElse(-1));
            runSummary.setTop50PercentAverageRps(
                rpsCounters.get().limit((int)(0.5 * signalCount)).average().orElse(-1));
            runSummary.setTop60PercentAverageRps(
                rpsCounters.get().limit((int)(0.6 * signalCount)).average().orElse(-1));
            runSummary.setTop70PercentAverageRps(
                rpsCounters.get().limit((int)(0.7 * signalCount)).average().orElse(-1));
            runSummary.setTop80PercentAverageRps(
                rpsCounters.get().limit((int)(0.8 * signalCount)).average().orElse(-1));
            runSummary.setTop90PercentAverageRps(
                rpsCounters.get().limit((int)(0.9 * signalCount)).average().orElse(-1));
            runSummary.setTop95PercentAverageRps(
                rpsCounters.get().limit((int)(0.95 * signalCount)).average().orElse(-1));
            runSummary.setAverageRps(rpsCounters.get().average().orElse(-1));

            if (this.config.isEnableLatencyPercentiles()) {
                TelemetrySpan.prepareForCalculations();
                runSummary.setTop50PercentLatencyInMs(TelemetrySpan.getLatencyPercentile(0.5));
                runSummary.setTop75PercentLatencyInMs(TelemetrySpan.getLatencyPercentile(0.75));
                runSummary.setTop90PercentLatencyInMs(TelemetrySpan.getLatencyPercentile(0.9));
                runSummary.setTop95PercentLatencyInMs(TelemetrySpan.getLatencyPercentile(0.95));
                runSummary.setTop99PercentLatencyInMs(TelemetrySpan.getLatencyPercentile(0.99));
            }

            String summary = JsonHelper.toJsonString(runSummary);
            Utility.traceInformation(summary);
        } else {
            Utility.traceInformation("!!! Please adjust ItemCount high to run of at-least 1M !!!");
        }

        Utility.traceInformation(
            "--------------------------------------------------------------------- ");

        return runSummary;
    }
}
