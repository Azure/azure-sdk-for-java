// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.SqlParameter;
import com.azure.data.cosmos.SqlParameterList;
import com.azure.data.cosmos.SqlQuerySpec;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DatabaseForTest {
    private static Logger logger = LoggerFactory.getLogger(DatabaseForTest.class);
    public static final String SHARED_DB_ID_PREFIX = "RxJava.SDKTest.SharedDatabase";
    private static final Duration CLEANUP_THRESHOLD_DURATION = Duration.ofHours(2);
    private static final String DELIMITER = "_";
    private static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

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
        return createdTime.isBefore(LocalDateTime.now().minus(dur));
    }

    public static String generateId() {
        return SHARED_DB_ID_PREFIX + DELIMITER + TIME_FORMATTER.format(LocalDateTime.now()) + DELIMITER + RandomStringUtils.randomAlphabetic(3);
    }

    private static DatabaseForTest from(Database db) {
        if (db == null || db.id() == null || db.selfLink() == null) {
            return null;
        }

        String id = db.id();
        if (id == null) {
            return null;
        }

        String[] parts = StringUtils.split(id, DELIMITER);
        if (parts.length != 3) {
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
        dbDef.id(generateId());

        Database db = client.createDatabase(dbDef).single().block().getResource();
        DatabaseForTest dbForTest = DatabaseForTest.from(db);
        assert(dbForTest != null);
        return dbForTest;
    }

    public static void cleanupStaleTestDatabases(DatabaseManager client) {
        logger.info("Cleaning stale test databases ...");
        List<Database> dbs = client.queryDatabases(
                new SqlQuerySpec("SELECT * FROM c WHERE STARTSWITH(c.id, @PREFIX)",
                                 new SqlParameterList(new SqlParameter("@PREFIX", DatabaseForTest.SHARED_DB_ID_PREFIX))))
                .flatMap(page -> Flux.fromIterable(page.results())).collectList().block();

        for (Database db : dbs) {
            assert(db.id().startsWith(DatabaseForTest.SHARED_DB_ID_PREFIX));

            DatabaseForTest dbForTest = DatabaseForTest.from(db);

            if (db != null && dbForTest.isStale()) {
                logger.info("Deleting database {}", db.id());
                client.deleteDatabase(db.id()).single().block();
            }
        }
    }

    public interface DatabaseManager {
        Flux<FeedResponse<Database>> queryDatabases(SqlQuerySpec query);
        Flux<ResourceResponse<Database>> createDatabase(Database databaseDefinition);
        Flux<ResourceResponse<Database>> deleteDatabase(String id);
    }
}