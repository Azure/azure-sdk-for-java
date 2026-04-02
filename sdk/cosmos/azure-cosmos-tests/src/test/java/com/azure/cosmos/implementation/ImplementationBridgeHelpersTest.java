// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.util.UtilBridgeInternal;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ImplementationBridgeHelpersTest {

    private static final Logger logger = LoggerFactory.getLogger(ImplementationBridgeHelpers.class);

    @Test(groups = { "unit" })
    public void accessorInitialization() {

        String helperClassSuffix = "Helper";

        Class<?>[] declaredClasses = ImplementationBridgeHelpers.class.getDeclaredClasses();

        try {
            for (Class<?> declaredClass : declaredClasses) {

                if (declaredClass.getSimpleName().endsWith(helperClassSuffix)) {

                    Field[] fields = declaredClass.getDeclaredFields();
                    boolean isAccessorReset = false;
                    boolean isClassLoadedReset = false;

                    for (Field field : fields) {

                        if (field.getName().contains("accessor")) {
                            field.setAccessible(true);
                            AtomicReference<?> value = (AtomicReference<?>) FieldUtils.readStaticField(field);
                            value.set(null);
                            isAccessorReset = true;
                        }

                        if (field.getName().contains("ClassLoaded")) {
                            field.setAccessible(true);
                            AtomicBoolean value = (AtomicBoolean) FieldUtils.readStaticField(field);
                            value.set(false);
                            isClassLoadedReset = true;
                        }
                    }
                    assertThat(isAccessorReset).isTrue();
                    assertThat(isClassLoadedReset).isTrue();
                }
            }

            BridgeInternal.initializeAllAccessors();
            ModelBridgeInternal.initializeAllAccessors();
            UtilBridgeInternal.initializeAllAccessors();

            declaredClasses = ImplementationBridgeHelpers.class.getDeclaredClasses();

            for (Class<?> declaredClass : declaredClasses) {

                if (declaredClass.getSimpleName().endsWith(helperClassSuffix)) {

                    logger.info("Helper class name : {}", declaredClass.getSimpleName());

                    Field[] fields = declaredClass.getDeclaredFields();
                    boolean isAccessorSet = false;
                    boolean isClassLoaded = false;

                    for (Field field : fields) {

                        if (field.getName().contains("accessor")) {
                            field.setAccessible(true);
                            AtomicReference<?> value = (AtomicReference<?>) FieldUtils.readStaticField(field);
                            logger.info("Accessor name : {}", field.getName());
                            assertThat(value.get()).isNotNull();
                            isAccessorSet = true;
                        }

                        if (field.getName().contains("ClassLoaded")) {
                            field.setAccessible(true);
                            AtomicBoolean value = (AtomicBoolean) FieldUtils.readStaticField(field);
                            logger.info("ClassLoaded name : {}", field.getName());
                            assertThat(value.get()).isTrue();
                            isClassLoaded = true;
                        }
                    }
                    assertThat(isAccessorSet).isTrue();
                    assertThat(isClassLoaded).isTrue();
                }
            }
        } catch (IllegalAccessException e) {
            fail("Failed with IllegalAccessException : ", e.getMessage());
        }
    }

    /**
     * Regression test for <a href="https://github.com/Azure/azure-sdk-for-java/issues/48622">#48622</a>
     * and <a href="https://github.com/Azure/azure-sdk-for-java/issues/48585">#48585</a>.
     * <p>
     * Forks a fresh JVM that concurrently triggers {@code <clinit>} of different Cosmos classes
     * from 6 threads. In a fresh JVM, {@code <clinit>} runs for the first time — the only way
     * to exercise the real deadlock scenario. A 30-second timeout detects the hang.
     */
    @Test(groups = { "unit" })
    public void concurrentAccessorInitializationShouldNotDeadlock() throws Exception {

        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + java.io.File.separator + "bin" + java.io.File.separator + "java";
        String classpath = System.getProperty("java.class.path");

        List<String> command = new ArrayList<>();
        command.add(javaBin);

        // --add-opens is only supported on JDK 9+
        try {
            int majorVersion = Integer.parseInt(System.getProperty("java.specification.version").split("\\.")[0]);
            if (majorVersion >= 9) {
                command.add("--add-opens");
                command.add("java.base/java.lang=ALL-UNNAMED");
            }
        } catch (NumberFormatException e) {
            // JDK 8 returns "1.8" — first element is "1", which is < 9, so no --add-opens
        }

        command.add("-cp");
        command.add(classpath);
        command.add(ConcurrentClinitChildProcess.class.getName());

        int timeoutSeconds = 30;
        int runs = 3;

        for (int run = 1; run <= runs; run++) {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                    logger.info("[child-jvm-run-{}] {}", run, line);
                }
            }

            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                fail("Run " + run + ": Child JVM did not complete within " + timeoutSeconds
                    + " seconds — <clinit> deadlock detected");
            }

            int exitCode = process.exitValue();
            assertThat(exitCode)
                .as("Run " + run + ": Child JVM exited with non-zero code. Output:\n" + output)
                .isEqualTo(0);
        }
    }

    /**
     * Entry point for the forked child JVM. Concurrently triggers {@code <clinit>} of 6 different
     * Cosmos classes that are involved in the circular initialization chain reported in the issues.
     * Exits 0 on success, 1 on deadlock (timeout), 2 on unexpected error.
     */
    public static final class ConcurrentClinitChildProcess {
        public static void main(String[] args) {
            int timeoutSeconds = 20;
            int threadCount = 6;
            CyclicBarrier barrier = new CyclicBarrier(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            String[] classesToLoad = {
                "com.azure.cosmos.CosmosAsyncClient",
                "com.azure.cosmos.models.SqlParameter",
                "com.azure.cosmos.models.FeedResponse",
                "com.azure.cosmos.models.CosmosItemRequestOptions",
                "com.azure.cosmos.CosmosAsyncContainer",
                "com.azure.cosmos.util.CosmosPagedFluxDefaultImpl"
            };

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < classesToLoad.length; i++) {
                final String className = classesToLoad[i];
                final int idx = i;
                futures.add(executor.submit(() -> {
                    try {
                        barrier.await();
                        System.out.println("[Thread-" + idx + "] Loading " + className);
                        Class.forName(className);
                        System.out.println("[Thread-" + idx + "] Done.");
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to load " + className, e);
                    }
                }));
            }

            boolean deadlock = false;
            for (int i = 0; i < futures.size(); i++) {
                try {
                    futures.get(i).get(timeoutSeconds, TimeUnit.SECONDS);
                } catch (java.util.concurrent.TimeoutException e) {
                    System.err.println("DEADLOCK: Thread-" + i + " timed out after " + timeoutSeconds + "s");
                    deadlock = true;
                } catch (Exception e) {
                    Throwable root = e;
                    while (root.getCause() != null) root = root.getCause();
                    System.err.println("Thread-" + i + " error: " + root);
                }
            }

            executor.shutdownNow();
            System.exit(deadlock ? 1 : 0);
        }
    }
}
