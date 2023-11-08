// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * A {@link JdbcConnectionStringEnhancer} will enhance a {@link JdbcConnectionString}
 * instance. It can add more properties to the JDBC connection string instance, or
 * append more attributes value to an existing property. An enhanced JDBC URL can be
 * built from this enhancer.
 */
public final class JdbcConnectionStringEnhancer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcConnectionStringEnhancer.class);

    private final Map<String, String> enhancedProperties = new TreeMap<>();

    private final JdbcConnectionString connectionString;
    private final DatabaseType databaseType;

    public JdbcConnectionStringEnhancer(@NonNull JdbcConnectionString connectionString) {
        this.connectionString = connectionString;
        this.databaseType = connectionString.getDatabaseType();
    }

    public String getJdbcUrl() {
        if (this.enhancedProperties.isEmpty()) {
            return this.connectionString.getJdbcUrl();
        }

        LOGGER.debug("Trying to construct enhanced jdbc url for {}", this.databaseType);

        StringBuilder builder = new StringBuilder(this.connectionString.getBaseUrl())
            .append(this.databaseType.getPathQueryDelimiter());

        Map<String, String> mergedProperties = new TreeMap<>(this.connectionString.getProperties());
        mergedProperties.putAll(this.enhancedProperties);

        this.connectionString.getOrderedPropertyKeys()
            .forEach(k -> builder
                .append(constructPropertyString(k, mergedProperties.remove(k)))
                .append(this.databaseType.getQueryDelimiter())
            );

        mergedProperties.forEach((k, v) -> builder
            .append(k)
            .append("=")
            .append(v)
            .append(this.databaseType.getQueryDelimiter())
        );

        String enhancedUrl = builder.toString();
        return enhancedUrl.substring(0, enhancedUrl.length() - 1);
    }

    private static String constructPropertyString(String key, String value) {
        return value == null ? key : (key + "=" + value);
    }

    public void enhanceProperties(Map<String, String> enhancedProperties) {
        this.enhanceProperties(enhancedProperties, false);
    }

    public void enhanceProperties(Map<String, String> enhancedProperties,
                                  boolean silentWhenInconsistentValuePresent) {
        for (Map.Entry<String, String> entry : enhancedProperties.entrySet()) {
            String key = entry.getKey(), value = entry.getValue();
            String valueProvidedInConnectionString = this.connectionString.getProperty(key);

            if (valueProvidedInConnectionString == null) {
                this.enhancedProperties.put(key, value);
            } else if (!value.equals(valueProvidedInConnectionString)) {
                if (silentWhenInconsistentValuePresent) {
                    LOGGER.debug("The property {} is set to another value than default {}", key, value);
                } else {
                    throw new IllegalArgumentException("Inconsistent property of key [" + key +  "] detected");
                }
            } else {
                LOGGER.debug("The property {} is already set", key);
            }
        }
    }

    public void enhancePropertyAttributes(String propertyKey, Map<String, String> enhancedAttributes,
                                          String attributeDelimiter, String attributeKeyValueDelimiter) {
        String propertyValue = this.connectionString.getProperties().get(propertyKey);
        if (propertyValue != null) {
            String[] attributes = propertyValue.split(attributeDelimiter);

            Map<String, String> originalAttributesMap = Arrays.stream(attributes)
                .map(attr -> attr.split(attributeKeyValueDelimiter))
                .collect(Collectors.toMap(pair -> pair[0], pair -> pair[1], (a, b) -> a));

            TreeMap<String, String> actualEnhancedAttributes = new TreeMap<>(enhancedAttributes);
            originalAttributesMap.keySet().forEach(key -> {
                LOGGER.debug("The attribute {} in property {} is already set", key, propertyKey);
                actualEnhancedAttributes.remove(key);
            });
            this.enhancedProperties.put(propertyKey, buildPropertyValueFromAttributes(propertyValue, attributeDelimiter,
                attributeKeyValueDelimiter, actualEnhancedAttributes));
        } else {
            this.enhancedProperties.put(propertyKey, buildPropertyValueFromAttributes(null, attributeDelimiter,
                attributeKeyValueDelimiter, new TreeMap<>(enhancedAttributes)));
        }
    }

    String getEnhancedProperty(String key) {
        return this.enhancedProperties.get(key);
    }

    String getOriginalProperty(String key) {
        return this.connectionString.getProperty(key);
    }

    DatabaseType getDatabaseType() {
        return databaseType;
    }

    private static String buildPropertyValueFromAttributes(String baseAttributes,
                                                           String attributeDelimiter,
                                                           String attributeKeyValueDelimiter,
                                                           TreeMap<String, String> enhancedAttributes) {
        String enhancedString = enhancedAttributes
            .entrySet()
            .stream()
            .map(entry -> entry.getKey() + attributeKeyValueDelimiter + entry.getValue())
            .collect(Collectors.joining(attributeDelimiter));
        return baseAttributes == null ? enhancedString : (baseAttributes + attributeDelimiter + enhancedString);
    }

}
