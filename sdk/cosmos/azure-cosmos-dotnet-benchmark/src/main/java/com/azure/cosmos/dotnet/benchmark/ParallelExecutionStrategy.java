package com.azure.cosmos.dotnet.benchmark;

import com.azure.cosmos.implementation.guava25.base.Function;
import org.fusesource.jansi.Ansi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;

public class ParallelExecutionStrategy implements IExecutionStrategy {
    private static final long OUTPUT_LOOP_DELAY_IN_MS = 1000;
    private final Function<Void, IBenchmarkOperation> benchmarkOperation;
    private final AtomicInteger pendingExecutorCount = new AtomicInteger();

    public ParallelExecutionStrategy(Function<Void, IBenchmarkOperation> benchmarkOperation) {
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
            this.benchmarkOperation.apply(null));

        // Block while warmup happens
        warmupExecutor.execute(
            (int)(serialExecutorIterationCount * warmupFraction),
            true,
            traceFailures,
            null);

        this.pendingExecutorCount.set(serialExecutorConcurrency);

        // Setting up executors and kick-off tests
        IExecutor[] executors = new IExecutor[serialExecutorConcurrency];

        ArrayList<Double> perLoopCounters = new ArrayList<>();
        Summary lastSummary = new Summary();
        long startTime = System.nanoTime();

        for (int i = 0; i < serialExecutorConcurrency; i++) {
            executors[i] = new SerialOperationExecutor(
                String.valueOf(i),
                this.benchmarkOperation.apply(null));
            executors[i].execute(
                serialExecutorIterationCount,
                false,
                traceFailures,
                this.pendingExecutorCount::decrementAndGet).subscribe();
        }

        // Wait for completion and regularly emit run summaries
        boolean isLastIterationCompleted;
        do {
            isLastIterationCompleted = this.pendingExecutorCount.get() <= 0;

            Summary currentTotalSummary = new Summary();
            for (IExecutor executor : executors) {
                Summary executorSummary = new Summary(
                    executor.getSuccessOperationCount(),
                    executor.getFailedOperationCount(),
                    executor.getTotalRuCharges());

                currentTotalSummary = currentTotalSummary.add(executorSummary);
            }

            // In-theory summary might be lower than real as its not transactional on time
            currentTotalSummary.setElapsedTimeInMs(
                TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS));

            Summary diff = currentTotalSummary.subtract(lastSummary);
            lastSummary = currentTotalSummary;

            diff.print(currentTotalSummary.getTotalOperationsCount());
            perLoopCounters.add(diff.getRps());

            try {
                Thread.sleep(OUTPUT_LOOP_DELAY_IN_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } while (!isLastIterationCompleted);

        Utility.traceInformation("", Ansi.Color.GREEN);
        Utility.traceInformation("Summary:", Ansi.Color.GREEN);
        Utility.traceInformation(
            "--------------------------------------------------------------------- ",
            Ansi.Color.GREEN);
        lastSummary.print(lastSummary.getTotalOperationsCount(), Ansi.Color.GREEN);


        Double[] summaryCounters = (Double[])perLoopCounters.stream().skip(5).limit(perLoopCounters.size() - 10).toArray();
        RunSummary runSummary = new RunSummary();

        if (summaryCounters.length > 10) {
            Utility.traceInformation("After Excluding outliers", Ansi.Color.GREEN);
            DoubleStream rpsCounters = Arrays.stream(summaryCounters).sorted(Comparator.reverseOrder()).mapToDouble(d -> d);
            long signalCount = rpsCounters.count();

            runSummary.setTop10PercentAverageRps(rpsCounters.limit((int)(0.1 * signalCount)).average().orElse(-1));
            runSummary.setTop10PercentAverageRps(rpsCounters.limit((int)(0.2 * signalCount)).average().orElse(-1));
            runSummary.setTop10PercentAverageRps(rpsCounters.limit((int)(0.3 * signalCount)).average().orElse(-1));
            runSummary.setTop10PercentAverageRps(rpsCounters.limit((int)(0.4 * signalCount)).average().orElse(-1));
            runSummary.setTop10PercentAverageRps(rpsCounters.limit((int)(0.5 * signalCount)).average().orElse(-1));
            runSummary.setTop10PercentAverageRps(rpsCounters.limit((int)(0.6 * signalCount)).average().orElse(-1));
            runSummary.setTop10PercentAverageRps(rpsCounters.limit((int)(0.7 * signalCount)).average().orElse(-1));
            runSummary.setTop10PercentAverageRps(rpsCounters.limit((int)(0.8 * signalCount)).average().orElse(-1));
            runSummary.setTop10PercentAverageRps(rpsCounters.limit((int)(0.9 * signalCount)).average().orElse(-1));
            runSummary.setTop10PercentAverageRps(rpsCounters.limit((int)(0.95 * signalCount)).average().orElse(-1));
            runSummary.setAverageRps(rpsCounters.average().orElse(-1));

            // TODO fabianm add latency percentiles

            String summary = JsonHelper.toJsonString(runSummary);
            Utility.traceInformation(summary, Ansi.Color.GREEN);
        } else {
            Utility.traceInformation("Please adjust ItemCount high to run of at-least 1M", Ansi.Color.RED);
        }

        Utility.traceInformation(
            "--------------------------------------------------------------------- ",
            Ansi.Color.GREEN);

        return runSummary;
    }
}
