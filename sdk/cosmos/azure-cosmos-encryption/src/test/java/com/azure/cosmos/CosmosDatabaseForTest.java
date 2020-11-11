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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosDatabaseForTest {
    private static Logger logger = LoggerFactory.getLogger(CosmosDatabaseForTest.class);
    public static final String SHARED_DB_ID_PREFIX = "RxJava.SDKTest.SharedDatabase";
    private static final Duration CLEANUP_THRESHOLD_DURATION = Duration.ofHours(2);
    private static final String DELIMITER = "_";
    private static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    public LocalDateTime createdTime;
    public CosmosAsyncDatabase createdDatabase;

    private CosmosDatabaseForTest(CosmosAsyncDatabase db, LocalDateTime createdTime) {
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

    private static CosmosDatabaseForTest from(CosmosAsyncDatabase db) {
        if (db == null || db.getId() == null || db.getLink() == null) {
            return null;
        }

        String id = db.getId();
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
            return new CosmosDatabaseForTest(db, parsedTime);
        } catch (Exception e) {
            return null;
        }
    }

    public static CosmosDatabaseForTest create(DatabaseManager client) {
        CosmosDatabaseProperties dbDef = new CosmosDatabaseProperties(generateId());

        client.createDatabase(dbDef).block();
        CosmosAsyncDatabase db = client.getDatabase(dbDef.getId());
        CosmosDatabaseForTest dbForTest = CosmosDatabaseForTest.from(db);
        assertThat(dbForTest).isNotNull();
        return dbForTest;
    }

    public static void cleanupStaleTestDatabases(DatabaseManager client) {
        logger.info("Cleaning stale test databases ...");
        List<SqlParameter> sqlParameterList = new ArrayList<>();
        sqlParameterList.add(new SqlParameter("@PREFIX", CosmosDatabaseForTest.SHARED_DB_ID_PREFIX));
        List<CosmosDatabaseProperties> dbs = client.queryDatabases(
            new SqlQuerySpec("SELECT * FROM c WHERE STARTSWITH(c.id, @PREFIX)", sqlParameterList)).collectList().block();

        for (CosmosDatabaseProperties db : dbs) {
            assertThat(db.getId()).startsWith(CosmosDatabaseForTest.SHARED_DB_ID_PREFIX);

            CosmosDatabaseForTest dbForTest = CosmosDatabaseForTest.from(client.getDatabase(db.getId()));

            if (db != null && dbForTest.isStale()) {
                logger.info("Deleting database {}", db.getId());
                dbForTest.deleteDatabase(db.getId());
            }
        }
    }

    private void deleteDatabase(String id) {
        this.createdDatabase.delete().block();
    }

    public interface DatabaseManager {
        CosmosPagedFlux<CosmosDatabaseProperties> queryDatabases(SqlQuerySpec query);
        Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseProperties databaseDefinition);
        CosmosAsyncDatabase getDatabase(String id);
    }
}
