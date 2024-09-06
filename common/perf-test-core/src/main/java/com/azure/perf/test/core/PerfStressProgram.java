// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.beust.jcommander.JCommander;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
        printOptions(options);

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
            tests[0].globalSetupAsync().block();

            boolean startedPlayback = false;

            try {
                Flux.just(tests).flatMap(PerfTestBase::setupAsync).blockLast();
                setupStatus.cancel();

                if (options.getTestProxies() != null && !options.getTestProxies().isEmpty()) {
                    Timer recordStatus = printStatus("=== Record and Start Playback ===", () -> ".", false, false);

                    int parallel = tests.length;
                    Flux.range(0, parallel)
                        .parallel(parallel)
                        .runOn(Schedulers.parallel())
                        .flatMap(i -> tests[i].postSetupAsync())
                        .sequential()
                        .then()
                        .block();

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
                        Flux.just(tests).flatMap(perfTestBase -> {
                            if (perfTestBase instanceof ApiPerfTestBase) {
                                return ((ApiPerfTestBase<?>) perfTestBase).stopPlaybackAsync();
                            } else {
                                return Mono.error(new IllegalStateException("Test Proxy not supported."));
                            }
                        }).blockLast();
                        playbackStatus.cancel();
                    }
                } finally {
                    if (!options.isNoCleanup()) {
                        cleanupStatus = printStatus("=== Cleanup ===", () -> ".", false, false);

                        Flux.just(tests).flatMap(PerfTestBase::cleanupAsync).blockLast();
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
            cleanupStatus.cancel();
        }
    }

    private static void printOptions(PerfStressOptions options) {
        try {
            Map<String, Object> parameters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (Method method : options.getClass().getMethods()) {
                String methodName = method.getName();
                if ((!methodName.startsWith("get") && !methodName.startsWith("is"))
                    || methodName.equals("getClass")) {
                    continue;
                }

                String parameterName = convertMethodName(methodName);
                parameters.put(parameterName, method.invoke(options));
            }

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append('{').append(System.lineSeparator());
            AtomicBoolean first = new AtomicBoolean(true);
            parameters.forEach((key, value) -> writeKeyValue(key, value, stringBuilder, first));
            stringBuilder.append(System.lineSeparator()).append('}');
            System.out.println(stringBuilder);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static String convertMethodName(String methodName) {
        return methodName.startsWith("is")
            ? Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3)
            : Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
    }

    private static void writeKeyValue(String key, Object value, StringBuilder sb, AtomicBoolean first) {
        if (!first.get()) {
            sb.append(',').append(System.lineSeparator());
        }

        sb.append("  ").append(key);
        if (value instanceof String) {
            sb.append(": \"").append(value).append('"');
        } else {
            sb.append(": ").append(value);
        }

        first.set(false);
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
