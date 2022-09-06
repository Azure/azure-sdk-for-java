// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionString.INVALID_PROPERTY_PAIR_FORMAT;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionStringUtils.buildEnhancedPropertiesOrderedString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

abstract class AbstractJdbcConnectionStringTest {

    protected static String PATH_WITH_QUERY_PATTERN = "%s://host/database%s%s";
    protected static String PATH_WITHOUT_QUERY_PATTERN = "%s://host/database";

    abstract DatabaseType getDatabaseType();
    @Test
    void testConnectionStringWithNonValueProperties() {
        String queries = "enableSwitch1" + getDatabaseType().getQueryDelimiter() + "property1=value1";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, getDatabaseType().getSchema(), getDatabaseType().getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        assertEquals(getDatabaseType(), jdbcConnectionString.getDatabaseType());
        assertEquals(null, jdbcConnectionString.getOriginalProperty("enableSwitch1"));
        assertEquals("value1", jdbcConnectionString.getOriginalProperty("property1"));
    }

    @Test
    void testConnectionStringWithMultipleProperties() {
        String queries = "property1=value1" + getDatabaseType().getQueryDelimiter() + "property2=value2";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, getDatabaseType().getSchema(), getDatabaseType().getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        assertEquals(getDatabaseType(), jdbcConnectionString.getDatabaseType());
        assertEquals("value1", jdbcConnectionString.getOriginalProperty("property1"));
        assertEquals("value2", jdbcConnectionString.getOriginalProperty("property2"));
    }

    @Test
    void testConnectionStringWithoutProperties() {
        String connectionString = String.format(PATH_WITHOUT_QUERY_PATTERN, getDatabaseType().getSchema());

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        assertEquals(getDatabaseType(), jdbcConnectionString.getDatabaseType());
        assertFalse(jdbcConnectionString.hasOriginalProperties());
    }

    @Test
    void testConnectionStringWithInvalidProperties() {
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, getDatabaseType().getSchema(), getDatabaseType().getPathQueryDelimiter(), "=");

        assertThrows(IllegalArgumentException.class, () -> JdbcConnectionString.resolve(connectionString),
            String.format(INVALID_PROPERTY_PAIR_FORMAT, connectionString));
    }

    @Test
    void testInconsistentPropertiesTest() {
        Map<String, String> defaultEnhancedProperties = getDatabaseType().getDefaultEnhancedProperties();
        Map.Entry<String, String> randomDefaultEnhancedProperty = defaultEnhancedProperties.entrySet().iterator().next();

        String queries = randomDefaultEnhancedProperty.getKey() + "=randomValue";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, getDatabaseType().getSchema(), getDatabaseType().getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        Map<String, String> configMap = new HashMap<>();
        configMap.putAll(defaultEnhancedProperties);

        assertEquals(getDatabaseType(), jdbcConnectionString.getDatabaseType());
        assertThrows(IllegalArgumentException.class, () -> jdbcConnectionString.enhanceProperties(configMap));
    }

    @Test
    void testConsistentPropertiesTest() {
        Map<String, String> defaultEnhancedProperties = getDatabaseType().getDefaultEnhancedProperties();
        Map.Entry<String, String> randomDefaultEnhancedProperty = defaultEnhancedProperties.entrySet().iterator().next();

        String queries = randomDefaultEnhancedProperty.getKey() + "=" + randomDefaultEnhancedProperty.getValue();
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, getDatabaseType().getSchema(), getDatabaseType().getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        Map<String, String> configMap = new HashMap<>();
        configMap.put(randomDefaultEnhancedProperty.getKey(), randomDefaultEnhancedProperty.getValue());
        jdbcConnectionString.enhanceProperties(configMap);

        assertEquals(getDatabaseType(), jdbcConnectionString.getDatabaseType());
        assertNotNull(jdbcConnectionString.getJdbcUrl());
        assertNull(jdbcConnectionString.getEnhancedProperty(randomDefaultEnhancedProperty.getKey()));
        assertEquals(randomDefaultEnhancedProperty.getValue(), jdbcConnectionString.getOriginalProperty(randomDefaultEnhancedProperty.getKey()));
    }

    @Test
    void invalidConnectionString() {
        String connectionString = String.format(PATH_WITHOUT_QUERY_PATTERN, getDatabaseType().getSchema() + "x");

        JdbcConnectionString resolve = JdbcConnectionString.resolve(connectionString);
        assertNull(resolve);
    }

    @Test
    void testEnhanceConnectionStringWithoutProperties() {
        DatabaseType databaseType = getDatabaseType();
        String connectionString = String.format(PATH_WITHOUT_QUERY_PATTERN, databaseType.getSchema());

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        jdbcConnectionString.enhanceProperties(databaseType.getDefaultEnhancedProperties());

        String enhancedUrl = jdbcConnectionString.getJdbcUrl();
        String expectedUrl = String.format("%s%s%s",
            connectionString,
            databaseType.getPathQueryDelimiter(),
            buildDefaultEnhancedPropertiesOrderedString(databaseType));

        Assertions.assertEquals(databaseType, jdbcConnectionString.getDatabaseType());
        Assertions.assertEquals(expectedUrl, enhancedUrl);
    }

    @Test
    void testEnhanceConnectionStringWithProperties() {
        String queries = "someProperty=someValue";
        DatabaseType databaseType = getDatabaseType();
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, databaseType.getSchema(), databaseType.getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        jdbcConnectionString.enhanceProperties(databaseType.getDefaultEnhancedProperties());

        String enhancedUrl = jdbcConnectionString.getJdbcUrl();
        String expectedUrl = String.format("%s%s%s",
            connectionString,
            databaseType.getQueryDelimiter(),
            buildDefaultEnhancedPropertiesOrderedString(databaseType));

        Assertions.assertEquals(databaseType, jdbcConnectionString.getDatabaseType());
        Assertions.assertEquals(expectedUrl, enhancedUrl);
    }

    @Test
    void enhancePropertyWithExistingValueShouldThrowException() {
        String queries = "applicationName=defaultApp";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, getDatabaseType().getSchema(), getDatabaseType().getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        Map<String, String> enhancedProperties = new HashMap<>();
        enhancedProperties.put("applicationName", "newApp");

        Assertions.assertThrows(IllegalArgumentException.class, () -> jdbcConnectionString.enhanceProperties(enhancedProperties));
    }

    @Test
    void enhancePropertyWithExistingValueShouldBeSilent() {
        String queries = "applicationName=defaultApp";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, getDatabaseType().getSchema(), getDatabaseType().getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        Map<String, String> enhancedProperties = new HashMap<>();
        enhancedProperties.put("applicationName", "newApp");

        Assertions.assertDoesNotThrow(() -> jdbcConnectionString.enhanceProperties(enhancedProperties, true));
        Assertions.assertNull(jdbcConnectionString.getEnhancedProperty("applicationName"));
        Assertions.assertEquals("defaultApp", jdbcConnectionString.getOriginalProperty("applicationName"));
    }

    @Test
    void enhancePropertyWithoutExistingValueShouldSet() {
        String queries = "someProperty=someValue";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, getDatabaseType().getSchema(), getDatabaseType().getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        Map<String, String> enhancedProperties = new HashMap<>();
        enhancedProperties.put("applicationName", "newApp");
        jdbcConnectionString.enhanceProperties(enhancedProperties);

        Assertions.assertEquals(getDatabaseType(), jdbcConnectionString.getDatabaseType());
        Assertions.assertEquals("newApp", jdbcConnectionString.getEnhancedProperty("applicationName"));
        Assertions.assertEquals(connectionString + getDatabaseType().getQueryDelimiter() + "applicationName=newApp", jdbcConnectionString.getJdbcUrl());
    }

    @Test
    void enhanceNotExistingAttributePropertyShouldSet() {
        String queries = "someProperty=someValue";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, getDatabaseType().getSchema(), getDatabaseType().getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("attr1", "val1");
        attributes.put("attr2", "val2");
        jdbcConnectionString.enhancePropertyAttributes("attributeProperty", attributes, ",", ":");

        Assertions.assertEquals("attr1:val1,attr2:val2", jdbcConnectionString.getEnhancedProperty("attributeProperty"));
    }

    @Test
    void enhanceNotExistingAttributePropertyShouldSetByOrder() {
        String queries = "someProperty=someValue";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, getDatabaseType().getSchema(), getDatabaseType().getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("attr5", "val5");
        attributes.put("attr1", "val1");
        attributes.put("attr4", "val4");
        attributes.put("attr2", "val2");
        jdbcConnectionString.enhancePropertyAttributes("attributeProperty", attributes, ",", ":");

        Assertions.assertEquals("attr1:val1,attr2:val2,attr4:val4,attr5:val5", jdbcConnectionString.getEnhancedProperty("attributeProperty"));
    }

    @Test
    void enhanceExistingAttributePropertyShouldMerge() {
        String queries = "someProperty=someValue" + getDatabaseType().getQueryDelimiter() + "attributeProperty=attr3:val3";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, getDatabaseType().getSchema(), getDatabaseType().getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("attr1", "val1");
        attributes.put("attr2", "val2");
        jdbcConnectionString.enhancePropertyAttributes("attributeProperty", attributes, ",", ":");

        Assertions.assertEquals("attr3:val3,attr1:val1,attr2:val2", jdbcConnectionString.getEnhancedProperty("attributeProperty"));
    }

    @Test
    void enhanceExistingAttributePropertySameAttributeDifferentValueShouldNotSet() {
        String queries = "someProperty=someValue" + getDatabaseType().getQueryDelimiter() + "attributeProperty=attr3:val3";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, getDatabaseType().getSchema(), getDatabaseType().getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("attr1", "val1");
        attributes.put("attr2", "val2");
        attributes.put("attr3", "anotherVal3");
        jdbcConnectionString.enhancePropertyAttributes("attributeProperty", attributes, ",", ":");

        Assertions.assertEquals("attr3:val3,attr1:val1,attr2:val2", jdbcConnectionString.getEnhancedProperty("attributeProperty"));
    }

    @Test
    void enhanceExistingAttributePropertyEnhancedShouldOrder() {
        String queries = "someProperty=someValue" + getDatabaseType().getQueryDelimiter() + "attributeProperty=attr3:val3";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, getDatabaseType().getSchema(), getDatabaseType().getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("attr2", "val2");
        attributes.put("attr1", "val1");
        attributes.put("attr5", "val5");
        attributes.put("attr4", "val4");
        jdbcConnectionString.enhancePropertyAttributes("attributeProperty", attributes, ",", ":");
        String jdbcUrl = jdbcConnectionString.getJdbcUrl();

        String expectedAttributeValue = "attr3:val3,attr1:val1,attr2:val2,attr4:val4,attr5:val5";
        Assertions.assertEquals(expectedAttributeValue,
            jdbcConnectionString.getEnhancedProperty("attributeProperty"));
        String newQueries = queries + ",attr1:val1,attr2:val2,attr4:val4,attr5:val5";
        Assertions.assertEquals(String.format(PATH_WITH_QUERY_PATTERN, getDatabaseType().getSchema(), getDatabaseType().getPathQueryDelimiter(), newQueries), jdbcUrl);
    }

    private String buildDefaultEnhancedPropertiesOrderedString(DatabaseType databaseType) {
        return buildEnhancedPropertiesOrderedString(databaseType.getDefaultEnhancedProperties(),
            databaseType.getQueryDelimiter());
    }
}
