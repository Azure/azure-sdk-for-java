// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public final class JdbcConnectionString {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcConnectionString.class);

    public static final String INVALID_CONNECTION_STRING_FORMAT = "Invalid connection string: %s";
    public static final String UNSUPPORTED_DATABASE_TYPE_STRING_FORMAT = "The DatabaseType specified in : %s is not "
        + "supported to enhance authentication with Azure AD by Spring Cloud Azure.";
    public static final String INVALID_PROPERTY_PAIR_FORMAT = "Connection string has invalid key value pair: %s";
    private static final String TOKEN_VALUE_SEPARATOR = "=";
    private final String jdbcUrl;

    private final String baseUrl;
    private final Map<String, String> properties = new TreeMap<>();
    private final List<String> orderedPropertyKeys = new ArrayList<>();
    private final DatabaseType databaseType;

    private JdbcConnectionString(String jdbcUrl, String baseUrl, DatabaseType databaseType,
                                 Map<String, String> properties, List<String> orderedPropertyKeys) {
        this.jdbcUrl = jdbcUrl;
        this.baseUrl = baseUrl;
        this.databaseType = databaseType;
        if (properties != null) {
            this.properties.putAll(properties);
            this.orderedPropertyKeys.addAll(orderedPropertyKeys);
        }
    }

    private JdbcConnectionString(String jdbcUrl, String baseUrl, DatabaseType databaseType) {
        this(jdbcUrl, baseUrl, databaseType, null, null);
    }

    private static JdbcConnectionString resolveSegments(String originalJdbcURL) {
        if (!StringUtils.hasText(originalJdbcURL)) {
            LOGGER.warn("'connectionString' doesn't have text.");
            throw new IllegalArgumentException(String.format(INVALID_CONNECTION_STRING_FORMAT, originalJdbcURL));
        }

        Optional<DatabaseType> optionalDatabaseType = Arrays.stream(DatabaseType.values())
                                                            .filter(databaseType -> originalJdbcURL.startsWith(databaseType.getSchema() + ":"))
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
                properties.put(key, null);
            } else {
                properties.put(key, pair[1]);
            }
            originalPropertiesOrder.add(key);
        }
        return new JdbcConnectionString(originalJdbcURL, baseURL, databaseType, properties, originalPropertiesOrder);
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    String getBaseUrl() {
        return baseUrl;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    String getProperty(String key) {
        return properties.get(key);
    }

    Map<String, String> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }

    List<String> getOrderedPropertyKeys() {
        return Collections.unmodifiableList(this.orderedPropertyKeys);
    }

    boolean hasProperties() {
        return !properties.isEmpty();
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
