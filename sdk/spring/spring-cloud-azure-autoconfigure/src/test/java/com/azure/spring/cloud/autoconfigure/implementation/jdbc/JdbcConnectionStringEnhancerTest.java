// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionStringTest.PATH_WITHOUT_QUERY_PATTERN;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionStringTest.PATH_WITH_QUERY_PATTERN;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionStringUtils.enhanceJdbcUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcConnectionStringEnhancerTest {

    @ParameterizedTest
    @EnumSource(value = DatabaseType.class, names = { "MYSQL", "POSTGRESQL" })
    void inconsistentPropertiesShouldThrow(DatabaseType databaseType) {
        Map<String, String> defaultEnhancedProperties = databaseType.getDefaultEnhancedProperties();
        Map.Entry<String, String> randomDefaultEnhancedProperty = defaultEnhancedProperties.entrySet().iterator().next();

        String queries = randomDefaultEnhancedProperty.getKey() + "=randomValue";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, databaseType.getSchema(), databaseType.getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        JdbcConnectionStringEnhancer jdbcConnectionStringEnhancer = new JdbcConnectionStringEnhancer(jdbcConnectionString);
        Map<String, String> configMap = new HashMap<>();
        configMap.putAll(defaultEnhancedProperties);

        assertEquals(databaseType, jdbcConnectionString.getDatabaseType());
        assertThrows(IllegalArgumentException.class, () -> jdbcConnectionStringEnhancer.enhanceProperties(configMap));
    }

    @ParameterizedTest
    @EnumSource(value = DatabaseType.class, names = { "MYSQL", "POSTGRESQL" })
    void consistentPropertiesShouldNotThrow(DatabaseType databaseType) {
        Map<String, String> defaultEnhancedProperties = databaseType.getDefaultEnhancedProperties();
        Map.Entry<String, String> randomDefaultEnhancedProperty = defaultEnhancedProperties.entrySet().iterator().next();

        String queries = randomDefaultEnhancedProperty.getKey() + "=" + randomDefaultEnhancedProperty.getValue();
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, databaseType.getSchema(), databaseType.getPathQueryDelimiter(), queries);

        JdbcConnectionStringEnhancer jdbcConnectionStringEnhancer = new JdbcConnectionStringEnhancer(JdbcConnectionString.resolve(connectionString));

        Map<String, String> configMap = new HashMap<>();
        configMap.put(randomDefaultEnhancedProperty.getKey(), randomDefaultEnhancedProperty.getValue());
        jdbcConnectionStringEnhancer.enhanceProperties(configMap);

        assertEquals(databaseType, jdbcConnectionStringEnhancer.getDatabaseType());
        assertNotNull(jdbcConnectionStringEnhancer.getJdbcUrl());
        assertNull(jdbcConnectionStringEnhancer.getEnhancedProperty(randomDefaultEnhancedProperty.getKey()));
        assertEquals(randomDefaultEnhancedProperty.getValue(), jdbcConnectionStringEnhancer.getOriginalProperty(randomDefaultEnhancedProperty.getKey()));
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void enhanceConnectionStringWithoutProperties(DatabaseType databaseType) {
        String connectionString = String.format(PATH_WITHOUT_QUERY_PATTERN, databaseType.getSchema());

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        JdbcConnectionStringEnhancer jdbcConnectionStringEnhancer = new JdbcConnectionStringEnhancer(jdbcConnectionString);

        jdbcConnectionStringEnhancer.enhanceProperties(databaseType.getDefaultEnhancedProperties());

        String actualJdbcUrl = jdbcConnectionStringEnhancer.getJdbcUrl();
        String expectedUrl = enhanceJdbcUrl(databaseType, false, connectionString);

        Assertions.assertEquals(databaseType, jdbcConnectionString.getDatabaseType());
        Assertions.assertEquals(expectedUrl, actualJdbcUrl);
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void enhanceConnectionStringWithProperties(DatabaseType databaseType) {
        String queries = "someProperty=someValue";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, databaseType.getSchema(), databaseType.getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        JdbcConnectionStringEnhancer jdbcConnectionStringEnhancer = new JdbcConnectionStringEnhancer(jdbcConnectionString);

        jdbcConnectionStringEnhancer.enhanceProperties(databaseType.getDefaultEnhancedProperties());

        String actualJdbcUrl = jdbcConnectionStringEnhancer.getJdbcUrl();
        String expectedUrl = enhanceJdbcUrl(databaseType, connectionString);

        Assertions.assertEquals(databaseType, jdbcConnectionString.getDatabaseType());
        Assertions.assertEquals(expectedUrl, actualJdbcUrl);
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void enhancePropertyWithExistingValueShouldThrowException(DatabaseType databaseType) {
        String queries = "applicationName=defaultApp";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, databaseType.getSchema(), databaseType.getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        JdbcConnectionStringEnhancer jdbcConnectionStringEnhancer = new JdbcConnectionStringEnhancer(jdbcConnectionString);

        Map<String, String> enhancedProperties = new HashMap<>();
        enhancedProperties.put("applicationName", "newApp");

        Assertions.assertThrows(IllegalArgumentException.class, () -> jdbcConnectionStringEnhancer.enhanceProperties(enhancedProperties));
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void enhancePropertyWithExistingValueShouldBeSilent(DatabaseType databaseType) {
        String queries = "applicationName=defaultApp";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, databaseType.getSchema(), databaseType.getPathQueryDelimiter(), queries);

        JdbcConnectionString jdbcConnectionString = JdbcConnectionString.resolve(connectionString);
        JdbcConnectionStringEnhancer jdbcConnectionStringEnhancer = new JdbcConnectionStringEnhancer(jdbcConnectionString);

        Map<String, String> enhancedProperties = new HashMap<>();
        enhancedProperties.put("applicationName", "newApp");

        Assertions.assertDoesNotThrow(() -> jdbcConnectionStringEnhancer.enhanceProperties(enhancedProperties, true));
        Assertions.assertNull(jdbcConnectionStringEnhancer.getEnhancedProperty("applicationName"));
        Assertions.assertEquals("defaultApp", jdbcConnectionString.getProperty("applicationName"));
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void enhancePropertyWithoutExistingValueShouldSet(DatabaseType databaseType) {
        String queries = "someProperty=someValue";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, databaseType.getSchema(), databaseType.getPathQueryDelimiter(), queries);

        JdbcConnectionStringEnhancer jdbcConnectionStringEnhancer = new JdbcConnectionStringEnhancer(JdbcConnectionString.resolve(connectionString));

        Map<String, String> enhancedProperties = new HashMap<>();
        enhancedProperties.put("applicationName", "newApp");
        jdbcConnectionStringEnhancer.enhanceProperties(enhancedProperties);

        Assertions.assertEquals(databaseType, jdbcConnectionStringEnhancer.getDatabaseType());
        Assertions.assertEquals("newApp", jdbcConnectionStringEnhancer.getEnhancedProperty("applicationName"));
        Assertions.assertEquals(connectionString + databaseType.getQueryDelimiter() + "applicationName=newApp", jdbcConnectionStringEnhancer.getJdbcUrl());
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void enhanceNotExistingAttributePropertyShouldSet(DatabaseType databaseType) {
        String queries = "someProperty=someValue";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, databaseType.getSchema(), databaseType.getPathQueryDelimiter(), queries);

        JdbcConnectionStringEnhancer jdbcConnectionStringEnhancer = new JdbcConnectionStringEnhancer(JdbcConnectionString.resolve(connectionString));

        Map<String, String> attributes = new HashMap<>();
        attributes.put("attr1", "val1");
        attributes.put("attr2", "val2");
        jdbcConnectionStringEnhancer.enhancePropertyAttributes("attributeProperty", attributes, ",", ":");

        Assertions.assertEquals("attr1:val1,attr2:val2", jdbcConnectionStringEnhancer.getEnhancedProperty("attributeProperty"));
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void enhanceNotExistingAttributePropertyShouldSetByOrder(DatabaseType databaseType) {
        String queries = "someProperty=someValue";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, databaseType.getSchema(), databaseType.getPathQueryDelimiter(), queries);

        JdbcConnectionStringEnhancer jdbcConnectionStringEnhancer = new JdbcConnectionStringEnhancer(JdbcConnectionString.resolve(connectionString));

        Map<String, String> attributes = new HashMap<>();
        attributes.put("attr5", "val5");
        attributes.put("attr1", "val1");
        attributes.put("attr4", "val4");
        attributes.put("attr2", "val2");
        jdbcConnectionStringEnhancer.enhancePropertyAttributes("attributeProperty", attributes, ",", ":");

        Assertions.assertEquals("attr1:val1,attr2:val2,attr4:val4,attr5:val5", jdbcConnectionStringEnhancer.getEnhancedProperty("attributeProperty"));
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void enhanceExistingAttributePropertyShouldMerge(DatabaseType databaseType) {
        String queries = "someProperty=someValue" + databaseType.getQueryDelimiter() + "attributeProperty=attr3:val3";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, databaseType.getSchema(), databaseType.getPathQueryDelimiter(), queries);

        JdbcConnectionStringEnhancer jdbcConnectionStringEnhancer = new JdbcConnectionStringEnhancer(JdbcConnectionString.resolve(connectionString));

        Map<String, String> attributes = new HashMap<>();
        attributes.put("attr1", "val1");
        attributes.put("attr2", "val2");
        jdbcConnectionStringEnhancer.enhancePropertyAttributes("attributeProperty", attributes, ",", ":");

        Assertions.assertEquals("attr3:val3,attr1:val1,attr2:val2", jdbcConnectionStringEnhancer.getEnhancedProperty("attributeProperty"));
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void enhanceExistingAttributePropertySameAttributeDifferentValueShouldNotSet(DatabaseType databaseType) {
        String queries = "someProperty=someValue" + databaseType.getQueryDelimiter() + "attributeProperty=attr3:val3";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, databaseType.getSchema(), databaseType.getPathQueryDelimiter(), queries);

        JdbcConnectionStringEnhancer jdbcConnectionStringEnhancer = new JdbcConnectionStringEnhancer(JdbcConnectionString.resolve(connectionString));

        Map<String, String> attributes = new HashMap<>();
        attributes.put("attr1", "val1");
        attributes.put("attr2", "val2");
        attributes.put("attr3", "anotherVal3");
        jdbcConnectionStringEnhancer.enhancePropertyAttributes("attributeProperty", attributes, ",", ":");

        Assertions.assertEquals("attr3:val3,attr1:val1,attr2:val2", jdbcConnectionStringEnhancer.getEnhancedProperty("attributeProperty"));
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void enhanceExistingAttributePropertyEnhancedShouldOrder(DatabaseType databaseType) {
        String queries = "someProperty=someValue" + databaseType.getQueryDelimiter() + "attributeProperty=attr3:val3";
        String connectionString = String.format(PATH_WITH_QUERY_PATTERN, databaseType.getSchema(), databaseType.getPathQueryDelimiter(), queries);

        JdbcConnectionStringEnhancer jdbcConnectionStringEnhancer = new JdbcConnectionStringEnhancer(JdbcConnectionString.resolve(connectionString));

        Map<String, String> attributes = new HashMap<>();
        attributes.put("attr2", "val2");
        attributes.put("attr1", "val1");
        attributes.put("attr5", "val5");
        attributes.put("attr4", "val4");
        jdbcConnectionStringEnhancer.enhancePropertyAttributes("attributeProperty", attributes, ",", ":");
        String actualJdbcUrl = jdbcConnectionStringEnhancer.getJdbcUrl();

        String expectedAttributeValue = "attr3:val3,attr1:val1,attr2:val2,attr4:val4,attr5:val5";
        Assertions.assertEquals(expectedAttributeValue, jdbcConnectionStringEnhancer.getEnhancedProperty("attributeProperty"));
        String newQueries = queries + ",attr1:val1,attr2:val2,attr4:val4,attr5:val5";
        Assertions.assertEquals(String.format(PATH_WITH_QUERY_PATTERN, databaseType.getSchema(), databaseType.getPathQueryDelimiter(), newQueries), actualJdbcUrl);
    }

}
