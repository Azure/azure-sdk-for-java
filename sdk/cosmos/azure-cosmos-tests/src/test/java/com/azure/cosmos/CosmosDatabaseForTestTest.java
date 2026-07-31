// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.util.UtilBridgeInternal;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the invariant that the whole cleanup design rests on: on the long lived shared accounts,
 * cleanup must delete this run's databases and nothing else. Deleting a database belonging to another,
 * still-running job would cause confusing cross-run failures, and deleting a hand created fixture would
 * be worse.
 */
public class CosmosDatabaseForTestTest {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    /**
     * A hand created database that matches the query prefix but does not parse as a generated id. This is
     * the fixture that only the production code protects - it reaches deleteMatching and survives solely
     * because ids that fail to parse are skipped.
     */
    private static final String PINNED_FIXTURE = "RxJava.SDKTest.SharedDatabase_pinnedFixture";

    /** A hand created database that does not even match the query prefix. */
    private static final String UNRELATED_FIXTURE = "permanentFixture";

    @Test(groups = {"unit"})
    public void generatedIdIsRecognizedAndCarriesTheRunId() {
        String id = CosmosDatabaseForTest.generateId("myFeature");

        assertThat(id).startsWith(CosmosDatabaseForTest.SHARED_DB_ID_PREFIX);
        assertThat(id).contains("_" + CosmosTestRunId.get() + "_");
        assertThat(CosmosDatabaseForTest.isTestDatabaseId(id)).isTrue();
        // Cosmos ids are capped at 255 characters and may not contain / \ # ?
        assertThat(id.length()).isLessThan(255);
        assertThat(id).doesNotContain("/").doesNotContain("\\").doesNotContain("#").doesNotContain("?");
    }

    @Test(groups = {"unit"})
    public void registeringAnUnattributableDatabaseIdFailsTheTest() {
        // The static ratchet cannot see an id built at runtime, or swapped inside a file that already
        // has a baseline allowance. This runtime check is what actually closes those gaps: an id that CI
        // cleanup could not find by name must never reach a shared account unnoticed.
        try {
            CosmosTestResourceRegistry.registerDatabase("myHardcodedLeakyDb");
            org.testng.Assert.fail("Expected an AssertionError for an unattributable database id");
        } catch (AssertionError expected) {
            assertThat(expected).hasMessageContaining("myHardcodedLeakyDb");
            assertThat(expected).hasMessageContaining("createTestDatabase");
        } finally {
            CosmosTestResourceRegistry.clear();
        }
    }

    @Test(groups = {"unit"})
    public void registeringAGeneratedDatabaseIdIsAccepted() {
        try {
            CosmosTestResourceRegistry.registerDatabase(CosmosDatabaseForTest.generateId("ok"));
            assertThat(CosmosTestResourceRegistry.leakedSnapshot()).hasSize(1);
        } finally {
            CosmosTestResourceRegistry.clear();
        }
    }

    @Test(groups = {"unit"})
    public void legacyIdsRemainRegisterableDuringRollout() {
        // Builds predating the run id still create three segment ids; those parse, so they must not trip
        // the new check while both formats are in flight.
        try {
            CosmosTestResourceRegistry.registerDatabase("RxJava.SDKTest.SharedDatabase_20240101T101010_abc");
            assertThat(CosmosTestResourceRegistry.leakedSnapshot()).hasSize(1);
        } finally {
            CosmosTestResourceRegistry.clear();
        }
    }

    @Test(groups = {"unit"})
    public void runIdNeverContainsTheIdDelimiter() {
        // parse() splits on "_". A run id containing one would give every generated id five segments,
        // parse() would return null everywhere, and run scoped cleanup would silently stop working.
        assertThat(CosmosTestRunId.get()).matches("[a-z0-9]{1,20}");
    }

    @Test(groups = {"unit"})
    public void legacyIdsAreStillRecognized() {
        // Builds predating the run id keep creating three segment ids on the same accounts during
        // rollout. If these stopped parsing, the age based sweep would silently stop cleaning them up.
        assertThat(CosmosDatabaseForTest.isTestDatabaseId(
            "RxJava.SDKTest.SharedDatabase_20240101T101010_abc")).isTrue();
    }

    @Test(groups = {"unit"})
    public void nonTestIdsAreNotRecognized() {
        assertThat(CosmosDatabaseForTest.isTestDatabaseId("myPermanentFixtureDb")).isFalse();
        assertThat(CosmosDatabaseForTest.isTestDatabaseId("RxJava.SDKTest.SharedDatabase")).isFalse();
        assertThat(CosmosDatabaseForTest.isTestDatabaseId(
            "RxJava.SDKTest.SharedDatabase_notatimestamp_run_abc")).isFalse();
        assertThat(CosmosDatabaseForTest.isTestDatabaseId(
            "SomethingElse_20240101T101010_run_abc")).isFalse();
        assertThat(CosmosDatabaseForTest.isTestDatabaseId(null)).isFalse();
    }

    @Test(groups = {"unit"})
    public void runScopedCleanupOnlyDeletesThatRunsDatabases() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String mine = idFor(now, "runmine", "aaaaaaaaaa");
        String theirs = idFor(now, "runtheirs", "bbbbbbbbbb");
        // Run ids that are a prefix and an extension of ours - matching must be exact. An extension in
        // particular catches a startsWith comparison, which would delete another run's databases.
        String prefixOfMine = idFor(now, "runmin", "cccccccccc");
        String extensionOfMine = idFor(now, "runmineextra", "dddddddddd");
        String legacy = "RxJava.SDKTest.SharedDatabase_20240101T101010_abc";

        FakeDatabaseManager manager = new FakeDatabaseManager(
            mine, theirs, prefixOfMine, extensionOfMine, legacy, PINNED_FIXTURE, UNRELATED_FIXTURE);

        List<String> deleted = CosmosDatabaseForTest.cleanupDatabasesForRun(manager, "runmine")
            .getDeletedDatabaseIds();

        assertThat(deleted).containsExactly(mine);
        assertThat(manager.deleted).containsExactly(mine);
    }

    @Test(groups = {"unit"})
    public void cleanupContinuesAfterAFailedDeleteAndDoesNotReportItAsDeleted() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String first = idFor(now, "runmine", "aaaaaaaaaa");
        String failing = idFor(now, "runmine", "bbbbbbbbbb");
        String last = idFor(now, "runmine", "cccccccccc");

        FakeDatabaseManager manager = new FakeDatabaseManager(first, failing, last);
        manager.failDeleteOf(failing);

        List<String> deleted = CosmosDatabaseForTest.cleanupDatabasesForRun(manager, "runmine")
            .getDeletedDatabaseIds();

        // One bad database must not abort the sweep, and must not be reported as deleted.
        assertThat(deleted).containsExactly(first, last);
        assertThat(manager.deleted).containsExactly(first, last);
    }

    @Test(groups = {"unit"})
    public void runScopedCleanupRejectsAnEmptyRunId() {
        // An empty run id would match legacy ids, whose run id is null, and those may belong to a job
        // that is still running.
        FakeDatabaseManager manager = new FakeDatabaseManager(
            "RxJava.SDKTest.SharedDatabase_20240101T101010_abc");

        try {
            CosmosDatabaseForTest.cleanupDatabasesForRun(manager, "");
            org.testng.Assert.fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertThat(manager.deleted).isEmpty();
        }
    }

    @Test(groups = {"unit"})
    public void aSweepWhereDeletesFailedIsNotReportedAsComplete() {
        // "Deleted nothing because everything failed" must not look like "found nothing to delete",
        // otherwise the janitor reports an all clear on a run whose resources are still on the account.
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String failing = idFor(now, "runmine", "aaaaaaaaaa");

        FakeDatabaseManager manager = new FakeDatabaseManager(failing);
        manager.failDeleteOf(failing);

        CosmosDatabaseForTest.CleanupResult result =
            CosmosDatabaseForTest.cleanupDatabasesForRun(manager, "runmine");

        assertThat(result.getDeletedDatabaseIds()).isEmpty();
        assertThat(result.isComplete()).isFalse();
        assertThat(result.getFailureCount()).isEqualTo(1);
    }

    @Test(groups = {"unit"})
    public void aSweepWithNothingToDeleteIsReportedAsComplete() {
        FakeDatabaseManager manager = new FakeDatabaseManager(PINNED_FIXTURE, UNRELATED_FIXTURE);

        CosmosDatabaseForTest.CleanupResult result =
            CosmosDatabaseForTest.cleanupDatabasesForRun(manager, "runmine");

        assertThat(result.getDeletedDatabaseIds()).isEmpty();
        assertThat(result.isComplete()).isTrue();
    }

    @Test(groups = {"unit"})
    public void ageBasedCleanupSparesYoungDatabasesAndNonTestIds() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String old = idFor(now.minusHours(9), "runold");
        String young = idFor(now.minusMinutes(30), "runyoung");
        String oldLegacy = "RxJava.SDKTest.SharedDatabase_20200101T101010_abc";

        FakeDatabaseManager manager = new FakeDatabaseManager(
            old, young, oldLegacy, PINNED_FIXTURE, UNRELATED_FIXTURE);

        List<String> deleted = CosmosDatabaseForTest.cleanupTestDatabasesOlderThan(manager, Duration.ofHours(8))
            .getDeletedDatabaseIds();

        // The young database may belong to a run that is still executing, and neither fixture is ours -
        // PINNED_FIXTURE in particular reaches the production code because it matches the query prefix.
        assertThat(deleted).containsExactlyInAnyOrder(old, oldLegacy);
    }

    private static String idFor(LocalDateTime createdAt, String runId) {
        return idFor(createdAt, runId, "abcdefghij");
    }

    private static String idFor(LocalDateTime createdAt, String runId, String randomSuffix) {
        return CosmosDatabaseForTest.SHARED_DB_ID_PREFIX
            + "_" + TIME_FORMATTER.format(createdAt)
            + "_" + runId
            + "_" + randomSuffix;
    }

    /**
     * Stands in for a Cosmos account holding the given databases. Records what cleanup deletes.
     */
    private static final class FakeDatabaseManager implements CosmosDatabaseForTest.DatabaseManager {
        private final Map<String, CosmosAsyncDatabase> databases = new LinkedHashMap<>();
        private final List<String> deleted = new ArrayList<>();
        private final Set<String> failingDeletes = new HashSet<>();

        private void failDeleteOf(String databaseId) {
            failingDeletes.add(databaseId);
        }

        private FakeDatabaseManager(String... databaseIds) {
            for (String databaseId : databaseIds) {
                CosmosAsyncDatabase database = Mockito.mock(CosmosAsyncDatabase.class);
                Mockito.when(database.getId()).thenReturn(databaseId);
                Mockito.when(database.getLink()).thenReturn("dbs/" + databaseId);
                // Recorded on subscription, not on invocation: if someone dropped the .block() in
                // deleteMatching the Mono would never run and production would delete nothing, so this
                // test has to fail in that case.
                CosmosDatabaseResponse response = Mockito.mock(CosmosDatabaseResponse.class);
                Mockito.when(database.delete()).thenAnswer(invocation -> Mono.fromRunnable(() -> {
                    if (failingDeletes.contains(databaseId)) {
                        throw new IllegalStateException("simulated delete failure for " + databaseId);
                    }

                    deleted.add(databaseId);
                }).thenReturn(response));

                databases.put(databaseId, database);
            }
        }

        @Override
        public CosmosPagedFlux<CosmosDatabaseProperties> queryDatabases(SqlQuerySpec query) {
            // Honour the query the production code actually issues rather than hardcoding the filter. If
            // the query lost its prefix binding, production would sweep nothing while these tests still
            // passed - exactly the "silently useless janitor" failure this suite exists to prevent.
            assertThat(query.getQueryText()).contains("STARTSWITH(c.id, @PREFIX)");
            String prefix = query.getParameters().stream()
                .filter(parameter -> "@PREFIX".equals(parameter.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("query has no @PREFIX parameter"))
                .getValue(String.class);

            List<CosmosDatabaseProperties> matching = new ArrayList<>();
            for (String databaseId : databases.keySet()) {
                if (databaseId.startsWith(prefix)) {
                    matching.add(new CosmosDatabaseProperties(databaseId));
                }
            }

            return UtilBridgeInternal.createCosmosPagedFlux(
                options -> Flux.just(ModelBridgeInternal.createFeedResponse(matching, new HashMap<>())));
        }


        @Override
        public CosmosAsyncDatabase getDatabase(String id) {
            return databases.get(id);
        }
    }
}
