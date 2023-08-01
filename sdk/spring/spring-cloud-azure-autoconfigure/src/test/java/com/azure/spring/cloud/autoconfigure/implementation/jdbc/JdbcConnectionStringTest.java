// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionString.INVALID_PROPERTY_PAIR_FORMAT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcConnectionStringTest {

    static final String PATH_WITH_QUERY_PATTERN = "%s://host/database%s%s";
    static final String PATH_WITHOUT_QUERY_PATTERN = "%s://host/database";

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void testConnectionStringWithNonValueProperties(DatabaseType databaseType) {
        String queries = "enableSwitch1" + databaseType.getQueryDelimiter() + "property1=value1";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, databaseType.getSchema(), databaseType.getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        assertEquals(databaseType, jdbcConnectionString.getDatabaseType());
        assertEquals(null, jdbcConnectionString.getProperty("enableSwitch1"));
        assertEquals("value1", jdbcConnectionString.getProperty("property1"));
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void testConnectionStringWithMultipleProperties(DatabaseType databaseType) {
        String queries = "property1=value1" + databaseType.getQueryDelimiter() + "property2=value2";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, databaseType.getSchema(), databaseType.getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        assertEquals(databaseType, jdbcConnectionString.getDatabaseType());
        assertEquals("value1", jdbcConnectionString.getProperty("property1"));
        assertEquals("value2", jdbcConnectionString.getProperty("property2"));
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void testConnectionStringWithoutProperties(DatabaseType databaseType) {
        String connectionString = String.format(PATH_WITHOUT_QUERY_PATTERN, databaseType.getSchema());

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        assertEquals(databaseType, jdbcConnectionString.getDatabaseType());
        assertTrue(jdbcConnectionString.getProperties().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void testConnectionStringWithInvalidProperties(DatabaseType databaseType) {
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, databaseType.getSchema(), databaseType.getPathQueryDelimiter(), "=");

        assertThrows(IllegalArgumentException.class, () -> JdbcConnectionString.resolve(connectionString),
            String.format(INVALID_PROPERTY_PAIR_FORMAT, connectionString));
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void invalidConnectionString(DatabaseType databaseType) {
        String connectionString = String.format(PATH_WITHOUT_QUERY_PATTERN, databaseType.getSchema() + "x");

        JdbcConnectionString resolve = JdbcConnectionString.resolve(connectionString);
        assertNull(resolve);
    }

}
