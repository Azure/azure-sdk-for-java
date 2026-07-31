// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the janitor's delete-ordering and leak-classification decisions.
 * <p>
 * These are the rules that decide what gets deleted, what gets reported as a leak, and how many round
 * trips cleanup spends against an account that is already throttling at the end of a long run. They are
 * easy to break in a way that no other test notices - a leak silently stops being reported, or the
 * container probe storm silently comes back - so they are pinned here against a fake deleter.
 */
public class CosmosTestResourceJanitorTest {

    // Real generated ids: registration now rejects ids that CI cleanup could not attribute to a run,
    // so the fixtures have to be as valid as production's.
    private static final String DB_ONE = CosmosDatabaseForTest.generateId("janitorOne");
    private static final String DB_OTHER = CosmosDatabaseForTest.generateId("janitorOther");
    private static final String DB_UNTRACKED = CosmosDatabaseForTest.generateId("janitorUntracked");

    @Test(groups = {"unit"})
    public void deletedResourcesAreReportedAsLeaks() {
        FakeDeleter deleter = new FakeDeleter();
        CosmosTestResourceRegistry.TrackedResource database = database(DB_ONE);
        deleter.outcome(database, CosmosTestResourceJanitor.DeleteOutcome.DELETED);

        List<String> leaks = CosmosTestResourceJanitor.deleteTrackedResources(
            Arrays.asList(database), deleter);

        assertThat(leaks).hasSize(1);
        assertThat(leaks.get(0)).contains(DB_ONE).contains("deleted");
    }

    @Test(groups = {"unit"})
    public void alreadyGoneResourcesAreNotReportedAsLeaks() {
        // The test deleted the resource but did not deregister. Nothing leaked, so nothing to report -
        // otherwise every healthy run would fail.
        FakeDeleter deleter = new FakeDeleter();
        CosmosTestResourceRegistry.TrackedResource database = database(DB_ONE);
        deleter.outcome(database, CosmosTestResourceJanitor.DeleteOutcome.ALREADY_GONE);

        assertThat(CosmosTestResourceJanitor.deleteTrackedResources(Arrays.asList(database), deleter))
            .isEmpty();
    }

    @Test(groups = {"unit"})
    public void failedDeletesAreReportedAsStillPresent() {
        FakeDeleter deleter = new FakeDeleter();
        CosmosTestResourceRegistry.TrackedResource database = database(DB_ONE);
        deleter.outcome(database, CosmosTestResourceJanitor.DeleteOutcome.DELETE_FAILED);

        List<String> leaks = CosmosTestResourceJanitor.deleteTrackedResources(
            Arrays.asList(database), deleter);

        // Must not claim it was deleted - it is still on the account.
        assertThat(leaks).hasSize(1);
        assertThat(leaks.get(0)).contains("STILL PRESENT");
    }

    @Test(groups = {"unit"})
    public void containersOfADeletedDatabaseAreNotProbed() {
        FakeDeleter deleter = new FakeDeleter();
        CosmosTestResourceRegistry.TrackedResource database = database(DB_ONE);
        CosmosTestResourceRegistry.TrackedResource container = container(DB_ONE, "c1");
        deleter.outcome(database, CosmosTestResourceJanitor.DeleteOutcome.DELETED);

        List<String> leaks = CosmosTestResourceJanitor.deleteTrackedResources(
            Arrays.asList(container, database), deleter);

        // Deleting the database removed the container; probing it would be a wasted round trip. Note the
        // container is listed FIRST here - the skip must not depend on registration order.
        assertThat(deleter.attempted).containsExactly(database);
        // The skipped container must not be reported - a skipped resource is not a separate leak.
        assertThat(leaks).hasSize(1);
    }

    @Test(groups = {"unit"})
    public void containersOfAnAlreadyGoneDatabaseAreNotProbed() {
        // This is the dominant real case - a test deleted its database without deregistering. If it did
        // not populate the skip set, a long suite would end by 404-probing every container it ever made.
        FakeDeleter deleter = new FakeDeleter();
        CosmosTestResourceRegistry.TrackedResource database = database(DB_ONE);
        deleter.outcome(database, CosmosTestResourceJanitor.DeleteOutcome.ALREADY_GONE);

        List<String> leaks = CosmosTestResourceJanitor.deleteTrackedResources(
            Arrays.asList(database, container(DB_ONE, "c1"), container(DB_ONE, "c2")), deleter);

        assertThat(deleter.attempted).containsExactly(database);
        assertThat(leaks).isEmpty();
    }

    @Test(groups = {"unit"})
    public void containersOfADatabaseThatCouldNotBeDeletedAreStillAttempted() {
        // The database is still there, so its containers are too and were never attempted.
        FakeDeleter deleter = new FakeDeleter();
        CosmosTestResourceRegistry.TrackedResource database = database(DB_ONE);
        CosmosTestResourceRegistry.TrackedResource container = container(DB_ONE, "c1");
        deleter.outcome(database, CosmosTestResourceJanitor.DeleteOutcome.DELETE_FAILED);
        deleter.outcome(container, CosmosTestResourceJanitor.DeleteOutcome.DELETED);

        List<String> leaks = CosmosTestResourceJanitor.deleteTrackedResources(
            Arrays.asList(database, container), deleter);

        assertThat(deleter.attempted).containsExactly(database, container);
        assertThat(leaks).hasSize(2);
    }

    @Test(groups = {"unit"})
    public void containersOfAnUnregisteredDatabaseAreDeletedIndividually() {
        FakeDeleter deleter = new FakeDeleter();
        CosmosTestResourceRegistry.TrackedResource container = container(DB_UNTRACKED, "c1");
        deleter.outcome(container, CosmosTestResourceJanitor.DeleteOutcome.DELETED);

        List<String> leaks = CosmosTestResourceJanitor.deleteTrackedResources(
            Arrays.asList(container), deleter);

        assertThat(deleter.attempted).containsExactly(container);
        assertThat(leaks).hasSize(1);
    }

    @Test(groups = {"unit"})
    public void databasesAreAlwaysDeletedBeforeContainers() {
        FakeDeleter deleter = new FakeDeleter();
        CosmosTestResourceRegistry.TrackedResource containerElsewhere = container(DB_OTHER, "c1");
        CosmosTestResourceRegistry.TrackedResource database = database(DB_ONE);
        deleter.outcome(containerElsewhere, CosmosTestResourceJanitor.DeleteOutcome.DELETED);
        deleter.outcome(database, CosmosTestResourceJanitor.DeleteOutcome.DELETED);

        CosmosTestResourceJanitor.deleteTrackedResources(
            Arrays.asList(containerElsewhere, database), deleter);

        assertThat(deleter.attempted).containsExactly(database, containerElsewhere);
    }

    @BeforeMethod(groups = {"unit"})
    @AfterMethod(groups = {"unit"})
    public void resetRegistry() {
        // The registry is JVM global. Reset on both sides: before, so helper call order cannot wipe a
        // resource registered earlier in the same test; after, so this class leaves no residue for
        // whatever test class the suite runs next.
        CosmosTestResourceRegistry.clear();
    }

    private static CosmosTestResourceRegistry.TrackedResource database(String databaseId) {
        CosmosTestResourceRegistry.registerDatabase(databaseId);
        return findTracked(databaseId, null);
    }

    private static CosmosTestResourceRegistry.TrackedResource container(String databaseId, String containerId) {
        CosmosTestResourceRegistry.registerContainer(databaseId, containerId);
        return findTracked(databaseId, containerId);
    }

    private static CosmosTestResourceRegistry.TrackedResource findTracked(String databaseId, String containerId) {
        for (CosmosTestResourceRegistry.TrackedResource resource : CosmosTestResourceRegistry.leakedSnapshot()) {
            if (databaseId.equals(resource.getDatabaseId())
                && (containerId == null ? resource.isDatabase() : containerId.equals(resource.getContainerId()))) {

                return resource;
            }
        }

        throw new AssertionError("resource was not registered");
    }

    /**
     * Stands in for the delete round trip, recording what was actually attempted so the tests can assert
     * on round trips avoided rather than only on the reported result.
     */
    private static final class FakeDeleter
        implements java.util.function.Function<
            CosmosTestResourceRegistry.TrackedResource, CosmosTestResourceJanitor.DeleteOutcome> {

        private final Map<CosmosTestResourceRegistry.TrackedResource,
            CosmosTestResourceJanitor.DeleteOutcome> outcomes = new LinkedHashMap<>();
        private final List<CosmosTestResourceRegistry.TrackedResource> attempted = new ArrayList<>();

        private void outcome(
            CosmosTestResourceRegistry.TrackedResource resource,
            CosmosTestResourceJanitor.DeleteOutcome outcome) {

            outcomes.put(resource, outcome);
        }

        @Override
        public CosmosTestResourceJanitor.DeleteOutcome apply(
            CosmosTestResourceRegistry.TrackedResource resource) {

            attempted.add(resource);
            CosmosTestResourceJanitor.DeleteOutcome outcome = outcomes.get(resource);
            if (outcome == null) {
                throw new AssertionError("unexpected delete attempt for " + resource);
            }

            return outcome;
        }
    }
}
