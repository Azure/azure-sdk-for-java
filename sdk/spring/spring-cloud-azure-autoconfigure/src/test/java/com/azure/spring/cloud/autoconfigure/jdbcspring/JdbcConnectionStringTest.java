package com.azure.spring.cloud.autoconfigure.jdbcspring;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.jdbcspring.JdbcPropertiesBeanPostProcessor.ENHANCED_PROPERTIES;
import static com.azure.spring.cloud.autoconfigure.jdbcspring.JdbcConnectionString.INVALID_CONNECTION_STRING_FORMAT;
import static com.azure.spring.cloud.autoconfigure.jdbcspring.JdbcConnectionString.INVALID_PROPERTY_PAIR_FORMAT;
import static com.azure.spring.cloud.autoconfigure.jdbcspring.JdbcConnectionStringPropertyConstants.MYSQL_PLUGIN_CLASS_NAME;
import static com.azure.spring.cloud.autoconfigure.jdbcspring.JdbcConnectionStringPropertyConstants.NONE_VALUE;
import static com.azure.spring.cloud.autoconfigure.jdbcspring.JdbcConnectionStringPropertyConstants.PROPERTY_MYSQL_AUTHENTICATION_PLUGINS;
import static com.azure.spring.cloud.autoconfigure.jdbcspring.JdbcConnectionStringPropertyConstants.PROPERTY_MYSQL_DEFAULT_AUTHENTICATION_PLUGIN;
import static com.azure.spring.cloud.autoconfigure.jdbcspring.JdbcConnectionStringPropertyConstants.PROPERTY_MYSQL_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.jdbcspring.JdbcConnectionStringPropertyConstants.PROPERTY_MYSQL_USE_SSL;
import static com.azure.spring.cloud.autoconfigure.jdbcspring.JdbcConnectionStringPropertyConstants.VALUE_MYSQL_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.jdbcspring.JdbcConnectionStringPropertyConstants.VALUE_MYSQL_USE_SSL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;


class JdbcConnectionStringTest {
    private static final String MYSQL_AUTH_PLUGIN_PROPERTY = PROPERTY_MYSQL_AUTHENTICATION_PLUGINS + "=" + MYSQL_PLUGIN_CLASS_NAME;
    private static final String MYSQL_DEFAULT_PLUGIN_PROPERTY = PROPERTY_MYSQL_DEFAULT_AUTHENTICATION_PLUGIN + "=" + MYSQL_PLUGIN_CLASS_NAME;
    private static final String MYSQL_SSL_MODE_PROPERTY = PROPERTY_MYSQL_SSL_MODE + "=" + VALUE_MYSQL_SSL_MODE;
    private static final String MYSQL_USE_SSL_PROPERTY = PROPERTY_MYSQL_USE_SSL + "=" + VALUE_MYSQL_USE_SSL;

    @Test
    void connectionStringWithNonValueProperties() {
        String connectionString = "jdbc:mysql://host/database?enableSwitch1&property1=value1";

        JdbcConnectionString jdbcConnectionString = new JdbcConnectionString(connectionString);
        assertEquals(DatabaseType.MYSQL, jdbcConnectionString.getDatabaseType());
        assertEquals(NONE_VALUE, jdbcConnectionString.getProperty("enableSwitch1"));
        assertEquals("value1", jdbcConnectionString.getProperty("property1"));
    }

    @Test
    void connectionStringWithMultipleProperties() {
        String connectionString = "jdbc:mysql://host/database?property1=value1&property2=value2";

        JdbcConnectionString jdbcConnectionString = new JdbcConnectionString(connectionString);
        assertEquals(DatabaseType.MYSQL, jdbcConnectionString.getDatabaseType());
        assertEquals("value1", jdbcConnectionString.getProperty("property1"));
        assertEquals("value2", jdbcConnectionString.getProperty("property2"));
    }

    @Test
    void connectionStringWithoutProperties() {
        String connectionString = "jdbc:mysql://host/database";

        JdbcConnectionString jdbcConnectionString = new JdbcConnectionString(connectionString);
        assertEquals(DatabaseType.MYSQL, jdbcConnectionString.getDatabaseType());
        assertFalse(jdbcConnectionString.hasProperties());
    }

    @Test
    void connectionStringWittInvalidProperties() {
        String connectionString = "jdbc:mysql://host/database?=";
        assertThrowsExactly(IllegalArgumentException.class, () -> new JdbcConnectionString(connectionString), String.format(INVALID_PROPERTY_PAIR_FORMAT, connectionString));
    }

    @Test
    void inconsistentPropertiesTest() {
        String connectionString = "jdbc:mysql://mockpostgresqlurl:3306/db?useSSL=false";
        JdbcConnectionString jdbcConnectionString = new JdbcConnectionString(connectionString);
        Map<String, String> configMap = new HashMap<>();
        configMap.putAll(ENHANCED_PROPERTIES.get(DatabaseType.MYSQL));
        assertThrowsExactly(IllegalArgumentException.class, () -> jdbcConnectionString.enhanceConnectionString(configMap));
    }

    @Test
    void consistentPropertiesTest() {
        String connectionString = "jdbc:mysql://mockpostgresqlurl:3306/db?useSSL=true";
        JdbcConnectionString jdbcConnectionString = new JdbcConnectionString(connectionString);
        Map<String, String> configMap = new HashMap<>();
        configMap.putAll(ENHANCED_PROPERTIES.get(DatabaseType.MYSQL));
        assertNotNull(jdbcConnectionString.enhanceConnectionString(configMap));
    }

    @Test
    void invalidConnectionString() {
        String connectionString = "jdbc:mysqx://host";
        assertThrowsExactly(IllegalArgumentException.class, () -> new JdbcConnectionString(connectionString), String.format(INVALID_CONNECTION_STRING_FORMAT, connectionString));
    }

    @Test
    void enhanceConnectionStringWithoutProperties() {
        String connectionString = "jdbc:mysql://host/database";
        JdbcConnectionString jdbcConnectionString = new JdbcConnectionString(connectionString);

        String enhancedUrl = jdbcConnectionString.enhanceConnectionString(ENHANCED_PROPERTIES.get(DatabaseType.MYSQL));
        String expectedUrl = String.format("%s?%s&%s&%s&%s", connectionString,
            MYSQL_AUTH_PLUGIN_PROPERTY,
            MYSQL_DEFAULT_PLUGIN_PROPERTY,
            MYSQL_SSL_MODE_PROPERTY,
            MYSQL_USE_SSL_PROPERTY);
        Assertions.assertEquals(expectedUrl, enhancedUrl);
    }

    @Test
    void enhanceConnectionStringWithProperties() {
        String connectionString = "jdbc:mysql://host/database?sslMode=REQUIRED";
        JdbcConnectionString jdbcConnectionString = new JdbcConnectionString(connectionString);

        String enhancedUrl = jdbcConnectionString.enhanceConnectionString(ENHANCED_PROPERTIES.get(DatabaseType.MYSQL));
        String expectedUrl = String.format("%s&%s&%s&%s", connectionString,
            MYSQL_AUTH_PLUGIN_PROPERTY,
            MYSQL_DEFAULT_PLUGIN_PROPERTY,
            MYSQL_USE_SSL_PROPERTY);
        Assertions.assertEquals(expectedUrl, enhancedUrl);
    }
}
