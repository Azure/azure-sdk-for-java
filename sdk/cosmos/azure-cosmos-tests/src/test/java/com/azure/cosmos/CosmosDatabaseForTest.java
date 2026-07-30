// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Naming and lifetime helper for databases created by the test suite.
 * <p>
 * Ids follow {@code RxJava.SDKTest.SharedDatabase_<timestamp>_<runId>_<random>}. The run id lets
 * cleanup delete exactly the databases created by the current run without touching resources that a
 * concurrently executing matrix leg or pipeline run is still using on the same shared account. The
 * legacy three segment form (without a run id) is still parsed so that databases left behind by
 * builds predating this change are picked up by the age based sweep.
 */
public class CosmosDatabaseForTest {
    private static Logger logger = LoggerFactory.getLogger(CosmosDatabaseForTest.class);
    public static final String SHARED_DB_ID_PREFIX = "RxJava.SDKTest.SharedDatabase";
    private static final Duration CLEANUP_THRESHOLD_DURATION = Duration.ofHours(8);
    private static final String DELIMITER = "_";
    private static final int RANDOM_SUFFIX_LENGTH = 10;
    private static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    public LocalDateTime createdTime;
    public CosmosAsyncDatabase createdDatabase;
    private final String runId;

    private CosmosDatabaseForTest(CosmosAsyncDatabase db, LocalDateTime createdTime, String runId) {
        this.createdDatabase = db;
        this.createdTime = createdTime;
        this.runId = runId;
    }

    private boolean isStale() {
        return isOlderThan(CLEANUP_THRESHOLD_DURATION);
    }

    private boolean isOlderThan(Duration dur) {
        return createdTime.isBefore(nowUtc().minus(dur));
    }

    /**
     * Timestamps are written by the test agent and compared on the janitor agent, which may be a
     * different machine. Both sides must use UTC - a local time zone offset would show a database as
     * older than it is and could make the age based sweep delete an in-flight run's database.
     */
    private static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public static String generateId() {
        return generateId(null);
    }

    /**
     * Generates a run tagged database id. The optional label only makes logs and portal views
     * readable - uniqueness and cleanup scoping come from the timestamp and run id.
     *
     * @param label optional human readable label, may be null.
     * @return a database id that cleanup is able to attribute to the current run.
     */
    public static String generateId(String label) {
        // The timestamp only has second resolution and many call sites now generate ids concurrently.
        // A collision is silently swallowed as a 409 by the create helpers, which would make two tests
        // share one database, so keep the random part wide enough that collisions do not happen.
        String suffix = StringUtils.isEmpty(label)
            ? RandomStringUtils.randomAlphabetic(RANDOM_SUFFIX_LENGTH)
            : sanitizeLabel(label) + RandomStringUtils.randomAlphabetic(RANDOM_SUFFIX_LENGTH);

        return SHARED_DB_ID_PREFIX
            + DELIMITER + TIME_FORMATTER.format(nowUtc())
            + DELIMITER + CosmosTestRunId.get()
            + DELIMITER + suffix;
    }

    private static String sanitizeLabel(String label) {
        String sanitized = label.replaceAll("[^A-Za-z0-9]", "");
        return sanitized.length() <= 40 ? sanitized : sanitized.substring(0, 40);
    }

    /**
     * @param id the database id to check.
     * @return true when the id was produced by {@link #generateId(String)} - and is therefore owned by
     * the test suite and safe for automated cleanup to delete.
     */
    public static boolean isTestDatabaseId(String id) {
        return parse(id) != null;
    }

    private static ParsedId parse(String id) {
        if (id == null) {
            return null;
        }

        String[] parts = StringUtils.split(id, DELIMITER);
        // 3 parts: legacy <prefix>_<timestamp>_<random>. 4 parts: <prefix>_<timestamp>_<runId>_<random>.
        if (parts == null || parts.length < 3 || parts.length > 4) {
            return null;
        }
        if (!StringUtils.equals(parts[0], SHARED_DB_ID_PREFIX)) {
            return null;
        }

        try {
            LocalDateTime parsedTime = LocalDateTime.parse(parts[1], TIME_FORMATTER);
            String runId = parts.length == 4 ? parts[2] : null;
            return new ParsedId(parsedTime, runId);
        } catch (Exception e) {
            return null;
        }
    }

    private static CosmosDatabaseForTest from(CosmosAsyncDatabase db) {
        if (db == null || db.getId() == null || db.getLink() == null) {
            return null;
        }

        ParsedId parsed = parse(db.getId());
        if (parsed == null) {
            return null;
        }

        return new CosmosDatabaseForTest(db, parsed.createdTime, parsed.runId);
    }

    public static CosmosDatabaseForTest create(DatabaseManager client) {
        CosmosDatabaseProperties dbDef = new CosmosDatabaseProperties(generateId());

        client.createDatabase(dbDef).block();
        CosmosAsyncDatabase db = client.getDatabase(dbDef.getId());
        CosmosDatabaseForTest dbForTest = CosmosDatabaseForTest.from(db);
        assertThat(dbForTest).isNotNull();
        CosmosTestResourceRegistry.registerDatabase(dbForTest.createdDatabase.getId());
        return dbForTest;
    }

    /**
     * Deletes databases created by other, long finished runs. Databases whose id does not follow the
     * test naming convention are never touched, and neither are databases younger than the cleanup
     * threshold, since those may belong to a run that is still executing.
     *
     * @param client the database manager to clean up with.
     */
    public static CleanupResult cleanupStaleTestDatabases(DatabaseManager client) {
        logger.info("Cleaning stale test databases ...");
        return deleteMatching(client, CosmosDatabaseForTest::isStale);
    }

    /**
     * Deletes every database created by the currently executing run, regardless of age. This is the
     * safety net for tests that created a database and failed to delete it.
     *
     * @param client the database manager to clean up with.
     * @return the ids of the databases that had been leaked and were deleted here.
     */
    public static CleanupResult cleanupDatabasesForCurrentRun(DatabaseManager client) {
        return cleanupDatabasesForRun(client, CosmosTestRunId.get());
    }

    /**
     * Deletes every database created by the given run, regardless of age. Used by the pipeline post
     * step, which knows the run id of the job that just finished.
     *
     * @param client the database manager to clean up with.
     * @param runId the run id to delete databases for.
     * @return the ids of the databases that were deleted.
     */
    public static CleanupResult cleanupDatabasesForRun(DatabaseManager client, String runId) {
        if (StringUtils.isEmpty(runId)) {
            // Matching a null run id would match every legacy (pre run id) database, which may belong to
            // a run that is still executing. Age based cleanup is the only safe option for those.
            throw new IllegalArgumentException("runId must not be empty");
        }

        logger.info("Cleaning test databases for run {} ...", runId);
        return deleteMatching(client, dbForTest -> StringUtils.equals(dbForTest.runId, runId));
    }

    /**
     * Deletes test databases older than the given duration. Used by the scheduled janitor pipeline to
     * recover resources from jobs that were cancelled or timed out. The threshold must be comfortably
     * longer than the longest test stage so that in-flight runs are never touched.
     *
     * @param client the database manager to clean up with.
     * @param threshold the minimum age a database must have to be deleted.
     * @return the ids of the databases that were deleted.
     */
    public static CleanupResult cleanupTestDatabasesOlderThan(DatabaseManager client, Duration threshold) {
        logger.info("Cleaning test databases older than {} ...", threshold);
        return deleteMatching(client, dbForTest -> dbForTest.isOlderThan(threshold));
    }

    private static CleanupResult deleteMatching(
        DatabaseManager client,
        Predicate<CosmosDatabaseForTest> predicate) {

        List<String> deleted = new ArrayList<>();
        int failures = 0;
        List<CosmosDatabaseProperties> dbs = client.queryDatabases(
            new SqlQuerySpec(
                "SELECT * FROM c WHERE STARTSWITH(c.id, @PREFIX)",
                Collections.singletonList(new SqlParameter("@PREFIX", SHARED_DB_ID_PREFIX))))
            .collectList()
            .block();

        if (dbs == null) {
            return new CleanupResult(deleted, failures);
        }

        for (CosmosDatabaseProperties db : dbs) {
            assertThat(db.getId()).startsWith(SHARED_DB_ID_PREFIX);

            CosmosDatabaseForTest dbForTest = CosmosDatabaseForTest.from(client.getDatabase(db.getId()));
            // A null dbForTest means the id does not follow the test convention - leave it alone.
            if (dbForTest == null || !predicate.test(dbForTest)) {
                continue;
            }

            logger.info("Deleting database {}", db.getId());
            try {
                dbForTest.createdDatabase.delete().block();
                deleted.add(db.getId());
            } catch (Exception e) {
                // Keep going - one undeletable database must not strand the rest - but remember that this
                // sweep did not fully succeed, so callers do not mistake "found nothing" for "all clean".
                failures++;
                logger.warn("Failed to delete database {}", db.getId(), e);
            } finally {
                CosmosTestResourceRegistry.unregisterDatabase(db.getId());
            }
        }

        return new CleanupResult(deleted, failures);
    }

    /**
     * What a cleanup sweep managed to do. A sweep that deleted nothing because every delete failed is not
     * the same as a sweep that found nothing, and callers must be able to tell them apart.
     */
    public static final class CleanupResult {
        private final List<String> deletedDatabaseIds;
        private final int failureCount;

        private CleanupResult(List<String> deletedDatabaseIds, int failureCount) {
            this.deletedDatabaseIds = deletedDatabaseIds;
            this.failureCount = failureCount;
        }

        public List<String> getDeletedDatabaseIds() {
            return this.deletedDatabaseIds;
        }

        public int getFailureCount() {
            return this.failureCount;
        }

        public boolean isComplete() {
            return this.failureCount == 0;
        }
    }

    private static final class ParsedId {
        private final LocalDateTime createdTime;
        private final String runId;

        private ParsedId(LocalDateTime createdTime, String runId) {
            this.createdTime = createdTime;
            this.runId = runId;
        }
    }

    public interface DatabaseManager {
        CosmosPagedFlux<CosmosDatabaseProperties> queryDatabases(SqlQuerySpec query);
        Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseProperties databaseDefinition);
        CosmosAsyncDatabase getDatabase(String id);
    }
}
