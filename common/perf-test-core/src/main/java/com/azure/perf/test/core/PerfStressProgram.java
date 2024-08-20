// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Represents the main program class which reflectively runs and manages the performance tests.
 */
public class PerfStressProgram {
    private static final int NANOSECONDS_PER_SECOND = 1_000_000_000;

    private static long getCompletedOperations(PerfTestBase<?>[] tests) {
        long completedOperations = 0;
        for (PerfTestBase<?> test : tests) {
            completedOperations += test.getCompletedOperations();
        }

        return completedOperations;
    }

    private static double getOperationsPerSecond(PerfTestBase<?>[] tests) {
        double operationsPerSecond = 0.0D;
        for (PerfTestBase<?> test : tests) {
            double temp = test.getCompletedOperations() / (((double) test.lastCompletionNanoTime) / NANOSECONDS_PER_SECOND);
            if (!Double.isNaN(temp)) {
                operationsPerSecond += temp;
            }
        }

        return operationsPerSecond;
    }

    /**
     * Runs the performance tests passed to be executed.
     *
     * @param classes the performance test classes to execute.
     * @param args the command line arguments ro run performance tests with.
     * @throws RuntimeException if the execution fails.
     */
    public static void run(Class<?>[] classes, String[] args) {
        List<Class<?>> classList = new ArrayList<>(Arrays.asList(classes));

        try {
            classList.add(Class.forName("com.azure.perf.test.core.NoOpTest"));
            classList.add(Class.forName("com.azure.perf.test.core.MockEventProcessorTest"));
            classList.add(Class.forName("com.azure.perf.test.core.ExceptionTest"));
            classList.add(Class.forName("com.azure.perf.test.core.SleepTest"));
            classList.add(Class.forName("com.azure.perf.test.core.HttpPipelineTest"));
            classList.add(Class.forName("com.azure.perf.test.core.MockBatchReceiverTest"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        String[] commands = classList.stream().map(c -> getCommandName(c.getSimpleName()))
            .toArray(i -> new String[i]);

        PerfStressOptions[] options = classList.stream().map(c -> {
            try {
                return c.getConstructors()[0].getParameterTypes()[0].getConstructors()[0].newInstance();
            } catch (ReflectiveOperationException e) {
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

        Timer setupStatus = printStatus("=== Setup ===", () -> ".", false, false);
        Timer cleanupStatus = null;

        PerfTestBase<?>[] tests = new PerfTestBase<?>[options.getParallel()];

        for (int i = 0; i < options.getParallel(); i++) {
            try {
                tests[i] = (PerfTestBase<?>) testClass.getConstructor(options.getClass()).newInstance(options);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            globalSetup(tests[0], options.isSync());

            boolean startedPlayback = false;

            try {
                setup(tests, options.isSync());
                setupStatus.cancel();

                if (options.getTestProxies() != null && !options.getTestProxies().isEmpty()) {
                    Timer recordStatus = printStatus("=== Record and Start Playback ===", () -> ".", false, false);

                    postSetup(tests, options.isSync());

                    startedPlayback = true;
                    recordStatus.cancel();
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
                        Timer playbackStatus = printStatus("=== Stop Playback ===", () -> ".", false, false);
                        stopPlayback(tests, options.isSync());
                        playbackStatus.cancel();
                    }
                } finally {
                    if (!options.isNoCleanup()) {
                        cleanupStatus = printStatus("=== Cleanup ===", () -> ".", false, false);

                        cleanup(tests, options.isSync());
                    }
                }
            }
        } finally {
            if (!options.isNoCleanup()) {
                if (cleanupStatus == null) {
                    cleanupStatus = printStatus("=== Cleanup ===", () -> ".", false, false);
                }

                globalCleanup(tests[0], options.isSync());
            }
        }

        if (cleanupStatus != null) {
            cleanupStatus.cancel();
        }
    }

    private static void globalSetup(PerfTestBase<?> test, boolean isSync) {
        if (isSync) {
            test.globalSetup();
        } else {
            test.globalSetupAsync().block();
        }
    }

    private static void setup(PerfTestBase<?>[] tests, boolean isSync) {
        if (isSync) {
            Arrays.stream(tests).forEach(PerfTestBase::setup);
        } else {
            Flux.just(tests).flatMap(PerfTestBase::setupAsync).blockLast();
        }
    }

    private static void postSetup(PerfTestBase<?>[] tests, boolean isSync) {
        if (isSync) {
            Arrays.stream(tests).forEach(PerfTestBase::postSetup);
        } else {
            Flux.just(tests).flatMap(PerfTestBase::postSetupAsync).blockLast();
        }
    }

    private static void stopPlayback(PerfTestBase<?>[] tests, boolean isSync) {
        if (isSync) {
            for (PerfTestBase<?> test : tests) {
                if (test instanceof ApiPerfTestBase) {
                    ((ApiPerfTestBase<?>) test).stopPlayback();
                } else {
                    throw new IllegalStateException("Test Proxy not supported.");
                }
            }
        } else {
            Flux.just(tests).flatMap(perfTestBase -> {
                if (perfTestBase instanceof ApiPerfTestBase) {
                    return ((ApiPerfTestBase<?>) perfTestBase).stopPlaybackAsync();
                } else {
                    return Mono.error(new IllegalStateException("Test Proxy not supported."));
                }
            }).blockLast();
        }
    }

    private static void cleanup(PerfTestBase<?>[] tests, boolean isSync) {
        if (isSync) {
            Arrays.stream(tests).forEach(PerfTestBase::cleanup);
        } else {
            Flux.just(tests).flatMap(PerfTestBase::cleanupAsync).blockLast();
        }
    }

    private static void globalCleanup(PerfTestBase<?> test, boolean isSync) {
        if (isSync) {
            test.globalCleanup();
        } else {
            test.globalCleanupAsync().block();
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
     * @throws RuntimeException if the execution fails.
     * @throws IllegalStateException if zero operations completed of the performance test.
     */
    public static void runTests(PerfTestBase<?>[] tests, boolean sync, int parallel, int durationSeconds, String title) {

        long endNanoTime = System.nanoTime() + ((long) durationSeconds * 1000000000);

        long[] lastCompleted = new long[]{0};
        Timer progressStatus = printStatus(
            "=== " + title + " ===" + System.lineSeparator() + "Current\t\tTotal\t\tAverage", () -> {
                long totalCompleted = getCompletedOperations(tests);
                long currentCompleted = totalCompleted - lastCompleted[0];
                double averageCompleted = getOperationsPerSecond(tests);

                lastCompleted[0] = totalCompleted;
                return String.format("%d\t\t%d\t\t%.2f", currentCompleted, totalCompleted, averageCompleted);
            }, true, true);

        try {
            if (sync) {
                ForkJoinPool forkJoinPool = new ForkJoinPool(parallel);
                List<Callable<Integer>> operations = new ArrayList<>(parallel);
                for (PerfTestBase<?> test : tests) {
                    operations.add(() -> {
                        test.runAll(endNanoTime);
                        return 1;
                    });
                }

                forkJoinPool.invokeAll(operations);

                forkJoinPool.awaitQuiescence(durationSeconds + 1, TimeUnit.SECONDS);
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
                    .parallel(parallel)
                    .runOn(Schedulers.parallel())
                    .flatMap(i -> tests[i].runAllAsync(endNanoTime))
                    .sequential()
                    .then()
                    .block();
            }
        } catch (Exception e) {
            System.err.println("Error occurred running tests: " + System.lineSeparator() + e);
            e.printStackTrace(System.err);
        } finally {
            progressStatus.cancel();
        }

        System.out.println("=== Results ===");

        long totalOperations = getCompletedOperations(tests);
        if (totalOperations == 0) {
            throw new IllegalStateException("Zero operations has been completed");
        }
        double operationsPerSecond = getOperationsPerSecond(tests);
        double secondsPerOperation = 1 / operationsPerSecond;
        double weightedAverageSeconds = totalOperations / operationsPerSecond;

        System.out.printf("Completed %,d operations in a weighted-average of %ss (%s ops/s, %s s/op)%n",
            totalOperations,
            NumberFormatter.Format(weightedAverageSeconds, 4),
            NumberFormatter.Format(operationsPerSecond, 4),
            NumberFormatter.Format(secondsPerOperation, 4));
        System.out.println();
    }

    private static Timer printStatus(String header, Supplier<Object> status, boolean newLine, boolean printFinalStatus) {
        System.out.println(header);

        boolean[] needsExtraNewline = new boolean[]{false};

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                printStatusHelper(status, newLine, needsExtraNewline);
            }

            @Override
            public boolean cancel() {
                if (printFinalStatus) {
                    printStatusHelper(status, newLine, needsExtraNewline);
                }

                if (needsExtraNewline[0]) {
                    System.out.println();
                }
                System.out.println();
                return super.cancel();
            }
        }, 1000, 1000);

        return timer;
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
