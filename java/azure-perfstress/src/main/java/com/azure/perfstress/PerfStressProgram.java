package com.azure.perfstress;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.JCommander.Builder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class PerfStressProgram {
    private static int[] _completedOperations;
    private static long[] _lastCompletionNanoTimes;

    public static void Run(Class<?>[] classes, String[] args) {
        List<Class<?>> classList = new ArrayList<>(Arrays.asList(classes));

        try {
            classList.add(Class.forName("com.azure.perfstress.NoOpTest"));
            classList.add(Class.forName("com.azure.perfstress.SleepTest"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        
        String[] commands = classList.stream().map(c -> GetCommandName(c.getSimpleName()))
                .toArray(i -> new String[i]);

        PerfStressOptions[] options = classList.stream().map(c -> {
            try {
                return c.getConstructors()[0].getParameterTypes()[0].getConstructors()[0].newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }).toArray(i -> new PerfStressOptions[i]);

        Builder builder = JCommander.newBuilder();

        for (int i = 0; i < commands.length; i++) {
            builder.addCommand(commands[i], options[i]);
        }

        JCommander jc = builder.build();

        jc.parse(args);

        String parsedCommand = jc.getParsedCommand();
        if (parsedCommand == null || parsedCommand.isEmpty()) {
            jc.usage();
        } else {
            int index = Arrays.asList(commands).indexOf(parsedCommand);
            Run(classList.get(index), options[index]);
        }
    }

    private static String GetCommandName(String testName) {
        String lower = testName.toLowerCase();
        return lower.endsWith("test") ? lower.substring(0, lower.length() - 4) : lower;
    }

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
        System.out.println();
        Disposable setupStatus = PrintStatus("=== Setup ===", () -> ".", false, false);
        Disposable cleanupStatus = null;

        PerfStressTest<?>[] tests = new PerfStressTest<?>[options.Parallel];

        for (int i = 0; i < options.Parallel; i++) {
            try {
                tests[i] = (PerfStressTest<?>) testClass.getConstructor(options.getClass()).newInstance(options);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | SecurityException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            tests[0].GlobalSetupAsync().block();
            try {
                Flux.just(tests).flatMap(t -> t.SetupAsync()).blockLast();
                setupStatus.dispose();

                if (options.Warmup > 0) {
                    RunTests(tests, options.Sync, options.Parallel, options.Warmup, "Warmup");
                }

                for (int i=0; i < options.Iterations; i++) {
                    String title = "Test";
                    if (options.Iterations > 1) {
                        title += " " + (i + 1);
                    }
                    RunTests(tests, options.Sync, options.Parallel, options.Duration, title);
                }
            } finally {
                if (!options.NoCleanup) {
                    if (cleanupStatus == null) {
                        cleanupStatus = PrintStatus("=== Cleanup ===", () -> ".", false, false);
                    }

                    Flux.just(tests).flatMap(t -> t.CleanupAsync()).blockLast();
                }
            }
        } finally {
            if (!options.NoCleanup) {
                if (cleanupStatus == null) {
                    cleanupStatus = PrintStatus("=== Cleanup ===", () -> ".", false, false);
                }

                tests[0].GlobalCleanupAsync().block();
            }
        }

        if (cleanupStatus != null) {
            cleanupStatus.dispose();
        }
    }

    public static void RunTests(PerfStressTest<?>[] tests, boolean sync, int parallel, int durationSeconds, String title) {
        _completedOperations = new int[parallel];
        _lastCompletionNanoTimes = new long[parallel];

        long endNanoTime = System.nanoTime() + ((long) durationSeconds * 1000000000);

        int[] lastCompleted = new int[] { 0 };
        Disposable progressStatus = PrintStatus(
                "=== " + title + " ===" + System.lineSeparator() + "Current\t\tTotal", () -> {
                    int totalCompleted = IntStream.of(_completedOperations).sum();
                    int currentCompleted = totalCompleted - lastCompleted[0];
                    lastCompleted[0] = totalCompleted;
                    return currentCompleted + "\t\t" + totalCompleted;
                }, true, true);

        if (sync) {
            ForkJoinPool forkJoinPool = new ForkJoinPool(parallel);
            try {
                forkJoinPool.submit(() -> {
                    IntStream.range(0, parallel).parallel().forEach(i -> RunLoop(tests[i], i, endNanoTime));
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            Flux.range(0, parallel)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(i -> RunLoopAsync(tests[i], i, endNanoTime))
                .then()
                .block();
        }

        progressStatus.dispose();

        System.out.println("=== Results ===");

        int totalOperations = IntStream.of(_completedOperations).sum();
        double operationsPerSecond = IntStream.range(0, parallel)
                .mapToDouble(i -> _completedOperations[i] / (((double)_lastCompletionNanoTimes[i]) / 1000000000))
                .sum();
        double secondsPerOperation = 1 / operationsPerSecond;
        double weightedAverageSeconds = totalOperations / operationsPerSecond;

        System.out.printf("Completed %d operations in a weighted-average of %.2fs (%.2f ops/s, %.3f s/op)%n",
                totalOperations, weightedAverageSeconds, operationsPerSecond, secondsPerOperation);
        System.out.println();
    }

    private static void RunLoop(PerfStressTest<?> test, int index, long endNanoTime) {
        long startNanoTime = System.nanoTime();
        while (System.nanoTime() < endNanoTime) {
            test.Run();
            _completedOperations[index]++;
            _lastCompletionNanoTimes[index] = System.nanoTime() - startNanoTime;
        }
    }

    private static Mono<Void> RunLoopAsync(PerfStressTest<?> test, int index, long endNanoTime) {
        long startNanoTime = System.nanoTime();

        return Flux.just(1)
            .repeat()
            .flatMap(i -> test.RunAsync().then(Mono.just(1)), 1)
            .doOnNext(v -> {
                _completedOperations[index]++;
                _lastCompletionNanoTimes[index] = System.nanoTime() - startNanoTime;
            })
            .take(Duration.ofNanos(endNanoTime - startNanoTime))
            .then();
    }

    private static Disposable PrintStatus(String header, Supplier<Object> status, boolean newLine, boolean printFinalStatus) {
        System.out.println(header);

        boolean[] needsExtraNewline = new boolean[] { false };

        return Flux.interval(Duration.ofSeconds(1)).doFinally(s -> {
            if (printFinalStatus) {
                PrintStatusHelper(status, newLine, needsExtraNewline);
            }

            if (needsExtraNewline[0]) {
                System.out.println();
            }
            System.out.println();
        }).subscribe(i -> {
            PrintStatusHelper(status, newLine, needsExtraNewline);
        });
    }

    private static void PrintStatusHelper(Supplier<Object> status, boolean newLine, boolean[] needsExtraNewline) {
        Object obj = status.get();
        if (newLine) {
            System.out.println(obj);
        } else {
            System.out.print(obj);
            needsExtraNewline[0] = true;
        }
    }
}