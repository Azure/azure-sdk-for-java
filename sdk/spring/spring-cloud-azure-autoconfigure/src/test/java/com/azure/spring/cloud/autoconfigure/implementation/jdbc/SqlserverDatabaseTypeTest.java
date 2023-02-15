// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlserverDatabaseTypeTest extends DatabaseTypeTest {

    private static final String SQLSERVER_SCHEMA = "jdbc:sqlserver";
    private static final String SQLSERVER_PATH_QUERY_DELIMITER = ";";
    private static final String SQLSERVER_QUERY_DELIMITER = ";";

    @Override
    void databaseTypeConstructor() {
        assertEquals(DatabaseType.SQLSERVER.getSchema(), SQLSERVER_SCHEMA);
        assertEquals(DatabaseType.SQLSERVER.getPathQueryDelimiter(), SQLSERVER_PATH_QUERY_DELIMITER);
        assertEquals(DatabaseType.SQLSERVER.getQueryDelimiter(), SQLSERVER_QUERY_DELIMITER);
    }

    @Override
    void databasePluginAvailable() {
        // No implementation for sqlserver.
    }

}
