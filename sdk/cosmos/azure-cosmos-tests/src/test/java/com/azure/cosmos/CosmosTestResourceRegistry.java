// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Tracks the databases and containers created by the test suite so that they can be deleted even
 * when the test that created them fails to do so.
 * <p>
 * Registration is done by the sanctioned factory methods on {@code TestSuiteBase}; deletion helpers
 * deregister. Whatever is still registered when the run ends is a leak, and
 * {@code CosmosTestResourceJanitor} deletes it and fails the run so it gets fixed at the source.
 */
public final class CosmosTestResourceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosTestResourceRegistry.class);
    private static final String ID_VALIDATION_PROPERTY = "COSMOS.TEST_RESOURCE_ID_VALIDATION_ENABLED";

    // LinkedHashMap keeps creation order, which makes the leak report read chronologically.
    private static final Map<String, TrackedResource> TRACKED_RESOURCES = new LinkedHashMap<>();
    private static final ThreadLocal<String> CURRENT_TEST = new ThreadLocal<>();

    private CosmosTestResourceRegistry() {
    }

    /**
     * Records the test currently executing on this thread so leaked resources can be attributed back
     * to the test that created them.
     *
     * @param testName fully qualified test name, or null to clear.
     */
    public static void setCurrentTest(String testName) {
        if (testName == null) {
            CURRENT_TEST.remove();
        } else {
            CURRENT_TEST.set(testName);
        }
    }

    public static void registerDatabase(String databaseId) {
        if (databaseId == null) {
            return;
        }

        requireCleanableId(databaseId);
        synchronized (TRACKED_RESOURCES) {
            // putIfAbsent, not put: re-registration (createDatabaseIfNotExists on an existing database)
            // must not reattribute the resource to a later test. A genuine delete-then-recreate still
            // records the new owner, because unregisterDatabase removes the entry first.
            TRACKED_RESOURCES.putIfAbsent(key(databaseId, null), new TrackedResource(databaseId, null, owner()));
        }
    }

    /**
     * Fails the test when a database id cannot be attributed to this run.
     * <p>
     * The in-process janitor can clean up any database it was told about, but the pipeline post step
     * finds databases by <em>name</em>. A database whose id does not carry the run
     * id is therefore invisible to them, and if the JVM is killed - a cancelled or timed out job, which
     * is exactly when cleanup matters most - it leaks permanently on a shared account.
     * <p>
     * This check is the backstop for the static ratchet in {@code TestResourceHygieneTest}: the ratchet
     * counts creation call sites per file and so cannot see an id that is built at runtime, swapped
     * inside a file that already has an allowance, or produced through an API it does not know about.
     * Checking the id itself catches all of those.
     *
     * @param databaseId the id to validate.
     */
    private static void requireCleanableId(String databaseId) {
        if (isValidationDisabled() || CosmosDatabaseForTest.isTestDatabaseId(databaseId)) {
            return;
        }

        throw new AssertionError(String.format(
            "Test database id '%s' created by %s does not follow the required naming convention, so CI"
                + " cleanup cannot attribute it to this run and it would leak permanently on the shared"
                + " test accounts. Create databases with TestSuiteBase.createTestDatabase(client, label)"
                + " or name them with CosmosDatabaseForTest.generateId(label) - see sdk/cosmos/AGENTS.md.",
            databaseId,
            owner()));
    }

    private static boolean isValidationDisabled() {
        return "false".equalsIgnoreCase(System.getProperty(ID_VALIDATION_PROPERTY));
    }

    public static void unregisterDatabase(String databaseId) {
        if (databaseId == null) {
            return;
        }

        synchronized (TRACKED_RESOURCES) {
            TRACKED_RESOURCES.remove(key(databaseId, null));
            // Deleting a database deletes its containers, so drop those entries too.
            TRACKED_RESOURCES.values().removeIf(resource -> databaseId.equals(resource.databaseId));
        }
    }

    public static void registerContainer(String databaseId, String containerId) {
        if (databaseId == null || containerId == null) {
            return;
        }

        // Containers are reclaimed transitively - deleting a database removes them - so the requirement
        // is on the parent database's id, not the container's.
        requireCleanableId(databaseId);

        synchronized (TRACKED_RESOURCES) {
            // putIfAbsent for the same reason as registerDatabase - see the comment there.
            TRACKED_RESOURCES.putIfAbsent(
                key(databaseId, containerId),
                new TrackedResource(databaseId, containerId, owner()));
        }
    }

    public static void unregisterContainer(String databaseId, String containerId) {
        if (databaseId == null || containerId == null) {
            return;
        }

        synchronized (TRACKED_RESOURCES) {
            TRACKED_RESOURCES.remove(key(databaseId, containerId));
        }
    }

    /**
     * @return a snapshot of everything still registered, i.e. everything that has leaked so far.
     */
    public static List<TrackedResource> leakedSnapshot() {
        synchronized (TRACKED_RESOURCES) {
            return Collections.unmodifiableList(new ArrayList<>(TRACKED_RESOURCES.values()));
        }
    }

    public static void clear() {
        synchronized (TRACKED_RESOURCES) {
            TRACKED_RESOURCES.clear();
        }
    }

    private static String owner() {
        String currentTest = CURRENT_TEST.get();
        if (currentTest != null) {
            return currentTest;
        }

        // Outside an invoked test method (for example @BeforeSuite) walk the stack for the first frame
        // that is not test infrastructure. Skipping the shared helpers matters: naming
        // TestSuiteBase.createDatabaseInternal tells nobody which test leaked, which is the whole point
        // of the report. Fall back to the first infrastructure frame only if nothing better exists.
        String infrastructureFrame = null;
        for (StackTraceElement frame : Thread.currentThread().getStackTrace()) {
            String className = frame.getClassName();
            if (!className.startsWith("com.azure.cosmos")) {
                continue;
            }

            if (isInfrastructure(className)) {
                if (infrastructureFrame == null) {
                    infrastructureFrame = className + "." + frame.getMethodName();
                }
                continue;
            }

            return className + "." + frame.getMethodName();
        }

        return infrastructureFrame != null ? infrastructureFrame + " (no test frame on stack)" : "<unknown>";
    }

    private static boolean isInfrastructure(String className) {
        return className.equals(CosmosTestResourceRegistry.class.getName())
            || className.equals(CosmosDatabaseForTest.class.getName())
            || className.equals(CosmosTestResourceJanitor.class.getName())
            || className.equals("com.azure.cosmos.rx.TestSuiteBase");
    }

    private static String key(String databaseId, String containerId) {
        return databaseId + "/" + (containerId == null ? "" : containerId);
    }

    /**
     * A database or container created by the test suite, together with the test that created it.
     */
    public static final class TrackedResource {
        private final String databaseId;
        private final String containerId;
        private final String createdBy;

        private TrackedResource(String databaseId, String containerId, String createdBy) {
            this.databaseId = databaseId;
            this.containerId = containerId;
            this.createdBy = createdBy;
        }

        public String getDatabaseId() {
            return this.databaseId;
        }

        /**
         * @return the container id, or null when this entry tracks a database.
         */
        public String getContainerId() {
            return this.containerId;
        }

        public String getCreatedBy() {
            return this.createdBy;
        }

        public boolean isDatabase() {
            return this.containerId == null;
        }

        @Override
        public String toString() {
            return (isDatabase() ? "database " + databaseId : "container " + databaseId + "/" + containerId)
                + " (created by " + createdBy + ")";
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TrackedResource)) {
                return false;
            }

            TrackedResource that = (TrackedResource) other;
            return Objects.equals(databaseId, that.databaseId)
                && Objects.equals(containerId, that.containerId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(databaseId, containerId);
        }
    }

    static {
        LOGGER.info("Cosmos test resource registry active for run id {}", CosmosTestRunId.get());
    }
}
