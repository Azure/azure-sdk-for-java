// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySqlDatabaseTypeTest extends DatabaseTypeTest {

    private static final String MYSQL_SCHEMA = "jdbc:mysql";
    private static final String MYSQL_PATH_QUERY_DELIMITER = "?";
    private static final String MYSQL_QUERY_DELIMITER = "&";

    @Override
    void databaseTypeConstructor() {
        assertEquals(DatabaseType.MYSQL.getSchema(), MYSQL_SCHEMA);
        assertEquals(DatabaseType.MYSQL.getPathQueryDelimiter(), MYSQL_PATH_QUERY_DELIMITER);
        assertEquals(DatabaseType.MYSQL.getQueryDelimiter(), MYSQL_QUERY_DELIMITER);
    }

    @Override
    void databasePluginAvailable() {
        assertTrue(DatabaseType.MYSQL.isDatabasePluginAvailable());
        new ApplicationContextRunner()
            .withClassLoader(new FilteredClassLoader("com.mysql.cj.protocol.AuthenticationPlugin"))
            .run(context -> assertFalse(DatabaseType.MYSQL.isDatabasePluginAvailable()));
    }

}
