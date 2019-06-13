/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosDatabaseForTest {
    private static Logger logger = LoggerFactory.getLogger(CosmosDatabaseForTest.class);
    public static final String SHARED_DB_ID_PREFIX = "RxJava.SDKTest.SharedDatabase";
    private static final Duration CLEANUP_THRESHOLD_DURATION = Duration.ofHours(2);
    private static final String DELIMITER = "_";
    private static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    public LocalDateTime createdTime;
    public CosmosDatabase createdDatabase;

    private CosmosDatabaseForTest(CosmosDatabase db, LocalDateTime createdTime) {
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

    private static CosmosDatabaseForTest from(CosmosDatabase db) {
        if (db == null || db.id() == null || db.getLink() == null) {
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
            return new CosmosDatabaseForTest(db, parsedTime);
        } catch (Exception e) {
            return null;
        }
    }

    public static CosmosDatabaseForTest create(DatabaseManager client) {
        CosmosDatabaseSettings dbDef = new CosmosDatabaseSettings(generateId());

        CosmosDatabase db = client.createDatabase(dbDef).block().database();
        CosmosDatabaseForTest dbForTest = CosmosDatabaseForTest.from(db);
        assertThat(dbForTest).isNotNull();
        return dbForTest;
    }

    public static void cleanupStaleTestDatabases(DatabaseManager client) {
        logger.info("Cleaning stale test databases ...");
        List<CosmosDatabaseSettings> dbs = client.queryDatabases(
                new SqlQuerySpec("SELECT * FROM c WHERE STARTSWITH(c.id, @PREFIX)",
                                 new SqlParameterCollection(new SqlParameter("@PREFIX", CosmosDatabaseForTest.SHARED_DB_ID_PREFIX))))
                .flatMap(page -> Flux.fromIterable(page.results())).collectList().block();

        for (CosmosDatabaseSettings db : dbs) {
            assertThat(db.id()).startsWith(CosmosDatabaseForTest.SHARED_DB_ID_PREFIX);

            CosmosDatabaseForTest dbForTest = CosmosDatabaseForTest.from(client.getDatabase(db.id()));

            if (db != null && dbForTest.isStale()) {
                logger.info("Deleting database {}", db.id());
                dbForTest.deleteDatabase(db.id());
            }
        }
    }

    private void deleteDatabase(String id) {
        this.createdDatabase.delete().block();
    }

    public interface DatabaseManager {
        Flux<FeedResponse<CosmosDatabaseSettings>> queryDatabases(SqlQuerySpec query);
        Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseSettings databaseDefinition);
        CosmosDatabase getDatabase(String id);
    }
}
