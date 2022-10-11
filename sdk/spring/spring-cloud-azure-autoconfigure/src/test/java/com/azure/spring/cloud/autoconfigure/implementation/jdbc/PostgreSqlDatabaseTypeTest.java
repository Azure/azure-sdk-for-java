// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgreSqlDatabaseTypeTest extends DatabaseTypeTest {


    private static final String POSTGRESQL_SCHEMA = "jdbc:postgresql";
    private static final String POSTGRESQL_PATH_QUERY_DELIMITER = "?";
    private static final String POSTGRESQL_QUERY_DELIMITER = "&";


    @Override
    void databaseTypeConstructor() {
        assertEquals(DatabaseType.POSTGRESQL.getSchema(), POSTGRESQL_SCHEMA);
        assertEquals(DatabaseType.POSTGRESQL.getPathQueryDelimiter(), POSTGRESQL_PATH_QUERY_DELIMITER);
        assertEquals(DatabaseType.POSTGRESQL.getQueryDelimiter(), POSTGRESQL_QUERY_DELIMITER);

    }

    @Override
    void databasePluginAvailable() {
        assertTrue(DatabaseType.POSTGRESQL.isDatabasePluginAvailable());
        new ApplicationContextRunner()
            .withClassLoader(new FilteredClassLoader("org.postgresql.plugin.AuthenticationPlugin"))
            .run(context -> {
                assertFalse(DatabaseType.POSTGRESQL.isDatabasePluginAvailable());
            });
    }

}
