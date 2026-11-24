// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Naming and lifetime helper for databases created by the test suite.
 * <p>
 * Ids follow {@code RxJava.SDKTest.SharedDatabase_<timestamp>_<runId>_<random>}. The run id lets cleanup
 * delete exactly the databases created by the current run without touching resources that a concurrently
 * executing matrix leg or pipeline run is still using on the same shared account. The legacy three
 * segment form (without a run id) is still parsed, so those ids are recognized as test databases and
 * never mistaken for hand created fixtures.
 * <p>
 * Timestamps are UTC: an id is written by the test agent and may be compared on a different machine.
 */
public final class CosmosDatabaseForTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosDatabaseForTest.class);
    public static final String SHARED_DB_ID_PREFIX = "RxJava.SDKTest.SharedDatabase";
    private static final String DELIMITER = "_";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final int RANDOM_SUFFIX_LENGTH = 8;
    private static final int MAX_LABEL_LENGTH = 10;

    private CosmosDatabaseForTest() {
    }

    private static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public static String generateId() {
        return generateId(null);
    }

    /**
     * Generates a run tagged database id. The optional label only makes logs and portal views readable -
     * uniqueness comes from the timestamp plus the random suffix, and cleanup scoping from the run id.
     * <p>
     * Length matters and is deliberately kept short. The database id is embedded in derived resource
     * names elsewhere in the SDK - notably the throughput control group id, which is
     * {@code <database>/<container>/<group>/<suffix>}, base64 encoded and then extended with a UUID to
     * form a control item id. A long database id overflows that and the emulator rejects the request
     * with "400 Bad Request - Invalid URL", so keep this comfortably shorter than the ~82 character ids
     * that are known to work.
     *
     * @param label optional human readable label, may be null.
     * @return a database id that cleanup is able to attribute to the current run.
     */
    public static String generateId(String label) {
        String random = RandomStringUtils.randomAlphanumeric(RANDOM_SUFFIX_LENGTH);
        String suffix = StringUtils.isEmpty(label) ? random : sanitizeLabel(label) + random;

        return SHARED_DB_ID_PREFIX
            + DELIMITER + TIME_FORMATTER.format(nowUtc())
            + DELIMITER + CosmosTestRunId.get()
            + DELIMITER + suffix;
    }

    private static String sanitizeLabel(String label) {
        String sanitized = label.replaceAll("[^A-Za-z0-9]", "");
        return sanitized.length() <= MAX_LABEL_LENGTH ? sanitized : sanitized.substring(0, MAX_LABEL_LENGTH);
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
            // Parsed purely to validate the id: a malformed timestamp means this is not one of ours, so
            // it is left alone. The value itself is not needed - deletion is only ever run scoped.
            LocalDateTime.parse(parts[1], TIME_FORMATTER);
            String runId = parts.length == 4 ? parts[2] : null;
            return new ParsedId(runId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Deletes every database created by the currently executing run, regardless of age. This is the
     * safety net for tests that created a database and failed to delete it.
     *
     * @param client the database manager to clean up with.
     * @return what the sweep deleted, and whether it ran to completion.
     */
    public static CleanupResult cleanupDatabasesForCurrentRun(DatabaseManager client) {
        return cleanupDatabasesForRun(client, CosmosTestRunId.get());
    }

    /**
     * Deletes every database created by the given run, regardless of age. Used by the pipeline post step,
     * which knows the run id of the job that just finished.
     *
     * @param client the database manager to clean up with.
     * @param runId the run id to delete databases for.
     * @return what the sweep deleted, and whether it ran to completion.
     */
    public static CleanupResult cleanupDatabasesForRun(DatabaseManager client, String runId) {
        if (StringUtils.isEmpty(runId)) {
            // Matching a null run id would match every legacy (pre run id) database, which may belong to a
            // run that is still executing.
            throw new IllegalArgumentException("runId must not be empty");
        }

        LOGGER.info("Cleaning test databases for run {} ...", runId);
        return deleteMatching(client, dbForTest -> StringUtils.equals(dbForTest.runId, runId));
    }

    private static CleanupResult deleteMatching(DatabaseManager client, Predicate<ParsedId> predicate) {
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
            ParsedId parsed = parse(db.getId());
            // A null parsed id means the id does not follow the test convention - it may be a hand created
            // fixture, so leave it alone.
            if (parsed == null || !predicate.test(parsed)) {
                continue;
            }

            LOGGER.info("Deleting database {}", db.getId());
            try {
                client.getDatabase(db.getId()).delete().block();
                deleted.add(db.getId());
            } catch (Exception e) {
                // Keep going - one undeletable database must not strand the rest - but remember that this
                // sweep did not fully succeed, so callers do not mistake "found nothing" for "all clean".
                failures++;
                LOGGER.warn("Failed to delete database {}", db.getId(), e);
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
        private final String runId;

        private ParsedId(String runId) {
            this.runId = runId;
        }
    }

    public interface DatabaseManager {
        CosmosPagedFlux<CosmosDatabaseProperties> queryDatabases(SqlQuerySpec query);
        CosmosAsyncDatabase getDatabase(String id);
    }
}
