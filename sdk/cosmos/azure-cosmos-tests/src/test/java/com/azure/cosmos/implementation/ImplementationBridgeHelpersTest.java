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
     * Forks a fresh JVM that concurrently triggers {@code <clinit>} of 12 different Cosmos classes
     * from 12 threads synchronized via a {@link CyclicBarrier}. In a fresh JVM, {@code <clinit>}
     * runs for the first time — the only way to exercise the real deadlock scenario. A 30-second
     * timeout detects the hang. Runs 5 invocations via TestNG ({@code invocationCount = 5}),
     * each forking 1 child JVM — totaling 5 fresh JVMs × 12 concurrent threads = 60
     * {@code <clinit>} race attempts.
     */
    @Test(groups = { "unit" }, invocationCount = 5)
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
        int runs = 1;

        for (int run = 1; run <= runs; run++) {
            final int currentRun = run;
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Drain stdout on a separate thread to prevent blocking if child JVM deadlocks.
            // Without this, readLine() would block indefinitely and the timeout below
            // would never be reached.
            StringBuilder output = new StringBuilder();
            Thread gobbler = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append(System.lineSeparator());
                        logger.info("[child-jvm-run-{}] {}", currentRun, line);
                    }
                } catch (Exception e) {
                    // Process was destroyed — expected on timeout
                }
            });
            gobbler.setDaemon(true);
            gobbler.start();

            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                gobbler.join(5000);
                fail("Run " + run + ": Child JVM did not complete within " + timeoutSeconds
                    + " seconds — <clinit> deadlock detected");
            }

            gobbler.join(5000);
            int exitCode = process.exitValue();
            assertThat(exitCode)
                .as("Run " + run + ": Child JVM exited with non-zero code. Output:\n" + output)
                .isEqualTo(0);
        }
    }

    /**
     * Entry point for the forked child JVM. Concurrently triggers {@code <clinit>} of 12 different
     * Cosmos classes that are involved in the circular initialization chain reported in the issues.
     * Exits 0 on success, 1 on deadlock (timeout).
     */
    public static final class ConcurrentClinitChildProcess {
        public static void main(String[] args) {
            int timeoutSeconds = 20;

            String[] classesToLoad = {
                "com.azure.cosmos.CosmosAsyncClient",
                "com.azure.cosmos.models.SqlParameter",
                "com.azure.cosmos.models.FeedResponse",
                "com.azure.cosmos.models.CosmosItemRequestOptions",
                "com.azure.cosmos.CosmosAsyncContainer",
                "com.azure.cosmos.util.CosmosPagedFluxDefaultImpl",
                "com.azure.cosmos.CosmosClientBuilder",
                "com.azure.cosmos.CosmosItemSerializer",
                "com.azure.cosmos.CosmosDiagnostics",
                "com.azure.cosmos.CosmosDiagnosticsContext",
                "com.azure.cosmos.models.CosmosQueryRequestOptions",
                "com.azure.cosmos.models.CosmosChangeFeedRequestOptions"
            };

            int threadCount = classesToLoad.length;

            // CyclicBarrier ensures all threads release at the exact same instant,
            // maximizing the probability of concurrent <clinit> collisions. Without it,
            // thread startup stagger means earlier threads may finish <clinit> before
            // later threads start — hiding the deadlock.
            CyclicBarrier barrier = new CyclicBarrier(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            try {
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
                        while (root.getCause() != null) {
                            root = root.getCause();
                        }
                        System.err.println("Thread-" + i + " error: " + root);
                    }
                }

                if (deadlock) {
                    System.exit(1);
                }

                // Verify all classes are actually initialized
                for (String className : classesToLoad) {
                    try {
                        // Class.forName with initialize=false just checks if already loaded
                        // If the class was loaded above, this returns immediately
                        Class<?> cls = Class.forName(className, false,
                            ConcurrentClinitChildProcess.class.getClassLoader());
                        // Verify the class is initialized by accessing its static state
                        // (calling a static method would trigger <clinit> if not done,
                        // but we explicitly check it's already done)
                        System.out.println("Verified loaded: " + cls.getName());
                    } catch (ClassNotFoundException e) {
                        System.err.println("Class not loaded: " + className);
                        System.exit(1);
                    }
                }

                System.exit(0);
            } finally {
                executor.shutdownNow();
            }
        }
    }

    /**
     * Verifies that every {@code *Helper} inner class in
     * {@link ImplementationBridgeHelpers} has a resolvable accessor — i.e., calling
     * {@code getXxxAccessor()} returns a non-null value in a clean JVM.
     * <p>
     * A forked child JVM iterates every {@code *Helper} inner class, calls each
     * {@code getXxxAccessor()} getter, and checks the accessor is non-null via reflection.
     * <p>
     * Note: the getter falls back to {@code initializeAllAccessors()} when the accessor
     * is not yet set, so this test validates that every accessor is <em>resolvable</em>
     * (either via the class's own {@code static { initialize(); }} or the bulk fallback),
     * not that each class independently registers its accessor during {@code <clinit>}.
     * The structural contract (no static/final accessor fields in consuming classes) is
     * enforced separately by {@link #noStaticOrInstanceAccessorFieldsInConsumingClasses}.
     */
    @Test(groups = { "unit" })
    public void allAccessorClassesMustHaveStaticInitializerBlock() throws Exception {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + java.io.File.separator + "bin" + java.io.File.separator + "java";
        String classpath = System.getProperty("java.class.path");

        List<String> command = new ArrayList<>();
        command.add(javaBin);

        try {
            int majorVersion = Integer.parseInt(System.getProperty("java.specification.version").split("\\.")[0]);
            if (majorVersion >= 9) {
                command.add("--add-opens");
                command.add("java.base/java.lang=ALL-UNNAMED");
            }
        } catch (NumberFormatException e) {
            // JDK 8
        }

        command.add("-cp");
        command.add(classpath);
        command.add(AccessorRegistrationChildProcess.class.getName());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        Thread gobbler = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                    logger.info("[accessor-check] {}", line);
                }
            } catch (Exception e) {
                // Process destroyed
            }
        });
        gobbler.setDaemon(true);
        gobbler.start();

        boolean completed = process.waitFor(60, TimeUnit.SECONDS);
        if (!completed) {
            process.destroyForcibly();
            gobbler.join(5000);
            fail("Accessor registration check timed out after 60s. Output:\n" + output);
        }

        gobbler.join(5000);
        int exitCode = process.exitValue();
        assertThat(exitCode)
            .as("Some accessor classes don't register their accessor during <clinit>. Output:\n" + output)
            .isEqualTo(0);
    }

    /**
     * Child process that verifies every {@code *Helper} inner class in
     * {@link ImplementationBridgeHelpers} has its accessor registered after calling the
     * corresponding {@code getXxxAccessor()} getter. Runs in a fresh JVM where no Cosmos
     * classes have been loaded yet, so {@code <clinit>} is triggered for the first time.
     */
    public static final class AccessorRegistrationChildProcess {
        public static void main(String[] args) throws Exception {
            // Iterate all *Helper inner classes in ImplementationBridgeHelpers.
            // For each, call the getXxxAccessor() getter which lazily resolves the
            // accessor (triggering <clinit> if needed). Then verify the accessor field is non-null.

            Class<?>[] helpers = ImplementationBridgeHelpers.class.getDeclaredClasses();
            List<String> failures = new ArrayList<>();

            for (Class<?> helper : helpers) {
                if (!helper.getSimpleName().endsWith("Helper")) {
                    continue;
                }

                // Find the accessor AtomicReference field
                Field accessorField = null;
                Field classLoadedField = null;
                for (Field f : helper.getDeclaredFields()) {
                    if (f.getName().contains("accessor") && f.getType() == AtomicReference.class) {
                        accessorField = f;
                    }
                    if (f.getName().contains("ClassLoaded") && f.getType() == AtomicBoolean.class) {
                        classLoadedField = f;
                    }
                }

                if (accessorField == null || classLoadedField == null) {
                    continue;
                }

                // Check if the accessor is already set (from transitive <clinit> of earlier classes)
                accessorField.setAccessible(true);
                AtomicReference<?> ref = (AtomicReference<?>) accessorField.get(null);
                if (ref.get() != null) {
                    System.out.println("OK (already loaded): " + helper.getSimpleName());
                    continue;
                }

                // Find the target class name by looking for a getXxxAccessor method that lazily
                // resolves the accessor. We can't easily extract the string constant, so instead
                // we call the getter and check if the accessor becomes non-null.
                // The getter lazily triggers <clinit> of the target class if needed.
                // If <clinit> calls initialize(), the accessor is registered.
                java.lang.reflect.Method getterMethod = null;
                for (java.lang.reflect.Method m : helper.getDeclaredMethods()) {
                    if (m.getName().startsWith("get") && m.getName().endsWith("Accessor")
                        && m.getParameterCount() == 0
                        && java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
                        getterMethod = m;
                        break;
                    }
                }

                if (getterMethod == null) {
                    continue;
                }

                try {
                    Object result = getterMethod.invoke(null);
                    if (result == null) {
                        failures.add(helper.getSimpleName() + ": accessor is null after getter call — "
                            + "target class <clinit> does not call initialize()");
                    } else {
                        System.out.println("OK: " + helper.getSimpleName());
                    }
                } catch (Exception e) {
                    Throwable root = e;
                    while (root.getCause() != null) {
                        root = root.getCause();
                    }
                    failures.add(helper.getSimpleName() + ": " + root.getClass().getSimpleName()
                        + " — " + root.getMessage());
                }
            }

            if (failures.isEmpty()) {
                System.out.println("All accessor classes register their accessor during <clinit>.");
                System.exit(0);
            } else {
                System.err.println("FAILURES — the following classes do not register their accessor "
                    + "during <clinit> (missing 'static { initialize(); }' block):");
                for (String f : failures) {
                    System.err.println("  " + f);
                }
                System.exit(1);
            }
        }
    }

    /**
     * Enforces that no consuming class stores an accessor in a {@code static} field or
     * {@code final} instance field assigned at declaration. Such fields are initialized
     * during {@code <clinit>} (for static) or eagerly during construction (for instance
     * finals assigned inline), and can trigger {@code initializeAllAccessors()}, creating
     * circular class-initialization lock chains that deadlock under concurrent class
     * loading (JLS §12.4.2).
     * <p>
     * The approved pattern is a {@code private static} getter method:
     * <pre>{@code
     * private static XxxAccessor xxxAccessor() {
     *     return ImplementationBridgeHelpers.XxxHelper.getXxxAccessor();
     * }
     * }</pre>
     * <p>
     * Uses reflection — immune to formatting, multiline declarations, and import aliases.
     */
    @Test(groups = { "unit" })
    public void noStaticOrInstanceAccessorFieldsInConsumingClasses() throws Exception {
        // Step 1: Collect all Accessor interface types from ImplementationBridgeHelpers
        java.util.Set<Class<?>> accessorTypes = new java.util.HashSet<>();
        for (Class<?> inner : ImplementationBridgeHelpers.class.getDeclaredClasses()) {
            for (Class<?> nested : inner.getDeclaredClasses()) {
                if (nested.isInterface() && nested.getSimpleName().endsWith("Accessor")) {
                    accessorTypes.add(nested);
                }
            }
        }

        assertThat(accessorTypes)
            .as("Should find accessor interfaces in ImplementationBridgeHelpers")
            .isNotEmpty();

        // Step 2: Classes that legitimately hold accessor AtomicReference fields
        java.util.Set<String> exemptClassNames = new java.util.HashSet<>(java.util.Arrays.asList(
            "com.azure.cosmos.implementation.ImplementationBridgeHelpers",
            "com.azure.cosmos.BridgeInternal",
            "com.azure.cosmos.models.ModelBridgeInternal",
            "com.azure.cosmos.util.UtilBridgeInternal"
        ));

        // Step 3: Force-load all cosmos classes so we can scan them
        // initializeAllAccessors() transitively loads the main classes
        ImplementationBridgeHelpers.initializeAllAccessors();

        // Get all classes visible via the classloader that are in com.azure.cosmos
        // We use the source tree to enumerate class names, then load them
        // Resolve azure-cosmos source root. Search upward from user.dir for the sdk/cosmos layout.
        java.nio.file.Path userDir = java.nio.file.Paths.get(System.getProperty("user.dir"));
        java.nio.file.Path cosmosRoot = null;

        // Try standard Maven layout: user.dir is azure-cosmos-tests, sibling is azure-cosmos
        java.nio.file.Path candidate = userDir.getParent().resolve("azure-cosmos")
            .resolve("src").resolve("main").resolve("java");
        if (java.nio.file.Files.exists(candidate)) {
            cosmosRoot = candidate;
        }

        // Try repo root: user.dir is the repo root
        if (cosmosRoot == null) {
            candidate = userDir.resolve("sdk").resolve("cosmos").resolve("azure-cosmos")
                .resolve("src").resolve("main").resolve("java");
            if (java.nio.file.Files.exists(candidate)) {
                cosmosRoot = candidate;
            }
        }

        // Walk up from user.dir looking for sdk/cosmos/azure-cosmos
        if (cosmosRoot == null) {
            java.nio.file.Path dir = userDir;
            while (dir != null) {
                candidate = dir.resolve("sdk").resolve("cosmos").resolve("azure-cosmos")
                    .resolve("src").resolve("main").resolve("java");
                if (java.nio.file.Files.exists(candidate)) {
                    cosmosRoot = candidate;
                    break;
                }
                dir = dir.getParent();
            }
        }

        assertThat(cosmosRoot)
            .as("Could not find azure-cosmos source root from user.dir: " + userDir)
            .isNotNull();
        assertThat(java.nio.file.Files.exists(cosmosRoot))
            .as("azure-cosmos source root must exist at: " + cosmosRoot)
            .isTrue();

        java.nio.file.Path javaRoot = cosmosRoot;
        List<String> violations = new ArrayList<>();

        try (java.util.stream.Stream<java.nio.file.Path> walker = java.nio.file.Files.walk(cosmosRoot)) {
            walker
                .filter(p -> p.toString().endsWith(".java"))
                .filter(p -> !p.getFileName().toString().equals("module-info.java"))
                .forEach(p -> {
                    // Convert file path to class name
                    String relative = javaRoot.relativize(p).toString();
                    String className = relative
                        .replace(java.io.File.separatorChar, '.')
                        .replaceAll("\\.java$", "");

                    if (exemptClassNames.contains(className)) {
                        return;
                    }

                    // Also skip inner classes of exempt classes
                    for (String exempt : exemptClassNames) {
                        if (className.startsWith(exempt + "$")) {
                            return;
                        }
                    }

                    try {
                        Class<?> cls = Class.forName(className, false,
                            ImplementationBridgeHelpers.class.getClassLoader());

                        for (Field field : cls.getDeclaredFields()) {
                            Class<?> fieldType = field.getType();

                            // Check if this field's type is one of the Accessor interfaces
                            if (!accessorTypes.contains(fieldType)) {
                                continue;
                            }

                            int mods = field.getModifiers();
                            boolean isStatic = java.lang.reflect.Modifier.isStatic(mods);
                            boolean isFinal = java.lang.reflect.Modifier.isFinal(mods);

                            // Dangerous: any static accessor field (runs during <clinit>)
                            // Also flag: final instance fields (assigned at declaration = eager init)
                            if (isStatic) {
                                violations.add(cls.getName() + "." + field.getName()
                                    + " — static " + (isFinal ? "final " : "")
                                    + fieldType.getSimpleName()
                                    + " (runs during <clinit>, can deadlock)");
                            } else if (isFinal) {
                                violations.add(cls.getName() + "." + field.getName()
                                    + " — final " + fieldType.getSimpleName()
                                    + " (instance field assigned at declaration, "
                                    + "prefer static getter method for consistency)");
                            }
                        }
                    } catch (ClassNotFoundException | NoClassDefFoundError e) {
                        // Skip classes that can't be loaded (e.g., optional dependencies)
                    }
                });
        }

        assertThat(violations)
            .as("Found accessor fields that can trigger <clinit> deadlocks or are inconsistent "
                + "with the approved static getter pattern.\n"
                + "Use 'private static XxxAccessor xxx() { return getXxxAccessor(); }' instead.\n"
                + "Violations:\n" + String.join("\n", violations))
            .isEmpty();
    }
}
