// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionString.INVALID_PROPERTY_PAIR_FORMAT;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.NONE_VALUE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_NAME_AUTHENTICATION_PLUGIN_CLASSNAME;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_NAME_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_VALUE_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRES_AUTH_PLUGIN_CLASS_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PostgreSqlJdbcConnectionStringTest extends AbstractJdbcConnectionStringTest {
    private static final String POSTGRESQL_AUTH_PLUGIN_PROPERTY = POSTGRESQL_PROPERTY_NAME_AUTHENTICATION_PLUGIN_CLASSNAME + "=" + POSTGRES_AUTH_PLUGIN_CLASS_NAME;
    private static final String POSTGRESQL_SSL_MODE_PROPERTY = POSTGRESQL_PROPERTY_NAME_SSL_MODE + "=" + POSTGRESQL_PROPERTY_VALUE_SSL_MODE;

    @Override
    void connectionStringWithNonValueProperties() {
        String connectionString = "jdbc:postgresql://host/database?enableSwitch1&property1=value1";
        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        assertEquals(DatabaseType.POSTGRESQL, jdbcConnectionString.getDatabaseType());
        assertEquals(NONE_VALUE, jdbcConnectionString.getOriginalProperty("enableSwitch1"));
        assertEquals("value1", jdbcConnectionString.getOriginalProperty("property1"));
    }

    @Override
    void connectionStringWithMultipleProperties() {
        String connectionString = "jdbc:postgresql://host/database?property1=value1&property2=value2";

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        assertEquals(DatabaseType.POSTGRESQL, jdbcConnectionString.getDatabaseType());
        assertEquals("value1", jdbcConnectionString.getOriginalProperty("property1"));
        assertEquals("value2", jdbcConnectionString.getOriginalProperty("property2"));
    }

    @Override
    void connectionStringWithoutProperties() {
        String connectionString = "jdbc:postgresql://host/database";

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        assertEquals(DatabaseType.POSTGRESQL, jdbcConnectionString.getDatabaseType());
        assertFalse(jdbcConnectionString.hasOriginalProperties());
    }

    @Override
    void connectionStringWittInvalidProperties() {
        String connectionString = "jdbc:postgresql://host/database?=";
        assertThrows(IllegalArgumentException.class, () -> JdbcConnectionString.resolve(connectionString),
            String.format(INVALID_PROPERTY_PAIR_FORMAT, connectionString));
    }

    @Override
    void inconsistentPropertiesTest() {
        String connectionString = "jdbc:postgresql://mockpostgresqlurl:3306/db?sslmode=required";
        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        Map<String, String> configMap = new HashMap<>();
        configMap.putAll(jdbcConnectionString.getDatabaseType().getDefaultEnhancedProperties());
        assertThrows(IllegalArgumentException.class, () -> jdbcConnectionString.enhanceProperties(configMap));
    }

    @Override
    void consistentPropertiesTest() {
        String connectionString = "jdbc:postgresql://mockpostgresqlurl:3306/db?useSSL=true";
        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        Map<String, String> configMap = new HashMap<>();
        jdbcConnectionString.enhanceProperties(configMap);
        assertNotNull(jdbcConnectionString.getJdbcUrl());
    }

    @Override
    void enhanceConnectionStringWithoutProperties() {
        String connectionString = "jdbc:postgresql://host/database";
        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        jdbcConnectionString.enhanceProperties(jdbcConnectionString.getDatabaseType().getDefaultEnhancedProperties());
        String enhancedUrl = jdbcConnectionString.getJdbcUrl();
        String expectedUrl = String.format("%s?%s&%s", connectionString,
        POSTGRESQL_AUTH_PLUGIN_PROPERTY,
        POSTGRESQL_SSL_MODE_PROPERTY);
        Assertions.assertEquals(expectedUrl, enhancedUrl);
    }

    @Override
    void enhanceConnectionStringWithProperties() {
        String connectionString = "jdbc:postgresql://host/database?sslmode=require";
        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        jdbcConnectionString.enhanceProperties(jdbcConnectionString.getDatabaseType().getDefaultEnhancedProperties());
        String enhancedUrl = jdbcConnectionString.getJdbcUrl();
        String expectedUrl = String.format("%s&%s", connectionString,
            POSTGRESQL_AUTH_PLUGIN_PROPERTY);
        Assertions.assertEquals(expectedUrl, enhancedUrl);
    }
}
