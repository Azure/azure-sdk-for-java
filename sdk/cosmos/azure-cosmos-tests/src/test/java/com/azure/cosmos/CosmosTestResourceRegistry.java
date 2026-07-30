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

        synchronized (TRACKED_RESOURCES) {
            TRACKED_RESOURCES.put(key(databaseId, null), new TrackedResource(databaseId, null, owner()));
        }
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

        synchronized (TRACKED_RESOURCES) {
            TRACKED_RESOURCES.put(
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

        // Outside an invoked test method (for example @BeforeSuite) walk the stack for the first
        // non-infrastructure frame so the leak report still names something actionable.
        for (StackTraceElement frame : Thread.currentThread().getStackTrace()) {
            String className = frame.getClassName();
            if (className.startsWith("com.azure.cosmos")
                && !className.equals(CosmosTestResourceRegistry.class.getName())
                && !className.equals(CosmosDatabaseForTest.class.getName())) {
                return className + "." + frame.getMethodName();
            }
        }

        return "<unknown>";
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
