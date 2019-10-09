package com.microsoft.storageperf.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

public class PerfStressProgram {
    private static AtomicInteger _completedOperations = new AtomicInteger();
    private static long[] _lastCompletionNanoTimes;

    public static void Run(Class<?> testClass, PerfStressOptions options) {
        System.out.println("=== Options ===");
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
            mapper.writeValue(System.out, options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println();
        Disposable setupStatus = PrintStatus("=== Setup ===", () -> ".", false);
        Disposable cleanupStatus = null;

        _lastCompletionNanoTimes = new long[options.Parallel];

        PerfStressTest<?>[] tests = new PerfStressTest<?>[options.Parallel];

        for (int i = 0; i < options.Parallel; i++) {
            try {
                tests[i] = (PerfStressTest<?>) testClass.getConstructor(options.getClass()).newInstance(options);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | SecurityException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool(options.Parallel);

        try {
            tests[0].GlobalSetup();
            try {
                try {
                    forkJoinPool.submit(() -> {
                        Arrays.stream(tests).parallel().forEach(t -> t.Setup());
                    }).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }

                setupStatus.dispose();

                long endNanoTime = System.nanoTime() + ((long)options.Duration * 1000000000);
 
                int[] lastCompleted = new int[] { 0 };
                Disposable progressStatus = PrintStatus(
                    "=== Progress ===" + System.lineSeparator() +
                    "Current\t\tTotal",
                    () -> {
                        int totalCompleted = _completedOperations.get();
                        int currentCompleted = totalCompleted - lastCompleted[0];
                        lastCompleted[0] = totalCompleted;
                        return currentCompleted + "\t\t" + totalCompleted;
                    },
                    true);

                try {
                    forkJoinPool.submit(() -> {
                        Arrays.stream(tests).parallel().forEach(t -> RunLoop(t, endNanoTime));
                    }).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }

                progressStatus.dispose();

            } finally {
                if (!options.NoCleanup) {
                    if (cleanupStatus == null) {
                        cleanupStatus = PrintStatus("=== Cleanup ===", () -> ".", false);
                    }

                    try {
                        forkJoinPool.submit(() -> {
                            Arrays.stream(tests).parallel().forEach(t -> t.Setup());
                        }).get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } finally {
            if (!options.NoCleanup) {
                if (cleanupStatus == null) {
                    cleanupStatus = PrintStatus("=== Cleanup ===", () -> ".", false);
                }

                tests[0].GlobalCleanup();
            }
        }

        cleanupStatus.dispose();

        System.out.println("=== Results ===");

        double averageElapsedSeconds = (Arrays.stream(_lastCompletionNanoTimes).average().orElse(Double.NaN))
                / 1000000000;
        double operationsPerSecond = _completedOperations.get() / averageElapsedSeconds;
        double secondsPerOperation = 1 / operationsPerSecond;

        System.out.printf("Completed %d operations in an average of %.2fs (%.2f ops/s, %.3f s/op)%n",
                _completedOperations.get(), averageElapsedSeconds, operationsPerSecond, secondsPerOperation);
        System.out.println();
    }

    private static void RunLoop(PerfStressTest<?> test, long endNanoTime) {
        long startNanoTime = System.nanoTime();
        while (System.nanoTime() < endNanoTime) {
            test.Run();
            int count = _completedOperations.incrementAndGet();
            _lastCompletionNanoTimes[count % _lastCompletionNanoTimes.length] = System.nanoTime() - startNanoTime;
        }
    }

    private static Disposable PrintStatus(String header, Supplier<Object> status, boolean newLine) {
        System.out.println(header);

        boolean[] needsExtraNewline = new boolean[] { false };

        return Flux.interval(Duration.ofSeconds(1)).doFinally(s -> {
            if (needsExtraNewline[0]) {
                System.out.println();
            }
            System.out.println();
        }).subscribe(i -> {
            Object obj = status.get();
            if (newLine) {
                System.out.println(obj);
            } else {
                System.out.print(obj);
                needsExtraNewline[0] = true;
            }
        });
    }
}