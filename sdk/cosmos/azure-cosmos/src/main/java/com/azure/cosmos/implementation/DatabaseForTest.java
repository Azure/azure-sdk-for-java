// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Test-only helper for creating and reaping the shared databases used by the SDK's own test and
 * benchmark suites. It lives in {@code src/main} purely for module visibility (azure-cosmos-benchmark
 * consumes it) and is not part of the public or supported surface.
 * <p>
 * {@link #cleanupStaleTestDatabases} performs destructive deletes against whatever account it is pointed
 * at, so the age threshold below must stay well above the longest test run. The equivalent helper used by
 * azure-cosmos-tests is {@code com.azure.cosmos.CosmosDatabaseForTest}; keep the two id formats
 * compatible.
 */
public class DatabaseForTest {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseForTest.class);
    public static final String SHARED_DB_ID_PREFIX = "RxJava.SDKTest.SharedDatabase";
    // Must stay comfortably above the longest live test stage timeout (currently 210 minutes), otherwise
    // this sweep can delete a database that a concurrently running test job is still using.
    private static final Duration CLEANUP_THRESHOLD_DURATION = Duration.ofHours(8);
    private static final String DELIMITER = "_";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    public LocalDateTime createdTime;
    public Database createdDatabase;

    private DatabaseForTest(Database db, LocalDateTime createdTime) {
        this.createdDatabase = db;
        this.createdTime = createdTime;
    }

    private boolean isStale() {
        return isOlderThan(CLEANUP_THRESHOLD_DURATION);
    }

    private boolean isOlderThan(Duration dur) {
        return createdTime.isBefore(nowUtc().minus(dur));
    }

    // Ids are written on one machine and compared on another, so both sides must use UTC.
    private static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public static String generateId() {
        return SHARED_DB_ID_PREFIX + DELIMITER + TIME_FORMATTER.format(nowUtc()) + DELIMITER + RandomStringUtils.randomAlphabetic(3);
    }

    private static DatabaseForTest from(Database db) {
        if (db == null || db.getId() == null || db.getSelfLink() == null) {
            return null;
        }

        String id = db.getId();
        if (id == null) {
            return null;
        }

        String[] parts = StringUtils.split(id, DELIMITER);
        // 3 parts: <prefix>_<timestamp>_<random>. 4 parts adds a run id and is produced by
        // azure-cosmos-tests' CosmosDatabaseForTest; both forms appear on the shared test accounts.
        if (parts == null || parts.length < 3 || parts.length > 4) {
            return null;
        }
        if (!StringUtils.equals(parts[0], SHARED_DB_ID_PREFIX)) {
            return null;
        }

        try {
            LocalDateTime parsedTime = LocalDateTime.parse(parts[1], TIME_FORMATTER);
            return new DatabaseForTest(db, parsedTime);
        } catch (Exception e) {
            return null;
        }
    }

    public static DatabaseForTest create(DatabaseManager client) {
        Database dbDef = new Database();
        dbDef.setId(generateId());

        Database db = client.createDatabase(dbDef).block().getResource();
        DatabaseForTest dbForTest = DatabaseForTest.from(db);
        assert(dbForTest != null);
        return dbForTest;
    }

    public static void cleanupStaleTestDatabases(DatabaseManager client) {
        logger.info("Cleaning stale test databases ...");

        List<Database> dbs = client.queryDatabases(
                new SqlQuerySpec("SELECT * FROM c WHERE STARTSWITH(c.id, @PREFIX)",
                    Collections.singletonList(new SqlParameter("@PREFIX", DatabaseForTest.SHARED_DB_ID_PREFIX))))
                .flatMap(page -> Flux.fromIterable(page.getResults())).collectList().block();

        // block() can return null if Flux is empty()
        if (dbs == null) {
            return;
        }

        for (Database db : dbs) {
            assert(db.getId().startsWith(DatabaseForTest.SHARED_DB_ID_PREFIX));

            DatabaseForTest dbForTest = DatabaseForTest.from(db);

            // A null dbForTest means the id does not follow the test convention - leave it alone.
            if (dbForTest != null && dbForTest.isStale()) {
                logger.info("Deleting database {}", db.getId());
                client.deleteDatabase(db.getId()).block();
            }
        }
    }

    public interface DatabaseManager {
        Flux<FeedResponse<Database>> queryDatabases(SqlQuerySpec query);
        Mono<ResourceResponse<Database>> createDatabase(Database databaseDefinition);
        Mono<ResourceResponse<Database>> deleteDatabase(String id);
    }
}
