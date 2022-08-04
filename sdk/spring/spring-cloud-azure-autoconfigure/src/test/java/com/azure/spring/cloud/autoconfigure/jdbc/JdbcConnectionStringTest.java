package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.spring.cloud.autoconfigure.implementation.jdbc.DatabaseType;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionString.INVALID_PROPERTY_PAIR_FORMAT;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_AUTH_PLUGIN_CLASS_NAME;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.NONE_VALUE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.PROPERTY_NAME_MYSQL_AUTHENTICATION_PLUGINS;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.PROPERTY_NAME_MYSQL_DEFAULT_AUTHENTICATION_PLUGIN;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.PROPERTY_NAME_MYSQL_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.PROPERTY_NAME_MYSQL_USE_SSL;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.PROPERTY_VALUE_MYSQL_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.PROPERTY_VALUE_MYSQL_USE_SSL;
import static com.azure.spring.cloud.autoconfigure.jdbc.JdbcPropertiesBeanPostProcessor.DEFAULT_ENHANCED_PROPERTIES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcConnectionStringTest {
    private static final String MYSQL_AUTH_PLUGIN_PROPERTY = PROPERTY_NAME_MYSQL_AUTHENTICATION_PLUGINS + "=" + MYSQL_AUTH_PLUGIN_CLASS_NAME;
    private static final String MYSQL_DEFAULT_PLUGIN_PROPERTY = PROPERTY_NAME_MYSQL_DEFAULT_AUTHENTICATION_PLUGIN + "=" + MYSQL_AUTH_PLUGIN_CLASS_NAME;
    private static final String MYSQL_SSL_MODE_PROPERTY = PROPERTY_NAME_MYSQL_SSL_MODE + "=" + PROPERTY_VALUE_MYSQL_SSL_MODE;
    private static final String MYSQL_USE_SSL_PROPERTY = PROPERTY_NAME_MYSQL_USE_SSL + "=" + PROPERTY_VALUE_MYSQL_USE_SSL;

    @Test
    void connectionStringWithNonValueProperties() {
        String connectionString = "jdbc:mysql://host/database?enableSwitch1&property1=value1";

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        assertEquals(DatabaseType.MYSQL, jdbcConnectionString.getDatabaseType());
        assertEquals(NONE_VALUE, jdbcConnectionString.getProperty("enableSwitch1"));
        assertEquals("value1", jdbcConnectionString.getProperty("property1"));
    }

    @Test
    void connectionStringWithMultipleProperties() {
        String connectionString = "jdbc:mysql://host/database?property1=value1&property2=value2";

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        assertEquals(DatabaseType.MYSQL, jdbcConnectionString.getDatabaseType());
        assertEquals("value1", jdbcConnectionString.getProperty("property1"));
        assertEquals("value2", jdbcConnectionString.getProperty("property2"));
    }

    @Test
    void connectionStringWithoutProperties() {
        String connectionString = "jdbc:mysql://host/database";

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        assertEquals(DatabaseType.MYSQL, jdbcConnectionString.getDatabaseType());
        assertFalse(jdbcConnectionString.hasProperties());
    }

    @Test
    void connectionStringWittInvalidProperties() {
        String connectionString = "jdbc:mysql://host/database?=";
        assertThrows(IllegalArgumentException.class, () -> JdbcConnectionString.resolve(connectionString), String.format(INVALID_PROPERTY_PAIR_FORMAT, connectionString));
    }

    @Test
    void inconsistentPropertiesTest() {
        String connectionString = "jdbc:mysql://mockpostgresqlurl:3306/db?useSSL=false";
        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        Map<String, String> configMap = new HashMap<>();
        configMap.putAll(DEFAULT_ENHANCED_PROPERTIES.get(DatabaseType.MYSQL));
        assertThrows(IllegalArgumentException.class, () -> jdbcConnectionString.enhanceConnectionString(configMap));
    }

    @Test
    void consistentPropertiesTest() {
        String connectionString = "jdbc:mysql://mockpostgresqlurl:3306/db?useSSL=true";
        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        Map<String, String> configMap = new HashMap<>();
        configMap.putAll(DEFAULT_ENHANCED_PROPERTIES.get(DatabaseType.MYSQL));
        assertNotNull(jdbcConnectionString.enhanceConnectionString(configMap));
    }

    @Test
    void invalidConnectionString() {
        String connectionString = "jdbc:mysqx://host";
        JdbcConnectionString resolve = JdbcConnectionString.resolve(connectionString);
        assertNull(resolve);
    }

    @Test
    void enhanceConnectionStringWithoutProperties() {
        String connectionString = "jdbc:mysql://host/database";
        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        String enhancedUrl = jdbcConnectionString.enhanceConnectionString(DEFAULT_ENHANCED_PROPERTIES.get(DatabaseType.MYSQL));
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
        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        String enhancedUrl = jdbcConnectionString.enhanceConnectionString(DEFAULT_ENHANCED_PROPERTIES.get(DatabaseType.MYSQL));
        String expectedUrl = String.format("%s&%s&%s&%s", connectionString,
            MYSQL_AUTH_PLUGIN_PROPERTY,
            MYSQL_DEFAULT_PLUGIN_PROPERTY,
            MYSQL_USE_SSL_PROPERTY);
        Assertions.assertEquals(expectedUrl, enhancedUrl);
    }
}
