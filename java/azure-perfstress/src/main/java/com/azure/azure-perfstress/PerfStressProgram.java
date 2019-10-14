package com.azure.perfstress;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.JCommander.Builder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PerfStressProgram {
    private static AtomicInteger _completedOperations = new AtomicInteger();
    private static long[] _lastCompletionNanoTimes;

    public static void Run(Class<?>[] classes, String[] args) {
        List<Class<?>> classList = new ArrayList<>(Arrays.asList(classes));

        try {
            classList.add(Class.forName("com.azure.perfstress.NoOpTest"));
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
        Disposable setupStatus = PrintStatus("=== Setup ===", () -> ".", false);
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
                        title += " " + i;
                    }
                    RunTests(tests, options.Sync, options.Parallel, options.Duration, title);
                }
            } finally {
                if (!options.NoCleanup) {
                    if (cleanupStatus == null) {
                        cleanupStatus = PrintStatus("=== Cleanup ===", () -> ".", false);
                    }

                    Flux.just(tests).flatMap(t -> t.CleanupAsync()).blockLast();
                }
            }
        } finally {
            if (!options.NoCleanup) {
                if (cleanupStatus == null) {
                    cleanupStatus = PrintStatus("=== Cleanup ===", () -> ".", false);
                }

                tests[0].GlobalCleanupAsync().block();
            }
        }

        cleanupStatus.dispose();
    }

    public static void RunTests(PerfStressTest<?>[] tests, boolean sync, int parallel, int durationSeconds, String title) {
        _completedOperations.set(0);
        _lastCompletionNanoTimes = new long[parallel];

        long endNanoTime = System.nanoTime() + ((long) durationSeconds * 1000000000);

        int[] lastCompleted = new int[] { 0 };
        Disposable progressStatus = PrintStatus(
                "=== " + title + " ===" + System.lineSeparator() + "Current\t\tTotal", () -> {
                    int totalCompleted = _completedOperations.get();
                    int currentCompleted = totalCompleted - lastCompleted[0];
                    lastCompleted[0] = totalCompleted;
                    return currentCompleted + "\t\t" + totalCompleted;
                }, true);

        if (sync) {
            ForkJoinPool forkJoinPool = new ForkJoinPool(parallel);
            try {
                forkJoinPool.submit(() -> {
                    Arrays.stream(tests).parallel().forEach(t -> RunLoop(t, endNanoTime));
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            Flux.just(tests).flatMap(t -> RunLoopAsync(t, endNanoTime)).blockLast();
        }

        progressStatus.dispose();

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

    private static Mono<Void> RunLoopAsync(PerfStressTest<?> test, long endNanoTime) {
        long startNanoTime = System.nanoTime();

        return Flux.just(1)
            .repeat()
            .flatMap(i -> test.RunAsync().then(Mono.just(1)), 1)
            .doOnNext(v -> {
                int count = _completedOperations.incrementAndGet();
                _lastCompletionNanoTimes[count % _lastCompletionNanoTimes.length] = System.nanoTime() - startNanoTime;
            })
            .take(Duration.ofNanos(endNanoTime - startNanoTime))
            .then();
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