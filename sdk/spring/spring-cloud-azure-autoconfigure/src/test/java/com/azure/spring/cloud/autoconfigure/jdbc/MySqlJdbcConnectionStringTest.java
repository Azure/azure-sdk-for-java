// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.spring.cloud.autoconfigure.implementation.jdbc.DatabaseType;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionString;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionString.INVALID_PROPERTY_PAIR_FORMAT;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_AUTH_PLUGIN_CLASS_NAME;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.NONE_VALUE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_NAME_AUTHENTICATION_PLUGINS;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_NAME_DEFAULT_AUTHENTICATION_PLUGIN;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_NAME_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_NAME_USE_SSL;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_VALUE_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_VALUE_USE_SSL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MySqlJdbcConnectionStringTest extends AbstractJdbcConnectionStringTest {
    private static final String MYSQL_AUTH_PLUGIN_PROPERTY = MYSQL_PROPERTY_NAME_AUTHENTICATION_PLUGINS + "=" + MYSQL_AUTH_PLUGIN_CLASS_NAME;
    private static final String MYSQL_DEFAULT_PLUGIN_PROPERTY = MYSQL_PROPERTY_NAME_DEFAULT_AUTHENTICATION_PLUGIN + "=" + MYSQL_AUTH_PLUGIN_CLASS_NAME;
    private static final String MYSQL_SSL_MODE_PROPERTY = MYSQL_PROPERTY_NAME_SSL_MODE + "=" + MYSQL_PROPERTY_VALUE_SSL_MODE;
    private static final String MYSQL_USE_SSL_PROPERTY = MYSQL_PROPERTY_NAME_USE_SSL + "=" + MYSQL_PROPERTY_VALUE_USE_SSL;

    @Override
    void connectionStringWithNonValueProperties() {
        String connectionString = "jdbc:mysql://host/database?enableSwitch1&property1=value1";
        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        assertEquals(DatabaseType.MYSQL, jdbcConnectionString.getDatabaseType());
        assertEquals(NONE_VALUE, jdbcConnectionString.getProperty("enableSwitch1"));
        assertEquals("value1", jdbcConnectionString.getProperty("property1"));
    }

    @Override
    void connectionStringWithMultipleProperties() {
        String connectionString = "jdbc:mysql://host/database?property1=value1&property2=value2";

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        assertEquals(DatabaseType.MYSQL, jdbcConnectionString.getDatabaseType());
        assertEquals("value1", jdbcConnectionString.getProperty("property1"));
        assertEquals("value2", jdbcConnectionString.getProperty("property2"));
    }

    @Override
    void connectionStringWithoutProperties() {
        String connectionString = "jdbc:mysql://host/database";

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        assertEquals(DatabaseType.MYSQL, jdbcConnectionString.getDatabaseType());
        assertFalse(jdbcConnectionString.hasProperties());
    }

    @Override
    void connectionStringWittInvalidProperties() {
        String connectionString = "jdbc:mysql://host/database?=";
        assertThrows(IllegalArgumentException.class, () -> JdbcConnectionString.resolve(connectionString),
            String.format(INVALID_PROPERTY_PAIR_FORMAT, connectionString));
    }

    @Override
    void inconsistentPropertiesTest() {
        String connectionString = "jdbc:mysql://mockpostgresqlurl:3306/db?useSSL=false";
        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        Map<String, String> configMap = new HashMap<>();
        configMap.putAll(jdbcConnectionString.getDatabaseType().getDefaultEnhancedProperties());
        assertThrows(IllegalArgumentException.class, () -> jdbcConnectionString.enhanceConnectionString(configMap));
    }

    @Override
    void consistentPropertiesTest() {
        String connectionString = "jdbc:mysql://mockpostgresqlurl:3306/db?useSSL=true";
        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        Map<String, String> configMap = new HashMap<>();
        assertNotNull(jdbcConnectionString.enhanceConnectionString(configMap));
    }

    @Override
    void enhanceConnectionStringWithoutProperties() {
        String connectionString = "jdbc:mysql://host/database";
        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        String enhancedUrl = jdbcConnectionString.enhanceConnectionString(jdbcConnectionString.getDatabaseType().getDefaultEnhancedProperties());
        String expectedUrl = String.format("%s?%s&%s&%s&%s", connectionString,
            MYSQL_AUTH_PLUGIN_PROPERTY,
            MYSQL_DEFAULT_PLUGIN_PROPERTY,
            MYSQL_SSL_MODE_PROPERTY,
            MYSQL_USE_SSL_PROPERTY);
        Assertions.assertEquals(expectedUrl, enhancedUrl);
    }

    @Override
    void enhanceConnectionStringWithProperties() {
        String connectionString = "jdbc:mysql://host/database?sslMode=REQUIRED";
        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        String enhancedUrl = jdbcConnectionString.enhanceConnectionString(jdbcConnectionString.getDatabaseType().getDefaultEnhancedProperties());
        String expectedUrl = String.format("%s&%s&%s&%s", connectionString,
            MYSQL_AUTH_PLUGIN_PROPERTY,
            MYSQL_DEFAULT_PLUGIN_PROPERTY,
            MYSQL_USE_SSL_PROPERTY);
        Assertions.assertEquals(expectedUrl, enhancedUrl);
    }
}
