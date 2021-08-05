// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

/**
 * Represents the main program class which reflectively runs and manages the performance tests.
 */
public class PerfStressProgram {
    private static final int NANOSECONDS_PER_SECOND = 1_000_000_000;

    private static int[] completedOperations;
    private static long[] lastCompletionNanoTimes;

    private static int getCompletedOperations() {
        return IntStream.of(completedOperations).sum();
    }

    private static double getOperationsPerSecond() {
        return IntStream.range(0, completedOperations.length)
            .mapToDouble(i -> completedOperations[i] / (((double) lastCompletionNanoTimes[i]) / NANOSECONDS_PER_SECOND))
            .sum();
    }

    /**
     * Runs the performance tests passed to be executed.
     *
     * @param classes the performance test classes to execute.
     * @param args the command line arguments ro run performance tests with.
     *
     * @throws RuntimeException if the execution fails.
     */
    public static void run(Class<?>[] classes, String[] args) {
        List<Class<?>> classList = new ArrayList<>(Arrays.asList(classes));

        try {
            classList.add(Class.forName("com.azure.perf.test.core.NoOpTest"));
            classList.add(Class.forName("com.azure.perf.test.core.ExceptionTest"));
            classList.add(Class.forName("com.azure.perf.test.core.SleepTest"));
            classList.add(Class.forName("com.azure.perf.test.core.HttpPipelineTest"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        String[] commands = classList.stream().map(c -> getCommandName(c.getSimpleName()))
            .toArray(i -> new String[i]);

        PerfStressOptions[] options = classList.stream().map(c -> {
            try {
                return c.getConstructors()[0].getParameterTypes()[0].getConstructors()[0].newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }).toArray(i -> new PerfStressOptions[i]);

        JCommander jc = new JCommander();

        for (int i = 0; i < commands.length; i++) {
            jc.addCommand(commands[i], options[i]);
        }


        jc.parse(args);

        String parsedCommand = jc.getParsedCommand();
        if (parsedCommand == null || parsedCommand.isEmpty()) {
            jc.usage();
        } else {
            int index = Arrays.asList(commands).indexOf(parsedCommand);
            run(classList.get(index), options[index]);
        }
    }

    private static String getCommandName(String testName) {
        String lower = testName.toLowerCase();
        return lower.endsWith("test") ? lower.substring(0, lower.length() - 4) : lower;
    }

    /**
     * Run the performance test passed to be executed.
     *
     * @param testClass the performance test class to execute.
     * @param options the configuration ro run performance test with.
     *
     * @throws RuntimeException if the execution fails.
     */
    public static void run(Class<?> testClass, PerfStressOptions options) {
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
        Disposable setupStatus = printStatus("=== Setup ===", () -> ".", false, false);
        Disposable cleanupStatus = null;

        PerfStressTest<?>[] tests = new PerfStressTest<?>[options.getParallel()];

        for (int i = 0; i < options.getParallel(); i++) {
            try {
                tests[i] = (PerfStressTest<?>) testClass.getConstructor(options.getClass()).newInstance(options);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | SecurityException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            tests[0].globalSetupAsync().block();

            boolean startedPlayback = false;

            try {
                Flux.just(tests).flatMap(PerfStressTest::setupAsync).blockLast();
                setupStatus.dispose();

                if (options.getTestProxy() != null) {
                    Disposable recordStatus = printStatus("=== Record and Start Playback ===", () -> ".", false, false);
                    Flux.just(tests).flatMap(PerfStressTest::recordAndStartPlaybackAsync).blockLast();
                    startedPlayback = true;
                    recordStatus.dispose();
                }

                if (options.getWarmup() > 0) {
                    runTests(tests, options.isSync(), options.getParallel(), options.getWarmup(), "Warmup");
                }

                for (int i = 0; i < options.getIterations(); i++) {
                    String title = "Test";
                    if (options.getIterations() > 1) {
                        title += " " + (i + 1);
                    }
                    runTests(tests, options.isSync(), options.getParallel(), options.getDuration(), title);
                }
            } finally {
                try {
                    if (startedPlayback) {
                        Disposable playbackStatus = printStatus("=== Stop Playback ===", () -> ".", false, false);
                        Flux.just(tests).flatMap(PerfStressTest::stopPlaybackAsync).blockLast();
                        playbackStatus.dispose();
                    }    
                } finally {
                    if (!options.isNoCleanup()) {
                        cleanupStatus = printStatus("=== Cleanup ===", () -> ".", false, false);
    
                        Flux.just(tests).flatMap(t -> t.cleanupAsync()).blockLast();
                    }    
                }
            }
        } finally {
            if (!options.isNoCleanup()) {
                if (cleanupStatus == null) {
                    cleanupStatus = printStatus("=== Cleanup ===", () -> ".", false, false);
                }

                tests[0].globalCleanupAsync().block();
            }
        }

        if (cleanupStatus != null) {
            cleanupStatus.dispose();
        }
    }

    /**
     * Runs the performance tests passed to be executed.
     *
     * @param tests the performance tests to be executed.
     * @param sync indicate if synchronous test should be run.
     * @param parallel the number of parallel threads to run the performance test on.
     * @param durationSeconds the duration for which performance test should be run on.
     * @param title the title of the performance tests.
     *
     * @throws RuntimeException if the execution fails.
     * @throws IllegalStateException if zero operations completed of the performance test.
     */
    public static void runTests(PerfStressTest<?>[] tests, boolean sync, int parallel, int durationSeconds, String title) {
        completedOperations = new int[parallel];
        lastCompletionNanoTimes = new long[parallel];

        long endNanoTime = System.nanoTime() + ((long) durationSeconds * 1000000000);

        int[] lastCompleted = new int[]{0};
        Disposable progressStatus = printStatus(
            "=== " + title + " ===" + System.lineSeparator() + "Current\t\tTotal\t\tAverage", () -> {
                int totalCompleted = getCompletedOperations();
                int currentCompleted = totalCompleted - lastCompleted[0];
                double averageCompleted = getOperationsPerSecond();

                lastCompleted[0] = totalCompleted;
                return String.format("%d\t\t%d\t\t%.2f", currentCompleted, totalCompleted, averageCompleted);
            }, true, true);

        try {
            if (sync) {
                ForkJoinPool forkJoinPool = new ForkJoinPool(parallel);
                forkJoinPool.submit(() -> {
                    IntStream.range(0, parallel).parallel().forEach(i -> runLoop(tests[i], i, endNanoTime));
                }).get();

            } else {
                // Exceptions like OutOfMemoryError are handled differently by the default Reactor schedulers. Instead of terminating the
                // Flux, the Flux will hang and the exception is only sent to the thread's uncaughtExceptionHandler and the Reactor
                // Schedulers.onHandleError.  This handler ensures the perf framework will fail fast on any such exceptions.
                Schedulers.onHandleError((t, e) -> {
                    System.err.print(t + " threw exception: ");
                    e.printStackTrace();
                    System.exit(1);
                });

                Flux.range(0, parallel)
                    .parallel()
                    .runOn(Schedulers.boundedElastic())
                    .flatMap(i -> runLoopAsync(tests[i], i, endNanoTime))
                    .then()
                    .block();
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error occurred when submitting jobs to ForkJoinPool. " + System.lineSeparator() + e);
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.err.println("Error occurred running tests: " + System.lineSeparator() + e);
            e.printStackTrace(System.err);
        } finally {
            progressStatus.dispose();
        }

        System.out.println("=== Results ===");

        int totalOperations = getCompletedOperations();
        if (totalOperations == 0) {
            throw new IllegalStateException("Zero operations has been completed");
        }
        double operationsPerSecond = getOperationsPerSecond();
        double secondsPerOperation = 1 / operationsPerSecond;
        double weightedAverageSeconds = totalOperations / operationsPerSecond;

        System.out.printf("Completed %,d operations in a weighted-average of %,.2fs (%,.2f ops/s, %,.3f s/op)%n",
            totalOperations, weightedAverageSeconds, operationsPerSecond, secondsPerOperation);
        System.out.println();
    }

    private static void runLoop(PerfStressTest<?> test, int index, long endNanoTime) {
        long startNanoTime = System.nanoTime();
        while (System.nanoTime() < endNanoTime) {
            test.run();
            completedOperations[index]++;
            lastCompletionNanoTimes[index] = System.nanoTime() - startNanoTime;
        }
    }

    private static Mono<Void> runLoopAsync(PerfStressTest<?> test, int index, long endNanoTime) {
        long startNanoTime = System.nanoTime();

        return Flux.just(1)
            .repeat()
            .flatMap(i -> test.runAsync().then(Mono.just(1)), 1)
            .doOnNext(v -> {
                completedOperations[index]++;
                lastCompletionNanoTimes[index] = System.nanoTime() - startNanoTime;
            })
            .takeWhile(i -> System.nanoTime() < endNanoTime)
            .then();
    }

    private static Disposable printStatus(String header, Supplier<Object> status, boolean newLine, boolean printFinalStatus) {
        System.out.println(header);

        boolean[] needsExtraNewline = new boolean[]{false};

        return Flux.interval(Duration.ofSeconds(1)).doFinally(s -> {
            if (printFinalStatus) {
                printStatusHelper(status, newLine, needsExtraNewline);
            }

            if (needsExtraNewline[0]) {
                System.out.println();
            }
            System.out.println();
        }).subscribe(i -> {
            printStatusHelper(status, newLine, needsExtraNewline);
        });
    }

    private static void printStatusHelper(Supplier<Object> status, boolean newLine, boolean[] needsExtraNewline) {
        Object obj = status.get();
        if (newLine) {
            System.out.println(obj);
        } else {
            System.out.print(obj);
            needsExtraNewline[0] = true;
        }
    }
}
