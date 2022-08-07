// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DatabaseTypeTest {

    private static final String MYSQL_SCHEMA = "jdbc:mysql";
    private static final String MYSQL_PATH_QUERY_DELIMITER = "?";
    private static final String MYSQL_QUERY_DELIMITER = "&";

    private static final String POSTGRESQL_SCHEMA = "jdbc:postgresql";
    private static final String POSTGRESQL_PATH_QUERY_DELIMITER = "?";
    private static final String POSTGRESQL_QUERY_DELIMITER = "&";

    private static final String SQLSERVER_SCHEMA = "jdbc:sqlserver";
    private static final String SQLSERVER_PATH_QUERY_DELIMITER = ";";
    private static final String SQLSERVER_QUERY_DELIMITER = ";";

    @Test
    void testMySQL() {
        assertEquals(DatabaseType.MYSQL.getSchema(), MYSQL_SCHEMA);
        assertEquals(DatabaseType.MYSQL.getPathQueryDelimiter(), MYSQL_PATH_QUERY_DELIMITER);
        assertEquals(DatabaseType.MYSQL.getQueryDelimiter(), MYSQL_QUERY_DELIMITER);
    }

    @Test
    void testPostGreSQL() {
        assertEquals(DatabaseType.POSTGRESQL.getSchema(), POSTGRESQL_SCHEMA);
        assertEquals(DatabaseType.POSTGRESQL.getPathQueryDelimiter(), POSTGRESQL_PATH_QUERY_DELIMITER);
        assertEquals(DatabaseType.POSTGRESQL.getQueryDelimiter(), POSTGRESQL_QUERY_DELIMITER);
    }

    @Test
    void testSqlServer() {
        assertEquals(DatabaseType.SQLSERVER.getSchema(), SQLSERVER_SCHEMA);
        assertEquals(DatabaseType.SQLSERVER.getPathQueryDelimiter(), SQLSERVER_PATH_QUERY_DELIMITER);
        assertEquals(DatabaseType.SQLSERVER.getQueryDelimiter(), SQLSERVER_QUERY_DELIMITER);
    }

    @Test
    void testMysqlPlugin() {
        assertFalse(DatabaseType.MYSQL.isDatabasePluginEnabled());
    }

    @Test
    void testPostGreSqlPlugin() {
        assertFalse(DatabaseType.POSTGRESQL.isDatabasePluginEnabled());
    }

}
