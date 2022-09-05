// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.NONE_VALUE;

public final class JdbcConnectionString {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcConnectionString.class);

    public static final String INVALID_CONNECTION_STRING_FORMAT = "Invalid connection string: %s";
    public static final String UNSUPPORTED_DATABASE_TYPE_STRING_FORMAT = "The DatabaseType specified in : %s is not "
        + "supported to enhance authentication with Azure AD by Spring Cloud Azure.";
    public static final String INVALID_PROPERTY_PAIR_FORMAT = "Connection string has invalid key value pair: %s";
    private static final String TOKEN_VALUE_SEPARATOR = "=";
    private final String originalJdbcUrl;

    private final String baseUrl;
    private final Map<String, String> originalProperties = new TreeMap<>();
    private final List<String> orderedOriginalPropertyKeys = new ArrayList<>();
    private final Map<String, String> enhancedProperties = new TreeMap<>();
    private final DatabaseType databaseType;

    private JdbcConnectionString(String originalJdbcUrl, String baseUrl, DatabaseType databaseType,
                                 Map<String, String> originalProperties, List<String> orderedOriginalPropertyKeys) {
        this.originalJdbcUrl = originalJdbcUrl;
        this.baseUrl = baseUrl;
        this.databaseType = databaseType;
        if (originalProperties != null) {
            this.originalProperties.putAll(originalProperties);
            this.orderedOriginalPropertyKeys.addAll(orderedOriginalPropertyKeys);
        }
    }

    private JdbcConnectionString(String originalJdbcUrl, String baseUrl, DatabaseType databaseType) {
        this(originalJdbcUrl, baseUrl, databaseType, null, null);
    }

    private static JdbcConnectionString resolveSegments(String originalJdbcURL) {
        if (!StringUtils.hasText(originalJdbcURL)) {
            LOGGER.warn("'connectionString' doesn't have text.");
            throw new IllegalArgumentException(String.format(INVALID_CONNECTION_STRING_FORMAT, originalJdbcURL));
        }

        Optional<DatabaseType> optionalDatabaseType = Arrays.stream(DatabaseType.values())
                                                            .filter(databaseType -> originalJdbcURL.startsWith(databaseType.getSchema()))
                                                            .findAny();
        DatabaseType databaseType = optionalDatabaseType.orElseThrow(() -> new AzureUnsupportedDatabaseTypeException(String.format(UNSUPPORTED_DATABASE_TYPE_STRING_FORMAT, originalJdbcURL)));

        int pathQueryDelimiterIndex = originalJdbcURL.indexOf(databaseType.getPathQueryDelimiter());

        if (pathQueryDelimiterIndex < 0) {
            return new JdbcConnectionString(originalJdbcURL, originalJdbcURL, databaseType);
        }

        String baseURL = originalJdbcURL.substring(0, pathQueryDelimiterIndex);

        final String[] tokenValuePairs = originalJdbcURL
            .substring(pathQueryDelimiterIndex + 1)
            .split(databaseType.getQueryDelimiter());

        Map<String, String> properties = new HashMap<>();
        List<String> originalPropertiesOrder = new ArrayList<>();

        for (String tokenValuePair : tokenValuePairs) {
            final String[] pair = tokenValuePair.split(TOKEN_VALUE_SEPARATOR, 2);
            String key = pair[0];
            if (!StringUtils.hasText(pair[0])) {
                throw new IllegalArgumentException(String.format(INVALID_PROPERTY_PAIR_FORMAT, tokenValuePair));
            }
            if (pair.length < 2) {
                properties.put(key, NONE_VALUE);
            } else {
                properties.put(key, pair[1]);
            }
            originalPropertiesOrder.add(key);
        }
        return new JdbcConnectionString(originalJdbcURL, baseURL, databaseType, properties, originalPropertiesOrder);
    }

    public String getJdbcUrl() {
        if (this.enhancedProperties.isEmpty()) {
            return this.originalJdbcUrl;
        }

        LOGGER.debug("Trying to construct enhanced jdbc url for {}", databaseType);

        StringBuilder builder = new StringBuilder(this.baseUrl).append(databaseType.getPathQueryDelimiter());

        Map<String, String> mergedProperties = new TreeMap<>(originalProperties);
        mergedProperties.putAll(this.enhancedProperties);

        this.orderedOriginalPropertyKeys.forEach(k -> builder.append(k)
            .append("=")
            .append(mergedProperties.remove(k))
            .append(databaseType.getQueryDelimiter()));
        mergedProperties.forEach((k, v) -> builder.append(k).append("=").append(v).append(databaseType.getQueryDelimiter()));

        String enhancedUrl = builder.toString();
        return enhancedUrl.substring(0, enhancedUrl.length() - 1);
    }

    public void enhanceProperties(Map<String, String> enhancedProperties) {
        for (Map.Entry<String, String> entry : enhancedProperties.entrySet()) {
            String key = entry.getKey(), value = entry.getValue();
            String valueProvidedInConnectionString = this.originalProperties.get(key);

            if (valueProvidedInConnectionString == null) {
                this.enhancedProperties.put(key, value);
            } else if (!value.equals(valueProvidedInConnectionString)) {
                LOGGER.debug("The property {} is set to another value than default {}", key, value);
                throw new IllegalArgumentException("Inconsistent property detected");
            } else {
                LOGGER.debug("The property {} is already set", key);
            }
        }
    }

    public void enhancePropertyAttributes(String propertyKey, Map<String, String> enhancedAttributes,
                                          String attributeDelimiter, String attributeKeyValueDelimiter) {
        if (this.originalProperties.containsKey(propertyKey)) {

            String value = this.originalProperties.get(propertyKey);
            String[] attributes = value.split(attributeDelimiter);

            Map<String, String> originalAttributesMap = Arrays.stream(attributes)
                .map(attr -> attr.split(attributeKeyValueDelimiter))
                .collect(Collectors.toMap(pair -> pair[0], pair -> pair[1], (a, b) -> a));

            Map<String, String> mergedAttributes = new TreeMap<>(originalAttributesMap);
            enhancedAttributes.entrySet().forEach(entry -> {
                LOGGER.debug("The attribute {} in property {} is already set", entry.getKey(), propertyKey);
                mergedAttributes.putIfAbsent(entry.getKey(), entry.getValue());
            });

            this.enhancedProperties.put(propertyKey, buildPropertyValueFromAttributes(attributeDelimiter,
                attributeKeyValueDelimiter, mergedAttributes));
        } else {
            this.enhancedProperties.put(propertyKey, buildPropertyValueFromAttributes(attributeDelimiter,
                attributeKeyValueDelimiter, enhancedAttributes));
        }
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    String getOriginalProperty(String key) {
        return originalProperties.get(key);
    }

    boolean hasOriginalProperties() {
        return !this.originalProperties.isEmpty();
    }

    boolean hasEnhancedProperties() {
        return !this.enhancedProperties.isEmpty();
    }

    String getEnhancedProperty(String key) {
        return originalProperties.get(key);
    }

    private static String buildPropertyValueFromAttributes(String attributeDelimiter,
                                                           String attributeKeyValueDelimiter,
                                                           Map<String, String> mergedAttributes) {
        return mergedAttributes.entrySet()
            .stream()
            .map(entry -> entry.getKey() + attributeKeyValueDelimiter + entry.getValue())
            .collect(Collectors.joining(attributeDelimiter));
    }

    public static JdbcConnectionString resolve(String url) {
        try {
            return resolveSegments(url);
        } catch (AzureUnsupportedDatabaseTypeException e) {
            LOGGER.debug(e.getMessage());
            return null;
        }
    }

}
