// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class DatabaseForTest {
    public static final String SHARED_DB_ID_PREFIX = "RxJava.SDKTest.SharedDatabase";
    private static final String DELIMITER = "_";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    public LocalDateTime createdTime;
    public Database createdDatabase;

    private DatabaseForTest(Database db, LocalDateTime createdTime) {
        this.createdDatabase = db;
        this.createdTime = createdTime;
    }

    public static String generateId() {
        return SHARED_DB_ID_PREFIX + DELIMITER + TIME_FORMATTER.format(LocalDateTime.now(ZoneOffset.UTC))
            + DELIMITER + UUID.randomUUID();
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
        dbDef.setId(generateId());

        Database db = client.createDatabase(dbDef).block().getResource();
        DatabaseForTest dbForTest = DatabaseForTest.from(db);
        assert(dbForTest != null);
        return dbForTest;
    }

    public interface DatabaseManager {
        Mono<ResourceResponse<Database>> createDatabase(Database databaseDefinition);
    }
}
